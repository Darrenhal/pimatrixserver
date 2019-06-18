package de.pimatrix.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import de.pimatrix.frontend.GameCenterUI;
import de.pimatrix.games.pacman.PacManController;
import de.pimatrix.games.pong.PongController;
import de.pimatrix.games.snake.SnakeController;
import de.pimatrix.games.tetris.TetrisController;
import de.pimatrix.games.tictactoe.TTTController;

public class ClientThread implements Runnable {

	private Socket so;
	private boolean clientConnected = true;
	private short[][][] matrix = new short[14][14][3];

	private static SnakeController snake;
	private static TetrisController tetris;
	private static TTTController ttt;
	private static PacManController pacMan;
	private static PongController pong;

	public ClientThread(Socket so) {
		GameCenterUI.trackRunState("Client Thread created");
		this.so = so; // Zuweisen des Sockets, über welchen die Kommunikation mit dem Client (also der
						// App) läuft
	}

	@Override
	public void run() {
		GameCenterUI.trackRunState("Client Thread running");
		GameCenterUI.clientCount++; // Hochzählen der Anzahl verbundener Clients in der GUI
		GameCenterUI.updateClientCount(); // Aktualisieren des Labels, welches die Anzahl verbundener Clients anzeigt

		showStartUpAnimation(); // "Anmeldebildschirm" anzeigen (Power-Symbol, Bild liegt als Excel in der
								// Dropbox)
		sendToSerialPort(new Matrix(matrix)); // Erstellen eines neuen Matrix-Objekts mit dem lokalen
												// int[14][14][3]-Array und übermitteln an den seriellen Port

		while (clientConnected) { // Schleife ausführen solange der Client verbunden ist
			waitForInput(); // Auf Interaktionscodes vom Client warten
		}
	}

	private void waitForInput() {
		try {
			InputStream in = so.getInputStream(); // InputStream vom Socket holen
			int input = in.read(); // in.read() = blockender Aufruf --> wartet bis Daten vorliegen --> Dann Daten
									// einlesen --> liefert int zurück
			GameCenterUI.trackLastKeystroke(input); // Anzeigen des Interaktionscodes auf der GUI --> Zum
													// Troubleshooting
			switch (input) { // switch-case Statement auf den Interaktionscode
			case -1:
			case 0:
				clientConnected = false; // Wenn input = -1 oder 0 --> Client nicht mehr verbunden --> boolean für
											// Schleifenbedingung in run() auf false setzen
				GameCenterUI.clientCount--; // Anzahl verbundener Clients runterzählen
				GameCenterUI.updateClientCount(); // Aktualisieren des Labels, welches die Anzahl verbundener Clients
													// anzeigt
				clearBoard(); // Alle LEDs ausschalten
				break;

			case 1: // start Snake
				if (noGameStarted()) { // Überprüfen, dass kein Spiel gestartet ist
					snake = new SnakeController(); // Neues Snake-Spiel erstellen
					SnakeController.running = true; // Status des Spiels auf laufend setzen
					new Thread(snake).start(); // Spiele-Thread (und damit das Spiel) starten
				}
				break;

			case 2: // Snake left
				if (!snake.lastMoveRight) { // Überprüfen, dass die letzte Bewegung der Schlange nicht nach rechts war
					snake.left = true; // setzen der Richtungsvariablen
					snake.up = false;
					snake.down = false;
				}
				break;

			case 3: // Snake right
				if (!snake.lastMoveLeft) { // Überprüfen, dass die letzte Bewegung der Schlange nicht nach links war
					snake.right = true; // setzen der Richtungsvariablen
					snake.up = false;
					snake.down = false;
				}
				break;

			case 4: // Snake up
				if (!snake.lastMoveDown) { // Überprüfen, dass die letzte Bewegung der Schlange nicht nach unten war
					snake.up = true; // setzen der Richtungsvariablen
					snake.right = false;
					snake.left = false;
				}
				break;

			case 5: // Snake down
				if (!snake.lastMoveUp) { // Überprüfen, dass die letzte Bewegung der Schlange nicht nach oben war
					snake.down = true; // setzen der Richtungsvariablen
					snake.right = false;
					snake.left = false;
				}
				break;

			case 6: // neu Starten von Snake (funktioniert noch nicht wirklich)
				SnakeController.running = false;
				snake = null;
				snake = new SnakeController();
				SnakeController.running = true;
				new Thread(snake).start();
				break;

			case 7: // end Snake
				SnakeController.running = false; // Status des Spiels auf beendet setzen
				snake = null; // Snake auf NULL setzen --> Freigabe des Speichers durch Garbage-Collector
				showStartUpAnimation(); // Berechnen des "Anmeldebildschirms" (Power-Symbol)
				sendToSerialPort(new Matrix(matrix)); // Senden der Matrix an seriellen Thread
				break;

			case 20: // start Tetris
				if (noGameStarted()) {
					tetris = new TetrisController();
					TetrisController.running = true;
				}
				break;

			case 21: // Tetris left
				break;

			case 22: // Tetris right

				break;

			case 23: // Tetris rotate left

				break;

			case 24: // Tetris rotate right

				break;

			case 25: // Tetris boost

				break;

			case 26: // Tetris pause
				if (tetris.isPaused) {
					tetris.isPaused = false;
				} else {
					tetris.isPaused = true;
				}
				break;

			case 27: // end Tetris
				TetrisController.running = false;
				tetris = null;
				break;

			case 40: // start Tic Tac Toe
				if (noGameStarted()) { // Überprüfen, dass kein Spiel gestartet ist
					ttt = new TTTController(); // neues TicTacToe-Spiel erstellen
					TTTController.running = true; // Status des Spiels auf laufend setzen
					new Thread(ttt).start(); // Spiele-Thread (und damit das Spiel) starten
				}
				break;

			case 41: // TTT [1][1]

			case 42: // TTT [2][1]

			case 43: // TTT [3][1]

			case 44: // TTT [1][2]

			case 45: // TTT [2][2]

			case 46: // TTT [3][2]

			case 47: // TTT [1][3]

			case 48: // TTT [2][3]

			case 49: // TTT [3][3]
				ttt.setUserInput(input); // für Codes 41-49 Eingabe an Spiel übermitteln
				break;

			case 50: // reset game
				endGame(TTTController.class); // laufendes Spiel beenden
				ttt = new TTTController(); // neues TicTacToe-Spiel erstellen
				TTTController.running = true; // Status des Spiels auf laufend setzen
				new Thread(ttt).start(); // Spiele-Thread (und damit das Spiel) starten
				break;

			case 51: // end Tic Tac Toe
				TTTController.running = false; // Status des Spiels auf beendet setzen
				ttt = null; // TTT auf NULL setzen --> Freigabe des Speichers durch Garbage-Collector
				showStartUpAnimation(); // Berechnen des "Anmeldebildschirms" (Power-Symbol)
				sendToSerialPort(new Matrix(matrix)); // Senden der Matrix an seriellen Thread
				break;

			case 60: // start Pac Man
				if (noGameStarted()) {
					pacMan = new PacManController();
					PacManController.running = true;
				}
				break;

			case 61: // Pac Man left

				break;

			case 62: // Pac Man right

				break;

			case 63: // Pac Man up

				break;

			case 64: // Pac Man down

				break;

			case 65: // end Pac Man
				PacManController.running = false;
				pacMan = null;
				break;

			case 80: // start Pong
				if (noGameStarted()) {
					pong = new PongController();
					PongController.running = true;
				}
				break;

			case 81: // Pong up

				break;

			case 82: // Pong down

				break;

			case 83: // end Pong
				PongController.running = false;
				pong = null;
				break;

			case 101:
				shutDownAll(); // Beenden laufender Spiele und zurücksetzen der seriellen Verbindung
				break;

			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void shutDownAll() {
		// Alle Spiele auf beendet setzen und Instanz auf NULL setzen --> Freigabe des
		// Speichers durch Garbage-Collector
		SnakeController.running = false;
		snake = null;
		TTTController.running = false;
		if (TTTController.running) {
			ttt.setUserInput(66); // TTT nicht zeitbasiert, sondern rundenbasiert --> Durchführen eines Zuges
									// notwendig, damit Spielabbruchbedingung überprüft werden kann
		}
		ttt = null;
		TetrisController.running = false;
		tetris = null;
		PacManController.running = false;
		pong = null;
		PongController.running = false;

		GameCenter.serialConnection.forceReset = true; // forceReset des SerialThreads auf true setzen --> nach nächstem
														// übermittelten Datensatz läuft Thread zu Ende
		sendToSerialPort(new Matrix(matrix)); // erneutes Senden einer Matrix, damit Schleifen Kopf im SerialThread
												// erreicht werden kann
		try {
			Thread.sleep(30); // Vor weiteren Befehlen 30ms warten
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		GameCenter.serialConnection.reset(); // Reset-Methode von SerialThread rufen --> Schließen aller Sockets und
												// Ports
		GameCenter.serialConnection = null; // SerialThread auf NULL setzen --> Freigabe von Speicher durch GC
		GameCenter.serialConnection = new SerialThread(); // neuen SerialThread erstellen
		new Thread(GameCenter.serialConnection).start(); // den neuen SerialThread starten

//		showStartUpAnimation();
//		sendToSerialPort(new Matrix(matrix), identifier);
	}

	private boolean noGameStarted() { // Überprüfen, ob eines der Spiele läuft --> wenn ja = true, sonst = false
		if (SnakeController.running || TetrisController.running || TTTController.running || PacManController.running
				|| PongController.running) {
			return false;
		} else {
			return true;
		}
	}

	public static void sendToSerialPort(Matrix matrix) {
		try {
			Socket socket = new Socket("127.0.0.1", SerialThread.internalConnectivityPort); // Bei jeder Übermittlung
																							// neuen Socket erstellen;
																							// Verbindung auf Localhost
																							// und vom SerialThread
																							// vorgegebenen Port
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); // OutputStream von Socket holen
																						// und daraus ObjectOutputStream
																						// erstellen
			out.writeObject(matrix); // Matrix-Objekt über Socket an SerialThread senden
			out.close(); // ObjectOutputStream schließen
			socket.close(); // Socket schließen
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public static void endGame(Class instance) { // Jeweiliges Spiel beenden (abhängig von übergebenem Parameter)
		if (instance.isInstance(SnakeController.class)) {
			snake = null;
		}
		if (instance.isInstance(TTTController.class)) {
			ttt = null;
		}
		if (instance.isInstance(TetrisController.class)) {
			tetris = null;
		}
		if (instance.isInstance(PacManController.class)) {
			pacMan = null;
		}
		if (instance.isInstance(PongController.class)) {
			pong = null;
		}
	}

	public boolean isConnected() { // Verbindungsstatus zurückgeben
		return clientConnected;
	}

	public void setConnection(boolean updateConnection) { // Verbindungsstatus setzen
		clientConnected = updateConnection;
	}

	private void showStartUpAnimation() { // Berechnen des Power-Symbols
		int colorValue = new Random().nextInt(3); // Zufällige Zahl von 0 bis 2, um festzulegen, welche Farbe das
													// Power-Symbol hat
		short hue = (short) (new Random().nextInt(200) + 50); // Hauptfarbintensität berechnen (Ergebnis = Wert zwischen
																// 0 und 249)

		for (int i = 0; i < 14; i++) { // Iterieren über matrix mit 3 Schleifen (1 Schleife pro Dimension)
			for (int j = 0; j < 14; j++) {
				for (int k = 0; k < 3; k++) {
					matrix[i][j][k] = 0; // RGB Werte aller LEDs auf 0 setzen
				}
			}
		}

		// Berechnen der Farbintensität für die einzelnen anzusteuerunden LEDs
		// (Power-Symbol hat keine einheitliche Farbintensität, sondern jedes Pixel hat
		// eine von der Hauptfarbintensität abweichende Helligkeit
		matrix[12][4][colorValue] = shiftColor(hue);
		matrix[12][5][colorValue] = shiftColor(hue);
		matrix[12][6][colorValue] = shiftColor(hue);
		matrix[12][7][colorValue] = shiftColor(hue);
		matrix[12][8][colorValue] = shiftColor(hue);
		matrix[12][9][colorValue] = shiftColor(hue);
		matrix[11][3][colorValue] = shiftColor(hue);
		matrix[11][4][colorValue] = shiftColor(hue);
		matrix[11][5][colorValue] = shiftColor(hue);
		matrix[11][6][colorValue] = shiftColor(hue);
		matrix[11][7][colorValue] = shiftColor(hue);
		matrix[11][8][colorValue] = shiftColor(hue);
		matrix[11][9][colorValue] = shiftColor(hue);
		matrix[11][10][colorValue] = shiftColor(hue);
		matrix[10][2][colorValue] = shiftColor(hue);
		matrix[10][3][colorValue] = shiftColor(hue);
		matrix[10][4][colorValue] = shiftColor(hue);
		matrix[10][9][colorValue] = shiftColor(hue);
		matrix[10][10][colorValue] = shiftColor(hue);
		matrix[10][11][colorValue] = shiftColor(hue);
		matrix[9][1][colorValue] = shiftColor(hue);
		matrix[9][2][colorValue] = shiftColor(hue);
		matrix[9][3][colorValue] = shiftColor(hue);
		matrix[9][10][colorValue] = shiftColor(hue);
		matrix[9][11][colorValue] = shiftColor(hue);
		matrix[8][1][colorValue] = shiftColor(hue);
		matrix[8][2][colorValue] = shiftColor(hue);
		matrix[7][1][colorValue] = shiftColor(hue);
		matrix[7][2][colorValue] = shiftColor(hue);
		matrix[7][7][colorValue] = shiftColor(hue);
		matrix[7][8][colorValue] = shiftColor(hue);
		matrix[7][9][colorValue] = shiftColor(hue);
		matrix[7][10][colorValue] = shiftColor(hue);
		matrix[7][11][colorValue] = shiftColor(hue);
		matrix[7][12][colorValue] = shiftColor(hue);

		matrix[1][4][colorValue] = shiftColor(hue);
		matrix[1][5][colorValue] = shiftColor(hue);
		matrix[1][6][colorValue] = shiftColor(hue);
		matrix[1][7][colorValue] = shiftColor(hue);
		matrix[1][8][colorValue] = shiftColor(hue);
		matrix[1][9][colorValue] = shiftColor(hue);
		matrix[2][3][colorValue] = shiftColor(hue);
		matrix[2][4][colorValue] = shiftColor(hue);
		matrix[2][5][colorValue] = shiftColor(hue);
		matrix[2][6][colorValue] = shiftColor(hue);
		matrix[2][7][colorValue] = shiftColor(hue);
		matrix[2][8][colorValue] = shiftColor(hue);
		matrix[2][9][colorValue] = shiftColor(hue);
		matrix[2][10][colorValue] = shiftColor(hue);
		matrix[3][2][colorValue] = shiftColor(hue);
		matrix[3][3][colorValue] = shiftColor(hue);
		matrix[3][4][colorValue] = shiftColor(hue);
		matrix[3][9][colorValue] = shiftColor(hue);
		matrix[3][10][colorValue] = shiftColor(hue);
		matrix[3][11][colorValue] = shiftColor(hue);
		matrix[4][1][colorValue] = shiftColor(hue);
		matrix[4][2][colorValue] = shiftColor(hue);
		matrix[4][3][colorValue] = shiftColor(hue);
		matrix[4][10][colorValue] = shiftColor(hue);
		matrix[4][11][colorValue] = shiftColor(hue);
		matrix[5][1][colorValue] = shiftColor(hue);
		matrix[5][2][colorValue] = shiftColor(hue);
		matrix[6][1][colorValue] = shiftColor(hue);
		matrix[6][2][colorValue] = shiftColor(hue);
		matrix[6][7][colorValue] = shiftColor(hue);
		matrix[6][8][colorValue] = shiftColor(hue);
		matrix[6][9][colorValue] = shiftColor(hue);
		matrix[6][10][colorValue] = shiftColor(hue);
		matrix[6][11][colorValue] = shiftColor(hue);
		matrix[6][12][colorValue] = shiftColor(hue);
	}

	private short shiftColor(short hue) {
		Random rand = new Random();
		if (rand.nextInt(2) == 0) { // Überprüfen ob zufällige Zahl von 0 bis 1 gleich 0 ist oder nicht
			hue += (short) (rand.nextInt(255 - (hue - 50)) * 0.8); // Wenn ja --> höhere Farbintensität --> heller
		} else {
			hue -= (short) (rand.nextInt(200 - (hue - 50)) * 0.8); // Wenn nein --> niedrigere Farbintensität -->
																	// dunkler
		}
		return hue; // Zurücliefern des Helligkeitswertes
	}

	private void clearBoard() { // Iterieren über matrix mit 3 Schleifen (1 Schleife pro Dimension)
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 14; j++) {
				for (int k = 0; k < 3; k++) {
					matrix[i][j][k] = 0; // RGB Werte aller LEDs auf 0 setzen
				}
			}
		}
		sendToSerialPort(new Matrix(matrix)); // erneutes Senden einer Matrix, damit Schleifen Kopf im SerialThread
												// erreicht werden kann
	}
}