package giis.demo.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.AgenciaDTO;
import giis.demo.model.AsignarReporterosAEventosModel;
import giis.demo.model.ReporteroDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.AsignarReporterosAEventosView;

public class AsignarReporterosAEventosController {

	private AsignarReporterosAEventosModel model;
	private AsignarReporterosAEventosView view;

	private List<ReporteroDTO> disponibles = new ArrayList<>();
	private List<ReporteroDTO> asignados = new ArrayList<>();
	private boolean ignoreEvents = false;

	public AsignarReporterosAEventosController(AsignarReporterosAEventosModel model,
			AsignarReporterosAEventosView view) {
		this.model = model;
		this.view = view;
	}

	public void initController() {
		view.addAgenciaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addFiltroEventosChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addEventosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onSeleccionEvento(e)));
		view.addSoloEspecialistasChangedListener(e -> SwingUtil.exceptionWrapper(() -> recargarSiHayEvento()));
		view.addFiltroTipoChangedListener(e -> SwingUtil.exceptionWrapper(() -> recargarSiHayEvento()));

		view.addAsignarListener(e -> SwingUtil.exceptionWrapper(() -> asignarSeleccionados()));
		view.addEliminarListener(e -> SwingUtil.exceptionWrapper(() -> eliminarSeleccionados()));
		view.addGuardarListener(e -> SwingUtil.exceptionWrapper(() -> guardar()));

		SwingUtil.exceptionWrapper(() -> {
			view.setAgencias(model.getAgencias());
			cargarEventos();
		});
	}

	private void cargarEventos() {
		AgenciaDTO ag = view.getAgenciaSeleccionada();

		limpiarTablasReporteros();

		if (ag == null) {
			view.setEventos(new ArrayList<>());
			view.setAccionesEnabled(false);
			return;
		}

		ignoreEvents = true;
		view.setEventos(model.getEventos(ag.getIdAgencia(), view.getFiltroEventosSeleccionado()));
		view.clearSeleccionEvento();
		ignoreEvents = false;

		view.setAccionesEnabled(false);
	}

	private void onSeleccionEvento(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		if (ignoreEvents) {
			return;
		}

		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			limpiarTablasReporteros();
			view.setAccionesEnabled(false);
			return;
		}

		cargarReporterosEvento();
	}

	private void recargarSiHayEvento() {
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			limpiarTablasReporteros();
			view.setAccionesEnabled(false);
			return;
		}
		cargarReporterosEvento();
	}

	private void cargarReporterosEvento() {
		AgenciaDTO ag = view.getAgenciaSeleccionada();
		Integer idEvento = view.getIdEventoSeleccionado();

		if (ag == null || idEvento == null) {
			limpiarTablasReporteros();
			view.setAccionesEnabled(false);
			return;
		}

		asignados = new ArrayList<>(model.getReporterosAsignados(idEvento));

		disponibles = new ArrayList<>(model.getReporterosDisponibles(
			ag.getIdAgencia(),
			idEvento,
			view.isFiltroSoloEspecialistasActivo(),
			view.isFiltroTipoBasicoActivo(),
			view.isFiltroTipoGraficoActivo(),
			view.isFiltroTipoCamarografoActivo()
		));

		// Evitar duplicados en la UI si se han movido elementos en memoria
		for (int i = disponibles.size() - 1; i >= 0; i--) {
			ReporteroDTO d = disponibles.get(i);
			if (containsReportero(asignados, d.getIdReportero())) {
				disponibles.remove(i);
			}
		}

		ordenarPorNombre(disponibles);
		ordenarPorNombre(asignados);

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
		view.clearSeleccionDisponibles();
		view.clearSeleccionAsignados();
		view.setAccionesEnabled(true);
	}

	private void asignarSeleccionados() {
		int[] filas = view.getFilasDisponiblesSeleccionadas();
		if (filas == null || filas.length == 0) {
			view.showInfo("Selecciona uno o varios reporteros disponibles.");
			return;
		}

		List<ReporteroDTO> mover = new ArrayList<>();
		for (int row : filas) {
			ReporteroDTO r = view.getReporteroDisponibleEnFila(row);
			if (r != null) {
				mover.add(r);
			}
		}

		for (ReporteroDTO r : mover) {
			if (!containsReportero(asignados, r.getIdReportero())) {
				asignados.add(r);
			}
			removeReportero(disponibles, r.getIdReportero());
		}

		ordenarPorNombre(disponibles);
		ordenarPorNombre(asignados);

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
		view.clearSeleccionDisponibles();
		view.clearSeleccionAsignados();
	}

	private void eliminarSeleccionados() {
		int[] filas = view.getFilasAsignadosSeleccionadas();
		if (filas == null || filas.length == 0) {
			view.showInfo("Selecciona uno o varios reporteros asignados.");
			return;
		}

		List<ReporteroDTO> mover = new ArrayList<>();
		for (int row : filas) {
			ReporteroDTO r = view.getReporteroAsignadoEnFila(row);
			if (r != null) {
				mover.add(r);
			}
		}

		for (ReporteroDTO r : mover) {
			removeReportero(asignados, r.getIdReportero());
		}

		/*
		 * Para evitar inconsistencias con disponibilidad por fecha, especialistas,
		 * tipo, etc., tras eliminar recargamos desde BD en vez de reconstruir a mano.
		 */
		cargarReporterosEvento();
	}

	private void guardar() {
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}

		if (!view.confirm("¿Guardar asignaciones del evento seleccionado?", "Confirmar")) {
			return;
		}

		try {
			model.guardarAsignaciones(idEvento, asignados);
			view.showInfo("Asignaciones guardadas correctamente.");
			cargarEventos();
		} catch (IllegalStateException ex) {
			view.showError(ex.getMessage());
		}
	}

	private void limpiarTablasReporteros() {
		disponibles.clear();
		asignados.clear();
		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
		view.clearSeleccionDisponibles();
		view.clearSeleccionAsignados();
	}

	private void ordenarPorNombre(List<ReporteroDTO> lista) {
		lista.sort(Comparator.comparing(ReporteroDTO::getNombre, String.CASE_INSENSITIVE_ORDER));
	}

	private boolean containsReportero(List<ReporteroDTO> lista, int idReportero) {
		for (ReporteroDTO r : lista) {
			if (r.getIdReportero() == idReportero) {
				return true;
			}
		}
		return false;
	}

	private void removeReportero(List<ReporteroDTO> lista, int idReportero) {
		for (int i = lista.size() - 1; i >= 0; i--) {
			if (lista.get(i).getIdReportero() == idReportero) {
				lista.remove(i);
			}
		}
	}
}