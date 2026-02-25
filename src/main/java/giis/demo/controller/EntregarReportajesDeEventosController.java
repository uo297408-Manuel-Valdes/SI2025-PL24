package giis.demo.controller;

import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.EventoDTO;
import giis.demo.model.EntregarReportajesDeEventosModel;
import giis.demo.model.ReportajeDTO;
import giis.demo.model.VersionReportajeDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.EntregarReportajesDeEventosView;

public class EntregarReportajesDeEventosController {

	private final EntregarReportajesDeEventosModel model;
	private final EntregarReportajesDeEventosView  view;

	/** Reportero que está usando la interfaz */
	private final int    idReportero;
	private final String nombreReportero;

	private List<EventoDTO> eventos;
	private EventoDTO       eventoSeleccionado;

	public EntregarReportajesDeEventosController(EntregarReportajesDeEventosModel model,
	                                    EntregarReportajesDeEventosView  view,
	                                    int idReportero) {
		this.model       = model;
		this.view        = view;
		this.idReportero = idReportero;
		// Obtenemos el nombre del reportero para mostrarlo en el campo Autor
		this.nombreReportero = model.getNombreReportero(idReportero);
	}

	public void initController() {
		view.addEventosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));
		view.addValidarTituloListener   (e -> SwingUtil.exceptionWrapper(() -> onValidarTitulo()));
		view.addEntregarListener        (e -> SwingUtil.exceptionWrapper(() -> onEntregar()));

		SwingUtil.exceptionWrapper(() -> cargarEventos());
	}


	private void cargarEventos() {
		eventos = model.getEventosAsignadosAReportero(idReportero);
		view.setEventos(eventos);
		// El autor siempre es el reportero logueado (de momento Ana Pérez idReportero=1)
		view.setAutor(nombreReportero);
		view.limpiarFormulario();
		view.setAutor(nombreReportero);
	}

	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		int fila = view.getIdEventoSeleccionado() != null
				? buscarFila(view.getIdEventoSeleccionado())
				: -1;

		// Resolvemos el evento seleccionado desde la tabla
		int selectedRow = getSelectedRow();
		if (selectedRow < 0 || selectedRow >= eventos.size()) {
			eventoSeleccionado = null;
			view.limpiarFormulario();
			view.setAutor(nombreReportero);
			return;
		}

		eventoSeleccionado = eventos.get(selectedRow);
		view.setLabelEventoSeleccionado(eventoSeleccionado.getNombre());
		view.setAutor(nombreReportero);

		// Cargamos reportaje existente si lo hay
		ReportajeDTO reportaje = model.getReportaje(eventoSeleccionado.getIdEvento());
		if (reportaje != null) {
			view.setTitulo(reportaje.getTitulo());

			// Cargamos la última versión para prerellenar subtítulo y cuerpo
			VersionReportajeDTO ultimaVersion = model.getUltimaVersion(reportaje.getIdReportaje());
			if (ultimaVersion != null) {
				view.setSubtitulo(ultimaVersion.getSubtitulo());
				view.setCuerpo(ultimaVersion.getCuerpo());
			} else {
				view.setSubtitulo("");
				view.setCuerpo("");
			}
		} else {
			view.setTitulo("");
			view.setSubtitulo("");
			view.setCuerpo("");
		}
		// El campo cambios siempre empieza vacío (describe los cambios de esta nueva entrega)
		view.setCambios("");
	}

	private int getSelectedRow() {
		Integer id = view.getIdEventoSeleccionado();
		if (id == null) return -1;
		for (int i = 0; i < eventos.size(); i++) {
			if (eventos.get(i).getIdEvento() == id) return i;
		}
		return -1;
	}

	private int buscarFila(int idEvento) {
		for (int i = 0; i < eventos.size(); i++) {
			if (eventos.get(i).getIdEvento() == idEvento) return i;
		}
		return -1;
	}

	
	private void onValidarTitulo() {
		try {
			model.validarTitulo(view.getTitulo());
			view.showInfo("El título es válido.");
		} catch (Exception ex) {
			view.showError(ex.getMessage());
		}
	}

	
	private void onEntregar() {
		if (eventoSeleccionado == null) {
			view.showInfo("Selecciona un evento primero.");
			return;
		}

		String titulo    = view.getTitulo();
		String subtitulo = view.getSubtitulo();
		String cuerpo    = view.getCuerpo();
		String cambios   = view.getCambios();

		// Determinar si es primera entrega o nueva versión
		ReportajeDTO existente = model.getReportaje(eventoSeleccionado.getIdEvento());
		String tipoEntrega     = (existente == null) ? "primera entrega" : "nueva versión";

		String msg = "Vas a registrar la " + tipoEntrega + " del reportaje:\n\n"
				+ "Evento:    " + eventoSeleccionado.getNombre()
				+ " (" + eventoSeleccionado.getFechaEvento() + ")\n"
				+ "Autor:     " + nombreReportero + "\n"
				+ "Título:    " + titulo + "\n"
				+ "Cambios:   " + cambios + "\n\n"
				+ "¿Confirmas la entrega?";

		if (!view.confirm(msg, "Confirmar entrega")) return;

		model.entregarReportaje(
			eventoSeleccionado.getIdEvento(), idReportero,
			titulo, subtitulo, cuerpo, cambios
		);

		view.showInfo("Reportaje entregado correctamente.");
		view.getFrame().dispose();
	}
}