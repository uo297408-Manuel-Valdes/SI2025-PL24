package giis.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.EmpresaDTO;
import giis.demo.model.GestionarOfrecimientosRecibidosModel;
import giis.demo.model.OfrecimientoDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.GestionarOfrecimientosRecibidosView;

public class GestionarOfrecimientosRecibidosController {

	private final GestionarOfrecimientosRecibidosModel model;
	private final GestionarOfrecimientosRecibidosView view;

	private List<OfrecimientoDTO> ofrecimientos = new ArrayList<>();
	private boolean ignoreSelectionEvents = false;

	public GestionarOfrecimientosRecibidosController(
			GestionarOfrecimientosRecibidosModel model,
			GestionarOfrecimientosRecibidosView view) {
		this.model = model;
		this.view = view;
	}

	public void initController() {
		view.addEmpresaChangedListener(e -> SwingUtil.exceptionWrapper(() -> onEmpresaChanged()));
		view.addFiltroCoincidentesChangedListener(e -> SwingUtil.exceptionWrapper(() -> onFiltrosChanged()));
		view.addTematicaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarOfrecimientos()));
		view.addOfrecimientosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onSeleccionTabla(e)));
		view.addAceptarListener(e -> SwingUtil.exceptionWrapper(() -> onAceptar()));
		view.addRechazarListener(e -> SwingUtil.exceptionWrapper(() -> onRechazar()));

		SwingUtil.exceptionWrapper(() -> {
			view.setEmpresas(model.getEmpresas());
			onEmpresaChanged();
		});
	}

	private void onEmpresaChanged() {
		EmpresaDTO emp = view.getEmpresaSeleccionada();
		if (emp == null) {
			view.setEspecialidadesEmpresa("-");
			view.setTematicas(new ArrayList<>());
			view.setOfrecimientos(new ArrayList<>());
			view.clearDetalle();
			view.setDecisionButtonsEnabled(false);
			return;
		}

		view.setEspecialidadesEmpresa(emp.getTematicasTexto());
		recargarComboTematicas();
		cargarOfrecimientos();
	}

	private void onFiltrosChanged() {
		recargarComboTematicas();
		cargarOfrecimientos();
	}

	private void recargarComboTematicas() {
		EmpresaDTO emp = view.getEmpresaSeleccionada();
		if (emp == null) {
			view.setTematicas(new ArrayList<>());
			return;
		}

		boolean soloCoincidentes = view.isFiltroCoincidentesActivo();
		List<String> tematicas = model.getTematicasFiltro(emp.getIdEmpresa(), soloCoincidentes);
		view.setTematicas(tematicas);
	}

	private void cargarOfrecimientos() {
		EmpresaDTO emp = view.getEmpresaSeleccionada();
		if (emp == null) {
			ofrecimientos = new ArrayList<>();
			view.setOfrecimientos(ofrecimientos);
			view.clearDetalle();
			view.setDecisionButtonsEnabled(false);
			return;
		}

		ofrecimientos = model.getOfrecimientosFiltrados(
			emp.getIdEmpresa(),
			view.isFiltroCoincidentesActivo(),
			view.getTematicaSeleccionada()
		);

		ignoreSelectionEvents = true;
		view.setOfrecimientos(ofrecimientos);
		ignoreSelectionEvents = false;

		view.clearDetalle();
		view.setDecisionButtonsEnabled(false);
	}

	private void onSeleccionTabla(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		if (ignoreSelectionEvents) return;

		Integer id = view.getIdOfrecimientoSeleccionado();
		if (id == null) {
			view.clearDetalle();
			view.setDecisionButtonsEnabled(false);
			return;
		}

		OfrecimientoDTO sel = getOfrecimientoSeleccionado(id);
		if (sel == null) {
			view.clearDetalle();
			view.setDecisionButtonsEnabled(false);
			return;
		}

		String decision = sel.getDecision() == null ? "Pendiente" : sel.getDecision();

		view.setDetalle(
			sel.getNombreEvento(),
			sel.getFechaEvento(),
			sel.getNombreAgencia(),
			decision,
			sel.getTematicasTexto()
		);

		view.setDecisionButtonsEnabled(sel.getDecision() == null);
	}

	private OfrecimientoDTO getOfrecimientoSeleccionado(Integer id) {
		for (OfrecimientoDTO o : ofrecimientos) {
			if (o.getIdOfrecimiento() == id) {
				return o;
			}
		}
		return null;
	}

	private void onAceptar() {
		Integer id = view.getIdOfrecimientoSeleccionado();
		if (id == null) {
			view.showInfo("Selecciona un ofrecimiento.");
			return;
		}
		if (!view.confirm("¿Aceptar el ofrecimiento seleccionado?", "Confirmar")) {
			return;
		}

		model.aceptar(id);
		view.showInfo("Decisión registrada: ACEPTADO");
		cargarOfrecimientos();
	}

	private void onRechazar() {
		Integer id = view.getIdOfrecimientoSeleccionado();
		if (id == null) {
			view.showInfo("Selecciona un ofrecimiento.");
			return;
		}
		if (!view.confirm("¿Rechazar el ofrecimiento seleccionado?", "Confirmar")) {
			return;
		}

		model.rechazar(id);
		view.showInfo("Decisión registrada: RECHAZADO");
		cargarOfrecimientos();
	}
}