package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class ConcederAccesoModel {

	private final Database db = new Database();

	// ── Agencias ──────────────────────────────────────────────────────────

	public List<AgenciaDTO> getAgencias() {
		String sql = "SELECT id_agencia, nombre FROM AGENCIA_PRENSA ORDER BY nombre";
		List<Object[]> rows = db.executeQueryArray(sql);
		List<AgenciaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new AgenciaDTO(((Number) r[0]).intValue(), (String) r[1]));
		}
		return res;
	}

	// ── Eventos ───────────────────────────────────────────────────────────

	/**
	 * Eventos de la agencia que tienen reportaje entregado y han sido
	 * FINALIZADOS por el reportero responsable.
	 *
	 * Un evento esta finalizado cuando su responsable ha insertado
	 * un registro con es_finalizacion=1 en comentario_revision.
	 *
	 * @param idAgencia    Agencia seleccionada
	 * @param conEmbargo   true  → reportajes CON fecha_embargo
	 *                     false → reportajes SIN fecha_embargo
	 */
	public List<EventoDTO> getEventosCubiertos(int idAgencia, boolean conEmbargo) {
		String filtroEmbargo = conEmbargo
			? "AND r.fecha_embargo IS NOT NULL"
			: "AND r.fecha_embargo IS NULL";

		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_inicio " +
			"FROM EVENTO e " +
			"JOIN REPORTAJE r ON r.id_evento = e.id_evento " +
			"WHERE e.id_agencia = ? " +
			filtroEmbargo + " " +
			// El responsable ha finalizado = tiene es_finalizacion=1 para este reportaje
			"AND EXISTS ( " +
			"  SELECT 1 FROM COMENTARIO_REVISION cr " +
			"  JOIN ASIGNACION_REPORTERO ar ON ar.id_reportero = cr.id_reportero " +
			"    AND ar.id_evento = e.id_evento " +
			"  WHERE cr.id_reportaje = r.id_reportaje " +
			"  AND ar.es_responsable = 1 " +
			"  AND cr.es_finalizacion = 1 " +
			") " +
			"ORDER BY e.fecha_inicio, e.nombre";

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

	// ── Empresas aptas ────────────────────────────────────────────────────

	/**
	 * Devuelve las empresas APTAS para recibir el reportaje de un evento.
	 *
	 * Una empresa es apta si:
	 *   1. Tiene el ofrecimiento ACEPTADO para este evento.
	 *   2. Tiene tarifa plana al corriente (pendiente=0).
	 *   3. Si el reportaje tiene embargo y NO es acceso especial:
	 *      solo empresas con embargos=1.
	 *
	 * Justificante: "T.P.: Al corriente de pagos".
	 */
	public List<EmpresaDTO> getEmpresasAptas(int idEvento, boolean sinAcceso,
	                                          boolean conEmbargo, boolean accesoEspecial) {
		String filtroAcceso = sinAcceso
			? "AND NOT EXISTS (SELECT 1 FROM ACCESO_REPORTAJE acc " +
			  "WHERE acc.id_evento = ofr.id_evento AND acc.id_empresa = emp.id_empresa)"
			: "AND EXISTS (SELECT 1 FROM ACCESO_REPORTAJE acc " +
			  "WHERE acc.id_evento = ofr.id_evento AND acc.id_empresa = emp.id_empresa)";

		String filtroEmbargos = (conEmbargo && !accesoEspecial)
			? "AND emp.embargos = 1"
			: "";

		String sql =
			"SELECT emp.id_empresa, emp.nombre, emp.embargos " +
			"FROM EMPRESA emp " +
			"JOIN OFRECER_REPORTAJE ofr " +
			"  ON ofr.id_empresa = emp.id_empresa " +
			"  AND ofr.id_evento  = ? " +
			"  AND ofr.decision   = 'ACEPTADO' " +
			"WHERE EXISTS ( " +
			"  SELECT 1 FROM TARIFA t " +
			"  JOIN EVENTO e ON e.id_agencia = t.id_agencia " +
			"  WHERE e.id_evento  = ? " +
			"  AND   t.id_empresa = emp.id_empresa " +
			"  AND   t.pendiente  = 0 " +
			") " +
			filtroEmbargos + " " +
			filtroAcceso + " " +
			"ORDER BY emp.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idEvento, idEvento);
		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new EmpresaDTO(
				((Number) r[0]).intValue(),
				(String)  r[1],
				((Number) r[2]).intValue(),
				"T.P.: Al corriente de pagos"
			));
		}
		return res;
	}

	// ── Acceso ────────────────────────────────────────────────────────────

	public AccesoDTO getAcceso(int idEmpresa, int idEvento) {
		String sql =
			"SELECT o.id_acceso, o.id_evento, o.id_empresa, o.descargado " +
			"FROM ACCESO_REPORTAJE o " +
			"WHERE o.id_evento = ? AND o.id_empresa = ?";
		List<Object[]> rows = db.executeQueryArray(sql, idEvento, idEmpresa);
		AccesoDTO res = null;
		for (Object[] r : rows) {
			res = new AccesoDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				((Number) r[2]).intValue(),
				((Number) r[3]).intValue()
			);
		}
		return res;
	}

	/**
	 * Concede acceso al reportaje del evento a las empresas seleccionadas.
	 */
	public void concederAcceso(int idEvento, List<Integer> idsEmpresas, boolean accesoEspecial) {
		if (idsEmpresas == null || idsEmpresas.isEmpty())
			throw new ApplicationException("Debes seleccionar al menos una empresa para conceder acceso.");

		// El reportaje debe estar finalizado por el responsable
		String checkFin =
			"SELECT 1 FROM REPORTAJE r " +
			"JOIN COMENTARIO_REVISION cr ON cr.id_reportaje = r.id_reportaje " +
			"JOIN ASIGNACION_REPORTERO ar ON ar.id_reportero = cr.id_reportero " +
			"  AND ar.id_evento = r.id_evento " +
			"WHERE r.id_evento = ? " +
			"AND ar.es_responsable = 1 " +
			"AND cr.es_finalizacion = 1 LIMIT 1";
		if (db.executeQueryArray(checkFin, idEvento).isEmpty())
			throw new ApplicationException(
				"El reportaje aun no ha sido finalizado por el reportero responsable.");

		// Determinar si el reportaje tiene embargo
		String checkEmbargo = "SELECT fecha_embargo FROM REPORTAJE WHERE id_evento = ? LIMIT 1";
		List<Object[]> rowsEmb = db.executeQueryArray(checkEmbargo, idEvento);
		boolean tieneEmbargo = !rowsEmb.isEmpty() && rowsEmb.get(0)[0] != null;

		for (Integer idEmpresa : idsEmpresas) {
			if (idEmpresa == null) continue;

			String checkOfr =
				"SELECT 1 FROM OFRECER_REPORTAJE " +
				"WHERE id_evento = ? AND id_empresa = ? AND decision = 'ACEPTADO' LIMIT 1";
			if (db.executeQueryArray(checkOfr, idEvento, idEmpresa).isEmpty())
				throw new ApplicationException(
					"La empresa con id " + idEmpresa +
					" no tiene un ofrecimiento aceptado para este evento.");

			String checkTarifa =
				"SELECT 1 FROM TARIFA t " +
				"JOIN EVENTO e ON e.id_agencia = t.id_agencia " +
				"WHERE e.id_evento = ? AND t.id_empresa = ? AND t.pendiente = 0 LIMIT 1";
			if (db.executeQueryArray(checkTarifa, idEvento, idEmpresa).isEmpty())
				throw new ApplicationException(
					"La empresa con id " + idEmpresa +
					" no tiene tarifa plana al corriente de pagos.");

			if (tieneEmbargo && !accesoEspecial) {
				String checkEmbargoEmp =
					"SELECT 1 FROM EMPRESA WHERE id_empresa = ? AND embargos = 1 LIMIT 1";
				if (db.executeQueryArray(checkEmbargoEmp, idEmpresa).isEmpty())
					throw new ApplicationException(
						"Este reportaje no tiene embargo, por lo que el acceso especial no cambia nada");
			}

			String checkAcc =
				"SELECT 1 FROM ACCESO_REPORTAJE WHERE id_evento = ? AND id_empresa = ? LIMIT 1";
			if (!db.executeQueryArray(checkAcc, idEvento, idEmpresa).isEmpty())
				throw new ApplicationException(
					"La empresa con id " + idEmpresa +
					" ya tiene acceso concedido a este reportaje.");
		}

		String insert = "INSERT INTO ACCESO_REPORTAJE(id_evento, id_empresa, especial) VALUES (?, ?, ?)";
		for (Integer idEmpresa : idsEmpresas) {
		    db.executeUpdate(insert, idEvento, idEmpresa, accesoEspecial ? 1 : 0);
		}
	}

	public void quitarAcceso(int idEvento, List<Integer> idsEmpresas) {
		if (idsEmpresas == null || idsEmpresas.isEmpty())
			throw new ApplicationException("Debes seleccionar al menos una empresa.");
		String delete = "DELETE FROM ACCESO_REPORTAJE WHERE id_evento = ? AND id_empresa = ?";
		for (Integer idEmpresa : idsEmpresas) {
			db.executeUpdate(delete, idEvento, idEmpresa);
		}
	}
}