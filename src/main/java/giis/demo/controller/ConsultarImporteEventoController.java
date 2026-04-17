package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.ConsultarImporteEventoModel;
import giis.demo.model.ImporteEventoDTO;
import giis.demo.model.ReporteroDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.ConsultarImporteEventoView;

public class ConsultarImporteEventoController {

	private final ConsultarImporteEventoModel model;
	private final ConsultarImporteEventoView view;

	public ConsultarImporteEventoController(ConsultarImporteEventoModel model,
			ConsultarImporteEventoView view) {
		this.model = model;
		this.view = view;
	}

	public void initController() {
		view.addReporteroChangedListener(e -> SwingUtil.exceptionWrapper(() -> onReporteroChanged()));
		view.addEventosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));

		SwingUtil.exceptionWrapper(() -> {
			view.setReporteros(model.getReporteros());
			onReporteroChanged();
		});
	}

	private void onReporteroChanged() {
		ReporteroDTO reportero = view.getReporteroSeleccionado();
		if (reportero == null) {
			view.setEventos(new ArrayList<>());
			view.clearDetalle();
			return;
		}

		List<Object[]> eventos = model.getEventosAsignadosAReportero(reportero.getIdReportero());
		view.setEventos(eventos);
		view.clearDetalle();
	}

	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		ReporteroDTO reportero = view.getReporteroSeleccionado();
		Integer idEvento = view.getIdEventoSeleccionado();

		if (reportero == null || idEvento == null) {
			view.clearDetalle();
			return;
		}

		ImporteEventoDTO dto = model.calcularImporte(reportero.getIdReportero(), idEvento);
		if (dto == null) {
			view.clearDetalle();
			return;
		}

		view.setDetalle(
			formatearInfo(dto),
			formatearDesglose(dto)
		);
	}

	private String formatearInfo(ImporteEventoDTO dto) {
		StringBuilder sb = new StringBuilder();

		sb.append("Evento: ").append(dto.getNombreEvento()).append("\n");
		sb.append("Reportero: ").append(dto.getNombreReportero()).append("\n");
		sb.append("Provincia reportero: ").append(dto.getProvinciaReportero()).append("\n");
		sb.append("Provincia evento: ").append(dto.getProvinciaEvento()).append("\n");
		sb.append("País del evento: ").append(dto.getPaisEvento()).append("\n");
		sb.append("Fecha inicio: ").append(dto.getFechaInicio()).append("\n");
		sb.append("Fecha fin: ").append(dto.getFechaFin()).append("\n");
		sb.append("Duración del evento: ").append(dto.getDuracionDias()).append(" día(s)");

		return sb.toString();
	}

	private String formatearDesglose(ImporteEventoDTO dto) {
		StringBuilder sb = new StringBuilder();

		sb.append("Alojamiento/día : ")
		  .append(String.format("%.2f €", dto.getAlojamientoDia()))
		  .append("\n\n");

		sb.append("Manutención/día : ")
		  .append(String.format("%.2f €", dto.getManutencionDia()))
		  .append("\n\n");

		sb.append("Total por día   : ")
		  .append(String.format("%.2f €", dto.getTotalDia()))
		  .append("\n\n");

		sb.append("TOTAL EVENTO    : ")
		  .append(String.format("%.2f €", dto.getTotalEvento()));

		return sb.toString();
	}
}