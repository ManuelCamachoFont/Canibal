package es.studium.main.java;

public enum Especie {

	PEQUENO(1),
	MEDIANO(10),
	GRANDE(25),
	TIBURON(50);

	private final int tamanoMinimo;

	Especie(int tamanoMinimo) {
		this.tamanoMinimo = tamanoMinimo;
	}

	public int getTamanoMinimo() {
		return tamanoMinimo;
	}

	public static Especie deTamano(int tamano) {
		Especie resultado = PEQUENO;
		for (Especie especie : values()) {
			if (tamano >= especie.tamanoMinimo) {
				resultado = especie;
			}
		}
		return resultado;
	}
}
