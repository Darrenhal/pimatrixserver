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
	private int[][][] matrix = new int[14][14][3];
	
	private int[][][] matrixLeft = new int[7][14][3];
	private int[][][] matrixRight = new int[7][14][3];
	
	private Socket localHost;
	private Matrix matrixData;
	private ObjectInputStream in;

	@Override
	public void run() {
		//Zuweisen der seriellen Ports
		arduino1 = SerialPort.getCommPort("ttyUSB_ArduRight");
		arduino2 = SerialPort.getCommPort("ttyUSB_ArduLeft");
		
		//setzen der Timeouts für die Kommunikation mit den Arduinos
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
			localHost = new Socket("127.0.0.1", 62000); //erstellen eines lokalen Sockets auf Port 62000, um die zu übertragende Matrix vom ClientThread 
		} catch (IOException e) {}
		
		PrintWriter outToArduino1 = null;
		PrintWriter outToArduino2 = null;
		
		if (arduino2.isOpen()) {
			System.out.println("Getting Output stream");
			outToArduino1 = new PrintWriter(arduino1.getOutputStream());
			outToArduino2 = new PrintWriter(arduino2.getOutputStream());
			System.out.println("Got outputstream");
		}

		System.out.println("Setting Red Matrix");
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				matrix[i][j][0] = 255;
				matrix[i][j][1] = 0;
				matrix[i][j][2] = 0;
			}
		}
		
		System.out.println("Sending Red Matrix");
//		outToArduino1.print(matrix);
		outToArduino2.print(matrix);
		System.out.println("sent red matrix");
		
		arduino2.closePort();
		
		while (true) {
			try {
				localHost = ss.accept();
			} catch (Exception e) {
				e.printStackTrace();
			}
			initializeInputStream();
			waitForMatrix();
			splitMatrix();
			
//			arduino1.openPort();
			arduino2.openPort();
			
			outToArduino1.print(matrixRight);
			outToArduino1.flush();
			outToArduino2.print(matrixLeft);
			outToArduino2.flush();
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
	
	private void splitMatrix() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (i < 6) {
					matrixRight[i][j][0] = matrix[i][j][0];
					matrixRight[i][j][1] = matrix[i][j][1];
					matrixRight[i][j][2] = matrix[i][j][2];
				} else {
					matrixLeft[i][j][0] = matrix[i][j][0];
					matrixLeft[i][j][1] = matrix[i][j][1];
					matrixLeft[i][j][2] = matrix[i][j][2];
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
	
	public void setMatrix(int[][][] pixelIdentifier) {
		matrix = pixelIdentifier;
	}
}