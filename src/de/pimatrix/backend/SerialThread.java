package de.pimatrix.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialThread implements Runnable {

	private short[][][] matrix = new short[14][14][3];
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
			ss = new ServerSocket(63000); // erstellen eines lokalen Sockets auf Port 62000, um die zu übertragende
											// Matrix vom ClientThread
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			try {
				localHost = ss.accept();
			} catch (Exception e) {
				e.printStackTrace();
			}
			initializeInputStream();
			waitForMatrix();

			try {
				writer.tryWrite();
			} catch (Exception e) {
				e.printStackTrace();
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
		private SerialPort arduinoMega;

		public SerialJSONWriter() {
			arduinoMega = new SerialPort("COM7");
			try {
				arduinoMega.openPort();
				arduinoMega.setParams(115200, 8, 1, SerialPort.PARITY_NONE);
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
		}

		public void write() {

		}

		private void tryWrite() throws IOException {
			try {
				for (int i = 0; i < 14; i++) {
					for (int j = 0; j < 14; j++) {
						for (int j2 = 0; j2 < 3; j2++) {
							arduinoMega.writeByte((byte) matrix[i][j][j2]);
						}
					}
				}
//				System.out.println("Waiting for response" + arduinoMega.readString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void close() throws Exception {
			arduinoMega.closePort();
		}
	}
}