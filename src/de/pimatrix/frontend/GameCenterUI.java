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

public class GameCenterUI extends JFrame{

	public static int clientCount = 0;
	
	private static final long serialVersionUID = 9167022395346644484L;
	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private Dimension screen = toolkit.getScreenSize();
	
	private SerialPort[] portNames = SerialPort.getCommPorts();
	
	private JLabel lblIP;
	private static JLabel lblClients, lblDetectedKeystroke, lblTracker;
	
	public GameCenterUI() {
		setTitle("Game Center Server");
		setSize(screen.width / 2, screen.height /2);
		setLocation(screen.width / 4, screen.height / 4);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel(null);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);
		
		setVisible(true);
		
		loadComponents(contentPane);
	}
	
	//erstellen der UI Elemente
	private void loadComponents(JPanel contentPane) {
		lblIP = new JLabel("Host IP Address: " + NetworkController.IPAddress);
		lblIP.setBounds(10, 10, screen.width / 4, 10);
		contentPane.add(lblIP);
		
		lblClients = new JLabel("Clients connected: " + clientCount);
		lblClients.setBounds(10, 30, screen.width / 4, 10);
		contentPane.add(lblClients);
		
		lblDetectedKeystroke = new JLabel("Last Detected Keystroke: ");
		lblDetectedKeystroke.setBounds(10, 50, screen.width / 4, 20);
		contentPane.add(lblDetectedKeystroke);
		
		lblTracker = new JLabel("Frame started");
		lblTracker.setBounds(10, 90, screen.width / 4, 20);
		contentPane.add(lblTracker);
		
		//Anzeigen der verfügbaren seriellen Ports in Dropdown Menü
		JComboBox<String> portNames = new JComboBox<String>();
		for (int i = 0; i < this.portNames.length; i++) {
			portNames.addItem(this.portNames[i].getSystemPortName());
		}
		portNames.setBounds(10, 70, 100, 20);
		contentPane.add(portNames);
	}
	//tracken der Steuerungsbefehle vom Client
	public static void trackLastKeystroke(int keyStroke) {
		lblDetectedKeystroke.setText("Last detected Keystroke: " + keyStroke);
	}
	//anzeigen der aktuell verbundenen Clients
	public static void updateClientCount() {
		lblClients.setText("Clients connected: " + clientCount);
	}
	//tracken des Anwendungsablaufs ohne Commandline
	public static void trackRunState(String tracker) {
		lblTracker.setText(tracker);
	}
}