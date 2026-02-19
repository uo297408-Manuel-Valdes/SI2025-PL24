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

		view.addAgenciaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventosSinAsignacion()));
		view.addEventosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));
		view.addAsignarListener(e -> SwingUtil.exceptionWrapper(() -> moverDisponiblesAAsignados()));
		view.addAceptarListener(e -> SwingUtil.exceptionWrapper(() -> confirmarYGuardar()));

		SwingUtil.exceptionWrapper(() -> {
			List<AgenciaDTO> agencias = model.getAgencias();
			view.setAgencias(agencias);
			cargarEventosSinAsignacion();
		});
	}

	private void cargarEventosSinAsignacion() {
		AgenciaDTO agencia = view.getAgenciaSeleccionada();
		if (agencia == null) {
			view.setEventos(new ArrayList<>());
			return;
		}

		eventos = model.getEventosSinAsignacion(agencia.getIdAgencia());
		view.setEventos(eventos);

		disponibles = new ArrayList<>();
		asignados = new ArrayList<>();
		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
	}

	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Integer idEvento = view.getIdEventoSeleccionado();
		AgenciaDTO agencia = view.getAgenciaSeleccionada();
		String fecha = view.getFechaEventoSeleccionado();

		if (idEvento == null || agencia == null || fecha == null) {
			disponibles = new ArrayList<>();
			asignados = new ArrayList<>();
			view.setDisponibles(disponibles);
			view.setAsignados(asignados);
			return;
		}

		asignados = new ArrayList<>();

		disponibles = model.getReporterosDisponibles(agencia.getIdAgencia(), fecha);

		view.setDisponibles(disponibles);
		view.setAsignados(asignados);
	}

	private void moverDisponiblesAAsignados() {
		int[] selectedRows = view.getFilasDisponiblesSeleccionadas();
		if (selectedRows == null || selectedRows.length == 0) {
			view.showInfo("Selecciona uno o varios reporteros disponibles.");
			return;
		}

		List<ReporteroDTO> aMover = new ArrayList<>();
		for (int row : selectedRows) {
			aMover.add(view.getReporteroDisponibleEnFila(row));
		}

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

	private void confirmarYGuardar() {
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}
		if (asignados.isEmpty()) {
			view.showInfo("Asigna al menos un reportero antes de aceptar.");
			return;
		}

		String evento = view.getNombreEventoSeleccionado();
		String fecha = view.getFechaEventoSeleccionado();

		String listaReporteros = asignados.stream()
				.map(ReporteroDTO::getNombre)
				.collect(Collectors.joining(", "));

		String msg = "Vas a asignar los siguientes reporteros:\n\n"
				+ "Evento: " + evento + " (" + fecha + ")\n"
				+ "Reporteros: " + listaReporteros + "\n\n"
				+ "¿Confirmas la asignación?";

		if (!view.confirm(msg, "Confirmar asignación")) return;

		List<Integer> ids = asignados.stream().map(ReporteroDTO::getIdReportero).collect(Collectors.toList());
		model.asignarReporteros(idEvento, ids);

		view.showInfo("Asignación guardada correctamente.");
		view.getFrame().dispose();
	}
}
