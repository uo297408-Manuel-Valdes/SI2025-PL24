-- ===============================
-- AGENCIAS
-- ===============================
INSERT INTO agencia_prensa (nombre) VALUES ('Agencia Norte');
INSERT INTO agencia_prensa (nombre) VALUES ('Agencia Sur');

-- ===============================
-- EMPRESAS DE COMUNICACIÓN
-- ===============================
INSERT INTO empresa (nombre) VALUES ('El Comercio');
INSERT INTO empresa (nombre) VALUES ('La nueva España');

-- ===============================
-- REPORTEROS
-- ===============================
INSERT INTO reportero (id_agencia, nombre) VALUES (1, 'Ana Pérez');
INSERT INTO reportero (id_agencia, nombre) VALUES (1, 'Luis Gómez');
INSERT INTO reportero (id_agencia, nombre) VALUES (2, 'Marta Ruiz');
INSERT INTO reportero (id_agencia, nombre) VALUES (2, 'Laura Martinez');


-- ===============================
-- EVENTOS
-- ===============================
-- Agencia Norte
INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (1, 'Rueda de prensa ', '2026-03-10');

INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (1, 'Manifestación ', '2026-03-11');

INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (1, 'Partido Champions', '2026-03-10');


-- Agencia Sur
INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (2, 'Presentación resultados', '2026-03-10');

INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (2, 'Presentación futbolista', '2026-03-11');







--Temáticas
INSERT INTO tematica (nombre) VALUES ('Deportes');
INSERT INTO tematica (nombre) VALUES ('Moda');
INSERT INTO tematica (nombre) VALUES ('Política');
INSERT INTO tematica (nombre) VALUES ('Economía');

-- Temáticas de eventos
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (1, 3); -- Rueda de prensa  Política
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (2, 3); -- Manifestación  Política
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (3, 1); -- Partido Champions  Deportes
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (4, 4); -- Presentación resultados  Economía
INSERT INTO evento_tematica (id_evento, id_tematica) VALUES (5, 1); -- Presentación futbolista  Deportes

-- Temáticas de reporteros
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (1, 3); -- Ana  Política
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (2, 1); -- Luis  Deportes
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (3, 4); -- Marta  Economía
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (4, 1); -- Laura  Deportes
INSERT INTO reportero_tematica (id_reportero, id_tematica) VALUES (4, 3); -- Laura  Política
