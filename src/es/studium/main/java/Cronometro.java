package es.studium.main.java;

public class Cronometro implements Runnable {

	private static final int INTERVALO_MS = 100;

	private Thread hilo;
	private boolean corriendo = false;
	private long inicio;
	private long milisegundos = 0;

	public synchronized void iniciar() {
		detener();
		inicio = System.nanoTime();
		milisegundos = 0;
		corriendo = true;
		hilo = new Thread(this, "Cronometro");
		hilo.setDaemon(true);
		hilo.start();
	}

	@Override
	public void run() {
		while (sigueContando()) {
			actualizar();
			try {
				Thread.sleep(INTERVALO_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private synchronized boolean sigueContando() {
		return corriendo && Thread.currentThread() == hilo;
	}

	private synchronized void actualizar() {
		if (corriendo) {
			milisegundos = (System.nanoTime() - inicio) / 1_000_000;
		}
	}

	public synchronized long detener() {
		if (corriendo) {
			milisegundos = (System.nanoTime() - inicio) / 1_000_000;
			corriendo = false;
			if (hilo != null) {
				hilo.interrupt();
			}
		}
		return milisegundos;
	}

	public synchronized long getMilisegundos() {
		return milisegundos;
	}

	public static String formatear(long milisegundos) {
		long minutos = milisegundos / 60_000;
		long segundos = (milisegundos / 1000) % 60;
		long decimas = (milisegundos / 100) % 10;
		return String.format("%02d:%02d.%d", minutos, segundos, decimas);
	}
}
