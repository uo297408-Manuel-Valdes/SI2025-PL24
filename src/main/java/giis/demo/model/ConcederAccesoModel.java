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
	 * Eventos de la agencia que tienen reportaje entregado y estan FINALIZADOS.
	 */
	public List<EventoDTO> getEventosCubiertos(int idAgencia) {
		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_inicio " +
			"FROM EVENTO e " +
			"JOIN REPORTAJE r ON r.id_evento = e.id_evento " +
			"WHERE e.id_agencia = ? " +
			"AND e.finalizada = 1 " +
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
	 * Una empresa es apta si cumple TODAS estas condiciones:
	 *   1. Tiene el ofrecimiento ACEPTADO para este evento.
	 *   2. Tiene una tarifa con pendiente=0 para la agencia del evento
	 *      (tarifa plana al corriente de pagos).
	 *
	 * El justificante mostrado siempre es "TP: Al corriente de pagos".
	 *
	 * @param idEvento   Evento seleccionado
	 * @param sinAcceso  true  → empresas que todavia NO tienen acceso concedido
	 *                   false → empresas que YA tienen acceso concedido
	 */
	public List<EmpresaDTO> getEmpresasAptas(int idEvento, boolean sinAcceso) {
		String filtroAcceso = sinAcceso
			? "AND NOT EXISTS (SELECT 1 FROM ACCESO_REPORTAJE acc " +
			  "WHERE acc.id_evento = ofr.id_evento AND acc.id_empresa = emp.id_empresa)"
			: "AND EXISTS (SELECT 1 FROM ACCESO_REPORTAJE acc " +
			  "WHERE acc.id_evento = ofr.id_evento AND acc.id_empresa = emp.id_empresa)";

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
			filtroAcceso + " " +
			"ORDER BY emp.nombre";

		List<Object[]> rows = db.executeQueryArray(sql, idEvento, idEvento);
		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new EmpresaDTO(
				((Number) r[0]).intValue(),
				(String)  r[1],
				((Number) r[2]).intValue(),
				"TP: Al corriente de pagos"
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
	 * Valida que el evento este finalizado, que la empresa tenga ofrecimiento
	 * aceptado, que sea apta (tarifa plana al corriente) y que no tenga ya acceso.
	 */
	public void concederAcceso(int idEvento, List<Integer> idsEmpresas) {
		if (idsEmpresas == null || idsEmpresas.isEmpty())
			throw new ApplicationException("Debes seleccionar al menos una empresa para conceder acceso.");

		// El evento debe estar finalizado y tener reportaje
		String checkFin =
			"SELECT 1 FROM REPORTAJE r " +
			"JOIN EVENTO e ON e.id_evento = r.id_evento " +
			"WHERE r.id_evento = ? AND e.finalizada = 1 LIMIT 1";
		if (db.executeQueryArray(checkFin, idEvento).isEmpty())
			throw new ApplicationException(
				"El evento no esta finalizado o no tiene reportaje entregado.");

		for (Integer idEmpresa : idsEmpresas) {
			if (idEmpresa == null) continue;

			// La empresa debe haber aceptado el ofrecimiento
			String checkOfr =
				"SELECT 1 FROM OFRECER_REPORTAJE " +
				"WHERE id_evento = ? AND id_empresa = ? AND decision = 'ACEPTADO' LIMIT 1";
			if (db.executeQueryArray(checkOfr, idEvento, idEmpresa).isEmpty())
				throw new ApplicationException(
					"La empresa con id " + idEmpresa +
					" no tiene un ofrecimiento aceptado para este evento.");

			// La empresa debe tener tarifa plana al corriente
			String checkTarifa =
				"SELECT 1 FROM TARIFA t " +
				"JOIN EVENTO e ON e.id_agencia = t.id_agencia " +
				"WHERE e.id_evento = ? AND t.id_empresa = ? AND t.pendiente = 0 LIMIT 1";
			if (db.executeQueryArray(checkTarifa, idEvento, idEmpresa).isEmpty())
				throw new ApplicationException(
					"La empresa con id " + idEmpresa +
					" no tiene tarifa plana al corriente de pagos.");

			// No debe tener acceso ya concedido
			String checkAcc =
				"SELECT 1 FROM ACCESO_REPORTAJE " +
				"WHERE id_evento = ? AND id_empresa = ? LIMIT 1";
			if (!db.executeQueryArray(checkAcc, idEvento, idEmpresa).isEmpty())
				throw new ApplicationException(
					"La empresa con id " + idEmpresa +
					" ya tiene acceso concedido a este reportaje.");
		}

		String insert = "INSERT INTO ACCESO_REPORTAJE(id_evento, id_empresa) VALUES (?, ?)";
		for (Integer idEmpresa : idsEmpresas) {
			db.executeUpdate(insert, idEvento, idEmpresa);
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