package de.pimatrix.backend;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkController implements Runnable {

	public static boolean serverSocketBuilt = false;
	public static String IPAddress = "";

	private ServerSocket ss = null;

	@Override
	public void run() {
		Socket so = null;

//		getOwnIP();

		setServerSocket();
		// melden, dass ServerSocket erstellt wurde
		serverSocketBuilt = true;
		// IP und Port auf UI anzeigen
		IPAddress = IPAddress + " - " + ss.getLocalPort();

		while (true) {
			try {
				so = ss.accept(); // ServerSocket wartet auf Verbindungsanfrage
									// von Client und weist ihm einen Socket für
									// die weitere Kommunikation zu
			} catch (IOException e) {
				e.printStackTrace();
			}
			new Thread(new ClientThread(so)).start(); //starten eines neuen ClientThreads mit dem dem Client zugeordneten Socket
		}
	}

	// Eigene IP Adresse abrufen
	private void getOwnIP() {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			IPAddress = socket.getLocalAddress().getHostAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Server Socket auf Port 35000 erstellen
	private void setServerSocket() {
		try {
			ss = new ServerSocket(35000);
		} catch (IOException e) {
			GameCenter.exit();
		}
	}

}