package es.studium.main.java;

import java.awt.Rectangle;


public class Comida {

	public static final int VALOR = 1;
	public static final int LADO = 14;

	private final int x;
	private final int y;

	public Comida(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, LADO, LADO);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
