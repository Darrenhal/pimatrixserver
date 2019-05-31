package de.pimatrix.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import de.pimatrix.frontend.GameCenterUI;
import de.pimatrix.games.pacman.PacManController;
import de.pimatrix.games.pong.PongController;
import de.pimatrix.games.snake.SnakeController;
import de.pimatrix.games.tetris.TetrisController;
import de.pimatrix.games.tictactoe.TTTController;

public class ClientThread implements Runnable {

	private Socket so;
	private boolean clientConnected = true;
	private short[][][] matrix = new short[14][14][3];

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

		showStartUpAnimation();
		sendToSerialPort(new Matrix(matrix));

//		new Thread(new KeepAlive(so, this)).start();

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
				clearBoard();
				break;

			case 1: // start Snake
				System.out.println("Snake started");
				if (noGameStarted()) {
					snake = new SnakeController();
					SnakeController.running = true;
					new Thread(snake).start();
				}
				break;

			case 2: // Snake left
				if (snake.right != true) {
					snake.left = true;
					snake.up = false;
					snake.down = false;
				}
				break;

			case 3: // Snake right
				if (snake.left != true) {
					snake.right = true;
					snake.up = false;
					snake.down = false;
				}
				break;

			case 4: // Snake up
				if (snake.down != true) {
					snake.up = true;
					snake.right = false;
					snake.left = false;
				}
				break;

			case 5: // Snake down
				if (snake.up != true) {
					snake.down = true;
					snake.right = false;
					snake.left = false;
				}
				break;

			case 6: // end Snake
				SnakeController.running = false;
				snake = null;
				showStartUpAnimation();
				sendToSerialPort(new Matrix(matrix));
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

			case 50: //reset game
				endGame(TTTController.class);
				ttt = new TTTController();
				TTTController.running = true;
				new Thread(ttt).start();
				break;
				
			case 51: // end Tic Tac Toe
				TTTController.running = false;
				ttt = null;
				showStartUpAnimation();
				sendToSerialPort(new Matrix(matrix));
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
		try {
			Socket socket = new Socket("127.0.0.1", 62000);
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

	public boolean isConnected() {
		return clientConnected;
	}

	public void setConnection(boolean updateConnection) {
		clientConnected = updateConnection;
	}

	private void showStartUpAnimation() {
		int colorValue = new Random().nextInt(3);
		short hue = (short) (new Random().nextInt(200) + 50);

		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 14; j++) {
				for (int k = 0; k < 3; k++) {
					matrix[i][j][k] = 0;
				}
			}
		}

		matrix[12][4][colorValue] = shiftColor(hue);
		matrix[12][5][colorValue] = shiftColor(hue);
		matrix[12][6][colorValue] = shiftColor(hue);
		matrix[12][7][colorValue] = shiftColor(hue);
		matrix[12][8][colorValue] = shiftColor(hue);
		matrix[12][9][colorValue] = shiftColor(hue);
		matrix[11][3][colorValue] = shiftColor(hue);
		matrix[11][4][colorValue] = shiftColor(hue);
		matrix[11][5][colorValue] = shiftColor(hue);
		matrix[11][6][colorValue] = shiftColor(hue);
		matrix[11][7][colorValue] = shiftColor(hue);
		matrix[11][8][colorValue] = shiftColor(hue);
		matrix[11][9][colorValue] = shiftColor(hue);
		matrix[11][10][colorValue] = shiftColor(hue);
		matrix[10][2][colorValue] = shiftColor(hue);
		matrix[10][3][colorValue] = shiftColor(hue);
		matrix[10][4][colorValue] = shiftColor(hue);
		matrix[10][9][colorValue] = shiftColor(hue);
		matrix[10][10][colorValue] = shiftColor(hue);
		matrix[10][11][colorValue] = shiftColor(hue);
		matrix[9][1][colorValue] = shiftColor(hue);
		matrix[9][2][colorValue] = shiftColor(hue);
		matrix[9][3][colorValue] = shiftColor(hue);
		matrix[9][10][colorValue] = shiftColor(hue);
		matrix[9][11][colorValue] = shiftColor(hue);
		matrix[8][1][colorValue] = shiftColor(hue);
		matrix[8][2][colorValue] = shiftColor(hue);
		matrix[7][1][colorValue] = shiftColor(hue);
		matrix[7][2][colorValue] = shiftColor(hue);
		matrix[7][7][colorValue] = shiftColor(hue);
		matrix[7][8][colorValue] = shiftColor(hue);
		matrix[7][9][colorValue] = shiftColor(hue);
		matrix[7][10][colorValue] = shiftColor(hue);
		matrix[7][11][colorValue] = shiftColor(hue);
		matrix[7][12][colorValue] = shiftColor(hue);

		matrix[1][4][colorValue] = shiftColor(hue);
		matrix[1][5][colorValue] = shiftColor(hue);
		matrix[1][6][colorValue] = shiftColor(hue);
		matrix[1][7][colorValue] = shiftColor(hue);
		matrix[1][8][colorValue] = shiftColor(hue);
		matrix[1][9][colorValue] = shiftColor(hue);
		matrix[2][3][colorValue] = shiftColor(hue);
		matrix[2][4][colorValue] = shiftColor(hue);
		matrix[2][5][colorValue] = shiftColor(hue);
		matrix[2][6][colorValue] = shiftColor(hue);
		matrix[2][7][colorValue] = shiftColor(hue);
		matrix[2][8][colorValue] = shiftColor(hue);
		matrix[2][9][colorValue] = shiftColor(hue);
		matrix[2][10][colorValue] = shiftColor(hue);
		matrix[3][2][colorValue] = shiftColor(hue);
		matrix[3][3][colorValue] = shiftColor(hue);
		matrix[3][4][colorValue] = shiftColor(hue);
		matrix[3][9][colorValue] = shiftColor(hue);
		matrix[3][10][colorValue] = shiftColor(hue);
		matrix[3][11][colorValue] = shiftColor(hue);
		matrix[4][1][colorValue] = shiftColor(hue);
		matrix[4][2][colorValue] = shiftColor(hue);
		matrix[4][3][colorValue] = shiftColor(hue);
		matrix[4][10][colorValue] = shiftColor(hue);
		matrix[4][11][colorValue] = shiftColor(hue);
		matrix[5][1][colorValue] = shiftColor(hue);
		matrix[5][2][colorValue] = shiftColor(hue);
		matrix[6][1][colorValue] = shiftColor(hue);
		matrix[6][2][colorValue] = shiftColor(hue);
		matrix[6][7][colorValue] = shiftColor(hue);
		matrix[6][8][colorValue] = shiftColor(hue);
		matrix[6][9][colorValue] = shiftColor(hue);
		matrix[6][10][colorValue] = shiftColor(hue);
		matrix[6][11][colorValue] = shiftColor(hue);
		matrix[6][12][colorValue] = shiftColor(hue);
	}

	private short shiftColor(short hue) {
		Random rand = new Random();
		if (rand.nextInt(2) == 0) {
			hue += (short) (rand.nextInt(255 - (hue - 50)) * 0.8);
		} else {
			hue -= (short) (rand.nextInt(200 - (hue - 50)) * 0.8);
		}
		return hue;
	}

	private void clearBoard() {
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 14; j++) {
				for (int k = 0; k < 3; k++) {
					matrix[i][j][k] = 0;
				}
			}
		}
		sendToSerialPort(new Matrix(matrix));
	}
}