package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.AgenciaDTO;
import giis.demo.model.ConcederAccesoModel;
import giis.demo.model.EmpresaDTO;
import giis.demo.model.EventoDTO;
import giis.demo.model.OfrecimientoDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.ConcederAccesoView;
import giis.demo.model.AccesoDTO;

public class ConcederAccesoController {

	private final ConcederAccesoModel model;
	private final ConcederAccesoView  view;



	private List<EventoDTO> eventos      = new ArrayList<>();
	private List<EmpresaDTO> aceptantes  = new ArrayList<>();
	private List<EmpresaDTO> seleccionadas = new ArrayList<>();

	public ConcederAccesoController(ConcederAccesoModel model,
	                                      ConcederAccesoView  view) {
		this.model     = model;
		this.view      = view;
	
	}

	public void initController() {
		view.addAgenciaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addEventosSelectionListener   (e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));
		view.addAceptantesSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onAceptanteSeleccionado(e)));
		view.addConcederAccesoListener     (e -> SwingUtil.exceptionWrapper(() -> onConcederAcceso()));
		view.addQuitarAccesoListener(e -> SwingUtil.exceptionWrapper(() -> quitarAcceso()) );
		view.addFiltroChangedListener(e -> SwingUtil.exceptionWrapper(() -> aplicarFiltro()));
		
		SwingUtil.exceptionWrapper(() -> cargarEventos());
		SwingUtil.exceptionWrapper(() -> {
		    List<AgenciaDTO> agencias = model.getAgencias();
		    view.setAgencias(agencias);
		    cargarEventos();
		});
	}


	private void aplicarFiltro() {
		
		Integer idEvento = view.getIdEventoSeleccionado();
		String nombreEvento = view.getNombreEventoSeleccionado();
		String filtro = view.getFiltroSeleccionado();
		seleccionadas = new ArrayList<>();
		
		if(idEvento==null)return;
		
		if(filtro.equals("Empresas sin acceso")) aceptantes = model.getEmpresasAceptantesSinAcceso(idEvento);
		
		if(filtro.equals("Empresas con acceso")) aceptantes = model.getEmpresasConAcceso(idEvento);

		view.setEmpresasAceptantes(aceptantes, nombreEvento);
		view.setEmpresasSeleccionadas(seleccionadas);
	}

	private void cargarEventos() {
		AgenciaDTO agencia = view.getAgenciaSeleccionada();
	    if (agencia == null) {
	        view.setEventos(new ArrayList<>());
	        view.limpiarPanelDerecho();
	        return;
	    }
	    eventos = model.getEventosCubiertos(agencia.getIdAgencia()); // ← antes era idAgencia
	    view.setEventos(eventos);
	    seleccionadas = new ArrayList<>();
	    aceptantes    = new ArrayList<>();
	    view.limpiarPanelDerecho();
	}


	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			aceptantes    = new ArrayList<>();
			seleccionadas = new ArrayList<>();
			view.limpiarPanelDerecho();
			return;
		}
		
		String filtro = view.getFiltroSeleccionado();
		String nombreEvento = view.getNombreEventoSeleccionado();

		//Al cambiar de evento reiniciamos la selección
		seleccionadas = new ArrayList<>();
		if(filtro.equals("Empresas sin acceso")) aceptantes = model.getEmpresasAceptantesSinAcceso(idEvento);
		
		if(filtro.equals("Empresas con acceso")) aceptantes = model.getEmpresasConAcceso(idEvento);

		view.setEmpresasAceptantes(aceptantes, nombreEvento);
		view.setEmpresasSeleccionadas(seleccionadas);
	}


	private void onAceptanteSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		int[] filas = view.getFilasAceptantesSeleccionadas();
		if (filas == null || filas.length == 0) return;

		for (int fila : filas) {
			EmpresaDTO emp = view.getEmpresaAceptanteEnFila(fila);
			boolean yaEsta = seleccionadas.stream()
					.anyMatch(x -> x.getIdEmpresa() == emp.getIdEmpresa());
			if (!yaEsta) seleccionadas.add(emp);
		}

		view.setEmpresasSeleccionadas(seleccionadas);
	}

	private void onConcederAcceso() {
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento primero.");
			return;
		}
		if (seleccionadas.isEmpty()) {
			view.showInfo("Selecciona al menos una empresa para conceder acceso.");
			return;
		}

		String nombreEvento = view.getNombreEventoSeleccionado();
		String listaEmpresas = seleccionadas.stream()
				.map(EmpresaDTO::getNombre)
				.collect(Collectors.joining(", "));

		String msg = "Vas a conceder acceso al reportaje de:\n\n"
				+ "Evento:    " + nombreEvento + "\n"
				+ "Empresas:  " + listaEmpresas + "\n\n"
				+ "¿Confirmas la operación?";

		if (!view.confirm(msg, "Confirmar acceso")) return;

		List<Integer> ids = seleccionadas.stream()
				.map(EmpresaDTO::getIdEmpresa)
				.collect(Collectors.toList());

		model.concederAcceso(idEvento, ids);

		view.showInfo("Acceso concedido correctamente.");
		view.getFrame().dispose();
	}
	
	private void quitarAcceso(){
		Integer idEvento = view.getIdEventoSeleccionado();
		if (idEvento == null) {
			view.showInfo("Selecciona un evento.");
			return;
		}
		if (seleccionadas.isEmpty()) {
			view.showInfo("Selecciona al menos una empresa antes de quitar el acceso.");
			return;
		}
		
		String evento = view.getNombreEventoSeleccionado();
		

		String listaEmpresass = seleccionadas.stream()
				.map(EmpresaDTO::getNombre)
				.collect(Collectors.joining(", "));

		String msg = "Vas a quitar el acceso del siguiente evento a las siguientes empresas:\n\n"
				+ "Evento: " + evento +" \n"
				+ "Empresa: " + listaEmpresass + "\n\n"
				+ "¿Estas seguro de quitar el acceso?";

		if (!view.confirm(msg, "Confirmar")) return;
		
		List<Integer> ids = seleccionadas.stream().map(EmpresaDTO::getIdEmpresa).collect(Collectors.toList());
		for(Integer idEmpresa:ids) {
			AccesoDTO ac=new AccesoDTO();
			ac=model.getAcceso(idEmpresa,idEvento);
			if(ac!=null) {
				if( ac.getDescargado()==1) {
					view.showInfo("Una o varias empresas ya descargaron el acceso");
					return;
				}
			}
		}
		model.quitarAcceso(idEvento, ids);

		view.showInfo("Se quito el acceso correctamente.");
		view.getFrame().dispose();
	}
}