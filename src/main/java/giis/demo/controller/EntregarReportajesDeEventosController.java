package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.EventoDTO;
import giis.demo.model.EntregarReportajesDeEventosModel;
import giis.demo.model.MultimediaDTO;
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

	// Id del reportaje del evento actualmente seleccionado (-1 si no hay)
	private int idReportajeActual = -1;

	public EntregarReportajesDeEventosController(EntregarReportajesDeEventosModel model,
	                                              EntregarReportajesDeEventosView  view) {
		this.model = model;
		this.view  = view;
	}

	public void initController() {
		view.addReporteroChangedListener   (e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addFiltroChangedListener      (e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addEventosSelectionListener   (e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));
		view.addValidarTituloListener      (e -> SwingUtil.exceptionWrapper(() -> onValidarTitulo()));
		view.addEntregarListener           (e -> SwingUtil.exceptionWrapper(() -> onEntregar()));
		view.addAnadirMultimediaListener   (e -> SwingUtil.exceptionWrapper(() -> onAnadirMultimedia()));
		view.addEliminarMultimediaListener (e -> SwingUtil.exceptionWrapper(() -> onEliminarMultimedia()));
		view.addCambiarEstadoListener(e -> SwingUtil.exceptionWrapper(() -> onCambiarEstado()));

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
		eventoSeleccionado  = null;
		idReportajeActual   = -1;
		view.limpiarFormulario();
	}


	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		int selectedRow = getSelectedRow();
		if (selectedRow < 0 || selectedRow >= eventos.size()) {
			eventoSeleccionado = null;
			idReportajeActual  = -1;
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
			idReportajeActual = reportaje.getIdReportaje();

			// Titulo bloqueado, cargar contenido existente
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

			// Cargar multimedia del reportaje
			cargarMultimedia();

			// Multimedia habilitada para cualquier reportero asignado
			view.setMultimediaEnabled(true);

			// Avisar si no puede modificar el contenido textual
			if (rep != null && !model.reporteroPuedeModificar(eventoSeleccionado.getIdEvento(), rep.getIdReportero()))
				view.showInfo("Solo puedes consultar y añadir multimedia a este reportaje. No eres el reportero que realizo la entrega original.");

		} else {
			idReportajeActual = -1;
			view.setTitulo("");
			view.setSubtitulo("");
			view.setCuerpo("");
			view.setTituloEditable(true);
			// Sin reportaje aun: multimedia deshabilitada
			view.setMultimediaEnabled(false);
			view.setMultimedia(new ArrayList<>());
		}
	}

	private int getSelectedRow() {
		Integer id = view.getIdEventoSeleccionado();
		if (id == null) return -1;
		for (int i = 0; i < eventos.size(); i++)
			if (eventos.get(i).getIdEvento() == id) return i;
		return -1;
	}


	private void cargarMultimedia() {
		if (idReportajeActual <= 0) {
			view.setMultimedia(new ArrayList<>());
			return;
		}
		List<MultimediaDTO> lista = model.getMultimedia(idReportajeActual);
		view.setMultimedia(lista);
	}

	private void onAnadirMultimedia() {
		if (idReportajeActual <= 0) {
			view.showInfo("Primero debe existir un reportaje entregado para añadir multimedia.");
			return;
		}

		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) {
			view.showInfo("Selecciona un reportero primero.");
			return;
		}

		String[] datos = view.mostrarDialogoAnadir();
		if (datos == null) return;  // Cancelado

		String path = datos[0];
		String tipo = datos[1];

		model.addMultimedia(idReportajeActual, reportero.getIdReportero(),
		                    eventoSeleccionado.getIdEvento(), path, tipo);

		cargarMultimedia();
	}

	private void onEliminarMultimedia() {
		int idMultimedia = view.getIdMultimediaSeleccionado();
		if (idMultimedia <= 0) {
			view.showInfo("Selecciona un elemento multimedia para eliminar.");
			return;
		}
		ReporteroDTO reportero=view.getReporteroSeleccionado();
		if(reportero==null) return;
		if (!view.confirm("Vas a eliminar el elemento multimedia seleccionado.\n¿Confirmas?",
				"Confirmar eliminacion")) return;

		model.removeMultimedia(idMultimedia, reportero.getIdReportero());
		cargarMultimedia();
	}


	private void onValidarTitulo() {
		try {
			int idExcluido = -1;
			if (eventoSeleccionado != null) {
				ReportajeDTO reportaje = model.getReportaje(eventoSeleccionado.getIdEvento());
				if (reportaje != null) idExcluido = reportaje.getIdReportaje();
			}
			model.validarTitulo(view.getTitulo(), idExcluido);
			view.showInfo("El titulo es valido y no esta repetido.");
		} catch (Exception ex) {
			view.showError(ex.getMessage());
		}
	}
	
	private void onCambiarEstado() {
	    int idMultimedia = view.getIdMultimediaSeleccionado();
	    if (idMultimedia <= 0) {
	        view.showInfo("Selecciona un elemento multimedia.");
	        return;
	    }
	    ReporteroDTO reportero = view.getReporteroSeleccionado();
	    if (reportero == null) return;

	    String estadoActual = view.getEstadoMultimediaSeleccionado();
	    String nuevoEstado  = "BORRADOR".equals(estadoActual) ? "DEFINITIVO" : "BORRADOR";

	    if (!view.confirm("Vas a cambiar el estado a " + nuevoEstado + ".\n¿Confirmas?",
	            "Cambiar estado")) return;

	    model.cambiarEstadoMultimedia(idMultimedia, reportero.getIdReportero(), nuevoEstado);
	    cargarMultimedia();
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

		// Tras la primera entrega habilitamos multimedia
		ReportajeDTO reportajeNuevo = model.getReportaje(eventoSeleccionado.getIdEvento());
		if (reportajeNuevo != null) {
			idReportajeActual = reportajeNuevo.getIdReportaje();
			view.setMultimediaEnabled(true);
			view.setTituloEditable(false);
		}

		view.showInfo("Reportaje entregado correctamente.");
		cargarMultimedia();
	}
}