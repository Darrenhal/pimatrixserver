package de.pimatrix.backend;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;

import de.pimatrix.frontend.GameCenterUI;

public class GameCenter {

	private static GameCenterUI frame;
	public static SerialThread serialConnection;

	// schließt Anwendung, wenn kein ServerSocket vom NetworkController erstellt
	// werden konnte (z.B. vorgesehener Port von anderem Socket blockiert)
	public static void exit() {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	public static void main(String[] args) {
		new GameCenter();
	}

	public GameCenter() {
		startGameCenter();
	}

	public static void startGameCenter() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				// JFrame erstellen
				frame = new GameCenterUI();
				// neuen NetworkController erstellen, der auf Verbindungsanfrage vom Client
				// wartet
				new Thread(new NetworkController()).start();
				// Solange Socket noch nicht bereit, 10ms schlafen
				while (!NetworkController.serverSocketBuilt) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
				// Thread für serielle Kommunikation mit Arduino erstellen und starten
				serialConnection = new SerialThread();
				new Thread(serialConnection).start();
			}
		});
	}
}