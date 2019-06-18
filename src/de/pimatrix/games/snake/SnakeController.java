package de.pimatrix.games.snake;

import java.util.Random;
import de.pimatrix.backend.ClientThread;
import de.pimatrix.backend.Matrix;

public class SnakeController implements Runnable {

	public static boolean running;
	public boolean right = false, left = true, up = false, down = false; // für nächsten Zug geplante Richtung
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
												// --> Für jeden Punkt der Schlange kann die jeweilige x-Koordinate
												// gespeichert werden; es wird nur die Länge des Arrays genutzt, die der
												// Länge der Schlange entspricht
	private final short y[] = new short[196]; // Feld besteht aus 196 Feldern, Schlange kann also 196 Felder lang sein
												// --> Für jeden Punkt der Schlange kann die jeweilige y-Koordinate
												// gespeichert werden; es wird nur die Länge des Arrays genutzt, die der
												// Länge der Schlange entspricht

	private short length = 3; // Länge der Schlange
	private short[] apple = new short[5]; // Repräsentation des Apfels bestehend aus 5 int-Werten: x-Koordinate,
											// y-Koordinate, Rot-, Grün- & Blau- Wert

	@Override
	public void run() {
		initGame(); // Initialisieren des Spiels
		try {
			Thread.sleep(3000); // 3s Warten bevor Spiel beginnt zu laufen, damit sich Spieler vorbereiten kann
		} catch (InterruptedException e1) {
		}

		while (running) { // Schleife durchlaufen, solange Spiel läuft
			checkApple(); // Überprüfen, ob Apfel auf dem Spielfeld ist oder gerade gegessen wurde und
							// eventuell erneutes Platzieren des Apfels
			move(); // Bewegen der Schlange
			checkCollision(); // Überprüfen, ob Schlange gegen sich oder eine Wand gestoßen ist
			if (running) {
				assembleMatrixInformation(); // Wenn Spiel nocht läuft (die Schlange also nicht gegen eine Wand oder
												// sich selbst gestoßen ist) Zusammensetzen und Übertragen der Matrix an
												// den Arduino
			}

			try {
				Thread.sleep(300); // 300ms Warten bevor der nächste Spielzug durchlaufen wird
			} catch (InterruptedException e) {
			}
		}
	}

	private void checkApple() {
		if ((x[0] == apple[0]) && (y[0] == apple[1]) || !foodAvailable) { // Überprüfen, ob der Kopf der Schlange an der
																			// gleichen Position ist, wie der Apfel oder
																			// kein Essen verfügbar ist
			length++; // Falls ja --> Länge der Schlange erhöhen
			foodAvailable = false; // Essen auf "nicht verfügbar" setzen
			while (!foodAvailable) { // Schleife durchlaufen bis Essen verfügbar ist
				locateFood(); // Essen platzieren
			}
		}
	}

	private void locateFood() {
		boolean collision = false; // boolean zum Überprüfen, ob der Apfel in der Schlange generiert wird
		Random random = new Random();

		short randomPos = (short) random.nextInt(13); // Zufallszahl von 0 bis 13
		apple[0] = randomPos; // Zuweisen der Zufallszahl als x-Koordinate des Apfels

		randomPos = (short) random.nextInt(13); // neue Zufallszahl berechnen
		apple[1] = randomPos; // Zuweisen als y-Koordinate des Apfels

		for (int i = 0; i < length; i++) { // über jeden Punkt der Schlange iterieren
			if (apple[0] == x[i] && apple[1] == y[i]) { // Prüfen ob der jeweilige Punkt der Schlange = der berechnete
														// Punkt des Apfels ist
				collision = true; // Falls ja --> collision = true
			}
		}

		if (!collision) { // Wenn der Apfel nicht in der Schlange generiert wurde (also collision = false)
			foodAvailable = true; // Dann foodAvailable auf true setzen --> while-Bedingung in checkApple()
									// erfüllt
		}

		// RGB Werte für Apfel setzen
		apple[2] = 255;
		apple[3] = 0;
		apple[4] = 0;
	}

	private void checkCollision() {
		if (length > 4) { // Überprüfen, dass die Schlange mehr als 4 Punkte hat (bei <= 4 Punkten kann
							// Schlange sich selbst niemals selbst fressen)
			for (int i = length; i > 0; i--) { // Iterieren über alle Punkte der Schlange außer dem Kopf
				if ((x[0] == x[i]) && (y[0] == y[i])) { // für jeden Punkt überprüfen, ob er die selbe Position, wie der
														// Kopf hat
					gameOver(); // Falls ja --> Game Over
				}
			}
		}

		if (y[0] >= 14) { // Überprüfen, ob die Schlange links anstößt
			gameOver();
		}
		if (y[0] < 0) { // Überprüfen, ob die Schlange rechts anstößt
			gameOver();
		}
		if (x[0] >= 14) { // Überprüfen, ob die Schlange oben anstößt
			gameOver();
		}
		if (x[0] < 0) { // Überprüfen, ob die Schlange unten anstößt
			gameOver();
		}

	}

	private void move() {
		for (int i = length; i > 0; i--) { // Iterieren über alle Punkte der Schlange außer dem Kopf
			x[i] = x[(i - 1)]; // x-Koordinate erhält die x-Position die der vorhergehende Punkt im letzten Zug
								// hatte
			y[i] = y[(i - 1)]; // y-Koordinate erhält die y-Position die der vorhergehende Punkt im letzten Zug
								// hatte
		}

		if (right) { // wenn Befehl für Bewegung nach rechts ...
			x[0] -= 1; // ... x-Koordinate des Kopfes -1 rechnen
			lastMoveRight = true; // ... setzen, dass die letzte Bewegung der Schlange nach rechts war
			lastMoveLeft = lastMoveUp = lastMoveDown = false; // ... setzen, dass die letzte Bewegung der Schlange nicht
																// nach links, oben oder unten war
		}
		if (left) { // wenn Befehl für Bewegung nach links ...
			x[0] += 1; // ... x-Koordinate des Kopfes +1 rechnen
			lastMoveLeft = true; // ... setzen, dass die letzte Bewegung der Schlange nach links war
			lastMoveRight = lastMoveUp = lastMoveDown = false; // ... setzen, dass die letzte Bewegung der Schlange nicht
																// nach rechts, oben oder unten war
		}
		if (up) { // wenn Befehl für Bewegung nach links ...
			y[0] += 1; // ... y-Koordinate des Kopfes +1 rechnen
			lastMoveUp = true; // ... setzen, dass die letzte Bewegung der Schlange nach oben war
			lastMoveLeft = lastMoveRight = lastMoveDown = false; // ... setzen, dass die letzte Bewegung der Schlange nicht
																// nach links, rechts oder unten war
		}
		if (down) { // wenn Befehl für Bewegung nach links ...
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
		assembleMatrixInformation(); // Zusammensetzen und Übertragen der Matrix an den Arduino
	}

	private void gameOver() {
		try {
			Thread.sleep(2000); // 2s Warten, bevor Game Over-Bildschirm angezeigt wird, damit man den letzten
								// Zug nochmal kurz sieht
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		short[][][] matrix = new short[14][14][3]; // int[14][14][3]-Array deklarieren

		for (int i = 0; i < 14; i++) { // über Array iterieren mit 2 Schleifen (je 1 für x- und y-Dimension)
			for (int j = 0; j < 14; j++) {
				matrix[i][j][0] = 255; // R Wert für alle LEDs auf 255 setzen
				matrix[i][j][1] = matrix[i][j][2] = 0; // G & B Wert für alle LEDs auf 0 setzen
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
		for (int i = 0; i < length; i++) { //Iterieren über alle Punkte der Schlange
			xTransform = x[i];
			yTransform = y[i];
			if (i == 0) { //Wenn i == 0 --> Kopf der Schlange --> andere Färbung
				matrix[xTransform][yTransform][0] = matrix[xTransform][yTransform][1] = 0;
				matrix[xTransform][yTransform][2] = 255; //Punkt blau färben
			} else {
				matrix[xTransform][yTransform][0] = matrix[xTransform][yTransform][2] = 0;
				matrix[xTransform][yTransform][1] = 255; //Punkt grün färben
			}
		}
		Matrix matrixData = new Matrix(matrix); // Erstellen eines Matrix-Objekts aus lokalem int[14][14][3]-Array
		ClientThread.sendToSerialPort(matrixData); // Senden der Matrix an seriellen Thread
	}
}