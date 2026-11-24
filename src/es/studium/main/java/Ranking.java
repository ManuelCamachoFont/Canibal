package es.studium.main.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;


public class Ranking {

	public record Puntuacion(String nombre, Dificultad dificultad, long milisegundos, String fecha) {
	}

	public static final int TOP = 10;
	public static final int MAX_NOMBRE = 20;

	private static final Path FICHERO = Path.of("ranking.json");
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final List<Puntuacion> puntuaciones = new ArrayList<>();

	public Ranking() {
		if (Files.exists(FICHERO)) {
			try {
				Puntuacion[] datos = gson.fromJson(Files.readString(FICHERO), Puntuacion[].class);
				if (datos != null) {
					puntuaciones.addAll(List.of(datos));
				}
			} catch (IOException | JsonParseException e) {
				System.out.println("No se puede leer el ranking: " + e.getMessage());
			}
		}
	}

	public int guardarRanking(String nombre, Dificultad dificultad, long milisegundos) {
		nombre = nombre.strip();
		if (nombre.length() > MAX_NOMBRE) {
			nombre = nombre.substring(0, MAX_NOMBRE);
		}
		Puntuacion nueva = new Puntuacion(nombre, dificultad, milisegundos, LocalDateTime.now().format(FORMATO_FECHA));
		puntuaciones.add(nueva);
		try {
			Files.writeString(FICHERO, gson.toJson(puntuaciones));
		} catch (IOException e) {
			System.out.println("No se puede guardar el ranking: " + e.getMessage());
		}
		return obtenerTopRanking(dificultad).indexOf(nueva) + 1;
	}

	public List<Puntuacion> obtenerTopRanking(Dificultad dificultad) {
		return puntuaciones.stream()
				.filter(p -> p.dificultad() == dificultad)
				.sorted(Comparator.comparingLong(Puntuacion::milisegundos))
				.limit(TOP)
				.toList();
	}
}
