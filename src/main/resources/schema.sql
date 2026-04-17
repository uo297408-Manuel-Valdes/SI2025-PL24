PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS tarifa;
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
DROP TABLE IF EXISTS multimedia_reportaje;
DROP TABLE IF EXISTS empresa_tematica;
DROP TABLE IF EXISTS comentario_revision;
DROP TABLE IF EXISTS pais;
DROP TABLE IF EXISTS provincia;

CREATE TABLE agencia_prensa (
  id_agencia INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE pais( 
	id_pais INTEGER PRIMARY KEY AUTOINCREMENT,
	nombre TEXT NOT NULL UNIQUE,
	dieta_manutencion REAL NOT NULL CHECK (dieta_manutencion >=0)
);

CREATE TABLE provincia(
	id_provincia INTEGER PRIMARY KEY AUTOINCREMENT, 
	id_pais INTEGER NOT NULL,
	nombre TEXT NOT NULL UNIQUE,
	dieta_alojamiento REAL NOT NULL CHECK (dieta_alojamiento >=0),
	FOREIGN KEY (id_pais) REFERENCES pais(id_pais)
);

CREATE TABLE evento (
  id_evento INTEGER PRIMARY KEY AUTOINCREMENT,
  id_agencia INTEGER NOT NULL,
  id_provincia INTEGER NOT NULL,
  nombre TEXT NOT NULL,
  fecha_inicio TEXT NOT NULL,
  fecha_fin TEXT NOT NULL,
  finalizada INTEGER NOT NULL DEFAULT 0 CHECK (finalizada IN (0,1)),
  CHECK (fecha_fin >= fecha_inicio),
  FOREIGN KEY (id_agencia) REFERENCES agencia_prensa(id_agencia),
  FOREIGN KEY (id_provincia) REFERENCES provincia(id_provincia)
);

CREATE TABLE reportero (
  id_reportero INTEGER PRIMARY KEY AUTOINCREMENT,
  id_agencia INTEGER NOT NULL,
  id_provincia INTEGER NOT NULL,
  nombre TEXT NOT NULL,
  tipo_reportero TEXT NOT NULL CHECK (tipo_reportero IN ('Básico', 'Gráfico', 'Camarógrafo')),
  FOREIGN KEY (id_agencia) REFERENCES agencia_prensa(id_agencia),
  FOREIGN KEY (id_provincia) REFERENCES provincia(id_provincia)
);

CREATE TABLE asignacion_reportero (
  id_evento INTEGER NOT NULL,
  id_reportero INTEGER NOT NULL,
  es_responsable INTEGER NOT NULL DEFAULT 0 CHECK (es_responsable IN (0,1)),
  PRIMARY KEY (id_evento, id_reportero),
  FOREIGN KEY (id_evento) REFERENCES evento(id_evento),
  FOREIGN KEY (id_reportero) REFERENCES reportero(id_reportero)
);

CREATE UNIQUE INDEX idx_un_responsable_por_evento
ON asignacion_reportero(id_evento)
WHERE es_responsable = 1;

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
  descargado INTEGER DEFAULT 0 CHECK (descargado IN (0,1)),
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

CREATE TABLE multimedia_reportaje (
  id_multimedia INTEGER PRIMARY KEY AUTOINCREMENT,
  id_reportaje INTEGER NOT NULL,
  id_reportero INTEGER NOT NULL,
  path TEXT NOT NULL UNIQUE,
  tipo TEXT NOT NULL CHECK (tipo IN ('IMAGEN', 'VIDEO')),
  estado TEXT NOT NULL CHECK (estado IN ('DEFINITIVO','BORRADOR')),
  FOREIGN KEY (id_reportaje) REFERENCES reportaje(id_reportaje),
  FOREIGN KEY (id_reportero) REFERENCES reportero(id_reportero)
);

CREATE TABLE empresa_tematica (
  id_empresa INTEGER NOT NULL,
  id_tematica INTEGER NOT NULL,
  PRIMARY KEY (id_empresa, id_tematica),
  FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
  FOREIGN KEY (id_tematica) REFERENCES tematica(id_tematica)
);

CREATE TABLE comentario_revision (
  id_comentario INTEGER PRIMARY KEY AUTOINCREMENT,
  id_reportaje INTEGER NOT NULL,
  id_reportero INTEGER NOT NULL,
  comentario TEXT NOT NULL,
  fecha_hora TEXT NOT NULL,
  es_finalizacion INTEGER NOT NULL DEFAULT 0 CHECK (es_finalizacion IN (0,1)),
  FOREIGN KEY (id_reportaje) REFERENCES reportaje(id_reportaje),
  FOREIGN KEY (id_reportero) REFERENCES reportero(id_reportero)
);

CREATE TABLE tarifa (
  id_tarifa INTEGER PRIMARY KEY AUTOINCREMENT,
  id_agencia INTEGER NOT NULL,
  id_empresa INTEGER NOT NULL,
  pendiente INTEGER NOT NULL DEFAULT 0 CHECK (pendiente IN (0,1)),
  FOREIGN KEY (id_agencia) REFERENCES agencia_prensa(id_agencia),
  FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
);

