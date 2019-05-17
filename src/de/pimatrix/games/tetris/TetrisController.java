package de.pimatrix.games.tetris;

import java.util.Timer;
import java.util.TimerTask;

import de.pimatrix.games.tetris.Shape.Tetrominoe;

public class TetrisController implements Runnable {

	public static boolean running;
	private int[][][] matrix = new int[14][14][3];

	private final int WIDTH = 14;
	private final int HEIGHT = 14;
	private final int INIT_DELAY = 14;
	private final int INTERVAL = 14;

	private Timer timer;
	private boolean isFallingFinished = false;
	private boolean isStarted = false;
	public boolean isPaused = false;
	private int numLinesRemoved = 0;
	private int curX = 0;
	private int curY = 0;
	private Shape curPiece;
	private Tetrominoe[] board;

	@Override
	public void run() {

		initGame();

		while (running) {

		}
	}

	private void initGame() {

		timer = new Timer();
		timer.scheduleAtFixedRate(new ScheduleTask(), INIT_DELAY, INTERVAL);

		curPiece = new Shape();
		board = new Tetrominoe[WIDTH * HEIGHT];

		clearBoard();
	}

	private void update() {

	}

	private void generateMatrix() {
		for (int i = 0; i < HEIGHT; ++i) {
			for (int j = 0; j < WIDTH; ++j) {
				Tetrominoe shape = shapeAt(j, HEIGHT - i - 1);

				if (shape != Tetrominoe.NoShape) {
					drawSquare(j, i, shape);
				}
			}
		}
	}

	private void drawSquare(int x, int y, Tetrominoe shape) {
		int[] colors[] = { { 0, 0, 0 }, { 204, 102, 102 }, { 102, 204, 102 }, { 102, 102, 204 }, { 204, 204, 102 },
				{ 204, 102, 204 }, { 102, 204, 204 }, { 218, 170, 0 } };
		
		int[] color = new int[3];
		color = colors[shape.ordinal()];
		
		
	}
	
	private void promptToMatrix() {
	}

	private void clearBoard() {
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < 14; j++) {
				for (int j2 = 0; j2 < 3; j2++) {
					matrix[i][j][j2] = 0;
				}
			}
		}
	}

	private Tetrominoe shapeAt(int x, int y) {
		return board[(y + WIDTH) * x];
	}

	public void start() {
		isStarted = true;
		clearBoard();
		newPiece();
	}

	private void pause() {

		if (isStarted) {
			return;
		}

		isPaused = !isPaused;

		if (isPaused) {
			// push paused notification back to android app
		}
	}

	private void newPiece() {
		curPiece.setRandomShape();
		curX = WIDTH / 2 + 1;
		curY = HEIGHT - 1 + curPiece.minY();
	}

	private class ScheduleTask extends TimerTask {
		@Override
		public void run() {

			update();
			generateMatrix();
			promptToMatrix();

		}
	}
}