package es.studium.main.java;

import java.awt.Rectangle;

public class Pez {

	public static final int DERECHA = 1;
	public static final int IZQUIERDA = -1;
	public static final double PROPORCION_POR_DEFECTO = 0.6;

	private double x;
	private double y;
	private int tamano;
	private int direccion;
	private boolean vivo = true;
	private final double velocidad;
	private final double proporcion;

	public Pez(double x, double y, int tamano, int direccion) {
		this(x, y, tamano, direccion, 1.0);
	}

	public Pez(double x, double y, int tamano, int direccion, double velocidad) {
		this(x, y, tamano, direccion, velocidad, PROPORCION_POR_DEFECTO);
	}

	public Pez(double x, double y, int tamano, int direccion, double velocidad, double proporcion) {
		this.x = x;
		this.y = y;
		this.tamano = tamano;
		this.direccion = direccion;
		this.velocidad = velocidad;
		this.proporcion = proporcion;
	}

	public synchronized int getAncho() {
		return 20 + tamano * 3;
	}

	public synchronized int getAlto() {
		return (int) (getAncho() * proporcion);
	}

	public synchronized double getVelocidad() {
		return (1 + tamano * 0.08) * velocidad;
	}

	public synchronized void mover() {
		x += direccion * getVelocidad();
	}

	public synchronized void desplazar(double dx, double dy) {
		x += dx;
		y += dy;
		if (dx > 0) {
			direccion = DERECHA;
		} else if (dx < 0) {
			direccion = IZQUIERDA;
		}
	}

	public synchronized boolean dentroPantalla(int anchoPantalla) {
		if (direccion == DERECHA) {
			return x < anchoPantalla;
		}
		return x + getAncho() > 0;
	}

	public synchronized void crecer(int cantidad) {
		double cx = getCentroX();
		double cy = getCentroY();
		tamano += cantidad;
		x = cx - getAncho() / 2.0;
		y = cy - getAlto() / 2.0;
	}

	public synchronized Rectangle getZonaColision() {
		int margenX = getAncho() / 6;
		int margenY = getAlto() / 6;
		return new Rectangle((int) x + margenX, (int) y + margenY,
				getAncho() - 2 * margenX, getAlto() - 2 * margenY);
	}

	public synchronized double getCentroX() {
		return x + getAncho() / 2.0;
	}

	public synchronized double getCentroY() {
		return y + getAlto() / 2.0;
	}

	public synchronized void setPosicion(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public synchronized double getX() {
		return x;
	}

	public synchronized double getY() {
		return y;
	}

	public synchronized int getTamano() {
		return tamano;
	}

	public synchronized int getDireccion() {
		return direccion;
	}

	public synchronized boolean estaVivo() {
		return vivo;
	}

	public synchronized void morir() {
		vivo = false;
	}
}
