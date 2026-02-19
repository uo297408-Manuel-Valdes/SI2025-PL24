package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class AsignarReporterosAEventosModel {

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

	
	public List<EventoDTO> getEventosSinAsignacion(int idAgencia) {
		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento " +
			"FROM EVENTO e " +
			"WHERE e.id_agencia = ? " +
			"AND NOT EXISTS (SELECT 1 FROM ASIGNACION_REPORTERO ar WHERE ar.id_evento = e.id_evento) " +
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

	
	public List<ReporteroDTO> getReporterosDisponibles(int idAgencia, String fechaEventoISO) {
		String sql =
			"SELECT r.id_reportero, r.id_agencia, r.nombre " +
			"FROM REPORTERO r " +
			"WHERE r.id_agencia = ? " +
			"AND NOT EXISTS ( " +
			"  SELECT 1 " +
			"  FROM ASIGNACION_REPORTERO ar " +
			"  JOIN EVENTO e2 ON e2.id_evento = ar.id_evento " +
			"  WHERE ar.id_reportero = r.id_reportero " +
			"  AND e2.fecha_evento = ? " +
			") " +
			"ORDER BY r.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idAgencia, fechaEventoISO);

		List<ReporteroDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int idRep = ((Number) r[0]).intValue();
			int idAg = ((Number) r[1]).intValue();
			String nombre = (String) r[2];
			res.add(new ReporteroDTO(idRep, idAg, nombre));
		}
		return res;
	}

	private EventoDTO getEventoById(int idEvento) {
		String sql = "SELECT id_evento, id_agencia, nombre, fecha_evento FROM EVENTO WHERE id_evento = ?";
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		if (rows.isEmpty())
			throw new ApplicationException("El evento seleccionado ya no existe.");
		Object[] r = rows.get(0);
		return new EventoDTO(
			((Number) r[0]).intValue(),
			((Number) r[1]).intValue(),
			(String) r[2],
			(String) r[3]
		);
	}

	
	public void asignarReporteros(int idEvento, List<Integer> idsReporteros) {

		if (idsReporteros == null || idsReporteros.isEmpty())
			throw new ApplicationException("Debes asignar al menos un reportero.");

		EventoDTO evento = getEventoById(idEvento);

		if (eventoTieneAsignaciones(idEvento)) {
			throw new ApplicationException("No se puede asignar: el evento ya tiene reporteros asignados.");
		}

		for (Integer idReportero : idsReporteros) {
			if (idReportero == null) continue;

			if (!reporteroPerteneceAAgencia(idReportero, evento.getIdAgencia())) {
				throw new ApplicationException("Hay un reportero que no pertenece a la agencia del evento.");
			}
			if (!reporteroDisponibleEnFecha(idReportero, evento.getFechaEvento())) {
				throw new ApplicationException("Hay un reportero que ya está asignado a otro evento en esa fecha.");
			}
		}

		String insert = "INSERT INTO ASIGNACION_REPORTERO(id_evento, id_reportero) VALUES (?, ?)";
		for (Integer idReportero : idsReporteros) {
			db.executeUpdate(insert, idEvento, idReportero);
		}
	}

	private boolean eventoTieneAsignaciones(int idEvento) {
		String sql = "SELECT 1 FROM ASIGNACION_REPORTERO WHERE id_evento = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEvento).isEmpty();
	}

	private boolean reporteroPerteneceAAgencia(int idReportero, int idAgencia) {
		String sql = "SELECT 1 FROM REPORTERO WHERE id_reportero = ? AND id_agencia = ? LIMIT 1";
		return !db.executeQueryArray(sql, idReportero, idAgencia).isEmpty();
	}

	private boolean reporteroDisponibleEnFecha(int idReportero, String fechaISO) {
		String sql =
			"SELECT 1 " +
			"FROM ASIGNACION_REPORTERO ar " +
			"JOIN EVENTO e ON e.id_evento = ar.id_evento " +
			"WHERE ar.id_reportero = ? AND e.fecha_evento = ? " +
			"LIMIT 1";
		return db.executeQueryArray(sql, idReportero, fechaISO).isEmpty();
	}
}
