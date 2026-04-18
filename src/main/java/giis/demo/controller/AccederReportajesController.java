package giis.demo.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ListSelectionEvent;

import giis.demo.model.AccederReportajesModel;
import giis.demo.model.AgenciaDTO;
import giis.demo.model.EmpresaDTO;
import giis.demo.model.EventoDTO;
import giis.demo.model.MultimediaDTO;
import giis.demo.model.ReportajeDTO;
import giis.demo.model.VersionDTO;
import giis.demo.util.ApplicationException;
import giis.demo.util.SwingUtil;
import giis.demo.view.AccederReportajesView;

public class AccederReportajesController {

	private final AccederReportajesModel model;
	private final AccederReportajesView view;
	
	private List<EventoDTO> eventos = new ArrayList<>();
	
	private ReportajeDTO reportaje;
	private VersionDTO version;
	List<MultimediaDTO> multimedia;
	
	public AccederReportajesController(AccederReportajesModel model, AccederReportajesView view) {
		this.model=model;
		this.view=view;
	}
	
	public void initController() {

		view.addEmpresaChangedListener(e -> SwingUtil.exceptionWrapper(() -> cargarReportajes()));
		view.addReportajesSelectionListener(e -> SwingUtil.exceptionWrapper(() -> onReportajeSeleccionado(e)));
		view.addFinalizarListener(e -> SwingUtil.exceptionWrapper(() -> finalizar()));
		view.addDescargarListener(e -> SwingUtil.exceptionWrapper(() -> descargar()));

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
		multimedia=model.getMultimedia(id);
		
		if (multimedia == null) {
			view.setMultimedia(new ArrayList<>());
			return;
		}
		view.setMultimedia(multimedia);
		
	}
	
	private void onReportajeSeleccionado(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		
		view.vaciarMultimedia();
		
		Integer idEvento = view.getIdReportajeSeleccionado();
		EmpresaDTO empresa = view.getEmpresaSeleccionada();
		
		if (idEvento == null || empresa == null) {
			view.setInfo(null, null, null);
			return;
		}
		
		int especial=model.getAccesoEspecial(idEvento, empresa.getIdEmpresa());
		String aux="Información no disponible hasta fin de embargo (";
		LocalDate fecha_hoy=LocalDate.now();
		LocalDate fecha_embargo=fecha_hoy;
		int embargo=0;
		reportaje=model.getInfoReportaje(idEvento);
		
		if (reportaje == null) {
			view.setInfo(null, null, null);
			return;
		}
		
		if(reportaje.getFecha_embargo()!=null) {
			aux+=reportaje.getFecha_embargo()+")";
			fecha_embargo=LocalDate.parse(reportaje.getFecha_embargo());
			embargo=1;
		}
		
		if(embargo==1 && fecha_embargo.isAfter(fecha_hoy)) view.setMultimediaEmbargo(aux);
		else cargarMultimedia(reportaje.getIdReportaje());
		
		version=model.getVersion(reportaje.getIdReportaje());
		
		if (version == null) {
			if(embargo==1 && fecha_embargo.isAfter(fecha_hoy) && especial==0) view.setInfo(aux, null, null);
			else view.setInfo(reportaje.getTitulo(), null, null);
			return;
		}
		
		if(embargo==1 && fecha_embargo.isAfter(fecha_hoy)&& especial==0) view.setInfo(aux, aux, aux);
		else view.setInfo(reportaje.getTitulo(), version.getSubtitulo(), version.getCuerpo());
	}
	
	private void finalizar() {
		view.getFrame().dispose();
	}

	private void descargar() {
		if(reportaje==null) {
			view.showInfo("Selecciona un evento.");
			return;
		}
		
		if(reportaje.getFecha_embargo()!=null) {
			LocalDate fecha_hoy=LocalDate.now();
			LocalDate fecha_embargo=LocalDate.parse(reportaje.getFecha_embargo());
			if(fecha_embargo.isAfter(fecha_hoy)) {
				view.showInfo("Este reportaje no puede ser descargado hasta fin de embargo ("+reportaje.getFecha_embargo()+")");
			}
			return;
		}
		try {

			String userHome = System.getProperty("user.home");
            Path rutaDescargas = Paths.get(userHome, "Downloads");

            Path rutaArchivo = rutaDescargas.resolve(reportaje.getTitulo() + ".json");
            
            Map<String, String> datos=new LinkedHashMap<>();
            datos.put("Titulo: ", reportaje.getTitulo());
            
            if(version!=null) {
            	if(version.getSubtitulo()!=null) {
            		datos.put("Subtitulo: ", version.getSubtitulo());
            	}
            	
            	if(version.getCuerpo()!=null) {
            		datos.put("Cuerpo: ", version.getCuerpo());
            	}
            }
			
            if(multimedia!=null) {
            	datos.put("Ruta ", "Tipo");
            	for(int i=0; i<multimedia.size(); i++) {
            		datos.put(multimedia.get(i).getPath(),multimedia.get(i).getTipo());
            	}
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(datos);

            FileWriter writer = new FileWriter(rutaArchivo.toFile());
            writer.write(json);
            writer.close();

            view.showInfo("Archivo guardado en: "+ rutaArchivo);
            
            EmpresaDTO empresa = view.getEmpresaSeleccionada();
            model.descargar(empresa.getIdEmpresa(), reportaje.getIdEvento());
            
		} catch (IOException e) {
			view.showInfo("Ocurrio un error durante la descarga.");
           
		}
	}
	
}
