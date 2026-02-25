-- ===============================
-- AGENCIAS
-- ===============================
INSERT INTO agencia_prensa (nombre) VALUES ('Agencia Norte');
INSERT INTO agencia_prensa (nombre) VALUES ('Agencia Sur');

-- ===============================
-- EMPRESAS DE COMUNICACIÓN
-- ===============================
INSERT INTO empresa (nombre) VALUES ('El Diario');
INSERT INTO empresa (nombre) VALUES ('Noticias Hoy');

-- ===============================
-- REPORTEROS
-- ===============================
INSERT INTO reportero (id_agencia, nombre) VALUES (1, 'Ana Pérez');
INSERT INTO reportero (id_agencia, nombre) VALUES (1, 'Luis Gómez');
INSERT INTO reportero (id_agencia, nombre) VALUES (2, 'Marta Ruiz');

-- ===============================
-- EVENTOS
-- ===============================
-- Agencia Norte
INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (1, 'Rueda de prensa Gobierno', '2026-03-10');

INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (1, 'Manifestación centro ciudad', '2026-03-11');


-- Agencia Sur
INSERT INTO evento (id_agencia, nombre, fecha_evento)
VALUES (2, 'Presentación resultados empresa', '2026-03-10');

-- ===============================
-- OFRECIMIENTOS 
-- ===============================
-- Evento 1 ofrecido a las dos empresas (pendiente)
INSERT INTO ofrecer_reportaje (id_evento, id_empresa, decision)
VALUES (1, 1, NULL);

INSERT INTO ofrecer_reportaje (id_evento, id_empresa, decision)
VALUES (1, 2, NULL);

-- Evento 2 ofrecido solo a El Diario
INSERT INTO ofrecer_reportaje (id_evento, id_empresa, decision)
VALUES (2, 1, NULL);

-- ===============================
-- ACCESO REPORTAJES
-- ===============================
-- El Diario con acceso a evento 3
INSERT INTO acceso_reportaje (id_evento, id_empresa)
VALUES (3,1);
-- ===============================
-- REPORTAJES
-- ===============================
INSERT INTO reportaje (id_evento, titulo, id_reportero_entrega)
VALUES(3, 'Presentación resultados empresa', 3)
-- ===============================
-- VERSION
-- ===============================
INSERT INTO version_reportaje (id_reportaje, subtitulo, cuerpo, cambios)
VALUES(3, 'Presentación resultados empresa', 'Una empresa presenta sus resultados del ultimo cuatrimestre...', 'creacion')

