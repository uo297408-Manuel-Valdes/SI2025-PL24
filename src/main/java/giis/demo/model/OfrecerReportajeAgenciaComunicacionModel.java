package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

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

	public List<EventoDTO> getReportajes(int idAgencia) {
		String sql =
				"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento " +
				"FROM EVENTO e " +
				"WHERE e.id_agencia = ? " +
				"AND EXISTS (SELECT 1 FROM ASIGNACION_REPORTERO ar WHERE ar.id_evento = e.id_evento) " +
				"ORDER BY e.fecha_evento, e.nombre";

			List<Object[]> rows = db.executeQueryArray(sql, idAgencia);

			List<EventoDTO> res = new ArrayList<>();
			for (Object[] r : rows) {
				int idEvento = ((Number) r[0]).intValue();
				int idAg = ((Number) r[1]).intValue();
				String nombre = (String) r[2];
				String fecha = (String) r[3];
				res.add(new EventoDTO(idEvento, idAg, nombre, fecha));
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
	
	public void ofrecerEmpresa(int idEvento, List<Integer> idsEmpresas) {

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


	
}
