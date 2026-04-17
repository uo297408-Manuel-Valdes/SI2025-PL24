package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.AgenciaDTO;
import giis.demo.model.AccesoDTO;
import giis.demo.model.ConcederAccesoModel;
import giis.demo.model.EmpresaDTO;
import giis.demo.model.EventoDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.ConcederAccesoView;

public class ConcederAccesoController {

	private final ConcederAccesoModel model;
	private final ConcederAccesoView  view;

	private List<EventoDTO>  eventos       = new ArrayList<>();
	private List<EmpresaDTO> aceptantes    = new ArrayList<>();
	private List<EmpresaDTO> seleccionadas = new ArrayList<>();

	public ConcederAccesoController(ConcederAccesoModel model, ConcederAccesoView view) {
		this.model = model;
		this.view  = view;
	}

	public void initController() {
		view.addAgenciaChangedListener     (e -> SwingUtil.exceptionWrapper(() -> cargarEventos()));
		view.addFiltroChangedListener      (e -> SwingUtil.exceptionWrapper(() -> aplicarFiltro()));
		view.addEventosSelectionListener   (e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));
		view.addAceptantesSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onAceptanteSeleccionado(e)));
		view.addConcederAccesoListener     (e -> SwingUtil.exceptionWrapper(() -> onConcederAcceso()));

		SwingUtil.exceptionWrapper(() -> {
			List<AgenciaDTO> agencias = model.getAgencias();
			view.setAgencias(agencias);
			cargarEventos();
		});
	}

	// ── Carga de eventos ─────────────────────────────────────────────────

	private void cargarEventos() {
		AgenciaDTO agencia = view.getAgenciaSeleccionada();
		if (agencia == null) {
			view.setEventos(new ArrayList<>());
			view.limpiarPanelDerecho();
			return;
		}
		eventos = model.getEventosCubiertos(agencia.getIdAgencia());
		view.setEventos(eventos);
		seleccionadas = new ArrayList<>();
		aceptantes    = new ArrayList<>();
		view.limpiarPanelDerecho();
	}

	// ── Filtro de acceso ──────────────────────────────────────────────────

	private void aplicarFiltro() {
		Integer idEvento     = view.getIdEventoSeleccionado();
		String  nombreEvento = view.getNombreEventoSeleccionado();
		if (idEvento == null) return;

		seleccionadas = new ArrayList<>();
		aceptantes    = getEmpresasSegunFiltro(idEvento);
		view.setEmpresasAceptantes(aceptantes, nombreEvento);
		view.setEmpresasSeleccionadas(seleccionadas);
	}

	// ── Seleccion de evento ──────────────────────────────────────────────

	private void onEventoSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Integer idEvento     = view.getIdEventoSeleccionado();
		String  nombreEvento = view.getNombreEventoSeleccionado();

		if (idEvento == null) {
			aceptantes    = new ArrayList<>();
			seleccionadas = new ArrayList<>();
			view.limpiarPanelDerecho();
			return;
		}

		seleccionadas = new ArrayList<>();
		aceptantes    = getEmpresasSegunFiltro(idEvento);
		view.setEmpresasAceptantes(aceptantes, nombreEvento);
		view.setEmpresasSeleccionadas(seleccionadas);
	}

	private List<EmpresaDTO> getEmpresasSegunFiltro(int idEvento) {
		boolean sinAcceso = view.getFiltroSeleccionado().equals("Empresas sin acceso");
		return model.getEmpresasAptas(idEvento, sinAcceso);
	}

	// ── Seleccion de empresa apta ─────────────────────────────────────────

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

	// ── Conceder acceso ───────────────────────────────────────────────────

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

		String nombreEvento  = view.getNombreEventoSeleccionado();
		String listaEmpresas = seleccionadas.stream()
			.map(EmpresaDTO::getNombre)
			.collect(Collectors.joining(", "));

		String msg = "Vas a conceder acceso al reportaje de:\n\n"
				+ "Evento:   " + nombreEvento + "\n"
				+ "Empresas: " + listaEmpresas + "\n\n"
				+ "¿Confirmas la operacion?";

		if (!view.confirm(msg, "Confirmar acceso")) return;

		List<Integer> ids = seleccionadas.stream()
			.map(EmpresaDTO::getIdEmpresa)
			.collect(Collectors.toList());

		model.concederAcceso(idEvento, ids);

		view.showInfo("Acceso concedido correctamente.");
		view.getFrame().dispose();
	}
}