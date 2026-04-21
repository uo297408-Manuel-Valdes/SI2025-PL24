package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.ComentarioRevisionDTO;
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
	private int             idReportajeActual = -1;
	private boolean         modoPrivilegiado  = false;

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

	// ── Carga de eventos ─────────────────────────────────────────────────

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
		eventoSeleccionado = null;
		idReportajeActual  = -1;
		modoPrivilegiado   = false;
		view.limpiarFormulario();
		view.setModoPrivilegiado(false);
	}

	// ── Seleccion de evento ──────────────────────────────────────────────

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

		boolean esResponsable = (rep != null)
			&& model.esResponsableDeEvento(eventoSeleccionado.getIdEvento(), rep.getIdReportero());
		modoPrivilegiado = esResponsable && (reportaje != null);
		view.setModoPrivilegiado(modoPrivilegiado);

		if (reportaje != null) {
			idReportajeActual = reportaje.getIdReportaje();
			view.setTitulo(reportaje.getTitulo());
			view.setTituloEditable(modoPrivilegiado); // responsable puede cambiar titulo

			VersionReportajeDTO ultimaVersion = model.getUltimaVersion(reportaje.getIdReportaje());
			if (ultimaVersion != null) {
				view.setSubtitulo(ultimaVersion.getSubtitulo());
				view.setCuerpo(ultimaVersion.getCuerpo());
			} else {
				view.setSubtitulo("");
				view.setCuerpo("");
			}

			cargarMultimedia();
			view.setMultimediaEnabled(true);

			if (modoPrivilegiado) {
				cargarRevisiones();
				// Habilitar Finalizar solo si no hay revision pendiente
				// (bien porque no se solicito, bien porque todos finalizaron)
				boolean puedeFinalizar = !model.isPendienteRevision(idReportajeActual);
				view.setFinalizarEnabled(puedeFinalizar);
			} else {
				if (rep != null && !model.reporteroPuedeModificar(eventoSeleccionado.getIdEvento(), rep.getIdReportero()))
					view.showInfo("Solo puedes consultar y anadir multimedia a este reportaje. No eres el reportero que realizo la entrega original.");
				boolean pendiente = model.isPendienteRevision(reportaje.getIdReportaje());
				view.setPendienteRevision(pendiente);
			}
		} else {
			idReportajeActual = -1;
			view.setTitulo("");
			view.setSubtitulo("");
			view.setCuerpo("");
			view.setTituloEditable(true);
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

	// ── Multimedia ────────────────────────────────────────────────────────

	private void cargarMultimedia() {
		if (idReportajeActual <= 0) { view.setMultimedia(new ArrayList<>()); return; }
		view.setMultimedia(model.getMultimedia(idReportajeActual));
	}

	private void cargarRevisiones() {
		if (idReportajeActual <= 0) { view.setRevisiones(new ArrayList<>()); return; }
		view.setRevisiones(model.getComentariosRevision(idReportajeActual));
	}

	private void onAnadirMultimedia() {
		if (idReportajeActual <= 0) {
			view.showInfo("Primero debe existir un reportaje entregado para anadir multimedia.");
			return;
		}
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) return;

		String[] datos = view.mostrarDialogoAnadir();
		if (datos == null) return;

		model.addMultimedia(idReportajeActual, reportero.getIdReportero(),
		                    eventoSeleccionado.getIdEvento(), datos[0], datos[1]);
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

		if (modoPrivilegiado)
			model.removeMultimediaPrivilegiado(idMultimedia);
		else
			model.removeMultimedia(idMultimedia, reportero.getIdReportero());
		cargarMultimedia();
	}

	private void onCambiarEstado() {
		int idMultimedia = view.getIdMultimediaSeleccionado();
		if (idMultimedia <= 0) { view.showInfo("Selecciona un elemento multimedia."); return; }
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) return;

		String estadoActual = view.getEstadoMultimediaSeleccionado();
		String nuevoEstado  = "BORRADOR".equals(estadoActual) ? "DEFINITIVO" : "BORRADOR";

		if (!view.confirm("Vas a cambiar el estado a " + nuevoEstado + ".\n¿Confirmas?",
				"Cambiar estado")) return;

		model.cambiarEstadoMultimedia(idMultimedia, reportero.getIdReportero(), nuevoEstado);
		cargarMultimedia();
	}

	// ── Validar titulo ────────────────────────────────────────────────────

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

	// ── Solicitar revision ────────────────────────────────────────────────

	private void onSolicitarRevision() {
		if (eventoSeleccionado == null) { view.showInfo("Selecciona un evento primero."); return; }
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) return;

		if (!view.confirm(
				"Vas a marcar el reportaje como pendiente de revision.\n" +
				"Todos los reporteros asignados deberan finalizar su revision.\n" +
				"No podras modificar el reportaje hasta que finalice.\n¿Confirmas?",
				"Solicitar revision")) return;

		model.solicitarRevision(eventoSeleccionado.getIdEvento(), reportero.getIdReportero());
		view.setPendienteRevision(true);
		view.showInfo("Revision solicitada correctamente.");
	}

	// ── Finalizar (modo privilegiado) ─────────────────────────────────────

	private void onFinalizar() {
		if (eventoSeleccionado == null) { view.showInfo("Selecciona un evento primero."); return; }
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) return;

		if (!view.confirm(
				"Vas a guardar los cambios y FINALIZAR el reportaje.\n\n" +
				"Evento: " + eventoSeleccionado.getNombre() + "\n\n" +
				"Esta accion no se puede deshacer.\n¿Confirmas?",
				"Finalizar reportaje")) return;

		// Guardar cambios del formulario con privilegios
		model.guardarVersionPrivilegiada(
			eventoSeleccionado.getIdEvento(),
			reportero.getIdReportero(),
			view.getTitulo(),
			view.getSubtitulo(),
			view.getCuerpo()
		);

		// Finalizar el evento
		model.finalizarReportajeResponsable(
			eventoSeleccionado.getIdEvento(), reportero.getIdReportero());

		view.setFinalizarEnabled(false);
		view.showInfo("Reportaje finalizado correctamente. El evento ha sido marcado como finalizado.");

		// Cambiar a filtro CON reportaje y recargar
		view.setFiltroSeleccionado("Eventos CON reportaje");
		cargarEventos();
	}

	// ── Entregar ──────────────────────────────────────────────────────────

	private void onEntregar() {
		if (eventoSeleccionado == null) { view.showInfo("Selecciona un evento primero."); return; }
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) { view.showInfo("Selecciona un reportero primero."); return; }

		String titulo    = view.getTitulo();
		String subtitulo = view.getSubtitulo();
		String cuerpo    = view.getCuerpo();

		ReportajeDTO existente = model.getReportaje(eventoSeleccionado.getIdEvento());

		if (modoPrivilegiado) {
			String msg = "Vas a guardar los cambios del reportaje (modo responsable):\n\n"
					+ "Evento: " + eventoSeleccionado.getNombre() + "\n"
					+ "Titulo: " + titulo + "\n\nConfirmas?";
			if (!view.confirm(msg, "Confirmar guardado privilegiado")) return;

			model.guardarVersionPrivilegiada(
				eventoSeleccionado.getIdEvento(), reportero.getIdReportero(),
				titulo, subtitulo, cuerpo
			);

			ReportajeDTO actualizado = model.getReportaje(eventoSeleccionado.getIdEvento());
			if (actualizado != null) view.setTitulo(actualizado.getTitulo());

			// Recalcular si puede finalizar
			boolean puedeFinalizar = !model.isPendienteRevision(idReportajeActual);
			view.setFinalizarEnabled(puedeFinalizar);
			cargarRevisiones();
			view.showInfo("Cambios guardados correctamente.");

		} else {
			String tipoEntrega = (existente == null) ? "primera entrega" : "nueva version";
			String msg = "Vas a registrar la " + tipoEntrega + " del reportaje:\n\n"
					+ "Evento:  " + eventoSeleccionado.getNombre()
					+ " (" + eventoSeleccionado.getFechaEvento() + ")\n"
					+ "Autor:   " + reportero.getNombre() + "\n"
					+ "Titulo:  " + titulo + "\n\nConfirmas la entrega?";

			if (!view.confirm(msg, "Confirmar entrega")) return;

			model.entregarReportaje(
				eventoSeleccionado.getIdEvento(), reportero.getIdReportero(),
				titulo, subtitulo, cuerpo
			);

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