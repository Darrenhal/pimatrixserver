package de.pimatrix.backend;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Matrix implements Serializable{
	
	public int[][][] matrix;
	
	public Matrix(int[][][] matrix) {
		this.matrix = matrix;
	}
}