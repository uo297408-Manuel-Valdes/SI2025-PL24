package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.EventoDTO;
import giis.demo.model.EntregarReportajesDeEventosModel;
import giis.demo.model.ReportajeDTO;
import giis.demo.model.ReporteroDTO;
import giis.demo.model.VersionReportajeDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.EntregarReportajesDeEventosView;

public class EntregarReportajesDeEventosController {

	private final EntregarReportajesDeEventosModel model;
	private final EntregarReportajesDeEventosView  view;

	private List<EventoDTO> eventos = new ArrayList<>();
	private EventoDTO       eventoSeleccionado;

	public EntregarReportajesDeEventosController(EntregarReportajesDeEventosModel model,
	                                              EntregarReportajesDeEventosView  view) {
		this.model = model;
		this.view  = view;
	}

	public void initController() {
		view.addReporteroChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addFiltroChangedListener   (e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addEventosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));
		view.addValidarTituloListener   (e -> SwingUtil.exceptionWrapper(() -> onValidarTitulo()));
		view.addEntregarListener        (e -> SwingUtil.exceptionWrapper(() -> onEntregar()));

		SwingUtil.exceptionWrapper(() -> {
			List<ReporteroDTO> reporteros = model.getReporteros();
			view.setReporteros(reporteros);
			cargarEventos();
		});
	}

	private void cargarEventos() {
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) {
			view.setEventos(new ArrayList<>());
			view.setAutor("");
			view.limpiarFormulario();
			return;
		}
		boolean conReportaje = view.getFiltroSeleccionado().equals("Eventos CON reportaje");
		eventos = model.getEventosAsignadosAReportero(reportero.getIdReportero(), conReportaje);
		view.setEventos(eventos);
		view.setAutor(reportero.getNombre());
		eventoSeleccionado = null;
		view.limpiarFormulario();
	}



	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		int selectedRow = getSelectedRow();
		if (selectedRow < 0 || selectedRow >= eventos.size()) {
			eventoSeleccionado = null;
			view.limpiarFormulario();
			ReporteroDTO rep = view.getReporteroSeleccionado();
			if (rep != null) view.setAutor(rep.getNombre());
			return;
		}

		eventoSeleccionado = eventos.get(selectedRow);
		view.setLabelEventoSeleccionado(eventoSeleccionado.getNombre());

		ReporteroDTO rep = view.getReporteroSeleccionado();
		if (rep != null) view.setAutor(rep.getNombre());

		ReportajeDTO reportaje = model.getReportaje(eventoSeleccionado.getIdEvento());
		if (reportaje != null) {

			view.setTitulo(reportaje.getTitulo());
			view.setTituloEditable(false);

			VersionReportajeDTO ultimaVersion = model.getUltimaVersion(reportaje.getIdReportaje());
			if (ultimaVersion != null) {
				view.setSubtitulo(ultimaVersion.getSubtitulo());
				view.setCuerpo(ultimaVersion.getCuerpo());
			} else {
				view.setSubtitulo("");
				view.setCuerpo("");
			}

	
			if (rep != null && !model.reporteroPuedeModificar(eventoSeleccionado.getIdEvento(), rep.getIdReportero()))
				view.showInfo("Solo puedes consultar este reportaje. No eres el reportero que realizo la entrega original.");

		} else {

			view.setTitulo("");
			view.setSubtitulo("");
			view.setCuerpo("");
			view.setTituloEditable(true);
		}
	}

	private int getSelectedRow() {
		Integer id = view.getIdEventoSeleccionado();
		if (id == null) return -1;
		for (int i = 0; i < eventos.size(); i++)
			if (eventos.get(i).getIdEvento() == id) return i;
		return -1;
	}



	private void onValidarTitulo() {
	    try {
	       
	        int idExcluido = -1;
	        if (eventoSeleccionado != null) {
	            ReportajeDTO reportaje = model.getReportaje(eventoSeleccionado.getIdEvento());
	            if (reportaje != null) idExcluido = reportaje.getIdReportaje();
	        }
	        model.validarTitulo(view.getTitulo(), idExcluido);
	        view.showInfo("El título es válido y no está repetido.");
	    } catch (Exception ex) {
	        view.showError(ex.getMessage());
	    }
	}


	private void onEntregar() {
		if (eventoSeleccionado == null) {
			view.showInfo("Selecciona un evento primero.");
			return;
		}

		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) {
			view.showInfo("Selecciona un reportero primero.");
			return;
		}

		String titulo    = view.getTitulo();
		String subtitulo = view.getSubtitulo();
		String cuerpo    = view.getCuerpo();

		ReportajeDTO existente = model.getReportaje(eventoSeleccionado.getIdEvento());
		String tipoEntrega = (existente == null) ? "primera entrega" : "nueva version";

		String msg = "Vas a registrar la " + tipoEntrega + " del reportaje:\n\n"
				+ "Evento:  " + eventoSeleccionado.getNombre()
				+ " (" + eventoSeleccionado.getFechaEvento() + ")\n"
				+ "Autor:   " + reportero.getNombre() + "\n"
				+ "Titulo:  " + titulo + "\n\n"
				+ "Confirmas la entrega?";

		if (!view.confirm(msg, "Confirmar entrega")) return;

		model.entregarReportaje(
			eventoSeleccionado.getIdEvento(), reportero.getIdReportero(),
			titulo, subtitulo, cuerpo
		);

		view.showInfo("Reportaje entregado correctamente.");
		view.getFrame().dispose();
	}
}