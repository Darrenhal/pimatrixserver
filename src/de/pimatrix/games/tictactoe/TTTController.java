package de.pimatrix.games.tictactoe;

import de.pimatrix.backend.ClientThread;
import de.pimatrix.backend.Matrix;

public class TTTController implements Runnable {

	public static boolean running;
	private int[] isBlocked = new int[9];
	private int inputOffset = 41; // app keycode für button[1][1] = 41 -->
									// Zugriff auf Array über Werte von 0-9; daher muss der übermittelte Wert -41
									// gerechnet werden, um sinnvolle Werte zu erhalten
	private int userInput = 0;
	private short[][][] matrix = new short[14][14][3];
	private boolean validMove = false;
	private boolean inputReceived = false;
	private Matrix matrixData;

	@Override
	public void run() {
		int turn = 1; // Setzen des beginnenden Spielers
		initGame(); // Initialisieren des Spiels

		while (running) {
			validMove = false;
			inputReceived = false;
			while (!validMove) { // Schleife durchlaufen, solange kein gültiger Zug gespielt wurde
				if (inputReceived) { // Wenn Daten vom Spieler gesendet wurden Spielzug überprüfen
					waitForUserTurn(turn);
				}
				try {
					Thread.sleep(100); // Am Ende der Schleife 100ms warten, um Busy-Wait zu vermeiden
				} catch (InterruptedException e) {
				}
			}
			calculatePattern(calculatePatternCenter(userInput - inputOffset), turn); // Spielermuster anhand des
																						// errechneten Feld-Zentrums
																						// berechnen
			if (turn == 1) {
				showUserTurn(2); // Wenn Spieler 1 am Zug war --> anzeigen, dass Spieler 2 jetzt am Zug ist
			} else {
				showUserTurn(1); // Sonst anzeigen, dass jetzt Spieler 1 am Zug ist
			}
			promptToMatrix(); // Matrix an Arduino senden

			checkWinCondition(turn); // Überprüfen, ob jemand gewonnen hat oder es unentschieden ist

			if (turn == 1) { // Wechseln des Spielers, der am Zug ist
				turn = 2;
			} else {
				turn = 1;
			}
		}
	}

	private int[] calculatePatternCenter(int field) { // Berechnen des Mittelpunkts eines auswählbaren Spielfelds
		int[] centerCoordinates = new int[2]; // int-Array für x- und y-Koordinate des Spielfeld-Zentrums

		switch (field) {
		case 0: // Zentrum des Felds links oben:
			centerCoordinates[0] = 11;
			centerCoordinates[1] = 11;
			return centerCoordinates;

		case 1: // Zentrum des Felds oben in der Mitte:
			centerCoordinates[0] = 6;
			centerCoordinates[1] = 11;
			return centerCoordinates;

		case 2: // Zentrum des Felds rechts oben:
			centerCoordinates[0] = 1;
			centerCoordinates[1] = 11;
			return centerCoordinates;

		case 3: // Zentrum des Felds in der Mitte links:
			centerCoordinates[0] = 11;
			centerCoordinates[1] = 6;
			return centerCoordinates;

		case 4: // Zentrum des Felds in der Mitte:
			centerCoordinates[0] = 6;
			centerCoordinates[1] = 6;
			return centerCoordinates;

		case 5: // Zentrum des Felds in der Mitte rechts:
			centerCoordinates[0] = 1;
			centerCoordinates[1] = 6;
			return centerCoordinates;

		case 6: // Zentrum des Felds links unten:
			centerCoordinates[0] = 11;
			centerCoordinates[1] = 1;
			return centerCoordinates;

		case 7: // Zentrum des Felds in der Mitte unten:
			centerCoordinates[0] = 6;
			centerCoordinates[1] = 1;
			return centerCoordinates;

		case 8: // Zentrum des Felds rechts unten:
			centerCoordinates[0] = 1;
			centerCoordinates[1] = 1;
			return centerCoordinates;

		default: // Wenn kein gültiges Feld übergeben wurde zuweisen der x- und y-Koordinaten
					// (0|0)
			int[] def = { 0, 0 };
			return def;
		}
	}

	private void initGame() {
		matrixData = new Matrix(matrix);
		for (int i = 0; i < isBlocked.length; i++) { // Einstellen, dass jedes Spielfeld noch von keinem der Spieler
														// blockiert wurde
			isBlocked[i] = 0;
		}
		showUserTurn(1); // Anzeigen, welcher Spieler gerade am Zug ist
		generateGrid(); // Generieren des Spielfeld-Rasters
		promptToMatrix(); // Matrix an Arduino senden
	}

	private void showUserTurn(int turn) { // Anzeigen, welcher Spieler gerade am Zug ist
		if (turn == 1) { // Wenn Spieler 1 am Zug:
			for (int i = 0; i < 14; i++) { // für den linken und oberen Rand R-Wert auf 255 und G-Wert auf 0 setzen
				matrix[13][i][0] = 255;
				matrix[i][13][0] = 255;
				matrix[13][i][1] = 0;
				matrix[i][13][1] = 0;
			}
		} else { // Wenn Spieler 2 am Zug:
			for (int i = 0; i < 14; i++) { // für den linken und oberen Rand R-Wert auf 0 und G-Wert auf 255 setzen
				matrix[13][i][1] = 255;
				matrix[i][13][1] = 255;
				matrix[13][i][0] = 0;
				matrix[i][13][0] = 0;
			}
		}
	}

	private void waitForUserTurn(int turn) {
		if (isBlocked[userInput - inputOffset] == 0) { // Überprüfen, ob das Spielfeld, das der Spieler ausgewählt hat
														// noch nicht blockiert ist
			isBlocked[userInput - inputOffset] = turn; // Wenn Spielfeld noch frei --> mit ID des Spielers belegen
			validMove = true; // markieren als gültigen Zug
		}
	}

	private void promptToMatrix() {
		matrixData.matrix = matrix; // Matrix-Objekt mit lokalem int[14][14][3]-Array befüllen
		ClientThread.sendToSerialPort(matrixData); // Senden der Matrix an seriellen Thread
	}

	private void checkWinCondition(int turn) { // Überprüfen des Feldes auf Spielende
		if ((isBlocked[0] == isBlocked[1] && isBlocked[0] == isBlocked[2] && isBlocked[0] == 1) // 1. Reihe = 1
				|| (isBlocked[3] == isBlocked[4] && isBlocked[3] == isBlocked[5] && isBlocked[3] == 1) // 2. Reihe
				|| (isBlocked[6] == isBlocked[7] && isBlocked[6] == isBlocked[8] && isBlocked[6] == 1) // 3. Reihe
				|| (isBlocked[0] == isBlocked[3] && isBlocked[0] == isBlocked[6] && isBlocked[0] == 1) // 1. Spalte
				|| (isBlocked[1] == isBlocked[4] && isBlocked[1] == isBlocked[7] && isBlocked[1] == 1) // 2. Spalte
				|| (isBlocked[2] == isBlocked[5] && isBlocked[2] == isBlocked[8] && isBlocked[2] == 1) // 3. Spalte
				|| (isBlocked[0] == isBlocked[4] && isBlocked[0] == isBlocked[8] && isBlocked[0] == 1) // Diagonale
				|| (isBlocked[2] == isBlocked[4] && isBlocked[2] == isBlocked[6] && isBlocked[2] == 1)) { // Diagonale
			gameOver(1); // Dann Spiel vorbei, Spieler 1 gewinnt
		} else if ((isBlocked[0] == isBlocked[1] && isBlocked[0] == isBlocked[2] && isBlocked[0] == 2) // 1. Reihe = 2
				|| (isBlocked[3] == isBlocked[4] && isBlocked[3] == isBlocked[5] && isBlocked[3] == 2) // 2. Reihe
				|| (isBlocked[6] == isBlocked[7] && isBlocked[6] == isBlocked[8] && isBlocked[6] == 2) // 3. Reihe
				|| (isBlocked[0] == isBlocked[3] && isBlocked[0] == isBlocked[6] && isBlocked[0] == 2) // 1. Spalte
				|| (isBlocked[1] == isBlocked[4] && isBlocked[1] == isBlocked[7] && isBlocked[1] == 2) // 2. Spalte
				|| (isBlocked[2] == isBlocked[5] && isBlocked[2] == isBlocked[8] && isBlocked[2] == 2) // 3. Spalte
				|| (isBlocked[0] == isBlocked[4] && isBlocked[0] == isBlocked[8] && isBlocked[0] == 2) // Diagonale
				|| (isBlocked[2] == isBlocked[4] && isBlocked[2] == isBlocked[6] && isBlocked[2] == 2)) { // Diagonale
			gameOver(2); // Dann Spiel vorbei, Spieler 2 gewinnt
		} else if (isBlocked[0] != 0 && isBlocked[1] != 0 && isBlocked[2] != 0 && isBlocked[3] != 0 && isBlocked[4] != 0
				&& isBlocked[5] != 0 && isBlocked[6] != 0 && isBlocked[7] != 0 && isBlocked[8] != 0) {
			gameOver(0); // Wenn alle Felder ungleich 0 und bisher Spiel nicht vorbei --> unentschieden
		}
	}

	private void gameOver(int turn) { // Berechnen der GameOver Anzeige abhängig vom Gewinner

		try {
			Thread.sleep(2000); // 2s Warten, bevor Daten an Arduino gesendet werden --> Spielfeld am Ende
								// nochmal anzeigen
		} catch (InterruptedException e) {
		}

		if (turn == 1) { // Wenn Spieler 1 gewonnen hat
			for (int i = 0; i < 14; i++) { // Iterieren über alle LEDs
				for (int j = 0; j < 14; j++) {
					matrix[i][j][0] = 255; // Ganzes Spielfeld rot färben
					matrix[i][j][1] = 0;
					matrix[i][j][2] = 0;
				}
			}
		} else if (turn == 2) { // Wenn Spieler 2 gewonnen hat
			for (int i = 0; i < 14; i++) { // Iterieren über alle LEDs
				for (int j = 0; j < 14; j++) {
					matrix[i][j][0] = 0;
					matrix[i][j][1] = 255; // Ganzes Spielfeld grün färben
					matrix[i][j][2] = 0;
				}
			}
		} else if (turn == 0) { // Wenn unentschieden
			for (int i = 0; i < 14; i++) { // Iterieren über alle LEDs
				for (int j = 0; j < 14; j++) {
					matrix[i][j][0] = 0;
					matrix[i][j][1] = 0;
					matrix[i][j][2] = 255; // Ganzes Spielfeld blau färben
				}
			}
		}
		promptToMatrix(); // Matrix an Arduino senden

		running = false; // Spiel beenden
		ClientThread.endGame(this.getClass()); // Spiel-Instanz im Client-Thread beenden
	}

	public void setUserInput(int userInput) { // Eingabe des Spielers verarbeiten
		inputReceived = true; // Beachrichtigen, dass ein Zug vom Spieler gemacht wurde
		this.userInput = userInput; // Auslesen der Eingabe
	}

	private void generateGrid() { // Generieren des Rasters (sollte mit der Excel-Zeichnung in der Dropbox
									// selbsterklärend sein)
		for (int i = 0; i < 13; i++) {
			matrix[3][i][0] = matrix[3][i][1] = 0;
			matrix[3][i][2] = 255;
			matrix[4][i][0] = matrix[4][i][1] = 0;
			matrix[4][i][2] = 255;
			matrix[8][i][0] = matrix[8][i][1] = 0;
			matrix[8][i][2] = 255;
			matrix[9][i][0] = matrix[9][i][1] = 0;
			matrix[9][i][2] = 255;

			matrix[i][3][0] = matrix[i][3][1] = 0;
			matrix[i][3][2] = 255;
			matrix[i][4][0] = matrix[i][4][1] = 0;
			matrix[i][4][2] = 255;
			matrix[i][8][0] = matrix[i][8][1] = 0;
			matrix[i][8][2] = 255;
			matrix[i][9][0] = matrix[i][9][1] = 0;
			matrix[i][9][2] = 255;
		}
	}

	private void calculatePattern(int[] patternCenter, int turn) { // Berechnen des Spielermusters im entsprechenden
																	// Feld anhand des berechneten Spielfeld-Zentrums
		if (turn == 1) { //Wenn Spieler 1 am Zug: Muster entspricht einem "x"
			matrix[patternCenter[0]][patternCenter[1]][0] = 255; //Mitte des Felds Rot
			matrix[patternCenter[0]][patternCenter[1]][1] = matrix[patternCenter[0]][patternCenter[1]][2] = 0;

			matrix[patternCenter[0] - 1][patternCenter[1] - 1][0] = 255; //untere rechte Ecke des Felds rot
			matrix[patternCenter[0] - 1][patternCenter[1]
					- 1][1] = matrix[patternCenter[0] - 1][patternCenter[1] - 1][2] = 0;

			matrix[patternCenter[0] + 1][patternCenter[1] - 1][0] = 255; //untere linke Ecke des Felds rot
			matrix[patternCenter[0] + 1][patternCenter[1]
					- 1][1] = matrix[patternCenter[0] + 1][patternCenter[1] - 1][2] = 0;

			matrix[patternCenter[0] - 1][patternCenter[1] + 1][0] = 255; //obere rechte Ecke des Felds rot
			matrix[patternCenter[0] - 1][patternCenter[1]
					+ 1][1] = matrix[patternCenter[0] - 1][patternCenter[1] + 1][2] = 0;

			matrix[patternCenter[0] + 1][patternCenter[1] + 1][0] = 255; //obere linke Ecke des Felds rot
			matrix[patternCenter[0] + 1][patternCenter[1]
					+ 1][1] = matrix[patternCenter[0] + 1][patternCenter[1] + 1][2] = 0;
		} else { //Wenn Spieler 2 am Zug: Muster entspricht einer 3x3 Pixel Raute
			matrix[patternCenter[0] - 1][patternCenter[1]][1] = 255; //Mitte rechts grün
			matrix[patternCenter[0] - 1][patternCenter[1]][0] = matrix[patternCenter[0] - 1][patternCenter[1]][2] = 0;

			matrix[patternCenter[0] + 1][patternCenter[1]][1] = 255; //Mitte links grün
			matrix[patternCenter[0] + 1][patternCenter[1]][0] = matrix[patternCenter[0] + 1][patternCenter[1]][2] = 0;

			matrix[patternCenter[0]][patternCenter[1] - 1][1] = 255; //Mitte unten grün
			matrix[patternCenter[0]][patternCenter[1] - 1][0] = matrix[patternCenter[0]][patternCenter[1] - 1][2] = 0;

			matrix[patternCenter[0]][patternCenter[1] + 1][1] = 255; //Mitte oben grün
			matrix[patternCenter[0]][patternCenter[1] + 1][0] = matrix[patternCenter[0]][patternCenter[1] + 1][2] = 0;
		}
	}
}