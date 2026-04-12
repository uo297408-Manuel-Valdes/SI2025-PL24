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
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento, e.finalizada, " +
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
			e.setAsignacionFinalizada(((Number) r[4]).intValue() == 1);
			e.setTematicasTexto((String) r[5]);
			res.add(e);
		}
		return res;
	}

	public boolean isAsignacionFinalizada(int idEvento) {
		String sql = "SELECT finalizada FROM evento WHERE id_evento = ?";
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		if (rows.isEmpty()) {
			throw new IllegalStateException("El evento no existe.");
		}
		return ((Number) rows.get(0)[0]).intValue() == 1;
	}

	public List<ReporteroDTO> getReporterosAsignados(int idEvento) {
		String sql =
			"SELECT r.id_reportero, r.id_agencia, r.nombre, r.tipo_reportero, ar.es_responsable, " +
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
			ReporteroDTO dto = new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[5],
				(String) r[3]
			);
			dto.setResponsable(((Number) r[4]).intValue() == 1);
			res.add(dto);
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
			ReporteroDTO dto = new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[4],
				(String) r[3]
			);
			dto.setResponsable(false);
			res.add(dto);
		}
		return res;
	}

	public boolean esEspecialistaEnEvento(int idReportero, int idEvento) {
		String sql =
			"SELECT COUNT(*) " +
			"FROM reportero_tematica rt " +
			"JOIN evento_tematica et ON et.id_tematica = rt.id_tematica " +
			"WHERE rt.id_reportero = ? " +
			"  AND et.id_evento = ?";

		List<Object[]> rows = db.executeQueryArray(sql, idReportero, idEvento);
		return ((Number) rows.get(0)[0]).intValue() > 0;
	}

	public boolean tieneResponsable(int idEvento) {
		String sql =
			"SELECT COUNT(*) " +
			"FROM asignacion_reportero " +
			"WHERE id_evento = ? AND es_responsable = 1";
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		return ((Number) rows.get(0)[0]).intValue() > 0;
	}

	public boolean tieneReporteroBasico(int idEvento) {
		String sql =
			"SELECT COUNT(*) " +
			"FROM asignacion_reportero ar " +
			"JOIN reportero r ON r.id_reportero = ar.id_reportero " +
			"WHERE ar.id_evento = ? " +
			"  AND r.tipo_reportero = 'Básico'";
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		return ((Number) rows.get(0)[0]).intValue() > 0;
	}

	public void finalizarAsignacion(int idEvento) {
		if (isAsignacionFinalizada(idEvento)) {
			throw new IllegalStateException("La asignación del evento ya está finalizada.");
		}
		if (!tieneResponsable(idEvento)) {
			throw new IllegalStateException("No se puede finalizar la asignación porque no hay un responsable asignado.");
		}
		if (!tieneReporteroBasico(idEvento)) {
			throw new IllegalStateException("No se puede finalizar la asignación porque no hay ningún reportero básico asignado.");
		}

		db.executeUpdate(
			"UPDATE evento SET finalizada = 1 WHERE id_evento = ?",
			idEvento
		);
	}

	public void guardarAsignaciones(int idEvento, List<ReporteroDTO> asignados) {
		if (isAsignacionFinalizada(idEvento)) {
			throw new IllegalStateException("No se puede modificar la asignación porque está finalizada.");
		}

		Integer idResponsable = null;
		Set<Integer> idsYaInsertados = new HashSet<>();

		for (ReporteroDTO r : asignados) {
			if (!idsYaInsertados.add(r.getIdReportero())) {
				throw new IllegalStateException("Hay reporteros repetidos en la lista de asignados.");
			}
			if (r.isResponsable()) {
				if (idResponsable != null) {
					throw new IllegalStateException("Solo puede haber un responsable por evento.");
				}
				idResponsable = r.getIdReportero();
			}
		}

		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_evento = ?", idEvento);

		for (ReporteroDTO r : asignados) {
			db.executeUpdate(
				"INSERT INTO asignacion_reportero (id_evento, id_reportero, es_responsable) VALUES (?, ?, ?)",
				idEvento, r.getIdReportero(), r.isResponsable() ? 1 : 0
			);
		}
	}
}