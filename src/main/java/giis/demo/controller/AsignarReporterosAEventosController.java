package giis.demo.controller;

import java.util.ArrayList;
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
		view.addSoloEspecialistasChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarReporterosEvento()));
		view.addFiltroTipoChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarReporterosEvento()));

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

		if (ag == null) {
			view.setEventos(new ArrayList<>());
			disponibles.clear();
			asignados.clear();
			view.setDisponibles(disponibles);
			view.setAsignados(asignados);
			view.setAccionesEnabled(false);
			return;
		}

		ignoreEvents = true;
		view.setEventos(model.getEventos(ag.getIdAgencia(), view.getFiltroEventosSeleccionado()));
		ignoreEvents = false;

		disponibles.clear();
		asignados.clear();
		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
		view.setAccionesEnabled(false);
	}

	private void onSeleccionEvento(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		if (ignoreEvents) return;

		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			disponibles.clear();
			asignados.clear();
			view.setDisponibles(disponibles);
			view.setAsignados(asignados);
			view.setAccionesEnabled(false);
			return;
		}

		cargarReporterosEvento();
		view.setAccionesEnabled(true);
	}

	private void cargarReporterosEvento() {
		AgenciaDTO ag = view.getAgenciaSeleccionada();
		Integer idEvento = view.getIdEventoSeleccionado();

		if (ag == null || idEvento == null) {
			disponibles.clear();
			asignados.clear();
			view.setDisponibles(disponibles);
			view.setAsignados(asignados);
			view.setAccionesEnabled(false);
			return;
		}

		asignados = model.getReporterosAsignados(idEvento);

		disponibles = model.getReporterosDisponibles(
			ag.getIdAgencia(),
			idEvento,
			view.isFiltroSoloEspecialistasActivo(),
			view.isFiltroTipoBasicoActivo(),
			view.isFiltroTipoGraficoActivo(),
			view.isFiltroTipoCamarografoActivo()
		);

		// evitar duplicados por si has movido en memoria antes de guardar
		for (int i = disponibles.size() - 1; i >= 0; i--) {
			ReporteroDTO d = disponibles.get(i);
			if (containsReportero(asignados, d.getIdReportero())) {
				disponibles.remove(i);
			}
		}

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
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
			mover.add(view.getReporteroDisponibleEnFila(row));
		}

		for (ReporteroDTO r : mover) {
			if (!containsReportero(asignados, r.getIdReportero())) {
				asignados.add(r);
			}
			removeReportero(disponibles, r.getIdReportero());
		}

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
	}

	private void eliminarSeleccionados() {
		int[] filas = view.getFilasAsignadosSeleccionadas();
		if (filas == null || filas.length == 0) {
			view.showInfo("Selecciona uno o varios reporteros asignados.");
			return;
		}

		List<ReporteroDTO> mover = new ArrayList<>();
		for (int row : filas) {
			mover.add(view.getReporteroAsignadoEnFila(row));
		}

		for (ReporteroDTO r : mover) {
			if (!containsReportero(disponibles, r.getIdReportero())) {
				// vuelve a disponibles solo si sigue cumpliendo filtros actuales
				if (cumpleFiltrosActuales(r)) {
					disponibles.add(r);
				}
			}
			removeReportero(asignados, r.getIdReportero());
		}

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
	}

	private boolean cumpleFiltrosActuales(ReporteroDTO r) {
		boolean hayTiposMarcados =
				view.isFiltroTipoBasicoActivo() ||
				view.isFiltroTipoGraficoActivo() ||
				view.isFiltroTipoCamarografoActivo();

		boolean cumpleTipo = true;
		if (hayTiposMarcados) {
			cumpleTipo =
					(view.isFiltroTipoBasicoActivo() && "Básico".equals(r.getTipoReportero())) ||
					(view.isFiltroTipoGraficoActivo() && "Gráfico".equals(r.getTipoReportero())) ||
					(view.isFiltroTipoCamarografoActivo() && "Camarógrafo".equals(r.getTipoReportero()));
		}

		// El filtro de especialistas se reevalúa recargando desde BD al seleccionar evento o filtros.
		// Aquí solo controlamos el de tipo para no complicarlo al mover en memoria.
		return cumpleTipo;
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

		model.guardarAsignaciones(idEvento, asignados);
		view.showInfo("Asignaciones guardadas correctamente.");
		cargarEventos();
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