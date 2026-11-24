package es.studium.main.java;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class PanelFondo extends JPanel {

	private static final long serialVersionUID = 1L;

	private final transient Image imagen;

	public PanelFondo(Image imagen, LayoutManager layout) {
		super(layout);
		this.imagen = imagen;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
	}
}
