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
INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (1, 'Rueda de prensa', '2026-03-10');

INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (1, 'Manifestación', '2026-03-11');

INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (1, 'Partido Champions', '2026-03-10');


-- Agencia Sur
INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (2, 'Presentación resultados', '2026-03-10');

INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (2, 'Presentación futbolista', '2026-03-11');





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

INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (1, 2);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (1, 4);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (2, 1);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (3, 3);
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (4, 1);

INSERT INTO empresa_tematica (id_empresa, id_tematica) VALUES (1, 1);
INSERT INTO empresa_tematica (id_empresa, id_tematica) VALUES (1, 3);
INSERT INTO empresa_tematica (id_empresa, id_tematica) VALUES (2, 2);


