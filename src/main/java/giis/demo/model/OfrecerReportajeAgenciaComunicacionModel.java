package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

import java.time.LocalDate;

public class OfrecerReportajeAgenciaComunicacionModel {
	
	private final Database db = new Database();

	public List<AgenciaDTO> getAgencias() {
		String sql = "SELECT id_agencia, nombre FROM AGENCIA_PRENSA ORDER BY nombre";
		List<Object[]> rows = db.executeQueryArray(sql);
		List<AgenciaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int id = ((Number) r[0]).intValue();
			String nombre = (String) r[1];
			res.add(new AgenciaDTO(id, nombre));
		}
		return res;
	}

	public List<EventoDTO> getEventos(int idAgencia) {
		String sql =
				"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_inicio, e.finalizada " +
				"FROM EVENTO e " +
				"WHERE e.id_agencia = ? " +
				"AND EXISTS (SELECT 1 FROM ASIGNACION_REPORTERO ar WHERE ar.id_evento = e.id_evento) " +
				"ORDER BY e.fecha_inicio, e.nombre";

			List<Object[]> rows = db.executeQueryArray(sql, idAgencia);

			List<EventoDTO> res = new ArrayList<>();
			for (Object[] r : rows) {
				int idEvento = ((Number) r[0]).intValue();
				int idAg = ((Number) r[1]).intValue();
				String nombre = (String) r[2];
				String fecha = (String) r[3];
				int finalizada = ((Number) r[4]).intValue();
				res.add(new EventoDTO(idEvento, idAg, nombre, fecha, finalizada));
			}
		return res;
	}
	
	public ReportajeDTO getReportaje(int idEvento){
		String sql =
				"SELECT r.id_reportaje, r.id_evento, r.titulo, r.id_reportero_entrega, r.fecha_embargo "+
				"FROM REPORTAJE r "+
				"WHERE r.id_evento=? "+
				"LIMIT 1";
		
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		
		ReportajeDTO res=null;
		
		for (Object[] r : rows) {
			int idReportaje = ((Number) r[0]).intValue();
			int idEv = ((Number) r[1]).intValue();
			String titulo = (String) r[2];
			int idReporteroEntrega = ((Number) r[3]).intValue();
			String fecha = (String) r[4];
			res=new ReportajeDTO(idReportaje, idEv, titulo, idReporteroEntrega,fecha);
		}
		return res;
	}

	public List<EmpresaDTO> getEmpresasSinOfrecimiento(int idEvento) {
		String sql =
				"SELECT e.id_empresa, e.nombre "+
				"FROM EMPRESA e "+
				"WHERE NOT EXISTS ( "+
						"SELECT 1 "+
						"FROM OFRECER_REPORTAJE o " +
						"WHERE o.id_empresa = e.id_empresa " +
						"AND o.id_evento = ? " +
						")";


		List<Object[]> rows = db.executeQueryArray(sql, idEvento);

		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int idEm = ((Number) r[0]).intValue();
			String nombre = (String) r[1];
			res.add(new EmpresaDTO(idEm, nombre));
		}
		return res;
	}
	
	public List<EmpresaDTO> getEmpresasConOfrecimiento(Integer idEvento) {
		String sql =
				"SELECT e.id_empresa, e.nombre "+
				"FROM EMPRESA e "+
				"WHERE EXISTS ( "+
						"SELECT 1 "+
						"FROM OFRECER_REPORTAJE o " +
						"WHERE o.id_empresa = e.id_empresa " +
						"AND o.id_evento = ? " +
						")";


		List<Object[]> rows = db.executeQueryArray(sql, idEvento);

		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int idEm = ((Number) r[0]).intValue();
			String nombre = (String) r[1];
			res.add(new EmpresaDTO(idEm, nombre));
		}
		return res;
	}

	public OfrecimientoDTO getOfrecimiento(int idEmpresa, int idEvento) {
		String sql=
				"SELECT o.id_ofrecimiento, o.id_evento, o.id_empresa, o.decision "+
				"FROM OFRECER_REPORTAJE o "+
				"WHERE o.id_evento=? AND o.id_empresa=?";
		
		List<Object[]> rows = db.executeQueryArray(sql, idEvento, idEmpresa);

		OfrecimientoDTO res=null;
		for (Object[] r : rows) {
			int idOf = ((Number) r[0]).intValue();
			int idEv = ((Number) r[1]).intValue();
			int idEm = ((Number) r[2]).intValue();
			String decision = (String) r[3];
			res=new OfrecimientoDTO(idOf,idEv,idEm,decision);
		}
		return res;
	}
	
	public List<String> getTematicaEvento(Integer idEvento) {
		String sql=
				"Select t.id_tematica, t.nombre "+
				"FROM TEMATICA t "+
				"WHERE EXISTS( "+
					"SELECT 1 " +
					"FROM EVENTO_TEMATICA et "+
					"WHERE et.id_tematica=t.id_tematica "+
					"AND et.id_evento=? " +
					")";
		
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		
		List<String> res = new ArrayList<>();
		for(Object[] r : rows) {
			String nombre = (String) r[1];
			res.add(nombre);
		}
		
		return res;
	}
	
	public List<String> getTematicaEmpresa(Integer idEmpresa) {
		String sql=
				"Select t.id_tematica, t.nombre "+
				"FROM TEMATICA t "+
				"WHERE EXISTS( "+
					"SELECT 1 " +
					"FROM EMPRESA_TEMATICA et "+
					"WHERE et.id_tematica=t.id_tematica "+
					"AND et.id_empresa=? " +
					")";
		
		List<Object[]> rows = db.executeQueryArray(sql, idEmpresa);
		
		List<String> res = new ArrayList<>();
		for(Object[] r : rows) {
			String nombre = (String) r[1];
			res.add(nombre);
		}
		
		return res;
	}
	
	public int buscarTarifa(int idAgencia, int idEmpresa) {
		String sql=
				"Select t.pendiente "+
				"FROM TARIFA t "+
				"WHERE t.id_agencia=? "+
				"AND t.id_empresa=? "+
				"LIMIT 1 ";
		List<Object[]> rows = db.executeQueryArray(sql, idAgencia, idEmpresa);
		
		int res=-1;
		
		for (Object[] r : rows) {
			res = ((Number) r[0]).intValue();
		}
		return res;
	}
	
	public void ofrecerEmpresa(int idEvento, List<Integer> idsEmpresas, int idAgencia) {

		if (idsEmpresas == null || idsEmpresas.isEmpty())
			throw new ApplicationException("Debes asignar al menos una empresa.");

		

		if (!eventoTieneAsignaciones(idEvento)) {
			throw new ApplicationException("No se puede asignar: el evento no tiene reporteros asignados.");
		}

		for (Integer idEmpresa : idsEmpresas) {
			if (idEmpresa == null) continue;

			if (empresaOfrecidoReportaje(idEmpresa, idEvento)) {
				throw new ApplicationException("Esta empresa ya tiene ofrecido este reportaje.");
			}
		}
		
		for(int i=0;i<idsEmpresas.size();i++) {
			int pend=buscarTarifa(idAgencia,idsEmpresas.get(i));
			if(pend==1) {
				throw new ApplicationException("No se puede ofrecer este evento porque la empresa de comunicación esta pendiente de pagos.");
			}
		}
		
		ReportajeDTO reportaje=getReportaje(idEvento);
		EmpresaDTO aux=null;
		
		for(int i=0; i<idsEmpresas.size(); i++) {
			aux=getEmpresaById(idsEmpresas.get(i));
			if(reportaje.getFecha_embargo()!=null && aux.getEmbargos()==0) {
				LocalDate fecha_embargo=LocalDate.parse(reportaje.getFecha_embargo());
				LocalDate fecha_hoy=LocalDate.now();
				if(fecha_embargo.isAfter(fecha_hoy)) {
					throw new ApplicationException("No se puede ofrecer este evento porque una o varias empresas seleccionadas no les interesan las reportajes con embargos.");
				}
			}
		}

		String insert = "INSERT INTO OFRECER_REPORTAJE(id_evento, id_empresa) VALUES (?, ?)";
		for (Integer idEmpresa : idsEmpresas) {
			db.executeUpdate(insert, idEvento, idEmpresa);
		}
	}
	
	
	public void quitarOfrecimiento(int idEvento, List<Integer> idsEmpresas){
		
		if (idsEmpresas == null || idsEmpresas.isEmpty())
			throw new ApplicationException("Debes seleccionar al menos una empresa.");
		
		if (!eventoTieneAsignaciones(idEvento)) {
			throw new ApplicationException("No se puede continuar: el evento no tiene reporteros asignados.");
		}
		
		for (Integer idEmpresa : idsEmpresas) {
			if (idEmpresa == null) continue;

			if (!empresaOfrecidoReportaje(idEmpresa, idEvento)) {
				throw new ApplicationException("Esta empresa no tiene ofrecido este reportaje.");
			}
			
			if(empresaAccesoReportaje(idEmpresa, idEvento)) {
				throw new ApplicationException("Esta empresa ya tiene acceso a este reportaje.");
			}
			
		}
		
		String delete = "DELETE FROM OFRECER_REPORTAJE WHERE id_evento = ? AND id_empresa=?";
		for (Integer idEmpresa : idsEmpresas) {
			db.executeUpdate(delete, idEvento, idEmpresa);
		}
		
	}
	
	private boolean eventoTieneAsignaciones(int idEvento) {
		String sql = "SELECT 1 FROM ASIGNACION_REPORTERO WHERE id_evento = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEvento).isEmpty();
	}

	private boolean empresaOfrecidoReportaje(int idEmpresa, int idEvento) {
		String sql = "SELECT 1 FROM OFRECER_REPORTAJE WHERE id_empresa = ? AND id_evento = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEmpresa, idEvento).isEmpty();
	}
	
	private boolean empresaAccesoReportaje(int idEmpresa, int idEvento) {
		String sql = "SELECT 1 FROM ACCESO_REPORTAJE WHERE id_empresa = ? AND id_evento = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEmpresa, idEvento).isEmpty();
	}
	
	private EmpresaDTO getEmpresaById(int idEmpresa) {
		String sql=
				"SELECT e.id_empresa, e.nombre, e.embargos "+
				"FROM EMPRESA e "+
				"WHERE e.id_empresa=? "+
				"LIMIT 1";
		
		List<Object[]> rows = db.executeQueryArray(sql, idEmpresa);

		EmpresaDTO res = null;
		for (Object[] r : rows) {
			int idEm = ((Number) r[0]).intValue();
			String nombre = (String) r[1];
			int embargos = ((Number) r[2]).intValue();
			res=new EmpresaDTO(idEm, nombre, embargos);
		}
		return res;
	}

	
}