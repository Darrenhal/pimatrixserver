package de.pimatrix.games.pacman;

public class PacManController implements Runnable{
	
	public static boolean running;
	private boolean validPacManMove;
	private boolean validGhostMove;
	
	@Override
	public void run() {
		initBoard();
		while (running) {
			while (!validPacManMove) {
				movePacMan();
				checkPacManCollision();
			}

			while(!validGhostMove) {
				
			}
			
		}
	}
	
	private void initBoard() {
		
	}
	
	private void movePacMan() {
		
	}
	
	private void checkPacManCollision() {
		validPacManMove = true;
		
		
	}
	
	private void moveGhost() {
		
	}
	
	private void checkGhostCollision() {
		
	}
	
	private void checkCollectible() {
		
	}
	
	private void setGhostMovementMethod() {
		
	}
	
}