package giis.demo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import giis.demo.util.Database;

public class AsignarReporterosAEventosModel {

	private Database db = new Database();

	public List<AgenciaDTO> getAgencias() {
		String sql = "SELECT id_agencia, nombre FROM agencia_prensa ORDER BY nombre";
		List<Object[]> rows = db.executeQueryArray(sql);

		List<AgenciaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new AgenciaDTO(
				((Number) r[0]).intValue(),
				(String) r[1]
			));
		}
		return res;
	}

	public List<EventoDTO> getEventos(int idAgencia, int filtroEventos) {
		StringBuilder sql = new StringBuilder();
		sql.append(
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento, " +
			"       COALESCE(( " +
			"           SELECT GROUP_CONCAT(t.nombre, ', ') " +
			"           FROM evento_tematica et " +
			"           JOIN tematica t ON t.id_tematica = et.id_tematica " +
			"           WHERE et.id_evento = e.id_evento " +
			"       ), '') AS tematicas " +
			"FROM evento e " +
			"WHERE e.id_agencia = ? "
		);

		if (filtroEventos == 0) {
			sql.append(
				"AND NOT EXISTS ( " +
				"  SELECT 1 FROM asignacion_reportero ar " +
				"  WHERE ar.id_evento = e.id_evento " +
				") "
			);
		} else {
			sql.append(
				"AND EXISTS ( " +
				"  SELECT 1 FROM asignacion_reportero ar " +
				"  WHERE ar.id_evento = e.id_evento " +
				") "
			);
		}

		sql.append("ORDER BY e.fecha_evento, e.nombre");

		List<Object[]> rows = db.executeQueryArray(sql.toString(), idAgencia);

		List<EventoDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			EventoDTO e = new EventoDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[3]
			);
			e.setTematicasTexto((String) r[4]);
			res.add(e);
		}
		return res;
	}

	public List<ReporteroDTO> getReporterosAsignados(int idEvento) {
		String sql =
			"SELECT r.id_reportero, r.id_agencia, r.nombre, r.tipo_reportero, " +
			"       COALESCE(( " +
			"           SELECT GROUP_CONCAT(t.nombre, ', ') " +
			"           FROM reportero_tematica rt " +
			"           JOIN tematica t ON t.id_tematica = rt.id_tematica " +
			"           WHERE rt.id_reportero = r.id_reportero " +
			"       ), '') AS tematicas " +
			"FROM asignacion_reportero ar " +
			"JOIN reportero r ON r.id_reportero = ar.id_reportero " +
			"WHERE ar.id_evento = ? " +
			"ORDER BY r.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idEvento);

		List<ReporteroDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[4],
				(String) r[3]
			));
		}
		return res;
	}

	public List<ReporteroDTO> getReporterosDisponibles(int idAgencia, int idEvento,
			boolean soloEspecialistas,
			boolean tipoBasico,
			boolean tipoGrafico,
			boolean tipoCamarografo) {

		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		sql.append(
			"SELECT r.id_reportero, r.id_agencia, r.nombre, r.tipo_reportero, " +
			"       COALESCE(( " +
			"           SELECT GROUP_CONCAT(t.nombre, ', ') " +
			"           FROM reportero_tematica rt2 " +
			"           JOIN tematica t ON t.id_tematica = rt2.id_tematica " +
			"           WHERE rt2.id_reportero = r.id_reportero " +
			"       ), '') AS tematicas " +
			"FROM reportero r " +
			"WHERE r.id_agencia = ? "
		);
		params.add(idAgencia);

		sql.append(
			"AND r.id_reportero NOT IN ( " +
			"   SELECT ar.id_reportero " +
			"   FROM asignacion_reportero ar " +
			"   WHERE ar.id_evento = ? " +
			") "
		);
		params.add(idEvento);

		if (soloEspecialistas) {
			sql.append(
				"AND EXISTS ( " +
				"   SELECT 1 " +
				"   FROM reportero_tematica rt " +
				"   JOIN evento_tematica et ON et.id_tematica = rt.id_tematica " +
				"   WHERE rt.id_reportero = r.id_reportero " +
				"     AND et.id_evento = ? " +
				") "
			);
			params.add(idEvento);
		}

		List<String> tipos = new ArrayList<>();
		if (tipoBasico) tipos.add("Básico");
		if (tipoGrafico) tipos.add("Gráfico");
		if (tipoCamarografo) tipos.add("Camarógrafo");

		if (!tipos.isEmpty()) {
			sql.append("AND r.tipo_reportero IN (");
			for (int i = 0; i < tipos.size(); i++) {
				if (i > 0) sql.append(", ");
				sql.append("?");
				params.add(tipos.get(i));
			}
			sql.append(") ");
		}

		sql.append("ORDER BY r.nombre");

		List<Object[]> rows = db.executeQueryArray(sql.toString(), params.toArray());

		List<ReporteroDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[4],
				(String) r[3]
			));
		}
		return res;
	}

	public void guardarAsignaciones(int idEvento, List<ReporteroDTO> asignados) {
		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_evento = ?", idEvento);

		Set<Integer> idsYaInsertados = new HashSet<>();
		for (ReporteroDTO r : asignados) {
			if (idsYaInsertados.add(r.getIdReportero())) {
				db.executeUpdate(
					"INSERT INTO asignacion_reportero (id_evento, id_reportero) VALUES (?, ?)",
					idEvento, r.getIdReportero()
				);
			}
		}
	}
}