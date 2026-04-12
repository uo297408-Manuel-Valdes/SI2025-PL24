package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.ComentarioRevisionDTO;
import giis.demo.model.EventoDTO;
import giis.demo.model.MultimediaDTO;
import giis.demo.model.ReportajeDTO;
import giis.demo.model.EntregarReportajesDeEventosModel;
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

	// True cuando el reportero seleccionado es responsable del evento seleccionado
	private boolean modoPrivilegiado = false;

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
		view.addCambiarEstadoListener      (e -> SwingUtil.exceptionWrapper(() -> onCambiarEstado()));
		view.addSolicitarRevisionListener  (e -> SwingUtil.exceptionWrapper(() -> onSolicitarRevision()));
		view.addFinalizarListener          (e -> SwingUtil.exceptionWrapper(() -> onFinalizar()));
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
			view.setModoPrivilegiado(false);
			return;
		}
		boolean conReportaje = view.getFiltroSeleccionado().equals("Eventos CON reportaje");
		eventos = model.getEventosAsignadosAReportero(reportero.getIdReportero(), conReportaje);
		view.setEventos(eventos);
		view.setAutor(reportero.getNombre());
		eventoSeleccionado  = null;
		idReportajeActual   = -1;
		modoPrivilegiado    = false;
		view.limpiarFormulario();
		view.setModoPrivilegiado(false);
	}


	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		int selectedRow = getSelectedRow();
		if (selectedRow < 0 || selectedRow >= eventos.size()) {
			eventoSeleccionado = null;
			idReportajeActual  = -1;
			modoPrivilegiado   = false;
			view.limpiarFormulario();
			view.setModoPrivilegiado(false);
			ReporteroDTO rep = view.getReporteroSeleccionado();
			if (rep != null) view.setAutor(rep.getNombre());
			return;
		}

		eventoSeleccionado = eventos.get(selectedRow);
		view.setLabelEventoSeleccionado(eventoSeleccionado.getNombre());

		ReporteroDTO rep = view.getReporteroSeleccionado();
		if (rep != null) view.setAutor(rep.getNombre());

		ReportajeDTO reportaje = model.getReportaje(eventoSeleccionado.getIdEvento());

		// Detectar si el reportero es responsable de este evento
		boolean esResponsable = (rep != null)
			&& model.esResponsableDeEvento(eventoSeleccionado.getIdEvento(), rep.getIdReportero());
		// El modo privilegiado solo aplica cuando hay un reportaje existente
		modoPrivilegiado = esResponsable && (reportaje != null);
		view.setModoPrivilegiado(modoPrivilegiado);

		if (reportaje != null) {
			idReportajeActual = reportaje.getIdReportaje();

			view.setTitulo(reportaje.getTitulo());

			if (modoPrivilegiado) {
				// Responsable: titulo siempre editable
				view.setTituloEditable(true);
			} else {
				// Reportero normal: titulo bloqueado tras primera entrega
				view.setTituloEditable(false);
			}

			VersionReportajeDTO ultimaVersion = model.getUltimaVersion(reportaje.getIdReportaje());
			if (ultimaVersion != null) {
				view.setSubtitulo(ultimaVersion.getSubtitulo());
				view.setCuerpo(ultimaVersion.getCuerpo());
			} else {
				view.setSubtitulo("");
				view.setCuerpo("");
			}

			// Cargar multimedia
			cargarMultimedia();

			// Multimedia habilitada para cualquier reportero asignado
			view.setMultimediaEnabled(true);

			if (modoPrivilegiado) {
				// Cargar tabla de revisiones
				cargarRevisiones();

				// Habilitar Finalizar si todos han enviado revision y no esta ya finalizado
				boolean todosFin    = model.todosHanEnviadoRevision(eventoSeleccionado.getIdEvento());
				boolean yaFinaliz   = model.estaFinalizadoPorResponsable(idReportajeActual, rep.getIdReportero());
				view.setFinalizarEnabled(todosFin && !yaFinaliz);

			} else {
				// Modo normal: avisar si no puede modificar contenido textual
				if (rep != null && !model.reporteroPuedeModificar(eventoSeleccionado.getIdEvento(), rep.getIdReportero()))
					view.showInfo("Solo puedes consultar y añadir multimedia a este reportaje. No eres el reportero que realizo la entrega original.");
				boolean pendiente = model.isPendienteRevision(reportaje.getIdReportaje());
				view.setPendienteRevision(pendiente);
			}

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

	private void cargarRevisiones() {
		if (idReportajeActual <= 0) {
			view.setRevisiones(new ArrayList<>());
			return;
		}
		List<ComentarioRevisionDTO> lista = model.getComentariosRevision(idReportajeActual);
		view.setRevisiones(lista);
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
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) return;
		if (!view.confirm("Vas a eliminar el elemento multimedia seleccionado.\n¿Confirmas?",
				"Confirmar eliminacion")) return;

		if (modoPrivilegiado) {
			// El responsable puede eliminar cualquier multimedia sin restricciones
			model.removeMultimediaPrivilegiado(idMultimedia);
		} else {
			model.removeMultimedia(idMultimedia, reportero.getIdReportero());
		}
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

	private void onSolicitarRevision() {
	    if (eventoSeleccionado == null) {
	        view.showInfo("Selecciona un evento primero.");
	        return;
	    }
	    ReporteroDTO reportero = view.getReporteroSeleccionado();
	    if (reportero == null) return;

	    if (!view.confirm(
	            "Vas a marcar el reportaje como pendiente de revision.\n" +
	            "No podras modificarlo hasta que finalice.\n¿Confirmas?",
	            "Solicitar revision")) return;

	    model.solicitarRevision(eventoSeleccionado.getIdEvento(), reportero.getIdReportero());
	    view.setPendienteRevision(true);
	    view.showInfo("Revision solicitada correctamente.");
	}

	private void onFinalizar() {
	    if (eventoSeleccionado == null) {
	        view.showInfo("Selecciona un evento primero.");
	        return;
	    }
	    ReporteroDTO reportero = view.getReporteroSeleccionado();
	    if (reportero == null) return;

	    if (!view.confirm(
	            "Vas a guardar los cambios actuales y finalizar la revision del reportaje.\n" +
	            "Esta accion no se puede deshacer.\n¿Confirmas?",
	            "Finalizar revision")) return;

	    // Primero guardar el contenido con privilegios (titulo editable)
	    model.guardarVersionPrivilegiada(
	        eventoSeleccionado.getIdEvento(),
	        reportero.getIdReportero(),
	        view.getTitulo(),
	        view.getSubtitulo(),
	        view.getCuerpo()
	    );

	    // Luego marcar como finalizado
	    model.finalizarReportajeResponsable(eventoSeleccionado.getIdEvento(), reportero.getIdReportero());

	    view.setFinalizarEnabled(false);
	    cargarRevisiones();
	    view.showInfo("Reportaje finalizado correctamente.");
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

		if (modoPrivilegiado) {
			// Modo privilegiado: guardar con permisos de responsable (titulo editable)
			String msg = "Vas a guardar los cambios del reportaje (modo responsable):\n\n"
					+ "Evento:  " + eventoSeleccionado.getNombre()
					+ " (" + eventoSeleccionado.getFechaEvento() + ")\n"
					+ "Titulo:  " + titulo + "\n\n"
					+ "Confirmas?";
			if (!view.confirm(msg, "Confirmar guardado privilegiado")) return;

			model.guardarVersionPrivilegiada(
				eventoSeleccionado.getIdEvento(), reportero.getIdReportero(),
				titulo, subtitulo, cuerpo
			);

			// Refrescar titulo por si se habia cambiado
			ReportajeDTO actualizado = model.getReportaje(eventoSeleccionado.getIdEvento());
			if (actualizado != null) view.setTitulo(actualizado.getTitulo());

			// Actualizar estado del boton Finalizar
			boolean todosFin  = model.todosHanEnviadoRevision(eventoSeleccionado.getIdEvento());
			boolean yaFinaliz = model.estaFinalizadoPorResponsable(idReportajeActual, reportero.getIdReportero());
			view.setFinalizarEnabled(todosFin && !yaFinaliz);

			view.showInfo("Cambios guardados correctamente.");

		} else {
			// Modo normal
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
		    view.setFiltroSeleccionado("Eventos CON reportaje");
			cargarMultimedia();
		}
	}
}