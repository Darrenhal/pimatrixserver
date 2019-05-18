package de.pimatrix.games.snake;

import java.util.Random;
import javax.swing.JOptionPane;
import de.pimatrix.backend.ClientThread;
import de.pimatrix.backend.Matrix;

public class SnakeController implements Runnable{
	
	public static boolean running;
	private boolean left = false, right = true, up = false, down = false;
	private boolean foodAvailable = false;
	
	private final short x[] = new short[196];
	private final short y[] = new short[196];
			
	private short length = 3;
	private short[] apple = new short[5];
	
	JOptionPane pane = new JOptionPane();
	
	@Override
	public void run() {
		initGame();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {}
		
		while (running) {
			checkApple();
			move();
			checkCollision();
			if (running) {
				assembleMatrixInformation();
			}
			
			try {
				Thread.sleep(700);
			} catch (InterruptedException e) {}
		}
	}
	
	private void checkApple() {
		if ((x[0] == apple[0]) && (y[0] == apple[1]) || !foodAvailable) {
			length++;
			foodAvailable = false;
			locateFood();
		}
	}
	
	public void setDirection(int modifier) {
		System.out.println("Set Direction");
		if (modifier == 2 && right != true) {
			left = true;
			up = false;
			down = false;
		}
		
		if (modifier == 3 && left != true) {
			right = true;
			up = false;
			down = false;
		}
		
		if (modifier == 4 && down != true) {
			up = true;
			left = false;
			right = false;
		}
		
		if (modifier == 5 && up != true) {
			down = true;
			left = false;
			right = false;
		}
		
	}
	
	private void locateFood() {	
		Random random = new Random();
		short randomPos = (short) random.nextInt(13);
		apple[0] = randomPos;

		randomPos = (short) random.nextInt(13);
		apple[1] = randomPos;
		
		apple[2] = 255;
		apple[2] = 0;
		apple[2] = 0;
		
		foodAvailable = true;
	}
	
	private void checkCollision() {
		for(int i = length; i > 0; i--) {
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
		
		if (left) {
			x[0] -= 1;
		}
		if (right) {
			x[0] += 1;
		}
		if (up) {
			y[0] += 1;
		}
		if (down) {
			y[0] -= 1;
		}
	}
	
	private void initGame() {
		for (int i = 0; i < length; i++) {
			x[i] = (short) (6 - i);
			y[i] = 7;
		}
		locateFood();
	}

	private void gameOver() {
		try {
			Thread.sleep(3000);
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
		ClientThread.sendToSerialPort(matrixData);
		
		running = false;
	}
	
	private void assembleMatrixInformation() {
		short[][][] matrix = new short[14][14][3];

		//apfel setzen
		matrix[apple[0]][apple[1]][0] = apple[2];
		matrix[apple[0]][apple[1]][1] = apple[3];
		matrix[apple[0]][apple[1]][2] = apple[4];
		
		//schlange setzen
		int xTransform = 0, yTransform = 0;
		for (int i = 0; i < length; i++) {
			xTransform = x[i];
			yTransform = y[i];
			System.out.println(xTransform);
			System.out.println(yTransform);
			matrix[xTransform][yTransform][0] = matrix[xTransform][yTransform][2] = 0;
			matrix[xTransform][yTransform][1] = 255;
		}
		Matrix matrixData = new Matrix(matrix);
		ClientThread.sendToSerialPort(matrixData);
	}
}