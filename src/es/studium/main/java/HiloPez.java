package es.studium.main.java;

public class HiloPez implements Runnable {

	private final Pez pez;
	private final Modelo modelo;
	private final int partida;


	public HiloPez(Pez pez, Modelo modelo, int partida) {
		this.pez = pez;
		this.modelo = modelo;
		this.partida = partida;
	}

	@Override
	public void run() {
		while (modelo.sigueActiva(partida) && pez.estaVivo() && pez.dentroPantalla(Modelo.ANCHO)) {
			pez.mover();
			try {
				Thread.sleep(Modelo.RETARDO_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		modelo.eliminarPez(pez);
	}
}
