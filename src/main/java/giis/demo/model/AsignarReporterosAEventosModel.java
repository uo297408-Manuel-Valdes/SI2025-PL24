package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

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
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_inicio, e.fecha_fin, e.finalizada, " +
			"       pr.nombre AS provincia, pa.nombre AS pais, " +
			"       COALESCE(( " +
			"           SELECT GROUP_CONCAT(t.nombre, ', ') " +
			"           FROM evento_tematica et " +
			"           JOIN tematica t ON t.id_tematica = et.id_tematica " +
			"           WHERE et.id_evento = e.id_evento " +
			"       ), '') AS tematicas " +
			"FROM evento e " +
			"JOIN provincia pr ON pr.id_provincia = e.id_provincia " +
			"JOIN pais pa ON pa.id_pais = pr.id_pais " +
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

		sql.append("ORDER BY e.fecha_inicio, e.nombre");

		List<Object[]> rows = db.executeQueryArray(sql.toString(), idAgencia);

		List<EventoDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			EventoDTO e = new EventoDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[3],
				(String) r[4],
				(String) r[6],
				(String) r[7]
			);
			e.setAsignacionFinalizada(((Number) r[5]).intValue() == 1);
			e.setTematicasTexto((String) r[8]);
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
			"       pr.nombre AS provincia, pa.nombre AS pais, " +
			"       COALESCE(( " +
			"           SELECT GROUP_CONCAT(t.nombre, ', ') " +
			"           FROM reportero_tematica rt " +
			"           JOIN tematica t ON t.id_tematica = rt.id_tematica " +
			"           WHERE rt.id_reportero = r.id_reportero " +
			"       ), '') AS tematicas " +
			"FROM asignacion_reportero ar " +
			"JOIN reportero r ON r.id_reportero = ar.id_reportero " +
			"JOIN provincia pr ON pr.id_provincia = r.id_provincia " +
			"JOIN pais pa ON pa.id_pais = pr.id_pais " +
			"WHERE ar.id_evento = ? " +
			"ORDER BY r.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idEvento);

		List<ReporteroDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			ReporteroDTO dto = new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[7],
				(String) r[3],
				(String) r[5],
				(String) r[6]
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
			"       pr.nombre AS provincia, pa.nombre AS pais, " +
			"       COALESCE(( " +
			"           SELECT GROUP_CONCAT(t.nombre, ', ') " +
			"           FROM reportero_tematica rt2 " +
			"           JOIN tematica t ON t.id_tematica = rt2.id_tematica " +
			"           WHERE rt2.id_reportero = r.id_reportero " +
			"       ), '') AS tematicas " +
			"FROM reportero r " +
			"JOIN provincia pr ON pr.id_provincia = r.id_provincia " +
			"JOIN pais pa ON pa.id_pais = pr.id_pais " +
			"WHERE r.id_agencia = ? "
		);
		params.add(idAgencia);

		sql.append(
			"AND r.id_reportero NOT IN ( " +
			"   SELECT ar.id_reportero " +
			"   FROM asignacion_reportero ar " +
			"   JOIN evento e_asig ON e_asig.id_evento = ar.id_evento " +
			"   JOIN evento e_sel ON e_sel.id_evento = ? " +
			"   WHERE ar.id_evento <> e_sel.id_evento " +
			"     AND e_asig.fecha_inicio <= e_sel.fecha_fin " +
			"     AND e_sel.fecha_inicio <= e_asig.fecha_fin " +
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
				(String) r[6],
				(String) r[3],
				(String) r[4],
				(String) r[5]
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
		return !rows.isEmpty() && ((Number) rows.get(0)[0]).intValue() > 0;
	}

	public void guardarAsignaciones(int idEvento, List<ReporteroDTO> asignados) {
		if (asignados == null || asignados.isEmpty()) {
			throw new IllegalStateException("Debe haber al menos un reportero asignado.");
		}

		boolean hayBasico = false;
		boolean hayResponsable = false;
		int responsables = 0;

		for (ReporteroDTO r : asignados) {
			if ("Básico".equals(r.getTipoReportero())) {
				hayBasico = true;
			}
			if (r.isResponsable()) {
				hayResponsable = true;
				responsables++;
			}
		}

		if (!hayBasico) {
			throw new IllegalStateException("Debe haber al menos un reportero básico asignado.");
		}
		if (!hayResponsable) {
			throw new IllegalStateException("Debe haber un reportero responsable asignado.");
		}
		if (responsables > 1) {
			throw new IllegalStateException("Solo puede haber un reportero responsable.");
		}

		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_evento = ?", idEvento);

		for (ReporteroDTO r : asignados) {
			db.executeUpdate(
				"INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (?, ?, ?)",
				idEvento, r.getIdReportero(), r.isResponsable() ? 1 : 0
			);
		}
	}

	public void finalizarAsignacion(int idEvento) {
		List<ReporteroDTO> asignados = getReporterosAsignados(idEvento);
		if (asignados.isEmpty()) {
			throw new IllegalStateException("Debe haber al menos un reportero asignado.");
		}

		boolean hayBasico = false;
		boolean hayResponsable = false;

		for (ReporteroDTO r : asignados) {
			if ("Básico".equals(r.getTipoReportero())) {
				hayBasico = true;
			}
			if (r.isResponsable()) {
				hayResponsable = true;
			}
		}

		if (!hayBasico) {
			throw new IllegalStateException("Para finalizar debe haber al menos un reportero básico asignado.");
		}
		if (!hayResponsable) {
			throw new IllegalStateException("Para finalizar debe haber un responsable asignado.");
		}

		db.executeUpdate("UPDATE evento SET finalizada = 1 WHERE id_evento = ?", idEvento);
	}
}