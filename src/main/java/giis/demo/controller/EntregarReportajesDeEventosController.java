package giis.demo.controller;


import java.util.List;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.EntregarReportajesDeEventosModel;
import giis.demo.model.EventoDTO;

import giis.demo.model.ReportajeDTO;
import giis.demo.util.SwingUtil;
import giis.demo.view.EntregarReportajesDeEventosView;

public class EntregarReportajesDeEventosController {

    private final EntregarReportajesDeEventosModel model;
    private final EntregarReportajesDeEventosView view;

    /** Id del reportero que está usando la interfaz */
    private final int idReportero;

    private List<EventoDTO> eventos;
    private EventoDTO eventoSeleccionado;

    public EntregarReportajesDeEventosController(EntregarReportajesDeEventosModel model,
    		EntregarReportajesDeEventosView view,
                                        int idReportero) {
        this.model      = model;
        this.view       = view;
        this.idReportero = idReportero;
    }

    public void initController() {
        view.addEventosSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onEventoSeleccionado(e)));
        view.addValidarTituloListener(e -> SwingUtil.exceptionWrapper(() -> onValidarTitulo()));
        view.addEntregarListener(e -> SwingUtil.exceptionWrapper(() -> onEntregar()));

        SwingUtil.exceptionWrapper(() -> cargarEventos());

        view.setVisible(true);
    }

    // ── Carga inicial ────────────────────────────────────────────────────

    private void cargarEventos() {
        eventos = model.getEventosAsignadosAReportero(idReportero);
        view.setEventos(eventos);
        view.limpiarFormulario();
    }

    // ── Selección de evento ──────────────────────────────────────────────

    private void onEventoSeleccionado(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        //int fila = view.getFilaEventoSeleccionada();
        int fila=1;
        if (fila < 0 || fila >= eventos.size()) {
            eventoSeleccionado = null;
            view.limpiarFormulario();
            return;
        }

        eventoSeleccionado = eventos.get(fila);
        view.setLabelEventoSeleccionado(eventoSeleccionado.getNombre());

        // Cargar reportaje existente si lo hay
        ReportajeDTO reportaje = model.getReportaje(eventoSeleccionado.getIdEvento(), idReportero);
        if (reportaje != null) {
            view.setAutor(reportaje.getAutor());
            view.setTitulo(reportaje.getTitulo());
            view.setSubtitulo(reportaje.getSubtitulo());
            view.setCuerpo(reportaje.getCuerpo());
        } else {
            view.setAutor("");
            view.setTitulo("");
            view.setSubtitulo("");
            view.setCuerpo("");
        }
    }

    // ── Validar título ───────────────────────────────────────────────────

    private void onValidarTitulo() {
        try {
            model.validarTitulo(view.getTitulo());
            view.showInfo("El título es válido.");
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    // ── Entregar reportaje ───────────────────────────────────────────────

    private void onEntregar() {
        if (eventoSeleccionado == null) {
            view.showInfo("Selecciona un evento primero.");
            return;
        }

        String autor     = view.getAutor();
        String titulo    = view.getTitulo();
        String subtitulo = view.getSubtitulo();
        String cuerpo    = view.getCuerpo();

        String msg = "Vas a entregar el reportaje:\n\n"
                + "Evento:    " + eventoSeleccionado.getNombre()
                + " (" + eventoSeleccionado.getFechaEvento() + ")\n"
                + "Autor:     " + autor + "\n"
                + "Título:    " + titulo + "\n\n"
                + "¿Confirmas la entrega?";

        if (!view.confirm(msg, "Confirmar entrega")) return;

        model.entregarReportaje(
            eventoSeleccionado.getIdEvento(), idReportero,
            autor, titulo, subtitulo, cuerpo
        );

        view.showInfo("Reportaje entregado correctamente.");
        view.getFrame().dispose();
    }
}






