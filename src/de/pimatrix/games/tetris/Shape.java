package de.pimatrix.games.tetris;

import java.util.Random;

public class Shape {

	protected enum Tetrominoe {
		NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape
	};

	private Tetrominoe pieceShape;
	private int[][] coords;
	private int[][][] coordsTable;

	public Shape() {
		initShape();
	}
	
	private void initShape() {
		coords = new int[4][2]; //enth�lt Koordinaten des Tetris-St�cks
		setShape(Tetrominoe.ZShape);
	}

	protected void setShape(Tetrominoe shape) {
		//enth�lt relative Positionsangaben aller m�glichen Shapes
		coordsTable = new int[][][] { { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } },
				{ { 0, -1 }, { 0, 0 }, { -1, 0 }, { -1, 1 } }, { { 0, -1 }, { 0, 0 }, { 1, 0 }, { 1, 1 } },
				{ { 0, -1 }, { 0, 0 }, { 0, 1 }, { 0, 2 } }, { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, 1 } },
				{ { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } }, { { -1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } },
				{ { 1, 1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } } };
		
		//aktuellen Shape aus Template erstellen
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2; j++) {
				coords[i][j] = coordsTable[shape.ordinal()][i][j];
			}
		}
		
		pieceShape = shape;
	}
	
	private void setX(int index, int x) {
		coords[index][0] = x;
	}
	
	private void setY(int index, int y) {
		coords[index][1] = y;
	}
	
	public int x(int index) {
		return coords[index][0];
	}
	
	public int y(int index) {
		return coords[index][1];
	}
	
	public Tetrominoe getShape() {
		return pieceShape;
	}
	
	public void setRandomShape() {
		Random random = new Random();
		int x = Math.abs(random.nextInt()) % 7 + 1;
		Tetrominoe[] values = Tetrominoe.values();
		setShape(values[x]);
	}
	
	public int minX() {
		int m = coords[0][0];
		
		for (int i = 0; i < 4; i++) {
			m = Math.min(m, coords[i][0]);
		}
		
		return m;
	}
	
	public int minY() {
		int m = coords[0][1];
		
		for (int i = 0; i < 4; i++) {
			m = Math.min(m, coords[i][1]);
		}
		
		return m;
	}
	
	//berechnet Drehund nach rechts
	public Shape rotateLeft() {
		//Viereck kann nicht gedreht werden --> wird direkt returned
		if (pieceShape == Tetrominoe.SquareShape) {
			return this;
		}
		
		//result = gedrehter Shape
		Shape result = new Shape();
		result.pieceShape = pieceShape;
		
		//Translation
		for (int i = 0; i < 4; i++) {
			result.setX(i, y(i));
			result.setY(i, -x(i));
		}
		
		return result;
	}
	
	//berechnet Drehung nach links
	//siehe rotateLeft f�r Doku
    public Shape rotateRight() {

        if (pieceShape == Tetrominoe.SquareShape)
            return this;

        Shape result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {

            result.setX(i, -y(i));
            result.setY(i, x(i));
        }
        
        return result;
    }
	
}