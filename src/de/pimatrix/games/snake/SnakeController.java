package de.pimatrix.games.snake;

import java.util.Random;
import de.pimatrix.backend.ClientThread;
import de.pimatrix.backend.Matrix;

public class SnakeController implements Runnable {

	public static boolean running;
	public boolean right = false, left = true, up = false, down = false; // f�r n�chsten Zug geplante Richtung
	public boolean lastMoveRight = false, lastMoveLeft = true, lastMoveUp = false, lastMoveDown = false; // Richtung, in
																											// der sich
																											// die
																											// Schlange
																											// im
																											// letzten
																											// Zug
																											// bewegt
																											// hat
	private boolean foodAvailable = false; // gibt an, ob Essen auf dem Spielfeld ist

	private final short x[] = new short[196]; // Feld besteht aus 196 Feldern, Schlange kann also 196 Felder lang sein
												// --> F�r jeden Punkt der Schlange kann die jeweilige x-Koordinate
												// gespeichert werden; es wird nur die L�nge des Arrays genutzt, die der
												// L�nge der Schlange entspricht
	private final short y[] = new short[196]; // Feld besteht aus 196 Feldern, Schlange kann also 196 Felder lang sein
												// --> F�r jeden Punkt der Schlange kann die jeweilige y-Koordinate
												// gespeichert werden; es wird nur die L�nge des Arrays genutzt, die der
												// L�nge der Schlange entspricht

	private short length = 3; // L�nge der Schlange
	private short[] apple = new short[5]; // Repr�sentation des Apfels bestehend aus 5 int-Werten: x-Koordinate,
											// y-Koordinate, Rot-, Gr�n- & Blau- Wert

	@Override
	public void run() {
		initGame(); // Initialisieren des Spiels
		try {
			Thread.sleep(3000); // 3s Warten bevor Spiel beginnt zu laufen, damit sich Spieler vorbereiten kann
		} catch (InterruptedException e1) {
		}

		while (running) { // Schleife durchlaufen, solange Spiel l�uft
			checkApple(); // �berpr�fen, ob Apfel auf dem Spielfeld ist oder gerade gegessen wurde und
							// eventuell erneutes Platzieren des Apfels
			move(); // Bewegen der Schlange
			checkCollision(); // �berpr�fen, ob Schlange gegen sich oder eine Wand gesto�en ist
			if (running) {
				assembleMatrixInformation(); // Wenn Spiel nocht l�uft (die Schlange also nicht gegen eine Wand oder
												// sich selbst gesto�en ist) Zusammensetzen und �bertragen der Matrix an
												// den Arduino
			}

			try {
				Thread.sleep(300); // 300ms Warten bevor der n�chste Spielzug durchlaufen wird
			} catch (InterruptedException e) {
			}
		}
	}

	private void checkApple() {
		if ((x[0] == apple[0]) && (y[0] == apple[1]) || !foodAvailable) { // �berpr�fen, ob der Kopf der Schlange an der
																			// gleichen Position ist, wie der Apfel oder
																			// kein Essen verf�gbar ist
			length++; // Falls ja --> L�nge der Schlange erh�hen
			foodAvailable = false; // Essen auf "nicht verf�gbar" setzen
			while (!foodAvailable) { // Schleife durchlaufen bis Essen verf�gbar ist
				locateFood(); // Essen platzieren
			}
		}
	}

	private void locateFood() {
		boolean collision = false; // boolean zum �berpr�fen, ob der Apfel in der Schlange generiert wird
		Random random = new Random();

		short randomPos = (short) random.nextInt(13); // Zufallszahl von 0 bis 13
		apple[0] = randomPos; // Zuweisen der Zufallszahl als x-Koordinate des Apfels

		randomPos = (short) random.nextInt(13); // neue Zufallszahl berechnen
		apple[1] = randomPos; // Zuweisen als y-Koordinate des Apfels

		for (int i = 0; i < length; i++) { // �ber jeden Punkt der Schlange iterieren
			if (apple[0] == x[i] && apple[1] == y[i]) { // Pr�fen ob der jeweilige Punkt der Schlange = der berechnete
														// Punkt des Apfels ist
				collision = true; // Falls ja --> collision = true
			}
		}

		if (!collision) { // Wenn der Apfel nicht in der Schlange generiert wurde (also collision = false)
			foodAvailable = true; // Dann foodAvailable auf true setzen --> while-Bedingung in checkApple()
									// erf�llt
		}

		// RGB Werte f�r Apfel setzen
		apple[2] = 255;
		apple[3] = 0;
		apple[4] = 0;
	}

	private void checkCollision() {
		if (length > 4) { // �berpr�fen, dass die Schlange mehr als 4 Punkte hat (bei <= 4 Punkten kann
							// Schlange sich selbst niemals selbst fressen)
			for (int i = length; i > 0; i--) { // Iterieren �ber alle Punkte der Schlange au�er dem Kopf
				if ((x[0] == x[i]) && (y[0] == y[i])) { // f�r jeden Punkt �berpr�fen, ob er die selbe Position, wie der
														// Kopf hat
					gameOver(); // Falls ja --> Game Over
				}
			}
		}

		if (y[0] >= 14) { // �berpr�fen, ob die Schlange links anst��t
			gameOver();
		}
		if (y[0] < 0) { // �berpr�fen, ob die Schlange rechts anst��t
			gameOver();
		}
		if (x[0] >= 14) { // �berpr�fen, ob die Schlange oben anst��t
			gameOver();
		}
		if (x[0] < 0) { // �berpr�fen, ob die Schlange unten anst��t
			gameOver();
		}

	}

	private void move() {
		for (int i = length; i > 0; i--) { // Iterieren �ber alle Punkte der Schlange au�er dem Kopf
			x[i] = x[(i - 1)]; // x-Koordinate erh�lt die x-Position die der vorhergehende Punkt im letzten Zug
								// hatte
			y[i] = y[(i - 1)]; // y-Koordinate erh�lt die y-Position die der vorhergehende Punkt im letzten Zug
								// hatte
		}

		if (right) { // wenn Befehl f�r Bewegung nach rechts ...
			x[0] -= 1; // ... x-Koordinate des Kopfes -1 rechnen
			lastMoveRight = true; // ... setzen, dass die letzte Bewegung der Schlange nach rechts war
			lastMoveLeft = lastMoveUp = lastMoveDown = false; // ... setzen, dass die letzte Bewegung der Schlange nicht
																// nach links, oben oder unten war
		}
		if (left) { // wenn Befehl f�r Bewegung nach links ...
			x[0] += 1; // ... x-Koordinate des Kopfes +1 rechnen
			lastMoveLeft = true; // ... setzen, dass die letzte Bewegung der Schlange nach links war
			lastMoveRight = lastMoveUp = lastMoveDown = false; // ... setzen, dass die letzte Bewegung der Schlange nicht
																// nach rechts, oben oder unten war
		}
		if (up) { // wenn Befehl f�r Bewegung nach links ...
			y[0] += 1; // ... y-Koordinate des Kopfes +1 rechnen
			lastMoveUp = true; // ... setzen, dass die letzte Bewegung der Schlange nach oben war
			lastMoveLeft = lastMoveRight = lastMoveDown = false; // ... setzen, dass die letzte Bewegung der Schlange nicht
																// nach links, rechts oder unten war
		}
		if (down) { // wenn Befehl f�r Bewegung nach links ...
			y[0] -= 1; // ... y-Koordinate des Kopfes -1 rechnen
			lastMoveDown = true; // ... setzen, dass die letzte Bewegung der Schlange nach unten war
			lastMoveLeft = lastMoveUp = lastMoveRight = false; // ... setzen, dass die letzte Bewegung der Schlange nicht
																// nach links, rechts oder oben war
		}
	}

	private void initGame() {
		for (int i = 0; i < length; i++) { // Positionieren der Schlange
			x[i] = (short) (6 - i);
			y[i] = 7;
		}
		locateFood(); // Platzieren des Essens
		assembleMatrixInformation(); // Zusammensetzen und �bertragen der Matrix an den Arduino
	}

	private void gameOver() {
		try {
			Thread.sleep(2000); // 2s Warten, bevor Game Over-Bildschirm angezeigt wird, damit man den letzten
								// Zug nochmal kurz sieht
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		short[][][] matrix = new short[14][14][3]; // int[14][14][3]-Array deklarieren

		for (int i = 0; i < 14; i++) { // �ber Array iterieren mit 2 Schleifen (je 1 f�r x- und y-Dimension)
			for (int j = 0; j < 14; j++) {
				matrix[i][j][0] = 255; // R Wert f�r alle LEDs auf 255 setzen
				matrix[i][j][1] = matrix[i][j][2] = 0; // G & B Wert f�r alle LEDs auf 0 setzen
			}
		}
		Matrix matrixData = new Matrix(matrix); // Erstellen eines Matrix-Objekts aus lokalem int[14][14][3]-Array
		ClientThread.sendToSerialPort(matrixData); // Senden der Matrix an seriellen Thread

		running = false; // Status auf beendet setzen
	}

	private void assembleMatrixInformation() { //Zusammensetzen der Matrix
		short[][][] matrix = new short[14][14][3];

		// Apfel setzen durch Zuweisen der RGB Werte an der x- und y-Koordinate des Apfels
		matrix[apple[0]][apple[1]][0] = apple[2];
		matrix[apple[0]][apple[1]][1] = apple[3];
		matrix[apple[0]][apple[1]][2] = apple[4];

		// Schlange setzen
		int xTransform = 0, yTransform = 0;
		for (int i = 0; i < length; i++) { //Iterieren �ber alle Punkte der Schlange
			xTransform = x[i];
			yTransform = y[i];
			if (i == 0) { //Wenn i == 0 --> Kopf der Schlange --> andere F�rbung
				matrix[xTransform][yTransform][0] = matrix[xTransform][yTransform][1] = 0;
				matrix[xTransform][yTransform][2] = 255; //Punkt blau f�rben
			} else {
				matrix[xTransform][yTransform][0] = matrix[xTransform][yTransform][2] = 0;
				matrix[xTransform][yTransform][1] = 255; //Punkt gr�n f�rben
			}
		}
		Matrix matrixData = new Matrix(matrix); // Erstellen eines Matrix-Objekts aus lokalem int[14][14][3]-Array
		ClientThread.sendToSerialPort(matrixData); // Senden der Matrix an seriellen Thread
	}
}