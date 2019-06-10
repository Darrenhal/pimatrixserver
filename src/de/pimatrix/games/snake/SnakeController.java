package de.pimatrix.games.snake;

import java.util.Random;
import javax.swing.JOptionPane;
import de.pimatrix.backend.ClientThread;
import de.pimatrix.backend.Matrix;

public class SnakeController implements Runnable {

	public static boolean running;
	public boolean right = false, left = true, up = false, down = false;
	public boolean lastMoveRight = false, lastMoveLeft = true, lastMoveUp = false, lastMoveDown = false;
	private boolean foodAvailable = false;

	private final short x[] = new short[196];
	private final short y[] = new short[196];

	private short length = 3;
	private short[] apple = new short[5];
	
	private int identifier = 2;

	JOptionPane pane = new JOptionPane();

	@Override
	public void run() {
		initGame();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
		}

		while (running) {
			checkApple();
			move();
			checkCollision();
			if (running) {
				assembleMatrixInformation();
			}

			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
	}

	private void checkApple() {
		if ((x[0] == apple[0]) && (y[0] == apple[1]) || !foodAvailable) {
			length++;
			foodAvailable = false;
			while (!foodAvailable) {
				locateFood();
			}
		}
	}

	private void locateFood() {
		boolean collision = false;
		Random random = new Random();
		
		short randomPos = (short) random.nextInt(13);
		apple[0] = randomPos;

		randomPos = (short) random.nextInt(13);
		apple[1] = randomPos;

		for (int i = 0; i < length; i++) {
			if (apple[0] == x[i] && apple[1] == y[i]) {
				collision = true;
			}
		}
		
		if (!collision) {
			foodAvailable = true;
		}
		
		apple[2] = 255;
		apple[3] = 0;
		apple[4] = 0;
	}

	private void checkCollision() {
		for (int i = length; i > 0; i--) {
			if ((i > 4) && (x[0] == x[i]) && (y[0] == y[i])) {
				gameOver();
			}
		}

		if (y[0] >= 14) {
			gameOver();
		}
		if (y[0] < 0) {
			gameOver();
		}
		if (x[0] >= 14) {
			gameOver();
		}
		if (x[0] < 0) {
			gameOver();
		}

	}

	private void move() {
		for (int i = length; i > 0; i--) {
			x[i] = x[(i - 1)];
			y[i] = y[(i - 1)];
		}

		if (right) {
			x[0] -= 1;
			lastMoveRight = true;
			lastMoveLeft = lastMoveUp = lastMoveDown = false;
		}
		if (left) {
			x[0] += 1;
			lastMoveLeft = true;
			lastMoveRight = lastMoveUp = lastMoveDown = false;
		}
		if (up) {
			y[0] += 1;
			lastMoveUp = true;
			lastMoveLeft = lastMoveRight = lastMoveDown = false;
		}
		if (down) {
			y[0] -= 1;
			lastMoveDown = true;
			lastMoveLeft = lastMoveUp = lastMoveRight = false;
		}
	}

	private void initGame() {
		for (int i = 0; i < length; i++) {
			x[i] = (short) (6 - i);
			y[i] = 7;
		}
		locateFood();
		assembleMatrixInformation();
	}

	private void gameOver() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		short[][][] matrix = new short[14][14][3];

		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 14; j++) {
				matrix[i][j][0] = 255;
				matrix[i][j][1] = matrix[i][j][2] = 0;
			}
		}
		Matrix matrixData = new Matrix(matrix);
		ClientThread.sendToSerialPort(matrixData, identifier);

		running = false;
	}

	private void assembleMatrixInformation() {
		short[][][] matrix = new short[14][14][3];

		// apfel setzen
		matrix[apple[0]][apple[1]][0] = apple[2];
		matrix[apple[0]][apple[1]][1] = apple[3];
		matrix[apple[0]][apple[1]][2] = apple[4];

		// schlange setzen
		int xTransform = 0, yTransform = 0;
		for (int i = 0; i < length; i++) {
			xTransform = x[i];
			yTransform = y[i];
			if (i == 0) {
				matrix[xTransform][yTransform][0] = matrix[xTransform][yTransform][1] = 0;
				matrix[xTransform][yTransform][2] = 255;
			} else {
				matrix[xTransform][yTransform][0] = matrix[xTransform][yTransform][2] = 0;
				matrix[xTransform][yTransform][1] = 255;
			}
		}
		Matrix matrixData = new Matrix(matrix);
		ClientThread.sendToSerialPort(matrixData, identifier);
	}
}