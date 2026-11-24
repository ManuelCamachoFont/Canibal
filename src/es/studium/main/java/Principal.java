package es.studium.main.java;

import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

public class Principal {

	public static void main(String[] args) {

		FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#ff9628"));
		FlatDarkLaf.setup();

		SwingUtilities.invokeLater(() -> {
			try {
				Modelo modelo = new Modelo();
				Vista vista = new Vista(modelo);
				new Controlador(modelo, vista);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
	}
}
