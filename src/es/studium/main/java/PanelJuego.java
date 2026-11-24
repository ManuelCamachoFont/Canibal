package es.studium.main.java;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JPanel;

public class PanelJuego extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String RUTA = "/es/studium/main/resources/img/";

	private static final Color COLOR_JUGADOR = new Color(255, 150, 40);

	private static final Color COLOR_COMESTIBLE = new Color(90, 200, 120);
	private static final Color COLOR_PELIGRO = new Color(220, 70, 70);

	private final Modelo modelo;

	private final Image imgJugador = Vista.cargarImagen(RUTA + "jugador.png");
	private final Image imgComida = Vista.cargarImagen(RUTA + "comida.png");

	private final Map<Especie, Image> imgEspecies = new EnumMap<>(Especie.class);

	private final BufferedImage fondo;

	private final HiloFondo hiloFondo;

	public PanelJuego(Modelo modelo) {
		this.modelo = modelo;
		setPreferredSize(new Dimension(Modelo.ANCHO, Modelo.ALTO));
		setFocusable(true);

		imgEspecies.put(Especie.PEQUENO, Vista.cargarImagen(RUTA + "pez_pequeno.png"));
		imgEspecies.put(Especie.MEDIANO, Vista.cargarImagen(RUTA + "pez_mediano.png"));
		imgEspecies.put(Especie.GRANDE, Vista.cargarImagen(RUTA + "pez_grande.png"));
		imgEspecies.put(Especie.TIBURON, Vista.cargarImagen(RUTA + "tiburon.png"));

		fondo = crearFondoContinuo(Vista.cargarImagen(RUTA + "fondo.png"), Modelo.ALTO);
		hiloFondo = new HiloFondo(fondo.getWidth());
	}

	private static BufferedImage crearFondoContinuo(Image imagen, int alto) {
		int ancho = Math.max(1, (int) Math.round(imagen.getWidth(null) * (alto / (double) imagen.getHeight(null))));

		BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = escalada.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(imagen, 0, 0, ancho, alto, null);
		g.dispose();

		BufferedImage continuo = new BufferedImage(ancho * 2, alto, BufferedImage.TYPE_INT_RGB);
		g = continuo.createGraphics();
		g.drawImage(escalada, 0, 0, null);
		g.drawImage(escalada, ancho * 2, 0, ancho, alto, 0, 0, ancho, alto, null);
		g.dispose();
		return continuo;
	}

	public void iniciarAnimacionFondo() {
		hiloFondo.iniciar();
	}

	public void detenerAnimacionFondo() {
		hiloFondo.detener();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		pintarFondo(g2);

		for (Comida comida : modelo.getComidas()) {
			pintarComida(g2, comida);
		}

		Pez jugador = modelo.getJugador();
		int tamJugador = jugador.getTamano();
		for (Pez pez : modelo.getPeces()) {
			if (pez.estaVivo()) {
				Color color = pez.getTamano() < tamJugador ? COLOR_COMESTIBLE : COLOR_PELIGRO;
				pintarPez(g2, pez, imgEspecies.get(Especie.deTamano(pez.getTamano())));
				pintarEtiqueta(g2, pez, color);
			}
		}

		pintarPez(g2, jugador, imgJugador);
		pintarMarcador(g2, tamJugador);
	}

	public double getProporcionJugador() {
		return proporcion(imgJugador);
	}

	public double getProporcion(Especie especie) {
		return proporcion(imgEspecies.get(especie));
	}

	private static double proporcion(Image imagen) {
		double p = imagen.getHeight(null) / (double) imagen.getWidth(null);
		return Math.max(0.3, Math.min(1.5, p));
	}

	private void pintarFondo(Graphics2D g2) {
		int ancho = fondo.getWidth();
		for (int x = -hiloFondo.getDesplazamiento(); x < getWidth(); x += ancho) {
			g2.drawImage(fondo, x, 0, null);
		}
	}

	private void pintarComida(Graphics2D g2, Comida comida) {
		g2.drawImage(imgComida, comida.getX(), comida.getY(), Comida.LADO, Comida.LADO, null);
	}

	private void pintarPez(Graphics2D g2, Pez pez, Image imagen) {
		int ancho = pez.getAncho();
		int alto = pez.getAlto();
		Graphics2D gp = (Graphics2D) g2.create();
		if (pez.getDireccion() == Pez.IZQUIERDA) {
			gp.translate(pez.getX() + ancho, pez.getY());
			gp.scale(-1, 1);
		} else {
			gp.translate(pez.getX(), pez.getY());
		}
		gp.drawImage(imagen, 0, 0, ancho, alto, null);
		gp.dispose();
	}

	private void pintarEtiqueta(Graphics2D g2, Pez pez, Color color) {
		String texto = String.valueOf(pez.getTamano());
		g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
		int anchoTexto = g2.getFontMetrics().stringWidth(texto);
		int x = (int) pez.getCentroX() - anchoTexto / 2;
		int y = (int) pez.getY() - 4;
		g2.setColor(Color.BLACK);
		g2.drawString(texto, x + 1, y + 1);
		g2.setColor(color.brighter());
		g2.drawString(texto, x, y);
	}

	private void pintarMarcador(Graphics2D g2, int tamano) {
		int tamanoVictoria = modelo.getTamanoVictoria();
		g2.setColor(new Color(0, 0, 0, 150));
		g2.fillRect(0, 0, getWidth(), Modelo.MARGEN_SUPERIOR);

		g2.setFont(getFont().deriveFont(Font.BOLD, 18f));
		g2.setColor(Color.WHITE);
		g2.drawString("Tamaño: " + tamano + " / " + tamanoVictoria, 20, 32);

		String derecha = modelo.getDificultad().getNombre() + "   " + Cronometro.formatear(modelo.getTiempoMs());
		int anchoDerecha = g2.getFontMetrics().stringWidth(derecha);
		g2.drawString(derecha, getWidth() - anchoDerecha - 20, 32);

		int barraX = 230;
		int barraY = 17;

		int reservado = g2.getFontMetrics().stringWidth("Difícil   88:88.8");
		int barraAncho = getWidth() - barraX - reservado - 40;
		int barraAlto = 16;
		double progreso = Math.min(1.0, (double) tamano / tamanoVictoria);
		g2.setColor(new Color(255, 255, 255, 60));
		g2.fillRoundRect(barraX, barraY, barraAncho, barraAlto, barraAlto, barraAlto);
		g2.setColor(COLOR_JUGADOR);
		g2.fillRoundRect(barraX, barraY, (int) (barraAncho * progreso), barraAlto, barraAlto, barraAlto);
		g2.setColor(Color.WHITE);
		g2.setStroke(new BasicStroke(1.5f));
		g2.drawRoundRect(barraX, barraY, barraAncho, barraAlto, barraAlto, barraAlto);
	}
}
