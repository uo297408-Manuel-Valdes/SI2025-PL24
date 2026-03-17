package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.AccederReportajesModel;
import giis.demo.model.AgenciaDTO;
import giis.demo.model.EmpresaDTO;
import giis.demo.model.EventoDTO;
import giis.demo.model.MultimediaDTO;
import giis.demo.model.ReportajeDTO;
import giis.demo.model.VersionDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.AccederReportajesView;

public class AccederReportajesController {

	private final AccederReportajesModel model;
	private final AccederReportajesView view;
	
	private List<EventoDTO> eventos = new ArrayList<>();
	
	public AccederReportajesController(AccederReportajesModel model, AccederReportajesView view) {
		this.model=model;
		this.view=view;
	}
	
	public void initController() {

		view.addEmpresaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarReportajes()));
		view.addReportajesSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onReportajeSeleccionado(e)));
		view.addFinalizarListener(e -> SwingUtil.exceptionWrapper(() -> finalizar()));
		//view.addDescargarListener(e -> SwingUtil.exceptionWrapper(() -> descargar()));

		SwingUtil.exceptionWrapper(() -> {
			List<EmpresaDTO> empresas = model.getEmpresas();
			view.setEmpresas(empresas);
			cargarReportajes();
		});
	}

	private void cargarReportajes() {
		EmpresaDTO empresa = view.getEmpresaSeleccionada();
		if (empresa == null) {
			view.setReportajes(new ArrayList<>());
			return;
		}

		eventos = model.getReportajes(empresa.getIdEmpresa());
		view.setReportajes(eventos);
	}
	
	private void cargarMultimedia(int id) {
		List<MultimediaDTO> multimedia=model.getMultimedia(id);
		
		if (multimedia == null) {
			view.setMultimedia(new ArrayList<>());
			return;
		}
		view.setMultimedia(multimedia);
		
	}
	
	private void onReportajeSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Integer idEvento = view.getIdReportajeSeleccionado();
		EmpresaDTO empresa = view.getEmpresaSeleccionada();

		if (idEvento == null || empresa == null) {
			view.setInfo(null, null, null);
			return;
		}

		ReportajeDTO reportaje=model.getInfoReportaje(idEvento);
		
		if (reportaje == null) {
			view.setInfo(null, null, null);
			return;
		}
		
		cargarMultimedia(reportaje.getIdReportaje());
		
		VersionDTO version=model.getVersion(reportaje.getIdReportaje());
		
		if (version == null) {
			view.setInfo(reportaje.getTitulo(), null, null);
			return;
		}
		
		view.setInfo(reportaje.getTitulo(), version.getSubtitulo(), version.getCuerpo());
	}
	
	private void finalizar() {
		view.getFrame().dispose();
	}


	
}
