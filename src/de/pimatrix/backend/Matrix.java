package de.pimatrix.backend;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Matrix implements Serializable{
	
	public short[][][] matrix;
	
	public Matrix(short[][][] matrix) {
		this.matrix = matrix;
	}
}