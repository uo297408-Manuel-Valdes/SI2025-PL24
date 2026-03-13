PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS reportero_tematica;
DROP TABLE IF EXISTS evento_tematica;
DROP TABLE IF EXISTS version_reportaje;
DROP TABLE IF EXISTS reportaje;
DROP TABLE IF EXISTS acceso_reportaje;
DROP TABLE IF EXISTS ofrecer_reportaje;
DROP TABLE IF EXISTS asignacion_reportero;
DROP TABLE IF EXISTS reportero;
DROP TABLE IF EXISTS evento;
DROP TABLE IF EXISTS empresa;
DROP TABLE IF EXISTS tematica;
DROP TABLE IF EXISTS agencia_prensa;

CREATE TABLE agencia_prensa (
  id_agencia INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE evento (
  id_evento INTEGER PRIMARY KEY AUTOINCREMENT,
  id_agencia INTEGER NOT NULL,
  nombre TEXT NOT NULL,
  fecha_evento TEXT NOT NULL,
  FOREIGN KEY (id_agencia) REFERENCES agencia_prensa(id_agencia)
);

CREATE TABLE reportero (
  id_reportero INTEGER PRIMARY KEY AUTOINCREMENT,
  id_agencia INTEGER NOT NULL,
  nombre TEXT NOT NULL,
  FOREIGN KEY (id_agencia) REFERENCES agencia_prensa(id_agencia)
);

CREATE TABLE asignacion_reportero (
  id_evento INTEGER NOT NULL,
  id_reportero INTEGER NOT NULL,
  PRIMARY KEY (id_evento, id_reportero),
  FOREIGN KEY (id_evento) REFERENCES evento(id_evento),
  FOREIGN KEY (id_reportero) REFERENCES reportero(id_reportero)
);

CREATE TABLE empresa (
  id_empresa INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE ofrecer_reportaje (
  id_ofrecimiento INTEGER PRIMARY KEY AUTOINCREMENT,
  id_evento INTEGER NOT NULL,
  id_empresa INTEGER NOT NULL,
  decision TEXT NULL CHECK (decision IN ('ACEPTADO','RECHAZADO') OR decision IS NULL),
  UNIQUE (id_evento, id_empresa),
  FOREIGN KEY (id_evento) REFERENCES evento(id_evento),
  FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
);

CREATE TABLE acceso_reportaje (
  id_acceso INTEGER PRIMARY KEY AUTOINCREMENT,
  id_evento INTEGER NOT NULL,
  id_empresa INTEGER NOT NULL,
  UNIQUE (id_evento, id_empresa),
  FOREIGN KEY (id_evento) REFERENCES evento(id_evento),
  FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
);

CREATE TABLE reportaje (
  id_reportaje INTEGER PRIMARY KEY AUTOINCREMENT,
  id_evento INTEGER NOT NULL UNIQUE,
  titulo TEXT NOT NULL UNIQUE,
  id_reportero_entrega INTEGER NOT NULL,
  FOREIGN KEY (id_evento) REFERENCES evento(id_evento),
  FOREIGN KEY (id_reportero_entrega) REFERENCES reportero(id_reportero)
);

CREATE TABLE version_reportaje (
  id_version INTEGER PRIMARY KEY AUTOINCREMENT,
  id_reportaje INTEGER NOT NULL,
  subtitulo TEXT NOT NULL,
  cuerpo TEXT NOT NULL,
  cambios TEXT NOT NULL,
  FOREIGN KEY (id_reportaje) REFERENCES reportaje(id_reportaje)
);

CREATE TABLE tematica (
  id_tematica INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE evento_tematica (
  id_evento INTEGER NOT NULL,
  id_tematica INTEGER NOT NULL,
  PRIMARY KEY (id_evento, id_tematica),
  FOREIGN KEY (id_evento) REFERENCES evento(id_evento),
  FOREIGN KEY (id_tematica) REFERENCES tematica(id_tematica)
);

CREATE TABLE reportero_tematica (
  id_reportero INTEGER NOT NULL,
  id_tematica INTEGER NOT NULL,
  PRIMARY KEY (id_reportero, id_tematica),
  FOREIGN KEY (id_reportero) REFERENCES reportero(id_reportero),
  FOREIGN KEY (id_tematica) REFERENCES tematica(id_tematica)
);
