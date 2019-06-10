package de.pimatrix.games.tictactoe;

import de.pimatrix.backend.ClientThread;
import de.pimatrix.backend.Matrix;

public class TTTController implements Runnable {

	public static boolean running;
	private int[] isBlocked = new int[9];
	private int inputOffset = 41; // app keycode für button[1][1] = 41 -->
									// Zugriff auf Array über Werte von 0-9
	private int userInput = 0;
	private short[][][] matrix = new short[14][14][3];
	private boolean validMove = false;
	private boolean inputReceived = false;
	private Matrix matrixData;
	private int identifier = 1;

	@Override
	public void run() {
		int turn = 1;
		initGame();

		while (running) {
			validMove = false;
			inputReceived = false;
			while (!validMove) {
				if (inputReceived) {
					waitForUserTurn(turn);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			calculatePattern(calculatePatternCenter(userInput - inputOffset), turn);
			if (turn == 1) {
				showUserTurn(2);
			} else {
				showUserTurn(1);
			}
			promptToMatrix();

			checkWinCondition(turn);

			if (turn == 1) {
				turn = 2;
			} else {
				turn = 1;
			}
		}
	}

	private int[] calculatePatternCenter(int field) {
		int[] centerCoordinates = new int[2];

		switch (field) {
		case 0:
			centerCoordinates[0] = 11;
			centerCoordinates[1] = 11;
			return centerCoordinates;

		case 1:
			centerCoordinates[0] = 6;
			centerCoordinates[1] = 11;
			return centerCoordinates;

		case 2:
			centerCoordinates[0] = 1;
			centerCoordinates[1] = 11;
			return centerCoordinates;

		case 3:
			centerCoordinates[0] = 11;
			centerCoordinates[1] = 6;
			return centerCoordinates;

		case 4:
			centerCoordinates[0] = 6;
			centerCoordinates[1] = 6;
			return centerCoordinates;

		case 5:
			centerCoordinates[0] = 1;
			centerCoordinates[1] = 6;
			return centerCoordinates;

		case 6:
			centerCoordinates[0] = 11;
			centerCoordinates[1] = 1;
			return centerCoordinates;

		case 7:
			centerCoordinates[0] = 6;
			centerCoordinates[1] = 1;
			return centerCoordinates;

		case 8:
			centerCoordinates[0] = 1;
			centerCoordinates[1] = 1;
			return centerCoordinates;

		default:
			int[] def = { 0, 0 };
			return def;
		}
	}
	
	private void initGame() {
		matrixData = new Matrix(matrix);
		for (int i = 0; i < isBlocked.length; i++) {
			isBlocked[i] = 0;
		}
		showUserTurn(1);
		generateGrid();
		promptToMatrix();
	}

	private void showUserTurn(int turn) {
		if (turn == 1) {
			for (int i = 0; i < 14; i++) {
				matrix[13][i][0] = 255;
				matrix[i][13][0] = 255;
				matrix[13][i][1] = 0;
				matrix[i][13][1] = 0;
			}
		} else {
			for (int i = 0; i < 14; i++) {
				matrix[13][i][1] = 255;
				matrix[i][13][1] = 255;
				matrix[13][i][0] = 0;
				matrix[i][13][0] = 0;
			}
		}
	}
	
	private void waitForUserTurn(int turn) {
		if (isBlocked[userInput - inputOffset] == 0) {
			isBlocked[userInput - inputOffset] = turn;
			validMove = true;
		}
	}

	private void promptToMatrix() {
		matrixData.matrix = matrix;
		ClientThread.sendToSerialPort(matrixData, identifier);
	}

	private void checkWinCondition(int turn) {
		if ((isBlocked[0] == isBlocked[1] && isBlocked[0] == isBlocked[2] && isBlocked[0] == 1)
				|| (isBlocked[3] == isBlocked[4] && isBlocked[3] == isBlocked[5] && isBlocked[3] == 1)
				|| (isBlocked[6] == isBlocked[7] && isBlocked[6] == isBlocked[8] && isBlocked[6] == 1)
				|| (isBlocked[0] == isBlocked[3] && isBlocked[0] == isBlocked[6] && isBlocked[0] == 1)
				|| (isBlocked[1] == isBlocked[4] && isBlocked[1] == isBlocked[7] && isBlocked[1] == 1)
				|| (isBlocked[2] == isBlocked[5] && isBlocked[2] == isBlocked[8] && isBlocked[2] == 1)
				|| (isBlocked[0] == isBlocked[4] && isBlocked[0] == isBlocked[8] && isBlocked[0] == 1)
				|| (isBlocked[2] == isBlocked[4] && isBlocked[2] == isBlocked[6] && isBlocked[2] == 1)) {
			gameOver(1);
		} else if ((isBlocked[0] == isBlocked[1] && isBlocked[0] == isBlocked[2] && isBlocked[0] == 2)
				|| (isBlocked[3] == isBlocked[4] && isBlocked[3] == isBlocked[5] && isBlocked[3] == 2)
				|| (isBlocked[6] == isBlocked[7] && isBlocked[6] == isBlocked[8] && isBlocked[6] == 2)
				|| (isBlocked[0] == isBlocked[3] && isBlocked[0] == isBlocked[6] && isBlocked[0] == 2)
				|| (isBlocked[1] == isBlocked[4] && isBlocked[1] == isBlocked[7] && isBlocked[1] == 2)
				|| (isBlocked[2] == isBlocked[5] && isBlocked[2] == isBlocked[8] && isBlocked[2] == 2)
				|| (isBlocked[0] == isBlocked[4] && isBlocked[0] == isBlocked[8] && isBlocked[0] == 2)
				|| (isBlocked[2] == isBlocked[4] && isBlocked[2] == isBlocked[6] && isBlocked[2] == 2)) {
			gameOver(2);
		} else if (isBlocked[0] != 0 && isBlocked[1] != 0 && isBlocked[2] != 0 && isBlocked[3] != 0 && isBlocked[4] != 0
				&& isBlocked[5] != 0 && isBlocked[6] != 0 && isBlocked[7] != 0 && isBlocked[8] != 0) {
			gameOver(0);
		}
	}

	private void gameOver(int turn) {
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
		
		if (turn == 1) {
			for (int i = 0; i < 14; i++) {
				for (int j = 0; j < 14; j++) {
					matrix[i][j][0] = 255;
					matrix[i][j][1] = 0;
					matrix[i][j][2] = 0;
				}
			}
		} else if (turn == 2) {
			for (int i = 0; i < 14; i++) {
				for (int j = 0; j < 14; j++) {
					matrix[i][j][0] = 0;
					matrix[i][j][1] = 255;
					matrix[i][j][2] = 0;
				}
			}
		} else if (turn == 0) {
			for (int i = 0; i < 14; i++) {
				for (int j = 0; j < 14; j++) {
					matrix[i][j][0] = 0;
					matrix[i][j][1] = 0;
					matrix[i][j][2] = 255;
				}
			}
		}
		promptToMatrix();
		
		running = false;
		ClientThread.endGame(this.getClass());
	}

	public void setUserInput(int userInput) {
		inputReceived = true;
		this.userInput = userInput;
	}

	private void generateGrid() {
		for (int i = 0; i < 13; i++) {
			matrix[3][i][0] = matrix[3][i][1] = 0;
			matrix[3][i][2] = 255;
			matrix[4][i][0] = matrix[4][i][1] = 0;
			matrix[4][i][2] = 255;
			matrix[8][i][0] = matrix[8][i][1] = 0;
			matrix[8][i][2] = 255;
			matrix[9][i][0] = matrix[9][i][1] = 0;
			matrix[9][i][2] = 255;

			matrix[i][3][0] = matrix[i][3][1] = 0;
			matrix[i][3][2] = 255;
			matrix[i][4][0] = matrix[i][4][1] = 0;
			matrix[i][4][2] = 255;
			matrix[i][8][0] = matrix[i][8][1] = 0;
			matrix[i][8][2] = 255;
			matrix[i][9][0] = matrix[i][9][1] = 0;
			matrix[i][9][2] = 255;
		}
	}

	private void calculatePattern(int[] patternCenter, int turn) {
		if (turn == 1) {
			matrix[patternCenter[0]][patternCenter[1]][0] = 255;
			matrix[patternCenter[0]][patternCenter[1]][1] = matrix[patternCenter[0]][patternCenter[1]][2] = 0;

			matrix[patternCenter[0] - 1][patternCenter[1] - 1][0] = 255;
			matrix[patternCenter[0] - 1][patternCenter[1]
					- 1][1] = matrix[patternCenter[0] - 1][patternCenter[1] - 1][2] = 0;

			matrix[patternCenter[0] + 1][patternCenter[1] - 1][0] = 255;
			matrix[patternCenter[0] + 1][patternCenter[1]
					- 1][1] = matrix[patternCenter[0] + 1][patternCenter[1] - 1][2] = 0;

			matrix[patternCenter[0] - 1][patternCenter[1] + 1][0] = 255;
			matrix[patternCenter[0] - 1][patternCenter[1]
					+ 1][1] = matrix[patternCenter[0] - 1][patternCenter[1] + 1][2] = 0;

			matrix[patternCenter[0] + 1][patternCenter[1] + 1][0] = 255;
			matrix[patternCenter[0] + 1][patternCenter[1]
					+ 1][1] = matrix[patternCenter[0] + 1][patternCenter[1] + 1][2] = 0;
		} else {
			matrix[patternCenter[0] - 1][patternCenter[1]][1] = 255;
			matrix[patternCenter[0] - 1][patternCenter[1]][0] = matrix[patternCenter[0] - 1][patternCenter[1]][2] = 0;

			matrix[patternCenter[0] + 1][patternCenter[1]][1] = 255;
			matrix[patternCenter[0] + 1][patternCenter[1]][0] = matrix[patternCenter[0] + 1][patternCenter[1]][2] = 0;

			matrix[patternCenter[0]][patternCenter[1] - 1][1] = 255;
			matrix[patternCenter[0]][patternCenter[1] - 1][0] = matrix[patternCenter[0]][patternCenter[1] - 1][2] = 0;

			matrix[patternCenter[0]][patternCenter[1] + 1][1] = 255;
			matrix[patternCenter[0]][patternCenter[1] + 1][0] = matrix[patternCenter[0]][patternCenter[1] + 1][2] = 0;
		}
	}
}