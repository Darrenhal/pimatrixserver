package de.pimatrix.games.pacman;

public class Ghost {
	
	private int[] color = new int[3];
	private boolean dead = false;
	private boolean inBase = true;
	private boolean chasing = false;
	private String name;
	private int chasingCounter = 0;
	
	
	
	public Ghost(int[] color, String name) {
		this.name = name;
		this.color[0] = color[0];
		this.color[1] = color[1];
		this.color[2] = color[2];
	}
	
	public void nextMove() {
		if (!chasing) {
			
		}
	}
	
}
