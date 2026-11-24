package es.studium.main.java;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public class Vista extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String PANTALLA_MENU = "menu";
	private static final String PANTALLA_DIFICULTAD = "dificultad";
	private static final String PANTALLA_AYUDA = "ayuda";
	private static final String PANTALLA_RANKING = "ranking";
	private static final String PANTALLA_JUEGO = "juego";
	private static final String TITULO = "CANÍBAL";
	private static final String[] COLUMNAS_RANKING = { "Puesto", "Nombre", "Tiempo", "Fecha" };

	private final CardLayout paneles = new CardLayout();
	private final JPanel panelPrincipal = new JPanel(paneles);

	private final Image icono = cargarImagen("/es/studium/main/resources/ico/icono.png");
	private final Image imgFondoMenu = cargarImagen("/es/studium/main/resources/img/fondomenu.png");

	private final Color naranja = new Color(255, 150, 40);
	private final Color texto = new Color(220, 225, 235);
	private static final Color[] COLORES_RANKING = { new Color(255, 210, 74), new Color(215, 222, 230),
			new Color(224, 154, 90) };

	private final JButton btnJugar = new JButton("Jugar");
	private final JButton btnRanking = new JButton("Ranking");
	private final JButton btnAyuda = new JButton("Ayuda");
	private final JButton btnSalir = new JButton("Salir");

	private final Map<Dificultad, JButton> botonesDificultad = new EnumMap<>(Dificultad.class);
	private final List<JButton> botonesVolver = new ArrayList<>();

	private final JTabbedPane pestanasRanking = new JTabbedPane();
	private final Map<Dificultad, DefaultTableModel> tablasRanking = new EnumMap<>(Dificultad.class);

	private final PanelJuego panelJuego;

	public Vista(Modelo modelo) {
		super(TITULO);
		panelJuego = new PanelJuego(modelo);

		panelPrincipal.add(crearMenu(), PANTALLA_MENU);
		panelPrincipal.add(crearPantallaDificultad(), PANTALLA_DIFICULTAD);
		panelPrincipal.add(crearPantallaAyuda(), PANTALLA_AYUDA);
		panelPrincipal.add(crearPantallaRanking(), PANTALLA_RANKING);
		panelPrincipal.add(panelJuego, PANTALLA_JUEGO);
		setContentPane(panelPrincipal);
		setIconImage(icono);
		getRootPane().putClientProperty("JRootPane.titleBarBackground", new Color(8, 40, 64));
		getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JPanel crearMenu() {
		JPanel panel = crearPantalla(TITULO, 48f);

		JPanel columna = new JPanel(new GridLayout(0, 1, 0, 18));
		columna.setOpaque(false);
		for (JButton boton : new JButton[] { btnJugar, btnRanking, btnAyuda, btnSalir }) {
			estiloBoton(boton, 22f);
			boton.setPreferredSize(new Dimension(280, 60));
			columna.add(boton);
		}
		panel.add(centrar(columna), BorderLayout.CENTER);
		return panel;
	}

	private JPanel crearPantallaDificultad() {
		JPanel panel = crearPantalla("Elige la dificultad", 36f);

		JPanel columna = new JPanel(new GridLayout(0, 1, 0, 18));
		columna.setOpaque(false);
		for (Dificultad d : Dificultad.values()) {
			String descripcion = d == Dificultad.FACIL ? "Velocidad normal"
					: d == Dificultad.NORMAL ? "Peces rápidos" : "Más rápidos y furiosos";
			JButton boton = new JButton("<html><center><b>" + d.getNombre() + "</b><br><small>" + descripcion
					+ " · gana con tamaño " + d.getTamanoVictoria() + "</small></center></html>");
			estiloBoton(boton, 20f);
			boton.setPreferredSize(new Dimension(420, 75));
			botonesDificultad.put(d, boton);
			columna.add(boton);
		}
		panel.add(centrar(columna), BorderLayout.CENTER);
		panel.add(crearBarraVolver(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel crearPantallaAyuda() {
		JPanel panel = crearPantalla("Ayuda", 36f);

		StringBuilder dificultadInfo = new StringBuilder();
		for (Dificultad d : Dificultad.values()) {
			dificultadInfo.append("<b>").append(d.getNombre()).append("</b> ").append(d.getTamanoVictoria()).append(" · ");
		}
		dificultadInfo.setLength(dificultadInfo.length() - 3);

		JLabel lblInstrucciones = new JLabel("<html><div style='font-size:13px'>"
				+ "<p>Eres un pequeño pez de y tu objetivo es crecer"
				+ " hasta el tamaño que marque la dificultad: " + dificultadInfo + ".</p><br>"
				+ "<ul>"
				+ "<li>La <b>comida</b> no se mueve y te hace crecer <b>1</b>.</li>"
				+ "<li>Puedes comerte a los <b>peces más pequeños</b> que tú (número en <font color='#5ac878'>verde</font>):"
				+ " creces tanto como medía el pez comido.</li>"
				+ "<li>Si tocas un pez de <b>tamaño igual o mayor</b> (número en <font color='#dc4646'>rojo</font>),"
				+ " te come a ti y se acaba la partida.</li>"
				+ "<li>Los peces nadan en horizontal y cuanto más grandes, <b>más rápido</b> van."
				+ " Cuanto más alta la dificultad, más rápidos van, en Difícil además aparecen peces <b>peligrosos</b>.</li>"
				+ "<li>Si ganas, puedes guardar tu nombre en el <b>ranking</b>: gana quien menos tarda.</li>"
				+ "</ul><br>"
				+ "<p><b>Controles:</b> flechas del teclado (o W A S D) o mueve el ratón y el pez te seguirá.</p>"
				+ "</div></html>");
		lblInstrucciones.setForeground(texto);
		lblInstrucciones.setVerticalAlignment(SwingConstants.TOP);

		panel.add(crearCajaTexto(lblInstrucciones), BorderLayout.CENTER);
		panel.add(crearBarraVolver(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel crearPantallaRanking() {
		JPanel panel = crearPantalla("Ranking", 36f);

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
					boolean foco, int fila, int columna) {
				super.getTableCellRendererComponent(tabla, valor, false, false, fila, columna);
				setOpaque(false);
				setHorizontalAlignment(columna == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
				if (fila < COLORES_RANKING.length) {
					setForeground(COLORES_RANKING[fila]);
					setFont(getFont().deriveFont(Font.BOLD));
				} else {
					setForeground(texto);
				}
				return this;
			}
		};

		for (Dificultad d : Dificultad.values()) {
			DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS_RANKING, 0) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int fila, int columna) {
					return false;
				}
			};
			JTable tabla = new JTable(modeloTabla) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics g) {
					g.setColor(new Color(255, 255, 255, 22));
					for (int fila = 1; fila < getRowCount(); fila += 2) {
						g.fillRect(0, fila * getRowHeight(), getWidth(), getRowHeight());
					}
					super.paintComponent(g);
				}
			};
			tabla.setOpaque(false);
			tabla.setShowGrid(false);
			tabla.setRowHeight(32);
			tabla.setFont(tabla.getFont().deriveFont(15f));
			tabla.getTableHeader().setReorderingAllowed(false);
			tabla.setFocusable(false);
			tabla.setRowSelectionAllowed(false);
			for (int i = 0; i < COLUMNAS_RANKING.length; i++) {
				tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
			}
			tabla.getColumnModel().getColumn(0).setMaxWidth(80);
			tablasRanking.put(d, modeloTabla);

			JScrollPane scroll = new JScrollPane(tabla);
			scroll.setOpaque(false);
			scroll.getViewport().setOpaque(false);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			pestanasRanking.addTab(d.getNombre(), scroll);
		}
		pestanasRanking.setFont(pestanasRanking.getFont().deriveFont(Font.BOLD, 15f));
		pestanasRanking.setOpaque(false);
		panel.add(crearCajaTexto(pestanasRanking), BorderLayout.CENTER);
		panel.add(crearBarraVolver(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel crearPantalla(String titulo, float tamanoTitulo) {
		JPanel panel = new PanelFondo(imgFondoMenu, new BorderLayout(0, 30));
		panel.setBorder(BorderFactory.createEmptyBorder(50, 70, 50, 70));
		panel.setPreferredSize(panelJuego.getPreferredSize());

		JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
		lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, tamanoTitulo));
		lblTitulo.setForeground(naranja);
		panel.add(lblTitulo, BorderLayout.NORTH);
		return panel;
	}

	private JPanel crearBarraVolver() {
		JButton volver = new JButton("Volver");
		estiloBoton(volver, 16f);
		botonesVolver.add(volver);
		JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER));
		barra.setOpaque(false);
		barra.add(volver);
		return barra;
	}

	private JPanel centrar(JPanel contenido) {
		JPanel envoltorio = new JPanel(new GridBagLayout());
		envoltorio.setOpaque(false);
		envoltorio.add(contenido);
		return envoltorio;
	}

	private JPanel crearCajaTexto(JComponent contenido) {
		JPanel caja = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(new Color(0, 0, 0, 170));
				g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
			}
		};
		caja.setOpaque(false);
		caja.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
		caja.add(contenido);
		return caja;
	}

	private void estiloBoton(JButton boton, float tamanoLetra) {
		boton.setFont(boton.getFont().deriveFont(Font.BOLD, tamanoLetra));
		boton.putClientProperty("FlatLaf.style", "arc: 999; focusWidth: 0; borderWidth: 2;"
				+ " borderColor: #4fb3d9; hoverBorderColor: #ff9628;"
				+ " background: #062a40e6; hoverBackground: #0f4c6e; pressedBackground: #041c2c;"
				+ " foreground: #ffffff");
		boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		boton.setFocusable(false);
	}

	public void mostrarMenu() {
		paneles.show(panelPrincipal, PANTALLA_MENU);
	}

	public void mostrarDificultad() {
		paneles.show(panelPrincipal, PANTALLA_DIFICULTAD);
	}

	public void mostrarAyuda() {
		paneles.show(panelPrincipal, PANTALLA_AYUDA);
	}

	public void mostrarJuego() {
		paneles.show(panelPrincipal, PANTALLA_JUEGO);
	}


	public void mostrarRanking(Map<Dificultad, List<Ranking.Puntuacion>> tops, Dificultad pestana) {
		for (Map.Entry<Dificultad, List<Ranking.Puntuacion>> entrada : tops.entrySet()) {
			DefaultTableModel modeloTabla = tablasRanking.get(entrada.getKey());
			modeloTabla.setRowCount(0);
			int puesto = 1;
			for (Ranking.Puntuacion p : entrada.getValue()) {
				modeloTabla.addRow(new Object[] { puesto++, p.nombre(), Cronometro.formatear(p.milisegundos()),
						p.fecha() });
			}
		}
		pestanasRanking.setSelectedIndex(pestana.ordinal());
		paneles.show(panelPrincipal, PANTALLA_RANKING);
	}


	public String dialogoRecord(long milisegundos, Dificultad dificultad) {
		JTextField campoNombre = new JTextField(15);
		Object[] contenido = {
			"¡Has ganado en " + Cronometro.formatear(milisegundos) + " (" + dificultad.getNombre() + ")!\n"
					+ "Escribe tu nombre para el ranking (máximo " + Ranking.MAX_NOMBRE + " letras):",
			campoNombre
		};
		Object[] opciones = { "Guardar", "Cancelar" };

		int seleccion = JOptionPane.showOptionDialog(
				this,
				contenido,
				"Nuevo récord",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				opciones,
				opciones[0]
		);

		if (seleccion == 0) {
			return campoNombre.getText();
		}
		return null;
	}


	public void dialogoVacio() {
		JOptionPane.showMessageDialog(this, "Escribe un nombre para poder guardarte en el ranking.",
				"Nombre vacío", JOptionPane.WARNING_MESSAGE);
	}


	public int dialogoFin(boolean victoria, int tamano, long milisegundos, int puesto) {
		String mensaje;
		if (victoria) {
			mensaje = "¡Enhorabuena! Has llegado a tamaño " + tamano + " en " + Cronometro.formatear(milisegundos)
			+ ".";
			if (puesto > 0) {
				mensaje += "\nHas quedado en el puesto " + puesto + " del ranking.";
			}
		} else {
			mensaje = "¡Te han comido! Llegaste a tamaño " + tamano + " en " + Cronometro.formatear(milisegundos)
			+ ".";
		}
		String titulo = victoria ? "Has ganado" : "Fin de la partida";
		int tipo = victoria ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
		Object[] opciones = { "Reintentar", "Ver ranking", "Menú" };
		return JOptionPane.showOptionDialog(this, mensaje, titulo, JOptionPane.DEFAULT_OPTION, tipo, null,
				opciones, opciones[0]);
	}


	public boolean dialogoSalir() {
		Object[] opciones = {"Sí", "No"};
		int seleccion = JOptionPane.showOptionDialog(
				this, 
				"¿Seguro que quieres salir?", 
				"Salir",
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE, 
				null, 
				opciones, 
				opciones[0]
				);
		return seleccion == 0;
	}


	static Image cargarImagen(String ruta) {
		URL url = Vista.class.getResource(ruta);
		return new ImageIcon(url).getImage();
	}


	public JButton getBtnJugar() {
		return btnJugar;
	}

	public JButton getBtnRanking() {
		return btnRanking;
	}

	public JButton getBtnAyuda() {
		return btnAyuda;
	}

	public JButton getBtnSalir() {
		return btnSalir;
	}

	public JButton getBotonDificultad(Dificultad dificultad) {
		return botonesDificultad.get(dificultad);
	}

	public List<JButton> getBotonesVolver() {
		return botonesVolver;
	}

	public PanelJuego getPanelJuego() {
		return panelJuego;
	}
}
