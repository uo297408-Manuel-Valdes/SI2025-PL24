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
	private boolean asignacionFinalizada = false;

	public AsignarReporterosAEventosController(AsignarReporterosAEventosModel model,
			AsignarReporterosAEventosView view) {
		this.model = model;
		this.view = view;
	}

	public void initController() {
		view.addAgenciaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addFiltroEventosChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addEventosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onSeleccionEvento(e)));
		view.addAsignadosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> actualizarEstadoBotonResponsable()));
		view.addSoloEspecialistasChangedListener(e -> SwingUtil.exceptionWrapper(() -> recargarSiHayEvento()));
		view.addFiltroTipoChangedListener(e -> SwingUtil.exceptionWrapper(() -> recargarSiHayEvento()));

		view.addAsignarListener(e -> SwingUtil.exceptionWrapper(() -> asignarSeleccionados()));
		view.addEliminarListener(e -> SwingUtil.exceptionWrapper(() -> eliminarSeleccionados()));
		view.addMarcarResponsableListener(e -> SwingUtil.exceptionWrapper(() -> marcarResponsableSeleccionado()));
		view.addFinalizarAsignacionListener(e -> SwingUtil.exceptionWrapper(() -> finalizarAsignacion()));
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
			view.setFinalizarAsignacionEnabled(false);
			view.setEstadoAsignacion(false);
			return;
		}

		ignoreEvents = true;
		view.setEventos(model.getEventos(ag.getIdAgencia(), view.getFiltroEventosSeleccionado()));
		view.clearSeleccionEvento();
		ignoreEvents = false;

		view.setAccionesEnabled(false);
		view.setResponsableEnabled(false);
		view.setFinalizarAsignacionEnabled(false);
		view.setEstadoAsignacion(false);
		asignacionFinalizada = false;
	}

	private void onSeleccionEvento(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		if (ignoreEvents) return;

		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			limpiarTablasReporteros();
			view.setAccionesEnabled(false);
			view.setResponsableEnabled(false);
			view.setFinalizarAsignacionEnabled(false);
			view.setEstadoAsignacion(false);
			asignacionFinalizada = false;
			return;
		}

		cargarReporterosEvento();
	}

	private void recargarSiHayEvento() {
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			limpiarTablasReporteros();
			view.setAccionesEnabled(false);
			view.setResponsableEnabled(false);
			view.setFinalizarAsignacionEnabled(false);
			view.setEstadoAsignacion(false);
			asignacionFinalizada = false;
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
			view.setResponsableEnabled(false);
			view.setFinalizarAsignacionEnabled(false);
			view.setEstadoAsignacion(false);
			asignacionFinalizada = false;
			return;
		}

		asignacionFinalizada = model.isAsignacionFinalizada(idEvento);

		asignados = new ArrayList<>(model.getReporterosAsignados(idEvento));
		disponibles = new ArrayList<>(model.getReporterosDisponibles(
			ag.getIdAgencia(),
			idEvento,
			view.isFiltroSoloEspecialistasActivo(),
			view.isFiltroTipoBasicoActivo(),
			view.isFiltroTipoGraficoActivo(),
			view.isFiltroTipoCamarografoActivo()
		));

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
		view.setEstadoAsignacion(asignacionFinalizada);
		view.setFinalizarAsignacionEnabled(!asignacionFinalizada);
		view.setEdicionAsignacionEnabled(!asignacionFinalizada);
		view.setResponsableEnabled(false);
	}

	private void asignarSeleccionados() {
		if (asignacionFinalizada) {
			view.showError("No se puede modificar la asignación porque está finalizada.");
			return;
		}

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
				r.setResponsable(false);
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
		view.setResponsableEnabled(false);
	}

	private ReporteroDTO findReportero(List<ReporteroDTO> lista, int idReportero) {
		for (ReporteroDTO r : lista) {
			if (r.getIdReportero() == idReportero) return r;
		}
		return null;
	}

	private boolean cumpleFiltrosActuales(ReporteroDTO r, int idEvento) {
		boolean cumpleEspecialista = true;
		if (view.isFiltroSoloEspecialistasActivo()) {
			cumpleEspecialista = model.esEspecialistaEnEvento(r.getIdReportero(), idEvento);
		}

		boolean hayTiposMarcados =
				view.isFiltroTipoBasicoActivo()
				|| view.isFiltroTipoGraficoActivo()
				|| view.isFiltroTipoCamarografoActivo();

		boolean cumpleTipo = true;
		if (hayTiposMarcados) {
			cumpleTipo =
					(view.isFiltroTipoBasicoActivo() && "Básico".equals(r.getTipoReportero()))
					|| (view.isFiltroTipoGraficoActivo() && "Gráfico".equals(r.getTipoReportero()))
					|| (view.isFiltroTipoCamarografoActivo() && "Camarógrafo".equals(r.getTipoReportero()));
		}
		return cumpleEspecialista && cumpleTipo;
	}

	private void eliminarSeleccionados() {
		if (asignacionFinalizada) {
			view.showError("No se puede modificar la asignación porque está finalizada.");
			return;
		}

		int[] filas = view.getFilasAsignadosSeleccionadas();
		if (filas == null || filas.length == 0) {
			view.showInfo("Selecciona uno o varios reporteros asignados.");
			return;
		}

		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}

		List<ReporteroDTO> mover = new ArrayList<>();
		for (int row : filas) {
			ReporteroDTO r = view.getReporteroAsignadoEnFila(row);
			if (r != null) {
				ReporteroDTO original = findReportero(asignados, r.getIdReportero());
				if (original != null) mover.add(original);
			}
		}

		for (ReporteroDTO r : mover) {
			removeReportero(asignados, r.getIdReportero());
			if (!containsReportero(disponibles, r.getIdReportero()) && cumpleFiltrosActuales(r, idEvento)) {
				r.setResponsable(false);
				disponibles.add(r);
			}
		}

		ordenarPorNombre(disponibles);
		ordenarPorNombre(asignados);

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
		view.clearSeleccionDisponibles();
		view.clearSeleccionAsignados();
		view.setResponsableEnabled(false);
	}

	private void marcarResponsableSeleccionado() {
		if (asignacionFinalizada) {
			view.showError("No se puede modificar el responsable porque la asignación está finalizada.");
			return;
		}

		Integer idReportero = view.getIdReporteroAsignadoSeleccionado();
		if (idReportero == null) {
			view.showInfo("Selecciona un reportero asignado.");
			return;
		}

		boolean encontrado = false;
		for (ReporteroDTO r : asignados) {
			boolean esResp = r.getIdReportero() == idReportero;
			r.setResponsable(esResp);
			if (esResp) encontrado = true;
		}

		if (!encontrado) {
			view.showError("El reportero seleccionado no está asignado al evento.");
			return;
		}

		view.setAsignados(asignados);
		view.setResponsableEnabled(false);
	}

	private void finalizarAsignacion() {
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}

		if (!view.confirm("¿Finalizar la asignación del evento seleccionado?", "Confirmar")) {
			return;
		}

		try {
			model.guardarAsignaciones(idEvento, asignados); // guarda primero cambios pendientes
			model.finalizarAsignacion(idEvento);
			view.showInfo("Asignación finalizada correctamente.");
			cargarReporterosEvento();
		} catch (IllegalStateException ex) {
			view.showError(ex.getMessage());
		}
	}

	private void actualizarEstadoBotonResponsable() {
		view.setResponsableEnabled(!asignacionFinalizada && view.getIdReporteroAsignadoSeleccionado() != null);
	}

	private void guardar() {
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}

		if (asignacionFinalizada) {
			view.showError("No se puede modificar la asignación porque está finalizada.");
			return;
		}

		if (!view.confirm("¿Guardar asignaciones del evento seleccionado?", "Confirmar")) {
			return;
		}

		try {
			model.guardarAsignaciones(idEvento, asignados);
			view.showInfo("Asignaciones guardadas correctamente.");
			cargarReporterosEvento();
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
			if (r.getIdReportero() == idReportero) return true;
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