package es.studium.main.java;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class Controlador implements Modelo.ModeloListener {

	private final Modelo modelo;
	private final Vista vista;
	private final Sonido sonido = new Sonido();

	private final Set<Integer> teclas = new HashSet<>();
	private Dificultad dificultad = Dificultad.NORMAL;

	public Controlador(Modelo modelo, Vista vista) {
		this.modelo = modelo;
		this.vista = vista;

		modelo.setProporcionJugador(vista.getPanelJuego().getProporcionJugador());
		for (Especie especie : Especie.values()) {
			modelo.setProporcionEspecie(especie, vista.getPanelJuego().getProporcion(especie));
		}

		vista.getBtnJugar().addActionListener(e -> vista.mostrarDificultad());
		vista.getBtnRanking().addActionListener(e -> mostrarRanking());
		vista.getBtnAyuda().addActionListener(e -> vista.mostrarAyuda());
		vista.getBtnSalir().addActionListener(e -> salir());

		for (Dificultad d : Dificultad.values()) {
			vista.getBotonDificultad(d).addActionListener(e -> {
				dificultad = d;
				empezarPartida();
			});
		}
		for (JButton volver : vista.getBotonesVolver()) {
			volver.addActionListener(e -> vista.mostrarMenu());
		}

		vista.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				salir();
			}
		});

		PanelJuego panel = vista.getPanelJuego();
		panel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				teclas.add(e.getKeyCode());
				actualizarTeclado();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				teclas.remove(e.getKeyCode());
				actualizarTeclado();
			}
		});
		MouseAdapter raton = new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				modelo.setObjetivoRaton(e.getX(), e.getY());
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				modelo.setObjetivoRaton(e.getX(), e.getY());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				panel.requestFocusInWindow();
			}
		};
		panel.addMouseListener(raton);
		panel.addMouseMotionListener(raton);

		vista.mostrarMenu();
	}

	private void empezarPartida() {
		teclas.clear();
		modelo.iniciarPartida(dificultad, this);
		vista.getPanelJuego().iniciarAnimacionFondo();
		sonido.musicaEnBucle();
		vista.mostrarJuego();
		SwingUtilities.invokeLater(() -> vista.getPanelJuego().requestFocusInWindow());
	}
	private void mostrarRanking() {
		Map<Dificultad, List<Ranking.Puntuacion>> tops = new EnumMap<>(Dificultad.class);
		for (Dificultad d : Dificultad.values()) {
			tops.put(d, modelo.getRanking().obtenerTopRanking(d));
		}
		vista.mostrarRanking(tops, dificultad);
	}

	private void actualizarTeclado() {
		int dx = 0;
		int dy = 0;
		if (teclas.contains(KeyEvent.VK_LEFT) || teclas.contains(KeyEvent.VK_A)) {
			dx--;
		}
		if (teclas.contains(KeyEvent.VK_RIGHT) || teclas.contains(KeyEvent.VK_D)) {
			dx++;
		}
		if (teclas.contains(KeyEvent.VK_UP) || teclas.contains(KeyEvent.VK_W)) {
			dy--;
		}
		if (teclas.contains(KeyEvent.VK_DOWN) || teclas.contains(KeyEvent.VK_S)) {
			dy++;
		}
		modelo.setDireccionTeclado(dx, dy);
	}

	@Override
	public void onActualizar() {
		SwingUtilities.invokeLater(() -> vista.getPanelJuego().repaint());
	}

	@Override
	public void onComer() {
		sonido.reproducirComer();
	}

	@Override
	public void onComido() {
		sonido.reproducirComido();
		SwingUtilities.invokeLater(() -> finPartida(false));
	}

	@Override
	public void onVictoria() {
		SwingUtilities.invokeLater(() -> finPartida(true));
	}

	private void finPartida(boolean victoria) {
		teclas.clear();
		modelo.setDireccionTeclado(0, 0);
		vista.getPanelJuego().detenerAnimacionFondo();
		sonido.pararMusica();
		vista.getPanelJuego().repaint();

		long tiempo = modelo.getTiempoMs();
		int puesto = victoria ? guardarEnRanking(tiempo) : -1;

		int opcion = vista.dialogoFin(victoria, modelo.getTamanoJugador(), tiempo, puesto);
		if (opcion == 0) {
			empezarPartida();
		} else if (opcion == 1) {
			mostrarRanking();
		} else {
			vista.mostrarMenu();
		}
	}

	private int guardarEnRanking(long tiempo) {
		while (true) {
			String nombre = vista.dialogoRecord(tiempo, dificultad);
			if (nombre == null) {
				return -1;
			}
			if (!nombre.isBlank()) {
				return modelo.getRanking().guardarRanking(nombre, dificultad, tiempo);
			}
			vista.dialogoVacio();
		}
	}

	private void salir() {
		if (vista.dialogoSalir()) {
			modelo.detener();
			vista.getPanelJuego().detenerAnimacionFondo();
			sonido.cerrar();
			vista.dispose();
			System.exit(0);
		}
	}
}
