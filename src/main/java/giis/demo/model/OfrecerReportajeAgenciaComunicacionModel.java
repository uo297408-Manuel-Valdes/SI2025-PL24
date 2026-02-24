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

	public List<EmpresaDTO> getEmpresasDisponibles(int idEvento) {
		String sql =
			"SELECT r.id_empresa, r.nombre " +
			"FROM EMPRESA r " +
			"WHERE NOT EXISTS ( " +
			"  SELECT 1 " +
			"  FROM OFRECER_REPORTAJE ar " +
			"  WHERE ar.id_empresa = r.id_empresa " +
			"  AND ar.id_evento=?"+
			") " +
			"ORDER BY r.nombre";

		List<Object[]> rows = db.executeQueryArray(sql);

		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int idEm = ((Number) r[0]).intValue();
			String nombre = (String) r[1];
			res.add(new EmpresaDTO(idEm, nombre));
		}
		return res;
	}
	
	public void ofrecerEmpresa(int idEvento, List<Integer> idsEmpresas) {

		if (idsEmpresas == null || idsEmpresas.isEmpty())
			throw new ApplicationException("Debes asignar al menos un reportero.");

		

		if (!eventoTieneAsignaciones(idEvento)) {
			throw new ApplicationException("No se puede asignar: el evento no tiene reporteros asignados.");
		}

		for (Integer idEmpresa : idsEmpresas) {
			if (idEmpresa == null) continue;

			if (empresaAccesoReportaje(idEmpresa, idEvento)) {
				throw new ApplicationException("Esta empresa ya tiene ofrecido este reportaje.");
			}
		}

		String insert = "INSERT INTO OFRECER_REPORTAJE(id_evento, id_empresa) VALUES (?, ?)";
		for (Integer idEmpresa : idsEmpresas) {
			db.executeUpdate(insert, idEvento, idEmpresa);
		}
	}
	
	private boolean eventoTieneAsignaciones(int idEvento) {
		String sql = "SELECT 1 FROM ASIGNACION_REPORTERO WHERE id_evento = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEvento).isEmpty();
	}

	private boolean empresaAccesoReportaje(int idEmpresa, int idEvento) {
		String sql = "SELECT 1 FROM OFRECER_REPORTAJE WHERE id_empresa = ? AND id_evento = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEmpresa, idEvento).isEmpty();
	}


	
}
