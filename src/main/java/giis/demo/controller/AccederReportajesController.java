package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.AccederReportajesModel;
import giis.demo.model.AgenciaDTO;
import giis.demo.model.EmpresaDTO;
import giis.demo.model.EventoDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.AccederReportajesView;

public class AccederReportajesController {

	private final AccederReportajesModel model;
	private final AccederReportajesView view;
	
	private List<EventoDTO> reportajes = new ArrayList<>();
	
	public AccederReportajesController(AccederReportajesModel model, AccederReportajesView view) {
		this.model=model;
		this.view=view;
	}
	
	public void initController() {

		view.addEmpresaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarReportajes()));
		view.addReportajesSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onReportajeSeleccionado(e)));
		view.addFinalizarListener(e -> SwingUtil.exceptionWrapper(() -> finalizar()));

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

		reportajes = model.getReportajes(empresa.getIdEmpresa());
		view.setReportajes(reportajes);
	}
	
	private void onReportajeSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Integer idEvento = view.getIdReportajeSeleccionado();
		EmpresaDTO empresa = view.getEmpresaSeleccionada();

		if (idEvento == null || empresa == null) {
			view.setInfo(null, null, null);
			return;
		}

		view.setInfo(null, null, null);
	}
	
	private void finalizar() {
		view.getFrame().dispose();
	}


	
}
