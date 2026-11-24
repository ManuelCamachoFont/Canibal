package es.studium.main.java;


public class HiloFondo implements Runnable {

	private static final int INTERVALO_MS = 30;
	private static final int PASO = 1;

	private final int anchoImagen;

	private Thread hilo;
	private boolean corriendo = false;
	private int desplazamiento = 0;

	public HiloFondo(int anchoImagen) {
		this.anchoImagen = Math.max(1, anchoImagen);
	}

	public synchronized void iniciar() {
		if (corriendo) {
			return;
		}
		corriendo = true;
		hilo = new Thread(this, "Fondo");
		hilo.setDaemon(true);
		hilo.start();
	}

	
	public synchronized void detener() {
		corriendo = false;
		if (hilo != null) {
			hilo.interrupt();
		}
	}

	@Override
	public void run() {
		while (sigue()) {
			avanzar();
			try {
				Thread.sleep(INTERVALO_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private synchronized boolean sigue() {
		return corriendo && Thread.currentThread() == hilo;
	}

	private synchronized void avanzar() {
		desplazamiento = (desplazamiento + PASO) % anchoImagen;
	}

	public synchronized int getDesplazamiento() {
		return desplazamiento;
	}
}
