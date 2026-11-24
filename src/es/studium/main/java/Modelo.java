package es.studium.main.java;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Modelo {

	public static final int ANCHO = 800;
	public static final int ALTO = 800;

	public static final int MARGEN_SUPERIOR = 50;
	public static final int TAM_INICIAL = 5;
	public static final int MAX_COMIDA = 8;

	public static final int RETARDO_MS = 16;

	private static final double VELOCIDAD_JUGADOR = 5;

	public interface ModeloListener {
		void onActualizar();

		void onComer();

		void onComido();

		void onVictoria();
	}

	private final List<Pez> peces = new CopyOnWriteArrayList<>();
	private final List<Comida> comidas = new CopyOnWriteArrayList<>();
	private final Random random = new Random();
	private final Cronometro cronometro = new Cronometro();
	private final Ranking ranking = new Ranking();

	private Dificultad dificultad = Dificultad.NORMAL;
	private Pez jugador = new Pez(ANCHO / 2.0, ALTO / 2.0, TAM_INICIAL, Pez.DERECHA);
	private boolean enJuego = false;
	
	private int partidaActual = 0;
	private int teclaDx = 0;
	private int teclaDy = 0;
	private Point objetivoRaton = null;
	private ModeloListener listener;
	private int contadorPeces = 0;

	private double proporcionJugador = Pez.PROPORCION_POR_DEFECTO;
	private final Map<Especie, Double> proporcionesEspecie = new EnumMap<>(Especie.class);

	public synchronized void iniciarPartida(Dificultad dificultad, ModeloListener listener) {
		this.dificultad = dificultad;
		this.listener = listener;
		peces.clear();
		comidas.clear();
		jugador = new Pez(0, 0, TAM_INICIAL, Pez.DERECHA, 1.0, proporcionJugador);
		jugador.setPosicion(ANCHO / 2.0 - jugador.getAncho() / 2.0, ALTO / 2.0 - jugador.getAlto() / 2.0);
		teclaDx = 0;
		teclaDy = 0;
		objetivoRaton = null;

		partidaActual++;
		int partida = partidaActual;
		enJuego = true;

		Thread generador = new Thread(() -> bucleGenerador(partida, dificultad), "Generador-" + partida);
		generador.setDaemon(true);
		generador.start();

		Thread bucleJuego = new Thread(() -> bucleJuego(partida), "BucleJuego-" + partida);
		bucleJuego.setDaemon(true);
		bucleJuego.start();

		cronometro.iniciar();
	}

	public synchronized void detener() {
		enJuego = false;
		cronometro.detener();
	}

	public synchronized boolean sigueActiva(int partida) {
		return enJuego && partida == partidaActual;
	}

	private void bucleGenerador(int partida, Dificultad dificultad) {
		while (sigueActiva(partida)) {
			if (peces.size() < dificultad.getMaxPeces()) {
				crearPez(partida);
			}
			if (comidas.size() < MAX_COMIDA) {
				crearComida();
			}
			try {
				Thread.sleep(random.nextInt(800));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private void bucleJuego(int partida) {
		while (sigueActiva(partida)) {
			moverJugador();
			comprobarColisiones();
			ModeloListener l = getListener();
			if (l != null) {
				l.onActualizar();
			}
			try {
				Thread.sleep(RETARDO_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private synchronized void crearPez(int partida) {
		int tamano = generarTamanoEnemigo();
		int direccion = random.nextBoolean() ? Pez.DERECHA : Pez.IZQUIERDA;
		double proporcion = proporcionesEspecie.getOrDefault(Especie.deTamano(tamano), Pez.PROPORCION_POR_DEFECTO);
		Pez pez = new Pez(0, 0, tamano, direccion, dificultad.getVelocidad(), proporcion);

		double x = (direccion == Pez.DERECHA) ? -pez.getAncho() : ANCHO;
		int altoLibre = Math.max(1, ALTO - MARGEN_SUPERIOR - pez.getAlto());
		double y = MARGEN_SUPERIOR + random.nextInt(altoLibre);
		pez.setPosicion(x, y);
		peces.add(pez);

		contadorPeces++;
		Thread hilo = new Thread(new HiloPez(pez, this, partida), "Pez-" + contadorPeces);
		hilo.setDaemon(true);
		hilo.start();
	}

	private int generarTamanoEnemigo() {
		int tamJugador = jugador.getTamano();
		if (tamJugador > 1 && random.nextInt(100) < dificultad.getPorcentajeComestibles()) {
			return 1 + random.nextInt(tamJugador - 1);
		}
		return tamJugador + random.nextInt(10);
	}

	private void crearComida() {
		int x = random.nextInt(ANCHO - Comida.LADO);
		int y = MARGEN_SUPERIOR + random.nextInt(ALTO - MARGEN_SUPERIOR - Comida.LADO);
		comidas.add(new Comida(x, y));
	}

	private synchronized void moverJugador() {
		int dx = teclaDx;
		int dy = teclaDy;
		if (dx != 0 || dy != 0) {
			objetivoRaton = null;
			double factor = (dx != 0 && dy != 0) ? VELOCIDAD_JUGADOR / Math.sqrt(2) : VELOCIDAD_JUGADOR;
			desplazarJugador(dx * factor, dy * factor);
			return;
		}
		Point objetivo = objetivoRaton;
		if (objetivo != null) {
			double distX = objetivo.x - jugador.getCentroX();
			double distY = objetivo.y - jugador.getCentroY();
			double distancia = Math.hypot(distX, distY);
			if (distancia > 1) {
				double paso = Math.min(VELOCIDAD_JUGADOR, distancia);
				desplazarJugador(distX / distancia * paso, distY / distancia * paso);
			}
		}
	}

	private void desplazarJugador(double dx, double dy) {
		jugador.desplazar(dx, dy);
		double x = Math.max(0, Math.min(jugador.getX(), ANCHO - jugador.getAncho()));
		double y = Math.max(MARGEN_SUPERIOR, Math.min(jugador.getY(), ALTO - jugador.getAlto()));
		jugador.setPosicion(x, y);
	}

	private synchronized void comprobarColisiones() {
		Rectangle zonaJugador = jugador.getZonaColision();

		for (Comida comida : comidas) {
			if (zonaJugador.intersects(comida.getBounds())) {
				comidas.remove(comida);
				jugador.crecer(Comida.VALOR);
				avisarComer();
			}
		}

		for (Pez pez : peces) {
			if (!pez.estaVivo() || !zonaJugador.intersects(pez.getZonaColision())) {
				continue;
			}
			if (pez.getTamano() < jugador.getTamano()) {
				pez.morir();
				peces.remove(pez);
				jugador.crecer(pez.getTamano());
				avisarComer();
			} else {
				detener();
				ModeloListener l = getListener();
				if (l != null) {
					l.onComido();
				}
				return;
			}
		}

		if (jugador.getTamano() >= dificultad.getTamanoVictoria()) {
			detener();
			ModeloListener l = getListener();
			if (l != null) {
				l.onVictoria();
			}
		}
	}

	private synchronized ModeloListener getListener() {
		return listener;
	}

	private void avisarComer() {
		ModeloListener l = getListener();
		if (l != null) {
			l.onComer();
		}
	}

	public void eliminarPez(Pez pez) {
		peces.remove(pez);
	}

	public synchronized void setDireccionTeclado(int dx, int dy) {
		teclaDx = dx;
		teclaDy = dy;
	}

	public synchronized void setProporcionJugador(double proporcion) {
		proporcionJugador = proporcion;
	}

	public synchronized void setProporcionEspecie(Especie especie, double proporcion) {
		proporcionesEspecie.put(especie, proporcion);
	}

	public synchronized void setObjetivoRaton(int x, int y) {
		objetivoRaton = new Point(x, y);
	}


	public synchronized Pez getJugador() {
		return jugador;
	}

	public List<Pez> getPeces() {
		return peces;
	}

	public List<Comida> getComidas() {
		return comidas;
	}

	public synchronized int getTamanoJugador() {
		return jugador.getTamano();
	}

	public synchronized Dificultad getDificultad() {
		return dificultad;
	}

	public synchronized int getTamanoVictoria() {
		return dificultad.getTamanoVictoria();
	}

	public long getTiempoMs() {
		return cronometro.getMilisegundos();
	}

	public Ranking getRanking() {
		return ranking;
	}
}
