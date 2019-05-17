package de.pimatrix.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import de.pimatrix.frontend.GameCenterUI;
import de.pimatrix.games.pacman.PacManController;
import de.pimatrix.games.pong.PongController;
import de.pimatrix.games.snake.SnakeController;
import de.pimatrix.games.tetris.TetrisController;
import de.pimatrix.games.tictactoe.TTTController;

public class ClientThread implements Runnable {

	private Socket so;
	private boolean clientConnected = true;

	private static SnakeController snake;
	private static TetrisController tetris;
	private static TTTController ttt;
	private static PacManController pacMan;
	private static PongController pong;

	public ClientThread(Socket so) {
		GameCenterUI.trackRunState("Client Thread created");
		this.so = so;
	}

	@Override
	public void run() {
		GameCenterUI.trackRunState("Client Thread running");
		GameCenterUI.clientCount++;
		GameCenterUI.updateClientCount();

		while (clientConnected) {
			waitForInput();
		}
	}

	private void waitForInput() {
		System.out.println("Waiting for input");
		try {
			InputStream in = so.getInputStream();
			int input = in.read();
			GameCenterUI.trackLastKeystroke(input);
			switch (input) {
			case -1:
			case 0:
				clientConnected = false;
				GameCenterUI.clientCount--;
				GameCenterUI.updateClientCount();
				break;

			case 1: // start Snake
				System.out.println("Snake started");
				if (noGameStarted()) {
					snake = new SnakeController();
					SnakeController.running = true;
					new Thread(snake).start();
					;
				}
				break;

			case 2: // Snake left

			case 3: // Snake right

			case 4: // Snake up

			case 5: // Snake down
				snake.setDirection(input);
				break;

			case 6: // end Snake
				SnakeController.running = false;
				snake = null;
				break;

			case 20: // start Tetris
				if (noGameStarted()) {
					tetris = new TetrisController();
					TetrisController.running = true;
				}
				break;

			case 21: // Tetris left
				break;

			case 22: // Tetris right

				break;

			case 23: // Tetris rotate left

				break;

			case 24: // Tetris rotate right

				break;

			case 25: // Tetris boost

				break;

			case 26: // Tetris pause
				if (tetris.isPaused) {
					tetris.isPaused = false;
				} else {
					tetris.isPaused = true;
				}
				break;

			case 27: // end Tetris
				TetrisController.running = false;
				tetris = null;
				break;

			case 40: // start Tic Tac Toe
				if (noGameStarted()) {
					ttt = new TTTController();
					TTTController.running = true;
					new Thread(ttt).start();
				}
				break;

			case 41: // TTT [1][1]

			case 42: // TTT [2][1]

			case 43: // TTT [3][1]

			case 44: // TTT [1][2]

			case 45: // TTT [2][2]

			case 46: // TTT [3][2]

			case 47: // TTT [1][3]

			case 48: // TTT [2][3]

			case 49: // TTT [3][3]
				ttt.setUserInput(input);
				break;

			case 50: // end Tic Tac Toe
				TTTController.running = false;
				ttt = null;
				break;

			case 60: // start Pac Man
				if (noGameStarted()) {
					pacMan = new PacManController();
					PacManController.running = true;
				}
				break;

			case 61: // Pac Man left

				break;

			case 62: // Pac Man right

				break;

			case 63: // Pac Man up

				break;
				
			case 64: // Pac Man down

				break;

			case 65: // end Pac Man
				PacManController.running = false;
				pacMan = null;
				break;

			case 80: // start Pong
				if (noGameStarted()) {
					pong = new PongController();
					PongController.running = true;
				}
				break;

			case 81: // Pong up

				break;

			case 82: // Pong down

				break;

			case 83: // end Pong
				PongController.running = false;
				pong = null;
				break;

			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean noGameStarted() {
		if (SnakeController.running || TetrisController.running || TTTController.running || PacManController.running
				|| PongController.running) {
			return false;
		} else {
			return true;
		}
	}

	public static void sendToSerialPort(Matrix matrix) {
		System.out.println("Prompt To Matrix");
		try {
			Socket socket = new Socket("127.0.0.1", 36000);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(matrix);
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void endGame(Class instance) {
		if (instance.isInstance(SnakeController.class)) {
			snake = null;
		}
		if (instance.isInstance(TTTController.class)) {
			ttt = null;
		}
		if (instance.isInstance(TetrisController.class)) {
			tetris = null;
		}
		if (instance.isInstance(PacManController.class)) {
			pacMan = null;
		}
		if (instance.isInstance(PongController.class)) {
			pong = null;
		}
	}
}