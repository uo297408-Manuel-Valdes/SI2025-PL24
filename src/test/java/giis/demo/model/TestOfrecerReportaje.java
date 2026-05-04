package giis.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

public class TestOfrecerReportaje {
	private static Database db = new Database();
	private OfrecerReportajeAgenciaComunicacionModel model;

	@BeforeEach
	public void setUp() {
		db.createDatabase(false);
		loadCleanDatabase();
		model = new OfrecerReportajeAgenciaComunicacionModel();
	}

	@AfterEach
	public void tearDown() {}
	
	private void loadCleanDatabase() {
		db.executeBatch(new String[] {
			"INSERT INTO agencia_prensa(id_agencia, nombre) VALUES (1, 'Agencia Norte')",
			
			"INSERT INTO pais(id_pais, nombre, dieta_manutencion) VALUES (1, 'España', 50)",
			"INSERT INTO provincia(id_provincia, id_pais, nombre, dieta_alojamiento) VALUES (1, 1, 'Asturias', 50)",
				
			"INSERT INTO evento(id_evento, id_agencia, id_provincia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (1, 1, 1, 'Evento sin finalizar', '2026-03-10', '2026-03-12', 0)",
			"INSERT INTO evento(id_evento, id_agencia, id_provincia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (2, 1, 1, 'Evento finalizado', '2026-03-11', '2026-03-13', 1)",
			"INSERT INTO evento(id_evento, id_agencia, id_provincia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (3, 1, 1, 'Evento con embargo', '2026-03-11', '2026-03-13', 1)",
			"INSERT INTO evento(id_evento, id_agencia, id_provincia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (4, 1, 1, 'Evento sin reporteros', '2026-03-11', '2026-03-13', 1)",

			"INSERT INTO reportero(id_reportero, id_agencia, id_provincia, nombre, tipo_reportero) VALUES (1, 1, 1, 'Ana Pérez', 'Básico')",

			"INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (1, 1, 1)",
			"INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (2, 1, 1)",
			"INSERT INTO asignacion_reportero(id_evento, id_reportero, es_responsable) VALUES (3, 1, 1)",
			
			"INSERT INTO empresa(nombre, embargos) VALUES ('Sin embargos', 0)",
			"INSERT INTO empresa(nombre, embargos) VALUES ('Embargos', 1)",
			"INSERT INTO empresa(nombre, embargos) VALUES ('Pendiente de pagos', 0)",
			
			"INSERT INTO ofrecer_reportaje (id_evento, id_empresa) VALUES (2, 2)",
			
			"INSERT INTO tarifa (id_agencia, id_empresa, pendiente) VALUES (1, 3, 1)",
			
			"INSERT INTO reportaje (id_evento, titulo, fecha_embargo, id_reportero_entrega) VALUES (2, 'Embargo caducado', '2026-01-24', 1)",
			"INSERT INTO reportaje (id_evento, titulo, fecha_embargo, id_reportero_entrega) VALUES (3, 'Embargo sin caducar', '2026-10-24', 1)"
		});
	}
	
	@Test
	public void testSinReporteros() {
		List<Integer> aux = new ArrayList<>();
		aux.add(1);
		ApplicationException ex = assertThrows(ApplicationException.class,
				() -> model.ofrecerEmpresa(4, aux, 1));
		
		assertEquals("No se puede asignar: el evento no tiene reporteros asignados.", ex.getMessage());
	}
	
	@Test
	public void testSinFinalizar() {
		List<Integer> aux = new ArrayList<>();
		aux.add(1);
		ApplicationException ex = assertThrows(ApplicationException.class,
				() -> model.ofrecerEmpresa(1, aux, 1));
		
		assertEquals("No se puede asignar: el evento no tiene finalizada la asignación de reporteros.", ex.getMessage());
	}
	
	@Test
	public void testYaOfrecido() {
		List<Integer> aux = new ArrayList<>();
		aux.add(2);
		ApplicationException ex = assertThrows(ApplicationException.class,
				() -> model.ofrecerEmpresa(2, aux, 1));
		
		assertEquals("Esta empresa ya tiene ofrecido este reportaje.", ex.getMessage());
	}
	
	@Test
	public void testPendientePagos() {
		List<Integer> aux = new ArrayList<>();
		aux.add(3);
		ApplicationException ex = assertThrows(ApplicationException.class,
				() -> model.ofrecerEmpresa(2, aux, 1));
		
		assertEquals("No se puede ofrecer este evento porque la empresa de comunicación esta pendiente de pagos.", ex.getMessage());
	}
	
	@Test
	public void testEmbargoSinCaducar() {
		List<Integer> aux = new ArrayList<>();
		aux.add(1);
		ApplicationException ex = assertThrows(ApplicationException.class,
				() -> model.ofrecerEmpresa(3, aux, 1));
		
		assertEquals("No se puede ofrecer este evento porque una o varias empresas seleccionadas no les interesan las reportajes con embargos.", ex.getMessage());
	}
	
	@Test
	public void testEmbargoCaducado() {
		List<Integer> aux = new ArrayList<>();
		aux.add(1);
		assertDoesNotThrow(() -> model.ofrecerEmpresa(2, aux, 1));
	}

}
