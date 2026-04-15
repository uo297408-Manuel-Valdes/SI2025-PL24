package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class GestionarOfrecimientosRecibidosModel {

	private final Database db = new Database();

	public List<EmpresaDTO> getEmpresas() {
		String sql =
			"SELECT e.id_empresa, e.nombre, " +
			"       COALESCE(GROUP_CONCAT(t.nombre, ', '), '') AS tematicas " +
			"FROM empresa e " +
			"LEFT JOIN empresa_tematica et ON et.id_empresa = e.id_empresa " +
			"LEFT JOIN tematica t ON t.id_tematica = et.id_tematica " +
			"GROUP BY e.id_empresa, e.nombre " +
			"ORDER BY e.nombre";

		List<Object[]> rows = db.executeQueryArray(sql);

		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new EmpresaDTO(
				((Number) r[0]).intValue(),
				(String) r[1],
				(String) r[2]
			));
		}
		return res;
	}

	public String getTematicasEmpresaTexto(int idEmpresa) {
		String sql =
			"SELECT COALESCE(GROUP_CONCAT(t.nombre, ', '), '') " +
			"FROM empresa_tematica et " +
			"JOIN tematica t ON t.id_tematica = et.id_tematica " +
			"WHERE et.id_empresa = ?";

		List<Object[]> rows = db.executeQueryArray(sql, idEmpresa);
		if (rows.isEmpty() || rows.get(0)[0] == null) {
			return "";
		}
		return (String) rows.get(0)[0];
	}

	public List<String> getTematicasFiltro(int idEmpresa, boolean soloCoincidentes) {
		String sql;

		if (soloCoincidentes) {
			sql =
				"SELECT DISTINCT t.nombre " +
				"FROM tematica t " +
				"JOIN empresa_tematica et ON et.id_tematica = t.id_tematica " +
				"WHERE et.id_empresa = ? " +
				"ORDER BY t.nombre";
			return toStringList(db.executeQueryArray(sql, idEmpresa));
		} else {
			sql =
				"SELECT nombre " +
				"FROM tematica " +
				"ORDER BY nombre";
			return toStringList(db.executeQueryArray(sql));
		}
	}

	public List<OfrecimientoDTO> getOfrecimientosFiltrados(int idEmpresa, boolean soloCoincidentes, String tematicaSeleccionada) {
		StringBuilder sql = new StringBuilder();

		sql.append(
			"SELECT o.id_ofrecimiento, e.id_evento, e.id_agencia, o.id_empresa, " +
			"       e.nombre, e.fecha_inicio, a.nombre, o.decision, " +
			"       COALESCE(( " +
			"           SELECT GROUP_CONCAT(t2.nombre, ', ') " +
			"           FROM evento_tematica et2 " +
			"           JOIN tematica t2 ON t2.id_tematica = et2.id_tematica " +
			"           WHERE et2.id_evento = e.id_evento " +
			"       ), '') AS tematicas " +
			"FROM ofrecer_reportaje o " +
			"JOIN evento e ON e.id_evento = o.id_evento " +
			"JOIN agencia_prensa a ON a.id_agencia = e.id_agencia " +
			"WHERE o.id_empresa = ? "
		);

		List<Object> params = new ArrayList<>();
		params.add(idEmpresa);

		if (soloCoincidentes) {
			sql.append(
				"AND EXISTS ( " +
				"    SELECT 1 " +
				"    FROM evento_tematica evte " +
				"    JOIN empresa_tematica ემ ON ემ.id_tematica = evte.id_tematica " +
				"    WHERE evte.id_evento = e.id_evento " +
				"      AND ემ.id_empresa = ? " +
				") "
			);
			params.add(idEmpresa);
		}

		if (tematicaSeleccionada != null && !tematicaSeleccionada.isBlank()) {
			sql.append(
				"AND EXISTS ( " +
				"    SELECT 1 " +
				"    FROM evento_tematica etf " +
				"    JOIN tematica tf ON tf.id_tematica = etf.id_tematica " +
				"    WHERE etf.id_evento = e.id_evento " +
				"      AND tf.nombre = ? " +
				") "
			);
			params.add(tematicaSeleccionada);
		}

		sql.append("ORDER BY e.fecha_inicio, e.nombre, a.nombre");

		List<Object[]> rows = db.executeQueryArray(sql.toString(), params.toArray());

		List<OfrecimientoDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new OfrecimientoDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				((Number) r[2]).intValue(),
				((Number) r[3]).intValue(),
				(String) r[4],
				(String) r[5],
				(String) r[6],
				(String) r[7],
				(String) r[8]
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
		String sqlCheck = "SELECT decision FROM ofrecer_reportaje WHERE id_ofrecimiento = ?";
		List<Object[]> rows = db.executeQueryArray(sqlCheck, idOfrecimiento);

		if (rows.isEmpty()) {
			throw new ApplicationException("El ofrecimiento ya no existe.");
		}

		Object decisionActual = rows.get(0)[0];
		if (decisionActual != null) {
			throw new ApplicationException("Ya se ha tomado una decisión para este ofrecimiento.");
		}

		String sqlUpd = "UPDATE ofrecer_reportaje SET decision = ? WHERE id_ofrecimiento = ?";
		db.executeUpdate(sqlUpd, decisionNueva, idOfrecimiento);
	}

	private List<String> toStringList(List<Object[]> rows) {
		List<String> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add((String) r[0]);
		}
		return res;
	}
}