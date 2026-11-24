package es.studium.main.java;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


public class Sonido {

	private static final String RUTA = "/es/studium/main/resources/sonidos/";
	private static final float VOLUMEN_MUSICA_DB = -12f;

	private final ExecutorService pool = Executors.newFixedThreadPool(2, tarea -> {
		Thread hilo = new Thread(tarea, "Sonido");
		hilo.setDaemon(true);
		return hilo;
	});

	private final Clip musica;
	private final Clip comer;
	private final Clip comido;

	public Sonido() {
		musica = cargar(RUTA + "musica.wav");
		comer = cargar(RUTA + "comer.wav");
		comido = cargar(RUTA + "comido.wav");

		// Música más baja que los efectos (en decibelios: 0 = original, negativo = más bajo)
		if (musica != null && musica.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl volumen = (FloatControl) musica.getControl(FloatControl.Type.MASTER_GAIN);
			volumen.setValue(VOLUMEN_MUSICA_DB);
		}
	}

	private Clip cargar(String ruta) {
		try (InputStream entrada = Sonido.class.getResourceAsStream(ruta)) {
			if (entrada == null) {
				System.err.println("No se encuentra el sonido: " + ruta);
				return null;
			}
			AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(entrada));
			Clip clip = AudioSystem.getClip();
			clip.open(audio);
			return clip;
		} catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
			System.err.println("No se puede cargar el sonido " + ruta + ": " + e.getMessage());
			return null;
		}
	}

	public void musicaEnBucle() {
		if (musica != null && !musica.isRunning()) {
			musica.setFramePosition(0);
			musica.loop(Clip.LOOP_CONTINUOUSLY);
		}
	}

	public void pararMusica() {
		if (musica != null) {
			musica.stop();
		}
	}

	public void reproducirComer() {
		reproducir(comer);
	}

	public void reproducirComido() {
		reproducir(comido);
	}

	private void reproducir(Clip clip) {
		if (clip == null || pool.isShutdown()) {
			return;
		}
		pool.submit(() -> {
			synchronized (clip) {
				clip.stop();
				clip.setFramePosition(0);
				clip.start();
			}
		});
	}

	public void cerrar() {
		pool.shutdownNow();
		for (Clip clip : new Clip[] { musica, comer, comido }) {
			if (clip != null) {
				clip.close();
			}
		}
	}
}
