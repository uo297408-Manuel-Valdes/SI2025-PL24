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
	 * Eventos finalizados de la agencia que tienen reportaje entregado,
	 * filtrados por si el reportaje tiene o no fecha de embargo.
	 *
	 * @param idAgencia    Agencia seleccionada
	 * @param conEmbargo   true  → reportajes CON fecha_embargo (en periodo de embargo)
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
			"AND e.finalizada = 1 " +
			filtroEmbargo + " " +
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
	 * Reglas:
	 *  - La empresa debe tener ofrecimiento ACEPTADO para este evento.
	 *  - La empresa debe tener tarifa plana al corriente (pendiente=0).
	 *  - Si el reportaje tiene embargo (conEmbargo=true) y NO es acceso especial:
	 *      solo empresas con embargos=1 (interesadas en reportajes con embargo).
	 *  - Si es acceso especial (accesoEspecial=true):
	 *      se ignora el campo embargos; cualquier empresa con tarifa al corriente es apta.
	 *
	 * Justificante: siempre "T.P.: Al corriente de pagos".
	 *
	 * @param idEvento       Evento seleccionado
	 * @param sinAcceso      true  → empresas SIN acceso concedido aun
	 *                       false → empresas CON acceso ya concedido
	 * @param conEmbargo     true  → el reportaje tiene embargo (aplica filtro embargos)
	 *                       false → sin embargo, no se filtra por embargos
	 * @param accesoEspecial true  → omite la restriccion de embargos por empresa
	 */
	public List<EmpresaDTO> getEmpresasAptas(int idEvento, boolean sinAcceso,
	                                          boolean conEmbargo, boolean accesoEspecial) {
		String filtroAcceso = sinAcceso
			? "AND NOT EXISTS (SELECT 1 FROM ACCESO_REPORTAJE acc " +
			  "WHERE acc.id_evento = ofr.id_evento AND acc.id_empresa = emp.id_empresa)"
			: "AND EXISTS (SELECT 1 FROM ACCESO_REPORTAJE acc " +
			  "WHERE acc.id_evento = ofr.id_evento AND acc.id_empresa = emp.id_empresa)";

		// Solo se restringe por embargos cuando el reportaje tiene embargo
		// y el usuario NO ha marcado acceso especial
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
	 *
	 * Validaciones:
	 *  - El evento debe estar finalizado y tener reportaje.
	 *  - La empresa debe tener ofrecimiento ACEPTADO.
	 *  - La empresa debe tener tarifa plana al corriente.
	 *  - Si el reportaje tiene embargo y NO es acceso especial:
	 *      la empresa debe tener embargos=1.
	 *  - La empresa no debe tener acceso ya concedido.
	 *
	 * @param idEvento       Evento al que se concede acceso
	 * @param idsEmpresas    Lista de empresas seleccionadas
	 * @param accesoEspecial true → omite restriccion de embargos por empresa
	 */
	public void concederAcceso(int idEvento, List<Integer> idsEmpresas, boolean accesoEspecial) {
		if (idsEmpresas == null || idsEmpresas.isEmpty())
			throw new ApplicationException("Debes seleccionar al menos una empresa para conceder acceso.");

		// El evento debe estar finalizado y tener reportaje
		String checkFin =
			"SELECT r.fecha_embargo FROM REPORTAJE r " +
			"JOIN EVENTO e ON e.id_evento = r.id_evento " +
			"WHERE r.id_evento = ? AND e.finalizada = 1 LIMIT 1";
		List<Object[]> rowsFin = db.executeQueryArray(checkFin, idEvento);
		if (rowsFin.isEmpty())
			throw new ApplicationException(
				"El evento no esta finalizado o no tiene reportaje entregado.");

		// Determinar si el reportaje tiene embargo
		boolean tieneEmbargo = rowsFin.get(0)[0] != null;

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

			// Si hay embargo y NO es acceso especial, la empresa debe tener embargos=1
			if (tieneEmbargo && !accesoEspecial) {
				String checkEmbargo =
					"SELECT 1 FROM EMPRESA WHERE id_empresa = ? AND embargos = 1 LIMIT 1";
				if (db.executeQueryArray(checkEmbargo, idEmpresa).isEmpty())
					throw new ApplicationException(
						"La empresa con id " + idEmpresa +
						" no esta interesada en reportajes con embargo. " +
						"Usa 'Conceder acceso especial' para distribuirlo igualmente.");
			}

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