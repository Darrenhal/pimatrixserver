package de.pimatrix.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class KeepAlive implements Runnable {

	private ClientThread endPoint;
	private InputStream in;
	private OutputStream out;

	public KeepAlive(Socket keepAliveSocket, ClientThread endPoint) {
		
		try {
			in = keepAliveSocket.getInputStream();
			out = keepAliveSocket.getOutputStream();
		} catch (IOException e) {
		}
		this.endPoint = endPoint;
	}

	@Override
	public void run() {

		boolean waitingForResponse = true;
		int keepAlive_waitCycles = 3;
		int keepAlive_waitedCycles = 0;

		while (endPoint.isConnected()) {
			sleep();

			try {
				out.write(1);
				System.out.println("sending");
			} catch (IOException e) {
			}

			sleep();

			try {
				while (waitingForResponse && keepAlive_waitedCycles < keepAlive_waitCycles) {
					if (in.available() > 0) {
						in.read();
						waitingForResponse = false;
					} else {
						keepAlive_waitedCycles++;
						sleep();
					}
					System.out.println("receiving - cycle: " + keepAlive_waitedCycles);
				}

				if (waitingForResponse && keepAlive_waitedCycles == 3) {
					endPoint.setConnection(false);
				} else {
					keepAlive_waitedCycles = 0;
				}
			} catch (IOException e) {
			}

		}
	}

	private void sleep() {
		int keepAlive_timeout = 3000;

		try {
			Thread.sleep(keepAlive_timeout);
		} catch (InterruptedException e) {
		}
	}

}