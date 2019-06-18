package de.pimatrix.frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.fazecast.jSerialComm.SerialPort;
import de.pimatrix.backend.NetworkController;

public class GameCenterUI extends JFrame {

	public static int clientCount = 0;

	private static final long serialVersionUID = 9167022395346644484L; // Seriennummer für das Fenster (weil JFrame
																		// serialisierbar --> empfohlen serialVersionUID
																		// zu verwenden)
	private Toolkit toolkit = Toolkit.getDefaultToolkit(); // Abrufen des Toolkits --> enthält Informationen über
															// Bildschirm (Größe, etc.)
	private Dimension screen = toolkit.getScreenSize(); // Auslesen der Bidlschirmgröße aus Toolkit

	private SerialPort[] portNames = SerialPort.getCommPorts(); // Auslesen der verfügbaren Port-Namen in ein Array

	private static JLabel lblClients, lblDetectedKeystroke, lblTracker, lblIP;

	public GameCenterUI() { // Initialisieren des Fensters
		setTitle("Game Center Server"); // Fenster Titel setzen
		setSize(screen.width / 2, screen.height / 2); // Größe auf halbe Bildschirmgröße setzen
		setLocation(screen.width / 4, screen.height / 4); // Fenster in Bildschirmmitte platzieren
		setDefaultCloseOperation(EXIT_ON_CLOSE); // Schließen der Anwendung durch Drücken auf Schließen-Symbol
													// einrichten
		JPanel contentPane = new JPanel(null);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setBackground(Color.WHITE); // Erstellen eines JPanels (auf dem alle UI Elemente angezeigt werden)
												// und setzen des Randes und der Hintergrundfarbe
		setContentPane(contentPane); // JPanel als Content Pane dem Fenster übergeben

		setVisible(true); // Fenster sichtbar machen

		loadComponents(contentPane); // laden der UI Komponenten auf die contentPane
	}

	// erstellen der UI Elemente
	private void loadComponents(JPanel contentPane) {
		lblIP = new JLabel("Host IP Address: " + NetworkController.IPAddress); // erstellen eines Labels, das später die
																				// IP Addresse des Hosts anzeigen soll
		lblIP.setBounds(10, 10, screen.width / 4, 10); // Setzen der Größe des Labels
		contentPane.add(lblIP); // Hinzufügen des Labels auf die contentPane

		lblClients = new JLabel("Clients connected: " + clientCount); // Erstellen eines Labels, das die Anzahl
																		// verbundener Clients anzeigt
		lblClients.setBounds(10, 30, screen.width / 4, 10); // Setzen der Größe des Labels
		contentPane.add(lblClients); // Hinzufügen des Labels auf die contentPane

		lblDetectedKeystroke = new JLabel("Last Detected Keystroke: "); // Erstellen eines Labels, das den zuletzt vom
																		// Client übermittelten Interaktionscode anzeigt
		lblDetectedKeystroke.setBounds(10, 50, screen.width / 4, 20); // Setzen der Größe des Labels
		contentPane.add(lblDetectedKeystroke); // Hinzufügen des Labels auf die contentPane

		// Anzeigen der verfügbaren seriellen Ports in Dropdown Menü
		JComboBox<String> portNames = new JComboBox<String>();
		for (int i = 0; i < this.portNames.length; i++) {
			portNames.addItem(this.portNames[i].getSystemPortName()); // Hinzufügen der verfügbaren Port-Namen zum
																		// Dropdown Menü
		}
		portNames.setBounds(10, 70, 100, 20); // Setzen der Größe des Dropdown Menüs mit den Port-Namen
		contentPane.add(portNames); // Hinzufügen des Dropdown Menüs auf die contentPane
	}

	// tracken der Steuerungsbefehle vom Client
	public static void trackLastKeystroke(int keyStroke) {
		lblDetectedKeystroke.setText("Last detected Keystroke: " + keyStroke);
	}

	// anzeigen der aktuell verbundenen Clients
	public static void updateClientCount() {
		lblClients.setText("Clients connected: " + clientCount);
	}

	// tracken des Anwendungsablaufs ohne Commandline
	public static void trackRunState(String tracker) {
		lblTracker.setText(tracker);
	}

	public static void setIPAndPort(String connectivityDetails) { // nachträgliches Setzen von Server IP und Server Port
		lblIP.setText(connectivityDetails);
	}
}