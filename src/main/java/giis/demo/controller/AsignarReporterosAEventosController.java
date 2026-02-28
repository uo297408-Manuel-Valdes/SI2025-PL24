package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.AgenciaDTO;
import giis.demo.model.AsignarReporterosAEventosModel;
import giis.demo.model.EventoDTO;
import giis.demo.model.ReporteroDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.AsignarReporterosAEventosView;

public class AsignarReporterosAEventosController {

	private final AsignarReporterosAEventosModel model;
	private final AsignarReporterosAEventosView view;

	private List<EventoDTO> eventos = new ArrayList<>();
	private List<ReporteroDTO> disponibles = new ArrayList<>();
	private List<ReporteroDTO> asignados = new ArrayList<>();

	public AsignarReporterosAEventosController(AsignarReporterosAEventosModel model, AsignarReporterosAEventosView view) {
		this.model = model;
		this.view = view;
	}

	public void initController() {

		view.addAgenciaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addFiltroChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addEventosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));

		view.addAsignarListener(e -> SwingUtil.exceptionWrapper(() -> moverDisponiblesAAsignados()));
		view.addEliminarListener(e -> SwingUtil.exceptionWrapper(() -> moverAsignadosADisponibles()));
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
			resetTablas();
			return;
		}

		int filtro = view.getFiltroSeleccionado(); // 0 / 1

		if (filtro == 0) {
			eventos = model.getEventosSinAsignacion(ag.getIdAgencia());
		} else {
			eventos = model.getEventosConAsignacion(ag.getIdAgencia());
		}

		view.setEventos(eventos);
		resetTablas();
	}

	private void resetTablas() {
		disponibles = new ArrayList<>();
		asignados = new ArrayList<>();
		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
		view.setAccionesEnabled(false);
	}

	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			resetTablas();
			return;
		}

		asignados = model.getReporterosdeEvento(idEvento);
		disponibles = model.getReporterosDisponiblesParaEvento(idEvento);

		view.setAsignados(asignados);
		view.setDisponibles(disponibles);
		view.setAccionesEnabled(true);
	}


	private void moverDisponiblesAAsignados() {
		int[] selectedRows = view.getFilasDisponiblesSeleccionadas();
		if (selectedRows == null || selectedRows.length == 0) {
			view.showInfo("Selecciona uno o varios reporteros disponibles.");
			return;
		}

		List<ReporteroDTO> aMover = new ArrayList<>();
		for (int row : selectedRows) aMover.add(view.getReporteroDisponibleEnFila(row));

		for (ReporteroDTO r : aMover) {
			boolean yaEsta = asignados.stream().anyMatch(x -> x.getIdReportero() == r.getIdReportero());
			if (!yaEsta) asignados.add(r);
		}

		List<Integer> idsMovidos = aMover.stream().map(ReporteroDTO::getIdReportero).collect(Collectors.toList());
		disponibles = disponibles.stream()
				.filter(r -> !idsMovidos.contains(r.getIdReportero()))
				.collect(Collectors.toList());

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
	}

	private void moverAsignadosADisponibles() {
		int[] selectedRows = view.getFilasAsignadosSeleccionadas();
		if (selectedRows == null || selectedRows.length == 0) {
			view.showInfo("Selecciona uno o varios reporteros asignados para eliminar.");
			return;
		}

		List<ReporteroDTO> aMover = new ArrayList<>();
		for (int row : selectedRows) aMover.add(view.getReporteroAsignadoEnFila(row));

		for (ReporteroDTO r : aMover) {
			boolean yaEsta = disponibles.stream().anyMatch(x -> x.getIdReportero() == r.getIdReportero());
			if (!yaEsta) disponibles.add(r);
		}

		List<Integer> idsMovidos = aMover.stream().map(ReporteroDTO::getIdReportero).collect(Collectors.toList());
		asignados = asignados.stream()
				.filter(r -> !idsMovidos.contains(r.getIdReportero()))
				.collect(Collectors.toList());

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
	}


	private void guardar() {
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}

		int filtro = view.getFiltroSeleccionado(); // 0 = sin asignación (HU 33550), 1 = con asignación (HU 33556)

		String evento = view.getNombreEventoSeleccionado();
		String fecha = view.getFechaEventoSeleccionado();

		String lista = asignados.isEmpty()
				? "(ninguno)"
				: asignados.stream().map(ReporteroDTO::getNombre).collect(Collectors.joining(", "));

		String msg = "Vas a guardar la asignación:\n\n"
				+ "Evento: " + evento + " (" + fecha + ")\n"
				+ "Reporteros: " + lista + "\n\n"
				+ "¿Confirmas?";

		if (!view.confirm(msg, "Confirmar")) return;

		List<Integer> idsFinales = asignados.stream().map(ReporteroDTO::getIdReportero).collect(Collectors.toList());

		if (filtro == 0) {
			model.asignarInicial(idEvento, idsFinales);
		}
		else {
			model.guardarAsignacionFinal(idEvento, idsFinales);
		}

		view.showInfo("Asignación guardada correctamente.");
		view.getFrame().dispose();
	}
}