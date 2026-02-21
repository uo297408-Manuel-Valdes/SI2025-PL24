INSERT INTO AGENCIA_PRENSA(nombre) VALUES ('Agencia Norte'), ('Agencia Sur');


INSERT INTO EVENTO(id_agencia, nombre, fecha_evento) VALUES
(1,'Final Copa','2026-03-01'),
(1,'Rueda de prensa','2026-03-01'),
(1,'Concierto','2026-03-10'),
(1,'Carrera ciclista','2026-03-7'),
(1,'Entrevista cantante','2026-03-8');


INSERT INTO EVENTO(id_agencia, nombre, fecha_evento) VALUES
(2,'Maratón','2026-03-01');


INSERT INTO REPORTERO(id_agencia, nombre) VALUES
(1,'Ana'),(1,'Luis'),(1,'Marta'),(1,'Pablo');


INSERT INTO REPORTERO(id_agencia, nombre) VALUES
(2,'Sergio'),(2,'Clara');

INSERT INTO ASIGNACION_REPORTERO(id_evento, id_reportero) VALUES (1, 1), (4,3), (5,2);

INSERT INTO EMPRESA(nombre) VALUES ('Empresa Comunicacion Norte'),('Empresa Comunicacion Sur'),('Empresa Comunicacion Este'),('Empresa Comunicacion Oeste');
