package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.ComentarioRevisionDTO;
import giis.demo.model.MultimediaDTO;
import giis.demo.model.ReportajeDTO;
import giis.demo.model.ReporteroDTO;
import giis.demo.model.RevisarReportajeModel;
import giis.demo.model.VersionReportajeDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.RevisarReportajeView;

public class RevisarReportajeController {

	private final RevisarReportajeModel model;
	private final RevisarReportajeView  view;

	private List<ReportajeDTO> reportajes = new ArrayList<>();
	private ReportajeDTO       reportajeSeleccionado;

	public RevisarReportajeController(RevisarReportajeModel model, RevisarReportajeView view) {
		this.model = model;
		this.view  = view;
	}

	public void initController() {
		view.addReporteroChangedListener   (e -> SwingUtil.exceptionWrapper(() -> cargarReportajes()));
		view.addReportajesSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onReportajeSeleccionado(e)));
		view.addAnadirComentarioListener   (e -> SwingUtil.exceptionWrapper(() -> onAnadirComentario()));
		view.addFinalizarRevisionListener  (e -> SwingUtil.exceptionWrapper(() -> onFinalizarRevision()));

		SwingUtil.exceptionWrapper(() -> {
			List<ReporteroDTO> reporteros = model.getReporteros();
			view.setReporteros(reporteros);
			cargarReportajes();
		});
	}

	// ── Carga de reportajes pendientes ───────────────────────────────────

	private void cargarReportajes() {
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) {
			view.setReportajesPendientes(new ArrayList<>());
			view.setReporteroRegistrado("");
			view.limpiarPanelDerecho();
			return;
		}
		view.setReporteroRegistrado(reportero.getNombre());
		reportajes = model.getReportajesPendientesDeRevision(reportero.getIdReportero());
		view.setReportajesPendientes(reportajes);
		reportajeSeleccionado = null;
		view.limpiarPanelDerecho();
	}

	// ── Seleccion de reportaje ────────────────────────────────────────────

	private void onReportajeSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Integer idReportaje = view.getIdReportajeSeleccionado();
		if (idReportaje == null) {
			reportajeSeleccionado = null;
			view.limpiarPanelDerecho();
			return;
		}

		reportajeSeleccionado = null;
		for (ReportajeDTO r : reportajes) {
			if (r.getIdReportaje() == idReportaje) {
				reportajeSeleccionado = r;
				break;
			}
		}
		if (reportajeSeleccionado == null) return;

		// Cargar titulo
		view.setTitulo(reportajeSeleccionado.getTitulo());

		// Cargar ultima version (subtitulo y cuerpo)
		VersionReportajeDTO ultimaVersion = model.getUltimaVersion(reportajeSeleccionado.getIdReportaje());
		if (ultimaVersion != null) {
			view.setSubtitulo(ultimaVersion.getSubtitulo());
			view.setCuerpo(ultimaVersion.getCuerpo());
		} else {
			view.setSubtitulo("");
			view.setCuerpo("");
		}

		// Cargar multimedia (todo: borrador y definitivo)
		List<MultimediaDTO> multimedia = model.getMultimedia(reportajeSeleccionado.getIdReportaje());
		view.setMultimedia(multimedia);

		// Cargar comentarios existentes (excluyendo el de finalizacion)
		cargarComentarios();

		// Habilitar controles de revision
		view.setRevisionEnabled(true);
	}

	private void cargarComentarios() {
		if (reportajeSeleccionado == null) return;
		List<ComentarioRevisionDTO> comentarios =
			model.getComentariosRevision(reportajeSeleccionado.getIdReportaje());
		view.setComentarios(comentarios);
	}

	// ── Anadir comentario ─────────────────────────────────────────────────

	private void onAnadirComentario() {
		if (reportajeSeleccionado == null) {
			view.showInfo("Selecciona un reportaje primero.");
			return;
		}
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) return;

		String comentario = view.getComentario();
		if (comentario == null || comentario.trim().isEmpty()) {
			view.showInfo("Escribe un comentario antes de anadir.");
			return;
		}

		model.addComentarioRevision(
			reportajeSeleccionado.getIdReportaje(),
			reportero.getIdReportero(),
			comentario
		);

		view.limpiarComentario();
		cargarComentarios();
	}

	// ── Finalizar revision ────────────────────────────────────────────────

	private void onFinalizarRevision() {
		if (reportajeSeleccionado == null) {
			view.showInfo("Selecciona un reportaje primero.");
			return;
		}
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) return;

		if (!view.confirm(
				"Vas a finalizar la revision del reportaje:\n\n" +
				"Titulo: " + reportajeSeleccionado.getTitulo() + "\n\n" +
				"Ya no podran anadirse mas comentarios.\n¿Confirmas?",
				"Finalizar revision")) return;

		model.finalizarRevision(
			reportajeSeleccionado.getIdReportaje(),
			reportero.getIdReportero()
		);

		view.showInfo("Revision finalizada correctamente.");

		// Recargar — el reportaje desaparece de la lista de pendientes
		cargarReportajes();
	}
}