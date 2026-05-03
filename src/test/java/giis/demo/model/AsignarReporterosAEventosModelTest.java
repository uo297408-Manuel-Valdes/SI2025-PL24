package giis.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import giis.demo.util.Database;

public class AsignarReporterosAEventosModelTest {

	private static Database db = new Database();
	private AsignarReporterosAEventosModel model;

	@BeforeEach
	public void setUp() {
		db.createDatabase(false);
		loadCleanDatabase();
		model = new AsignarReporterosAEventosModel();
	}

	private void loadCleanDatabase() {
		db.executeBatch(new String[] {
			"INSERT INTO agencia_prensa(id_agencia, nombre) VALUES (1, 'Agencia Norte')",
			"INSERT INTO agencia_prensa(id_agencia, nombre) VALUES (2, 'Agencia Sur')",

			"INSERT INTO pais(id_pais, nombre, dieta_manutencion) VALUES (1, 'España', 50)",
			"INSERT INTO provincia(id_provincia, id_pais, nombre, dieta_alojamiento) VALUES (1, 1, 'Asturias', 50)",
			"INSERT INTO provincia(id_provincia, id_pais, nombre, dieta_alojamiento) VALUES (2, 1, 'Madrid', 100)",

			"INSERT INTO tematica(id_tematica, nombre) VALUES (1, 'Deportes')",
			"INSERT INTO tematica(id_tematica, nombre) VALUES (2, 'Política')",

			"INSERT INTO evento(id_evento, id_agencia, id_provincia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (1, 1, 1, 'Evento seleccionado', '2026-03-10', '2026-03-12', 0)",
			"INSERT INTO evento(id_evento, id_agencia, id_provincia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (2, 1, 1, 'Evento solapado', '2026-03-11', '2026-03-13', 0)",
			"INSERT INTO evento(id_evento, id_agencia, id_provincia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (3, 1, 1, 'Evento no solapado', '2026-03-20', '2026-03-21', 0)",
			"INSERT INTO evento(id_evento, id_agencia, id_provincia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (4, 1, 1, 'Evento frontera', '2026-03-12', '2026-03-14', 0)",

			"INSERT INTO reportero(id_reportero, id_agencia, id_provincia, nombre, tipo_reportero) VALUES (1, 1, 1, 'Ana Pérez', 'Básico')",
			"INSERT INTO reportero(id_reportero, id_agencia, id_provincia, nombre, tipo_reportero) VALUES (2, 1, 1, 'Luis Gómez', 'Gráfico')",
			"INSERT INTO reportero(id_reportero, id_agencia, id_provincia, nombre, tipo_reportero) VALUES (3, 1, 1, 'Marta Ruiz', 'Camarógrafo')",
			"INSERT INTO reportero(id_reportero, id_agencia, id_provincia, nombre, tipo_reportero) VALUES (4, 1, 1, 'Laura Martínez', 'Básico')",
			"INSERT INTO reportero(id_reportero, id_agencia, id_provincia, nombre, tipo_reportero) VALUES (5, 2, 1, 'Pedro Sur', 'Básico')",
			"INSERT INTO reportero(id_reportero, id_agencia, id_provincia, nombre, tipo_reportero) VALUES (6, 1, 1, 'Carmen Cámara', 'Camarógrafo')",

			"INSERT INTO evento_tematica(id_evento, id_tematica) VALUES (1, 1)",

			"INSERT INTO reportero_tematica(id_reportero, id_tematica) VALUES (1, 1)",
			"INSERT INTO reportero_tematica(id_reportero, id_tematica) VALUES (2, 2)",
			"INSERT INTO reportero_tematica(id_reportero, id_tematica) VALUES (4, 1)",
			"INSERT INTO reportero_tematica(id_reportero, id_tematica) VALUES (6, 1)",

			"INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (2, 2, 0)",
			"INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (4, 3, 0)"
		});
	}

	@Test
	public void testReporteroDisponibleSinEventos() {
		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, false, false, false, false);

		assertTrue(contiene(disponibles, 1));
	}

	@Test
	public void testReporteroConEventoNoSolapadoDisponible() {
		db.executeUpdate("INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (3, 4, 0)");

		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, false, false, false, false);

		assertTrue(contiene(disponibles, 4));
	}

	@Test
	public void testReporteroConSolapeNoDisponible() {
		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, false, false, false, false);

		assertFalse(contiene(disponibles, 2));
	}

	@Test
	public void testReporteroConCoincidenciaUnDiaNoDisponible() {
		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, false, false, false, false);

		assertFalse(contiene(disponibles, 3));
	}

	@Test
	public void testReporteroDeOtraAgenciaNoAparece() {
		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, false, false, false, false);

		assertFalse(contiene(disponibles, 5));
	}

	@Test
	public void testFiltroTematicaCoincidente() {
		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, true, false, false, false);

		assertTrue(contiene(disponibles, 1));
		assertTrue(contiene(disponibles, 4));
		assertTrue(contiene(disponibles, 6));
	}

	@Test
	public void testFiltroTematicaNoCoincidente() {
		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_reportero = 2");

		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, true, false, false, false);

		assertFalse(contiene(disponibles, 2));
	}

	@Test
	public void testFiltroTipoBasico() {
		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, false, true, false, false);

		assertTrue(disponibles.stream().allMatch(r -> "Básico".equals(r.getTipoReportero())));
	}

	@Test
	public void testFiltroTipoGrafico() {
		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_reportero = 2");

		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, false, false, true, false);

		assertTrue(disponibles.stream().allMatch(r -> "Gráfico".equals(r.getTipoReportero())));
		assertTrue(contiene(disponibles, 2));
	}

	@Test
	public void testFiltroTipoCamarografo() {
		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, false, false, false, true);

		assertTrue(disponibles.stream().allMatch(r -> "Camarógrafo".equals(r.getTipoReportero())));
		assertTrue(contiene(disponibles, 6));
	}

	@Test
	public void testFiltroTematicaYTipo() {
		List<ReporteroDTO> disponibles = model.getReporterosDisponibles(1, 1, true, true, false, false);

		assertTrue(disponibles.stream().allMatch(r ->
			"Básico".equals(r.getTipoReportero()) &&
			r.getTematicasTexto().contains("Deportes")
		));
	}

	@Test
	public void testGuardarAsignacionValida() {
		List<ReporteroDTO> asignados = new ArrayList<>();

		ReporteroDTO ana = new ReporteroDTO(1, 1, "Ana Pérez", "Deportes", "Básico");
		ana.setResponsable(true);
		asignados.add(ana);

		assertDoesNotThrow(() -> model.guardarAsignaciones(1, asignados));

		List<Object[]> filas = db.executeQueryArray(
			"SELECT id_evento, id_reportero, es_responsable FROM asignacion_reportero WHERE id_evento = ?",
			1
		);

		assertEquals(1, filas.size());
		assertEquals(1, ((Number) filas.get(0)[1]).intValue());
		assertEquals(1, ((Number) filas.get(0)[2]).intValue());
	}

	@Test
	public void testGuardarListaVacia() {
		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> model.guardarAsignaciones(1, new ArrayList<>()));

		assertEquals("Debe haber al menos un reportero asignado.", ex.getMessage());
	}

	@Test
	public void testGuardarSinResponsable() {
		List<ReporteroDTO> asignados = new ArrayList<>();

		ReporteroDTO ana = new ReporteroDTO(1, 1, "Ana Pérez", "Deportes", "Básico");
		ana.setResponsable(false);
		asignados.add(ana);

		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> model.guardarAsignaciones(1, asignados));

		assertEquals("Debe haber un reportero responsable asignado.", ex.getMessage());
	}

	@Test
	public void testGuardarConDosResponsables() {
		List<ReporteroDTO> asignados = new ArrayList<>();

		ReporteroDTO ana = new ReporteroDTO(1, 1, "Ana Pérez", "Deportes", "Básico");
		ana.setResponsable(true);

		ReporteroDTO laura = new ReporteroDTO(4, 1, "Laura Martínez", "Deportes", "Básico");
		laura.setResponsable(true);

		asignados.add(ana);
		asignados.add(laura);

		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> model.guardarAsignaciones(1, asignados));

		assertEquals("Solo puede haber un reportero responsable.", ex.getMessage());
	}

	@Test
	public void testGuardarSinReporteroBasico() {
		List<ReporteroDTO> asignados = new ArrayList<>();

		ReporteroDTO luis = new ReporteroDTO(2, 1, "Luis Gómez", "Política", "Gráfico");
		luis.setResponsable(true);
		asignados.add(luis);

		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> model.guardarAsignaciones(1, asignados));

		assertEquals("Debe haber al menos un reportero básico asignado.", ex.getMessage());
	}

	@Test
	public void testFinalizarAsignacionValida() {
		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_evento = ?", 1);
		db.executeUpdate("INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (1, 1, 1)");

		model.finalizarAsignacion(1);

		assertTrue(model.isAsignacionFinalizada(1));
	}

	@Test
	public void testFinalizarSinAsignados() {
		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_evento = ?", 1);

		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> model.finalizarAsignacion(1));

		assertEquals("Debe haber al menos un reportero asignado.", ex.getMessage());
	}

	@Test
	public void testFinalizarSinResponsable() {
		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_evento = ?", 1);
		db.executeUpdate("INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (1, 1, 0)");

		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> model.finalizarAsignacion(1));

		assertEquals("Para finalizar debe haber un responsable asignado.", ex.getMessage());
	}

	@Test
	public void testFinalizarSinBasico() {
		db.executeUpdate("DELETE FROM asignacion_reportero WHERE id_evento = ?", 1);
		db.executeUpdate("INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (1, 2, 1)");

		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> model.finalizarAsignacion(1));

		assertEquals("Para finalizar debe haber al menos un reportero básico asignado.", ex.getMessage());
	}

	private boolean contiene(List<ReporteroDTO> lista, int idReportero) {
		return lista.stream().anyMatch(r -> r.getIdReportero() == idReportero);
	}
}