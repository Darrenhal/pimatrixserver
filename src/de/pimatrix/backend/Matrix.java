package de.pimatrix.backend;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Matrix implements Serializable{ //serialisierbare Klasse, zum Übermitteln mittels Sockets
	
	public short[][][] matrix = new short[14][14][3]; //stellt RGB Werte aller LEDs der LED-Matrix dar
	
	public Matrix(short[][][] matrix) {
		this.matrix = matrix; //speichern des Arrays als Objektvariable
	}
}