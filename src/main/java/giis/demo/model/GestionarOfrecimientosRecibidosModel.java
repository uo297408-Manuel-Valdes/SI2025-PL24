package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class GestionarOfrecimientosRecibidosModel {

	private final Database db = new Database();

	public List<EmpresaDTO> getEmpresas() {
		String sql = "SELECT id_empresa, nombre FROM EMPRESA ORDER BY nombre";
		List<Object[]> rows = db.executeQueryArray(sql);

		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int id = ((Number) r[0]).intValue();
			String nombre = (String) r[1];
			res.add(new EmpresaDTO(id, nombre));
		}
		return res;
	}

	public List<OfrecimientoDTO> getOfrecimientosPendientes(int idEmpresa) {
		String sql =
			"SELECT o.id_ofrecimiento, e.id_evento, e.id_agencia, o.id_empresa, " +
			"       e.nombre, e.fecha_evento, a.nombre, o.decision " +
			"FROM OFRECER_REPORTAJE o " +
			"JOIN EVENTO e ON e.id_evento = o.id_evento " +
			"JOIN AGENCIA_PRENSA a ON a.id_agencia = e.id_agencia " +
			"WHERE o.id_empresa = ? " +
			"  AND o.decision IS NULL " +
			"ORDER BY e.fecha_evento, e.nombre, a.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idEmpresa);

		List<OfrecimientoDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new OfrecimientoDTO(
				((Number) r[0]).intValue(), // id_ofrecimiento
				((Number) r[1]).intValue(), // id_evento
				((Number) r[2]).intValue(), // id_agencia
				((Number) r[3]).intValue(), // id_empresa
				(String) r[4],              // nombre_evento
				(String) r[5],              // fecha_evento
				(String) r[6],              // nombre_agencia
				(String) r[7]               // decision (null)
			));
		}
		return res;
	}

	public void aceptar(int idOfrecimiento) {
		decidir(idOfrecimiento, "ACEPTADO");
	}

	public void rechazar(int idOfrecimiento) {
		decidir(idOfrecimiento, "RECHAZADO");
	}


	private void decidir(int idOfrecimiento, String decisionNueva) {
		String sqlCheck = "SELECT decision FROM OFRECER_REPORTAJE WHERE id_ofrecimiento = ?";
		List<Object[]> rows = db.executeQueryArray(sqlCheck, idOfrecimiento);

		if (rows.isEmpty()) {
			throw new ApplicationException("El ofrecimiento ya no existe.");
		}

		Object decisionActual = rows.get(0)[0];
		if (decisionActual != null) {
			throw new ApplicationException("Ya se ha tomado una decisión para este ofrecimiento.");
		}

		String sqlUpd = "UPDATE OFRECER_REPORTAJE SET decision = ? WHERE id_ofrecimiento = ?";
		db.executeUpdate(sqlUpd, decisionNueva, idOfrecimiento);
	}
}