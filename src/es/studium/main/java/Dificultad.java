package es.studium.main.java;


public enum Dificultad {

	FACIL("Fácil", 1.0, 50, 60, 20),
	NORMAL("Normal", 1.5, 75, 40, 40),
	DIFICIL("Difícil", 2.0, 150, 25, 70);

	private final String nombre;
	private final double velocidad;
	private final int tamanoVictoria;
	private final int porcentajeComestibles;
	private final int maxPeces;

	Dificultad(String nombre, double velocidad, int tamanoVictoria, int porcentajeComestibles, int maxPeces) {
		this.nombre = nombre;
		this.velocidad = velocidad;
		this.tamanoVictoria = tamanoVictoria;
		this.porcentajeComestibles = porcentajeComestibles;
		this.maxPeces = maxPeces;
	}

	public String getNombre() {
		return nombre;
	}

	public double getVelocidad() {
		return velocidad;
	}

	public int getTamanoVictoria() {
		return tamanoVictoria;
	}

	public int getPorcentajeComestibles() {
		return porcentajeComestibles;
	}

	public int getMaxPeces() {
		return maxPeces;
	}

	@Override
	public String toString() {
		return nombre;
	}
}
