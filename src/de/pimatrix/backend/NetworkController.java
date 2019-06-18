package de.pimatrix.backend;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.pimatrix.frontend.GameCenterUI;

public class NetworkController implements Runnable {

	public static boolean serverSocketBuilt = false; // boolean, um zu ermitteln, ob der ServerSocket schon bereit ist,
														// oder nicht
	public static String IPAddress = "";
	public static boolean forceReset = false;

	private ServerSocket ss = null;

	@Override
	public void run() {
		Socket so = null;

		getOwnIP(); // versuchen, eigene IP Adresse abzurufen (funktioniert nur, wenn Internet
					// verfügbar ist, da ein DNS-Server (hier der Google-DNS 8.8.8.8) angepingt
					// werden muss)

		setServerSocket();
		serverSocketBuilt = true; // melden, dass ServerSocket erstellt wurde
		IPAddress = IPAddress + " - " + ss.getLocalPort(); // IP und Port auf UI anzeigen
		GameCenterUI.setIPAndPort(IPAddress);

		while (!forceReset) {
			try {
				so = ss.accept(); // ServerSocket wartet auf Verbindungsanfrage
									// von Client und weist ihm einen Socket für
									// die weitere Kommunikation zu
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!forceReset) {
				new Thread(new ClientThread(so)).start(); // starten eines neuen ClientThreads mit dem dem Client
															// zugeordneten Socket
			}
		}
	}

	// Eigene IP Adresse abrufen
	private void getOwnIP() {
		try (final DatagramSocket socket = new DatagramSocket()) { // try-with-resource Block --> Wenn Datagram Socket
																	// erfolgreich erstellt wurde Ausführen des
																	// Try-Blocks
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002); // Versuchen Verbindung zum Google-DNS auf Port
																		// 10002 herzustellen
			IPAddress = socket.getLocalAddress().getHostAddress(); // Auslesen der eigenen IP Adresse
		} catch (IOException e) {
		}
	}

	// Server Socket auf Port 35000 erstellen
	private void setServerSocket() {
		try {
			ss = new ServerSocket(35000); // versuchen ServerSocket zu erstellen
		} catch (IOException e) {
			GameCenter.exit(); // Falls Versuch fehlschlägt --> Anwendung beenden
		}
	}

}