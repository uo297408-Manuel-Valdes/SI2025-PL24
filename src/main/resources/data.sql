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


