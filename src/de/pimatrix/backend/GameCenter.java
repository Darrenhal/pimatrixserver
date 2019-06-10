package de.pimatrix.backend;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;

import de.pimatrix.frontend.GameCenterUI;

public class GameCenter {
	
	private static GameCenterUI frame;
	public static SerialThread serialConnection;
	
	//schließt Anwendung, wenn ServerSocket nicht erstellt werden kann
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
				//neuen NetworkController erstellen, der auf Verbindungsanfrage vom Client wartet
				new Thread(new NetworkController()).start();
				//Solange Socket noch nicht bereit, 10ms schlafen
				while (!NetworkController.serverSocketBuilt) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
				//JFrame erstellen
				frame = new GameCenterUI();
				
				//Thread für serielle Kommunikation mit Arduinos erstellen und starten
				serialConnection = new SerialThread();
				new Thread(serialConnection).start();
			}
		});
	}
}