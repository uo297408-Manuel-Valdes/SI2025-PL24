package giis.demo.model;

import java.util.ArrayList;
import java.util.List;

import giis.demo.util.Database;

public class AccederReportajesModel {
	
	private final Database db = new Database();

	public List<EmpresaDTO> getEmpresas() {
		String sql = "SELECT id_empresa, nombre FROM EMPRESA ORDER BY nombre";
		List<Object[]> rows = db.executeQueryArray(sql);
		List<EmpresaDTO> res = new ArrayList<>();
		for (Object[] r : rows) {
			int id = ((Number) r[0]).intValue();
			String nombre = (String) r[1];
			res.add(new EmpresaDTO(id, nombre));
		}
		return res;
	}

	public List<EventoDTO> getReportajes(int idEmpresa) {
			String sql =
					"SELECT e.id_evento, e.id_agencia, e.nombre, e.fecha_evento " +
					"FROM EVENTO e " +
					"WHERE EXISTS (SELECT 1 FROM ACCESO_REPORTAJE ar WHERE ar.id_evento = e.id_evento " +
					"AND ar.id_empresa=?)"+
					"ORDER BY e.fecha_evento, e.nombre";

				List<Object[]> rows = db.executeQueryArray(sql, idEmpresa);

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
	
	public ReportajeDTO getInfoReportaje(int idEvento) {
		String sql=
				"SELECT r.id_reportaje,r.id_evento, r.id_reportero_entrega, r.titulo " +
				"FROM REPORTAJE r " +
				"WHERE r.id_evento=? ";
		
		List<Object[]> rows = db.executeQueryArray(sql, idEvento);

		ReportajeDTO res = null;
		
		for (Object[] r : rows) {
			int idreportaje= ((Number) r[0]).intValue();
			int idevento=((Number) r[1]).intValue();
			int idreportero=((Number) r[2]).intValue();
			String titulo = (String) r[3];
			res=new ReportajeDTO(idreportaje,idevento,titulo,idreportero);
		}
		
		return res;
	}

	public VersionDTO getVersion(int idReportaje) {
		String sql=
				"SELECT v.id_version,v.id_reportaje,v.subtitulo,v.cuerpo,v.cambios " +
				"FROM VERSION_REPORTAJE v " +
				"WHERE v.id_reportaje=?"+
				"ORDER BY v.id_version DESC "+
				"LIMIT 1";
		
		List<Object[]> rows = db.executeQueryArray(sql, idReportaje);
		
		VersionDTO res = null;
		
		for (Object[] r : rows) {
			int idversion=((Number) r[0]).intValue();
			int idreportaje= ((Number) r[1]).intValue();
			String subtitulo = (String) r[2];
			String cuerpo = (String) r[3];
			String cambio = (String) r[4];
			res=new VersionDTO(idversion,idreportaje,subtitulo,cuerpo,cambio);
		}
		
		return res;
	}
	
}
