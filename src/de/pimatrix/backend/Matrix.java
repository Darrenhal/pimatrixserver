package de.pimatrix.backend;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Matrix implements Serializable{
	
	public short[][][] matrix = new short[14][14][3];
	
	public Matrix(short[][][] matrix) {
		this.matrix = matrix;
	}
}