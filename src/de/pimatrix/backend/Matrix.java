package de.pimatrix.backend;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Matrix implements Serializable{
	
	public int[][][] matrix = new int[14][14][3];
	
	public Matrix(int[][][] matrix) {
		this.matrix = matrix;
	}
}