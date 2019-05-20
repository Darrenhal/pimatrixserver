package de.pimatrix.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;

public class SerialThread implements Runnable {

	public static SerialPort arduino1, arduino2;
	private int[][][] matrix = new int[14][14][3];

	private int[][][] matrixLeft = new int[7][14][3];
	private int[][][] matrixRight = new int[7][14][3];

	private Socket localHost;
	private Matrix matrixData;
	private ObjectInputStream in;

	@Override
	public void run() {

		SerialJSONWriter writer = new SerialJSONWriter();

		ServerSocket ss = null;
		localHost = null;
		matrixData = new Matrix(matrix);
		try {
			ss = new ServerSocket(62000); // erstellen eines lokalen Sockets auf Port 62000, um die zu übertragende
											// Matrix vom ClientThread
		} catch (IOException e) {
		}

		while (true) {
			try {
				localHost = ss.accept();
			} catch (Exception e) {
				e.printStackTrace();
			}
			initializeInputStream();
			waitForMatrix();
			splitMatrix();

			try {
				writer.tryWrite(matrixRight, matrixLeft);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void splitMatrix() {
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 14; j++) {
				if (i <= 6) {
					matrixRight[i][j][0] = matrix[i][j][0];
					matrixRight[i][j][1] = matrix[i][j][1];
					matrixRight[i][j][2] = matrix[i][j][2];
				} else {
					matrixLeft[i - 7][j][0] = matrix[i][j][0];
					matrixLeft[i - 7][j][1] = matrix[i][j][1];
					matrixLeft[i - 7][j][2] = matrix[i][j][2];
				}
			}
		}
	}

	private void initializeInputStream() {
		try {
			InputStream input = localHost.getInputStream();
			in = new ObjectInputStream(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void waitForMatrix() {
		System.out.println("Waiting for Matrix");
		try {
			matrixData = (Matrix) in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		this.matrix = matrixData.matrix;
	}

	class SerialJSONWriter implements AutoCloseable {

		// Zuweisen der seriellen Ports
		private final SerialPort arduino1, arduino2;

		public SerialJSONWriter() {
			arduino1 = SerialPort.getCommPort("COM5");
			arduino2 = SerialPort.getCommPort("COM6");

			// setzen der Timeouts für die Kommunikation mit den Arduinos
			arduino1.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
			arduino2.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
			arduino1.setBaudRate(115200);
			arduino2.setBaudRate(115200);
			arduino1.openPort();
			arduino2.openPort();
			arduino1.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 0,
					0);
			arduino2.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 0,
					0);
		}

		public void write() {

		}

		private void tryWrite(Object dataRight, Object dataLeft) throws IOException {
			String dataAsJSONRight = new ObjectMapper().writeValueAsString(dataRight) + "\n";
			String dataAsJSONLeft = new ObjectMapper().writeValueAsString(dataLeft) + "\n";
			for (int i = 0; i < dataAsJSONRight.length(); i++) {
				arduino1.getOutputStream().write(dataAsJSONRight.getBytes()[i]);
			}
			for (int i = 0; i < dataAsJSONLeft.length(); i++) {
				arduino2.getOutputStream().write(dataAsJSONLeft.getBytes()[i]);
			}
		}

		@Override
		public void close() throws Exception {
			arduino1.closePort();
			arduino2.closePort();
		}
	}
}