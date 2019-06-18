package de.pimatrix.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialThread implements Runnable {

	private short[][][] matrix = new short[14][14][3]; //lokale Referenz auf zu übertragende RGB-Matrix
	private Socket localHost;
	private Matrix matrixData;
	private ObjectInputStream in;
	public static int internalConnectivityPort = 55000; // Port für interne Kommunikation --> ClientThread kann auf
														// Variable zugreifen, sodass die Problematik eines blockierten
														// Ports wegfällt
	public boolean forceReset = false; // boolean zur Steuerung eines Resets der seriellen Verbindung
	private SerialWriter writer;
	private ServerSocket ss;

	@Override
	public void run() {

		writer = new SerialWriter(); // Erstellen eines SerialWriters, über welchen Daten an den Arduino geschrieben
										// werden

		ss = null;
		localHost = null;
		matrixData = new Matrix(matrix); // Erstellen eines neuen Matrix-Objekts mit leerem int[14][14][3]-Array
		while (ss == null) { // Solange versuchen ServerSocket zu erstellen, bis ServerSocket nicht mehr NULL
								// ist
			try {
				ss = new ServerSocket(internalConnectivityPort); // erstellen eines lokalen Sockets auf Port 62000, um
																	// die zu übertragende
																	// Matrix vom ClientThread
			} catch (IOException e) {
				internalConnectivityPort++; // Falls ServerSocket nicht erstellt werden kann --> Hochzählen der
											// Port-Nummer und erneut versuchen ServerSocket zu erstellen
			}
		}

		while (!forceReset) { // Solange Schleife durchführen bis Verbindung zum Arduino zurückgesetzt werden
								// soll
			try {
				localHost = ss.accept(); // Warten auf Verbindungsanfrage des ClientThreads --> blockender Aufruf -->
											// erst, wenn ClientThread sich mit Daten meldet läuft Thread weiter
			} catch (Exception e) {
			}
			initializeInputStream(); // Initialisieren des InputStreams
			waitForMatrix(); // Einlesen der Matrix-Daten

			try {
				writer.write(); // Schreiben der Daten an den Arduino
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void reset() {
		try {
			ss.close(); // Schließen des ServerSockets, falls Verbindung zurückgesetzt wird
		} catch (IOException e) {
			e.printStackTrace();
		}
		writer.reset(); // Schließen des seriellen Ports, falls Verbindung zurückgesetzt wird
		writer = null; // writer-Objekt auf NULL setzen --> Freigeben des Speichers durch
						// Garbage-Collector
	}

	private void initializeInputStream() {
		try {
			InputStream input = localHost.getInputStream(); // InputStream vom ServerSocket holen
			in = new ObjectInputStream(input); // ObjectInputStream vom InputStream holen
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void waitForMatrix() {
		try {
			matrixData = (Matrix) in.readObject(); // Einlesen des serialisierten Matrix-Objekts und Casten auf ein
													// Objekt der Matrix-Klasse
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		this.matrix = matrixData.matrix; // Zuweisen des int[14][14][3]-Arrays, welches im übertragenen Matrix-Objekt
											// gespeichert ist, auf lokales int[14][14][3]-Array
	}

	class SerialWriter implements AutoCloseable {

		// Deklarieren des seriellen Ports
		private SerialPort arduinoMega;

		public SerialWriter() {
			arduinoMega = new SerialPort("/dev/ttyUSB_arduMega"); // Zuweisen des seriellen Ports auf selbst
																	// eingerichteten statischen Port --> Pi weist
																	// "unserem" Arduino Mega über Identifikation der
																	// Seriennummer der Arduinos einen statisch
																	// benannten Port zu --> Betriebssicherheit
			try {
				arduinoMega.openPort(); // Öffnen des seriellen Ports
				arduinoMega.setParams(115200, 8, 1, SerialPort.PARITY_NONE); // Einstellen der Parameter: Baudrate
																				// 115200 Baud, 8 Data Bits, 1 Stop Bit,
																				// keine Parität
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
		}

		public void reset() {
			try {
				close(); // Schließen des seriellen Ports, falls Verbindung zurückgesetzt wird
			} catch (Exception e) {
				e.printStackTrace();
			}
			arduinoMega = null; // seriellen Port auf NULL setzen --> Freigeben des Speichers durch
								// Garbage-Collector
		}

		private void write() throws IOException {
			try { //Iterieren über int[14][14][3]-Array mit 3 Schleifen (je Dimension eine Schleife)
				for (int i = 0; i < 14; i++) {
					for (int j = 0; j < 14; j++) {
						for (int j2 = 0; j2 < 3; j2++) {
							arduinoMega.writeByte((byte) matrix[i][j][j2]); //byteweises Schreiben der Daten an den Arduino
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void close() throws Exception {
			arduinoMega.closePort(); //Schließen des seriellen Ports, falls Verbindung zurückgesetzt wird
		}
	}
}