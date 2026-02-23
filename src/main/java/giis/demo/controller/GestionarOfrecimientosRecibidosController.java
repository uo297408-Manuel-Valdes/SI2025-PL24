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

	public GestionarOfrecimientosRecibidosController(GestionarOfrecimientosRecibidosModel model,
			GestionarOfrecimientosRecibidosView view) {
		this.model = model;
		this.view = view;
	}

	public void initController() {

		view.addEmpresaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarOfrecimientosPendientes()));
		view.addOfrecimientosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onSeleccionTabla(e)));
		view.addAceptarListener(e -> SwingUtil.exceptionWrapper(() -> onAceptar()));
		view.addRechazarListener(e -> SwingUtil.exceptionWrapper(() -> onRechazar()));

		SwingUtil.exceptionWrapper(() -> {
			view.setEmpresas(model.getEmpresas());
			cargarOfrecimientosPendientes();
		});
	}

	private void cargarOfrecimientosPendientes() {
		EmpresaDTO emp = view.getEmpresaSeleccionada();
		if (emp == null) {
			ofrecimientos = new ArrayList<>();
			view.setOfrecimientos(ofrecimientos);
			view.setDetalle(null, null, null, "Pendiente");
			view.setDecisionButtonsEnabled(false);
			return;
		}

		ofrecimientos = model.getOfrecimientosPendientes(emp.getIdEmpresa());
		view.setOfrecimientos(ofrecimientos);
		view.setDetalle(null, null, null, "Pendiente");
		view.setDecisionButtonsEnabled(false);
	}

	private void onSeleccionTabla(ListSelectionEvent e) {
	    if (e.getValueIsAdjusting()) return;
	    if (ignoreSelectionEvents) return;  // <-- clave

	    Integer id = view.getIdOfrecimientoSeleccionado();
	    if (id == null) {
	        
	        view.setDecisionButtonsEnabled(false);
	        return;
	    }

	    view.setDetalle(
	        view.getEventoSeleccionado(),
	        view.getFechaEventoSeleccionado(),
	        view.getAgenciaSeleccionadaEnTabla(),
	        "Pendiente"
	    );
	    view.setDecisionButtonsEnabled(true);
	}

	private void onAceptar() {
	    Integer id = view.getIdOfrecimientoSeleccionado();
	    if (id == null) { view.showInfo("Selecciona un ofrecimiento."); return; }
	    if (!view.confirm("¿Aceptar el ofrecimiento seleccionado?", "Confirmar")) return;

	    model.aceptar(id);

	    view.setDetalle(
	        view.getEventoSeleccionado(),
	        view.getFechaEventoSeleccionado(),
	        view.getAgenciaSeleccionadaEnTabla(),
	        "ACEPTADO"
	    );

	    ignoreSelectionEvents = true;
	    view.removeFilaSeleccionada();
	    ignoreSelectionEvents = false;

	    view.setDecisionButtonsEnabled(false);
	    view.showInfo("Decisión registrada: ACEPTADO");
	}

	private void onRechazar() {
	    Integer id = view.getIdOfrecimientoSeleccionado();
	    if (id == null) { view.showInfo("Selecciona un ofrecimiento."); return; }
	    if (!view.confirm("¿Rechazar el ofrecimiento seleccionado?", "Confirmar")) return;

	    model.rechazar(id);

	    view.setDetalle(
	        view.getEventoSeleccionado(),
	        view.getFechaEventoSeleccionado(),
	        view.getAgenciaSeleccionadaEnTabla(),
	        "RECHAZADO"
	    );

	    ignoreSelectionEvents = true;
	    view.removeFilaSeleccionada();
	    ignoreSelectionEvents = false;

	    view.setDecisionButtonsEnabled(false);
	    view.showInfo("Decisión registrada: RECHAZADO");
	}
}