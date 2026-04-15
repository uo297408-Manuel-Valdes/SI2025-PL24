-- ===============================
-- AGENCIAS
-- ===============================
INSERT INTO agencia_prensa (nombre) VALUES ('Agencia Norte');
INSERT INTO agencia_prensa (nombre) VALUES ('Agencia Sur');


-- ===============================
-- EMPRESAS DE COMUNICACIÓN
-- ===============================
INSERT INTO empresa (nombre) VALUES ('El Comercio');
INSERT INTO empresa (nombre) VALUES ('La Nueva España');


-- ===============================
-- REPORTEROS
-- ===============================
INSERT INTO reportero (id_agencia, nombre, tipo_reportero) VALUES (1, 'Ana Pérez', 'Básico');
INSERT INTO reportero (id_agencia, nombre, tipo_reportero) VALUES (1, 'Luis Gómez', 'Gráfico');
INSERT INTO reportero (id_agencia, nombre, tipo_reportero) VALUES (2, 'Marta Ruiz', 'Camarógrafo');
INSERT INTO reportero (id_agencia, nombre, tipo_reportero) VALUES (2, 'Laura Martínez', 'Básico');
INSERT INTO reportero (id_agencia, nombre, tipo_reportero) VALUES (1, 'Carlos Vega', 'Camarógrafo');
INSERT INTO reportero (id_agencia, nombre, tipo_reportero) VALUES (1, 'Lucía Díaz', 'Básico');

-- ===============================
-- EVENTOS
-- ===============================

-- Agencia Norte
-- Evento 1: del 10 al 12
INSERT INTO evento (id_agencia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (1, 'Congreso de prensa', '2026-03-10', '2026-03-13', 0); 

INSERT INTO evento (id_agencia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (1, 'Manifestación', '2026-03-13', '2026-03-14', 0); 

INSERT INTO evento (id_agencia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (1, 'Partido Champions', '2026-03-12', '2026-03-15', 0);

INSERT INTO evento (id_agencia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (1, 'Presentación libro', '2026-03-20', '2026-03-20', 0); 

INSERT INTO evento (id_agencia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (1, 'Entrevista exclusiva', '2026-03-21', '2026-03-21', 0); 

INSERT INTO evento (id_agencia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (1, 'Cumbre económica', '2026-03-25', '2026-03-27', 1); 

INSERT INTO evento (id_agencia, nombre, fecha_inicio, fecha_fin, finalizada) VALUES (2, 'Presentación resultados', '2026-03-10', '2026-03-11', 0); 


INSERT INTO tematica (nombre) VALUES ('Deportes');
INSERT INTO tematica (nombre) VALUES ('Política');
INSERT INTO tematica (nombre) VALUES ('Economía');
INSERT INTO tematica (nombre) VALUES ('Sociedad');

INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (1, 2);
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (1, 4);
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (2, 4);
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (3, 1);
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (4, 3);
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (5, 1);
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (6, 3);


INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (1, 2);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (1, 4);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (2, 1);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (3, 3);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (4, 1);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (5, 2);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (6, 3);




INSERT INTO empresa_tematica (id_empresa, id_tematica) VALUES (1, 1);
INSERT INTO empresa_tematica (id_empresa, id_tematica) VALUES (1, 3);
INSERT INTO empresa_tematica (id_empresa, id_tematica) VALUES (2, 2);

-- ===============================
-- Tarifas
-- ===============================

INSERT INTO tarifa (id_agencia, id_empresa, pendiente) values (1, 2, 1);

--Temporal
INSERT INTO reportaje (id_evento, titulo, id_reportero_entrega) VALUES (1, 'Temporal',1);
INSERT INTO acceso_reportaje (id_evento, id_empresa) VALUES (1,1);
INSERT INTO multimedia_reportaje (id_reportero, id_reportaje, tipo, estado, path) VALUES (1, 1, 'VIDEO', 'DEFINITIVO', 'a');
INSERT INTO multimedia_reportaje (id_reportero, id_reportaje, tipo, estado, path) VALUES (1, 1, 'IMAGEN', 'DEFINITIVO', 'b');
INSERT INTO multimedia_reportaje (id_reportero, id_reportaje, tipo, estado, path) VALUES (1, 1, 'VIDEO', 'BORRADOR', 'c');



