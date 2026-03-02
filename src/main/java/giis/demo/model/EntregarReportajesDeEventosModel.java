package giis.demo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class EntregarReportajesDeEventosModel {

	private final Database db = new Database();

	public List<ReporteroDTO> getReporteros() {
		String sql = "SELECT id_reportero, id_agencia, nombre FROM REPORTERO ORDER BY nombre";
		List<Object[]> rows = db.executeQueryArray(sql);
		List<ReporteroDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new ReporteroDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String)  r[2]
			));
		}
		return res;
	}

	public List<EventoDTO> getEventosAsignadosAReportero(int idReportero, boolean conReportaje) {
		String condicion = conReportaje
			? "AND EXISTS     (SELECT 1 FROM REPORTAJE r WHERE r.id_evento = e.id_evento)"
			: "AND NOT EXISTS (SELECT 1 FROM REPORTAJE r WHERE r.id_evento = e.id_evento)";
		String sql =
			"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento " +
			"FROM EVENTO e " +
			"JOIN ASIGNACION_REPORTERO ar ON ar.id_evento = e.id_evento " +
			"WHERE ar.id_reportero = ? " +
			condicion + " " +
			"ORDER BY e.fecha_evento, e.nombre";
		List<Object[]> rows = db.executeQueryArray(sql, idReportero);
		List<EventoDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			res.add(new EventoDTO(
				((Number) r[0]).intValue(),
				((Number) r[1]).intValue(),
				(String)  r[2],
				(String)  r[3]
			));
		}
		return res;
	}

	public ReportajeDTO getReportaje(int idEvento) {
		String sql =
			"SELECT id_reportaje, id_evento, titulo, id_reportero_entrega " +
			"FROM REPORTAJE WHERE id_evento = ?";
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);
		if (rows.isEmpty()) return null;
		Object[] r = rows.get(0);
		return new ReportajeDTO(
			((Number) r[0]).intValue(),
			((Number) r[1]).intValue(),
			(String)  r[2],
			((Number) r[3]).intValue()
		);
	}

	public VersionReportajeDTO getUltimaVersion(int idReportaje) {
		String sql =
			"SELECT id_version, id_reportaje, subtitulo, cuerpo, cambios " +
			"FROM VERSION_REPORTAJE " +
			"WHERE id_reportaje = ? " +
			"ORDER BY id_version DESC LIMIT 1";
		List<Object[]> rows = db.executeQueryArray(sql, idReportaje);
		if (rows.isEmpty()) return null;
		Object[] r = rows.get(0);
		return new VersionReportajeDTO(
			((Number) r[0]).intValue(),
			((Number) r[1]).intValue(),
			(String)  r[2],
			(String)  r[3],
			(String)  r[4]
		);
	}

	public boolean reporteroPuedeModificar(int idEvento, int idReportero) {
		String sql = "SELECT 1 FROM REPORTAJE WHERE id_evento = ? AND id_reportero_entrega = ? LIMIT 1";
		return !db.executeQueryArray(sql, idEvento, idReportero).isEmpty();
	}

	public void validarTitulo(String titulo, int idReportajeExcluido) {
	    if (titulo == null || titulo.trim().isEmpty())
	        throw new ApplicationException("El título no puede estar vacío.");
	    if (tituloExiste(titulo.trim(), idReportajeExcluido))
	        throw new ApplicationException("Ya existe otro reportaje con ese título.");
	}


	public void entregarReportaje(int idEvento, int idReportero,
	                               String titulo, String subtitulo, String cuerpo) {
		if (titulo    == null || titulo.trim().isEmpty())
			throw new ApplicationException("El campo Titulo no puede estar vacio.");
		if (subtitulo == null || subtitulo.trim().isEmpty())
			throw new ApplicationException("El campo Subtitulo no puede estar vacio.");
		if (cuerpo    == null || cuerpo.trim().isEmpty())
			throw new ApplicationException("El campo Cuerpo no puede estar vacio.");

		String checkAsig = "SELECT 1 FROM ASIGNACION_REPORTERO WHERE id_evento = ? AND id_reportero = ? LIMIT 1";
		if (db.executeQueryArray(checkAsig, idEvento, idReportero).isEmpty())
			throw new ApplicationException("El reportero no tiene asignacion para este evento.");

		String tituloTrim    = titulo.trim();
		String subtituloTrim = subtitulo.trim();
		String cuerpoTrim    = cuerpo.trim();

		ReportajeDTO reportaje = getReportaje(idEvento);

		if (reportaje == null) {
			if (tituloExiste(tituloTrim, -1))
				throw new ApplicationException("Ya existe otro reportaje con ese titulo.");
			String insertRep = "INSERT INTO REPORTAJE(id_evento, titulo, id_reportero_entrega) VALUES (?, ?, ?)";
			db.executeUpdate(insertRep, idEvento, tituloTrim, idReportero);
			reportaje = getReportaje(idEvento);
		} else {
			if (!reporteroPuedeModificar(idEvento, idReportero))
				throw new ApplicationException("Solo el reportero que hizo la entrega original puede modificar el reportaje.");
		}

		String cambiosAuto = generarCambios(reportaje, subtituloTrim, cuerpoTrim);
		String insertVer = "INSERT INTO VERSION_REPORTAJE(id_reportaje, subtitulo, cuerpo, cambios) VALUES (?, ?, ?, ?)";
		db.executeUpdate(insertVer, reportaje.getIdReportaje(), subtituloTrim, cuerpoTrim, cambiosAuto);
	}

	private String generarCambios(ReportajeDTO reportaje, String nuevoSubtitulo, String nuevoCuerpo) {
		String ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		VersionReportajeDTO ultima = getUltimaVersion(reportaje.getIdReportaje());
		if (ultima == null) return "Primera entrega. " + ahora;
		List<String> cambiosList = new ArrayList<>();
		if (!ultima.getSubtitulo().equals(nuevoSubtitulo)) cambiosList.add("subtitulo");
		if (!ultima.getCuerpo().equals(nuevoCuerpo)) cambiosList.add("cuerpo");
		if (cambiosList.isEmpty()) return "Sin cambios. " + ahora;
		return "Modificado: " + String.join(" y ", cambiosList) + ". " + ahora;
	}

	private boolean tituloExiste(String titulo, int idReportajePropio) {
		String sql = "SELECT 1 FROM REPORTAJE WHERE titulo = ? AND id_reportaje != ? LIMIT 1";
		return !db.executeQueryArray(sql, titulo, idReportajePropio).isEmpty();
	}
}