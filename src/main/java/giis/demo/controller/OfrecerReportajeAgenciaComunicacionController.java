package giis.demo.controller;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.AgenciaDTO;
import giis.demo.model.EmpresaDTO;
import giis.demo.model.OfrecerReportajeAgenciaComunicacionModel;
import giis.demo.model.OfrecimientoDTO;
import giis.demo.model.EventoDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.OfrecerReportajeAgenciaComunicacionView;



public class OfrecerReportajeAgenciaComunicacionController {
	private final OfrecerReportajeAgenciaComunicacionModel model;
	private final OfrecerReportajeAgenciaComunicacionView view;

	private List<EventoDTO> reportajes = new ArrayList<>();
	private List<EmpresaDTO> disponibles = new ArrayList<>();
	private List<EmpresaDTO> seleccionados = new ArrayList<>();

	public OfrecerReportajeAgenciaComunicacionController(OfrecerReportajeAgenciaComunicacionModel model, OfrecerReportajeAgenciaComunicacionView view) {
		this.model = model;
		this.view = view;
	}
	
	public void initController() {

		view.addAgenciaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarReportajes()));
		view.addFiltroChangedListener(e -> SwingUtil.exceptionWrapper(() -> aplicarFiltro()));
		view.addReportajesSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onReportajeSeleccionado(e)));
		view.addAsignarListener(e -> SwingUtil.exceptionWrapper(() -> moverDisponiblesASeleccionados()));
		view.addOfrecerListener(e -> SwingUtil.exceptionWrapper(() -> confirmarYGuardar()));
		view.addQuitarOfrecimientoListener(e -> SwingUtil.exceptionWrapper(() -> quitarOfrecimiento()) );
		view.addSalirListener(e -> SwingUtil.exceptionWrapper(() -> salir()));

		SwingUtil.exceptionWrapper(() -> {
			List<AgenciaDTO> agencias = model.getAgencias();
			view.setAgencias(agencias);
			cargarReportajes();
		});
	}
	
	private void cargarReportajes() {
		AgenciaDTO agencia = view.getAgenciaSeleccionada();
		if (agencia == null) {
			view.setReportajes(new ArrayList<>());
			return;
		}

		reportajes = model.getReportajes(agencia.getIdAgencia());
		view.setReportajes(reportajes);

		disponibles = new ArrayList<>();
		seleccionados = new ArrayList<>();
		view.setDisponibles(disponibles);
		view.setAsignados(seleccionados);
	}
	
	private void aplicarFiltro() {
		Integer idEvento = view.getIdReportajeSeleccionado();
		String filtro = view.getFiltroSeleccionado();
		
		if(idEvento==null)return;
		
		if(filtro.equals("Empresas sin ofrecimiento")) disponibles = model.getEmpresasSinOfrecimiento(idEvento);
		
		if(filtro.equals("Empresas con ofrecimiento")) disponibles = model.getEmpresasConOfrecimiento(idEvento);
		
		view.setDisponibles(disponibles);
	}
	private void onReportajeSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Integer idEvento = view.getIdReportajeSeleccionado();
		AgenciaDTO agencia = view.getAgenciaSeleccionada();
		String fecha = view.getFechaReportajeSeleccionado();
		String filtro = view.getFiltroSeleccionado();

		if (idEvento == null || agencia == null || fecha == null) {
			disponibles = new ArrayList<>();
			seleccionados = new ArrayList<>();
			view.setDisponibles(disponibles);
			view.setAsignados(seleccionados);
			return;
		}

		seleccionados = new ArrayList<>();
		
		if(filtro.equals("Empresas sin ofrecimiento")) disponibles = model.getEmpresasSinOfrecimiento(idEvento);
			
		if(filtro.equals("Empresas con ofrecimiento")) disponibles = model.getEmpresasConOfrecimiento(idEvento);
		

		view.setDisponibles(disponibles);
		view.setAsignados(seleccionados);
	}
	
	private void moverDisponiblesASeleccionados() {
		int[] selectedRows = view.getFilasDisponiblesSeleccionadas();
		if (selectedRows == null || selectedRows.length == 0) {
			view.showInfo("Selecciona una o varias empresas disponibles.");
			return;
		}

		List<EmpresaDTO> aMover = new ArrayList<>();
		for (int row : selectedRows) {
			aMover.add(view.getEmpresaDisponibleEnFila(row));
		}

		for (EmpresaDTO r : aMover) {
			boolean yaEsta = seleccionados.stream().anyMatch(x -> x.getIdEmpresa() == r.getIdEmpresa());
			if (!yaEsta) seleccionados.add(r);
		}

		List<Integer> idsMovidos = aMover.stream().map(EmpresaDTO::getIdEmpresa).collect(Collectors.toList());
		disponibles = disponibles.stream()
				.filter(r -> !idsMovidos.contains(r.getIdEmpresa()))
				.collect(Collectors.toList());

		view.setDisponibles(disponibles);
		view.setAsignados(seleccionados);
	}
	
	private void confirmarYGuardar() {
		Integer idEvento = view.getIdReportajeSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}
		if (seleccionados.isEmpty()) {
			view.showInfo("Selecciona al menos una empresa antes de ofrecer.");
			return;
		}

		String evento = view.getNombreReportajeSeleccionado();
		String fecha = view.getFechaReportajeSeleccionado();

		String listaEmpresass = seleccionados.stream()
				.map(EmpresaDTO::getNombre)
				.collect(Collectors.joining(", "));

		String msg = "Vas a ofrecer el siguiente evento a las siguientes empresas:\n\n"
				+ "Evento: " + evento + " (" + fecha + ")\n"
				+ "Empresa: " + listaEmpresass + "\n\n"
				+ "¿Confirmas el ofrecimiento?";

		if (!view.confirm(msg, "Confirmar ofrecimiento")) return;

		List<Integer> ids = seleccionados.stream().map(EmpresaDTO::getIdEmpresa).collect(Collectors.toList());
		model.ofrecerEmpresa(idEvento, ids);

		view.showInfo("Asignación guardada correctamente.");
		view.getFrame().dispose();
	}
	
	private void quitarOfrecimiento(){
		Integer idEvento = view.getIdReportajeSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}
		if (seleccionados.isEmpty()) {
			view.showInfo("Selecciona al menos una empresa antes de quitar ofrecimiento.");
			return;
		}
		
		String evento = view.getNombreReportajeSeleccionado();
		String fecha = view.getFechaReportajeSeleccionado();

		String listaEmpresass = seleccionados.stream()
				.map(EmpresaDTO::getNombre)
				.collect(Collectors.joining(", "));

		String msg = "Vas a quitar el ofrecimiento del siguiente evento a las siguientes empresas:\n\n"
				+ "Evento: " + evento + " (" + fecha + ")\n"
				+ "Empresa: " + listaEmpresass + "\n\n"
				+ "¿Estas seguro de quitar el ofrecimiento?";

		if (!view.confirm(msg, "Confirmar")) return;
		
		List<Integer> ids = seleccionados.stream().map(EmpresaDTO::getIdEmpresa).collect(Collectors.toList());
		for(Integer idEmpresa:ids) {
			OfrecimientoDTO of=new OfrecimientoDTO();
			of=model.getOfrecimiento(idEmpresa,idEvento);
			if(of!=null) {
				if(of.getDecision()!=null && of.getDecision().equals("ACEPTADO")) {
					view.showInfo("Una o varias empresas ya aceptaron el ofrecimiento, se les mandara un correo notificando el cambio");
				}
			}
		}
		model.quitarOfrecimiento(idEvento, ids);
		for(Integer idEmpresa:ids) {
			OfrecimientoDTO of=new OfrecimientoDTO();
			of=model.getOfrecimiento(idEmpresa,idEvento);
			
			if(of!=null && of.getDecision().equals("ACEPTADO")) {
				view.showInfo("Una o varias empresas ya aceptaron el ofrecimiento, se les mandara un correo notificando el cambio");
			}
		}

		view.showInfo("Se quito el ofrecimiento correctamente.");
		view.getFrame().dispose();
	}
	
	private void salir() {
		view.getFrame().dispose();
	}
}
