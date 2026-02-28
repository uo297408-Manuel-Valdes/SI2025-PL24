package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class ConcederAccesoModel {

	private final Database db = new Database();

	
	public List<AgenciaDTO> getAgencias() {
	    String sql = "SELECT id_agencia, nombre FROM AGENCIA_PRENSA ORDER BY nombre";
	    List<Object[]> rows = db.executeQueryArray(sql);
	    List<AgenciaDTO> res = new ArrayList<>();
	    for (Object[] r : rows) {
	        res.add(new AgenciaDTO(((Number) r[0]).intValue(), (String) r[1]));
	    }
	    return res;
	}
	
	
	
	public List<EventoDTO> getEventosCubiertos(int idAgencia) {
		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento " +
			"FROM EVENTO e " +
			"JOIN REPORTAJE r ON r.id_evento = e.id_evento " +
			"WHERE e.id_agencia = ? " +
			"ORDER BY e.fecha_evento, e.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idAgencia);
		List<EventoDTO> res = new ArrayList<>();
		for (Object[] row : rows) {
			res.add(new EventoDTO(
				((Number) row[0]).intValue(),
				((Number) row[1]).intValue(),
				(String)  row[2],
				(String)  row[3]
			));
		}
		return res;
	}

	public List<EmpresaDTO> getEmpresasAceptantesSinAcceso(int idEvento) {
		String sql =
			"SELECT emp.id_empresa, emp.nombre " +
			"FROM EMPRESA emp " +
			"JOIN OFRECER_REPORTAJE ofr " +
			"  ON ofr.id_empresa = emp.id_empresa " +
			"  AND ofr.id_evento  = ? " +
			"  AND ofr.decision   = 'ACEPTADO' " +
			"WHERE NOT EXISTS ( " +
			"  SELECT 1 FROM ACCESO_REPORTAJE acc " +
			"  WHERE acc.id_evento  = ofr.id_evento " +
			"  AND   acc.id_empresa = emp.id_empresa " +
			") " +
			"ORDER BY emp.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] row : rows) {
			res.add(new EmpresaDTO(
				((Number) row[0]).intValue(),
				(String)  row[1]
			));
		}
		return res;
	}


	public void concederAcceso(int idEvento, List<Integer> idsEmpresas) {

		if (idsEmpresas == null || idsEmpresas.isEmpty())
			throw new ApplicationException(
				"Debes seleccionar al menos una empresa para conceder acceso.");

		// Verificar que el evento tiene reportaje
		String checkRep = "SELECT 1 FROM REPORTAJE WHERE id_evento = ? LIMIT 1";
		if (db.executeQueryArray(checkRep, idEvento).isEmpty())
			throw new ApplicationException(
				"El evento seleccionado no tiene ningún reportaje entregado.");

		for (Integer idEmpresa : idsEmpresas) {
			if (idEmpresa == null) continue;

			// Verificar que la empresa aceptó el ofrecimiento
			String checkOfr =
				"SELECT 1 FROM OFRECER_REPORTAJE " +
				"WHERE id_evento = ? AND id_empresa = ? AND decision = 'ACEPTADO' LIMIT 1";
			if (db.executeQueryArray(checkOfr, idEvento, idEmpresa).isEmpty())
				throw new ApplicationException(
					"La empresa con id " + idEmpresa +
					" no tiene un ofrecimiento aceptado para este evento.");

			// Verificar que no tiene ya acceso
			String checkAcc =
				"SELECT 1 FROM ACCESO_REPORTAJE " +
				"WHERE id_evento = ? AND id_empresa = ? LIMIT 1";
			if (!db.executeQueryArray(checkAcc, idEvento, idEmpresa).isEmpty())
				throw new ApplicationException(
					"La empresa con id " + idEmpresa +
					" ya tiene acceso concedido a este reportaje.");
		}

		String insert =
			"INSERT INTO ACCESO_REPORTAJE(id_evento, id_empresa) VALUES (?, ?)";
		for (Integer idEmpresa : idsEmpresas) {
			db.executeUpdate(insert, idEvento, idEmpresa);
		}
	}
}