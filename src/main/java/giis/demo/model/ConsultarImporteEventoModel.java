package giis.demo.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import giis.demo.util.Database;

public class ConsultarImporteEventoModel {

	private final Database db = new Database();

	public List<ReporteroDTO> getReporteros() {
		String sql =
			"SELECT r.id_reportero, r.id_agencia, r.nombre, " +
			"       COALESCE(GROUP_CONCAT(t.nombre, ', '), '') AS tematicas, " +
			"       r.tipo_reportero " +
			"FROM reportero r " +
			"LEFT JOIN reportero_tematica rt ON rt.id_reportero = r.id_reportero " +
			"LEFT JOIN tematica t ON t.id_tematica = rt.id_tematica " +
			"GROUP BY r.id_reportero, r.id_agencia, r.nombre, r.tipo_reportero " +
			"ORDER BY r.nombre";

		List<Object[]> rows = db.executeQueryArray(sql);
		List<ReporteroDTO> res = new ArrayList<>();

		for (Object[] r : rows) {
			res.add(new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String) r[2],
				(String) r[3],
				(String) r[4]
			));
		}
		return res;
	}

	public List<Object[]> getEventosAsignadosAReportero(int idReportero) {
		String sql =
			"SELECT e.nombre, e.fecha_inicio, e.fecha_fin, pr.nombre, pa.nombre, e.id_evento " +
			"FROM asignacion_reportero ar " +
			"JOIN evento e ON e.id_evento = ar.id_evento " +
			"JOIN provincia pr ON pr.id_provincia = e.id_provincia " +
			"JOIN pais pa ON pa.id_pais = pr.id_pais " +
			"WHERE ar.id_reportero = ? " +
			"ORDER BY e.fecha_inicio, e.nombre";

		return db.executeQueryArray(sql, idReportero);
	}

	public ImporteEventoDTO calcularImporte(int idReportero, int idEvento) {
		String sql =
			"SELECT e.id_evento, e.nombre, e.fecha_inicio, e.fecha_fin, " +
			"       prev.nombre AS provincia_evento, " +
			"       paev.nombre AS pais_evento, " +
			"       r.id_reportero, r.nombre, " +
			"       prrep.nombre AS provincia_reportero, " +
			"       prev.id_provincia AS id_provincia_evento, " +
			"       prrep.id_provincia AS id_provincia_reportero, " +
			"       prev.dieta_alojamiento, " +
			"       paev.dieta_manutencion " +
			"FROM asignacion_reportero ar " +
			"JOIN evento e ON e.id_evento = ar.id_evento " +
			"JOIN provincia prev ON prev.id_provincia = e.id_provincia " +
			"JOIN pais paev ON paev.id_pais = prev.id_pais " +
			"JOIN reportero r ON r.id_reportero = ar.id_reportero " +
			"JOIN provincia prrep ON prrep.id_provincia = r.id_provincia " +
			"WHERE ar.id_reportero = ? AND e.id_evento = ?";

		List<Object[]> rows = db.executeQueryArray(sql, idReportero, idEvento);
		if (rows.isEmpty()) {
			return null;
		}

		Object[] r = rows.get(0);

		String fechaInicio = (String) r[2];
		String fechaFin = (String) r[3];

		LocalDate inicio = LocalDate.parse(fechaInicio);
		LocalDate fin = LocalDate.parse(fechaFin);
		int duracionDias = (int) ChronoUnit.DAYS.between(inicio, fin) + 1;

		int idProvinciaEvento = ((Number) r[9]).intValue();
		int idProvinciaReportero = ((Number) r[10]).intValue();

		double alojamientoDia = 0.0;
		if (idProvinciaEvento != idProvinciaReportero) {
			alojamientoDia = ((Number) r[11]).doubleValue();
		}

		double manutencionDia = ((Number) r[12]).doubleValue();
		double totalDia = alojamientoDia + manutencionDia;
		double totalEvento = totalDia * duracionDias;

		return new ImporteEventoDTO(
			((Number) r[0]).intValue(),
			(String) r[1],
			fechaInicio,
			fechaFin,
			(String) r[4],
			(String) r[5],
			((Number) r[6]).intValue(),
			(String) r[7],
			(String) r[8],
			duracionDias,
			alojamientoDia,
			manutencionDia,
			totalDia,
			totalEvento
		);
	}
}