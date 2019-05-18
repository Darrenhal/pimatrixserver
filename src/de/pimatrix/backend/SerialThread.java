package de.pimatrix.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import com.fazecast.jSerialComm.SerialPort;

public class SerialThread implements Runnable {

	public static SerialPort arduino1, arduino2;
	private short[][][] matrix = new short[14][14][3];

	private short[][][] matrixLeft = new short[7][14][3];
	private String matrixTranslationRight = "";
	private short[][][] matrixRight = new short[7][14][3];
	private String matrixTranslationLeft = "";

	private Socket localHost;
	private Matrix matrixData;
	private ObjectInputStream in;

	@Override
	public void run() {
		// Zuweisen der seriellen Ports
		arduino1 = SerialPort.getCommPort("COM5");
		arduino2 = SerialPort.getCommPort("COM6");

		// setzen der Timeouts für die Kommunikation mit den Arduinos
		arduino1.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
		arduino2.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
		arduino1.setBaudRate(115200);
		arduino2.setBaudRate(115200);
		arduino1.openPort();
		arduino2.openPort();

		System.out.println("Comm Ports set");

		ServerSocket ss = null;
		localHost = null;
		matrixData = new Matrix(matrix);
		try {
			ss = new ServerSocket(62000); // erstellen eines lokalen Sockets auf Port 62000, um die zu übertragende
											// Matrix vom ClientThread
		} catch (IOException e) {
		}

		PrintWriter outToArduino1 = null;
		PrintWriter outToArduino2 = null;

		if (arduino1.isOpen()) {
			System.out.println("Getting Output stream");
			outToArduino1 = new PrintWriter(arduino1.getOutputStream());
			outToArduino2 = new PrintWriter(arduino2.getOutputStream());
			System.out.println("Got outputstream");
		}

		while (true) {
			try {
				localHost = ss.accept();
			} catch (Exception e) {
				e.printStackTrace();
			}
			initializeInputStream();
			waitForMatrix();
			translateMatrix();

			arduino1.openPort();
			arduino2.openPort();

//			for (int i = 0; i < 7; i++) {
//				for (int j = 0; j < 14; j++) {
//					for (int j2 = 0; j2 < 3; j2++) {
			System.out.println("Right: " + matrixTranslationRight);
			outToArduino1.print(matrixTranslationRight);
			outToArduino1.flush();
//					}
//				}
//			}
//			
//			for (int i = 0; i < 7; i++) {
//				for (int j = 0; j < 14; j++) {
//					for (int j2 = 0; j2 < 3; j2++) {
			System.out.println("Left: " + matrixTranslationRight);
			outToArduino2.print(matrixTranslationLeft);
			outToArduino2.flush();
//					}
//				}
//			}	
			
			matrixTranslationRight = matrixTranslationLeft = "";
			
			System.out.println("printed to Arduinos");
		}
//		while (true) {
//			for (int i = 0; i < matrix.length; i++) {
//				for (int j = 0; j < matrix.length; j++) {
//					for (int j2 = 0; j2 < matrix.length; j2++) {
//						if (i < 6) {
//							outToArduino1.print(matrix[i][j][j2]);
//							outToArduino1.flush();
//						} else {
//							outToArduino1.print(matrix[i][j][j2]);
//							outToArduino2.flush();
//						}
//					}
//				}
//			}

//			try {
//				Thread.sleep(10);
//			} catch (Exception e) {}

//		}
	}

	private void translateMatrix() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (i <= 6) {
					matrixTranslationRight += (char) matrix[i][j][0];
					matrixTranslationRight += (char) matrix[i][j][1];
					matrixTranslationRight += (char) matrix[i][j][2];
					System.out.println(matrixTranslationRight);
				} else {
					matrixTranslationLeft += (char) matrix[i][j][0];
					matrixTranslationLeft += (char) matrix[i][j][1];
					matrixTranslationLeft += (char) matrix[i][j][2];
				}
			}
		}
	}

	private void splitMatrix() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
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

	public void setMatrix(short[][][] pixelIdentifier) {
		matrix = pixelIdentifier;
	}
}