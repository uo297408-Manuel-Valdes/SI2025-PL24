
DROP TABLE IF EXISTS Carreras;
DROP TABLE IF EXISTS asignacion_reportero;
DROP TABLE IF EXISTS reportero;
DROP TABLE IF EXISTS evento;
DROP TABLE IF EXISTS agencia_prensa;

CREATE TABLE agencia_prensa (
  id_agencia     INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre         TEXT NOT NULL UNIQUE
);

CREATE TABLE evento (
  id_evento      INTEGER PRIMARY KEY AUTOINCREMENT,
  id_agencia     INTEGER NOT NULL,
  nombre         TEXT NOT NULL,
  fecha_evento   TEXT NOT NULL, 
  FOREIGN KEY (id_agencia) REFERENCES agencia_prensa(id_agencia)
);

CREATE TABLE reportero (
  id_reportero   INTEGER PRIMARY KEY AUTOINCREMENT,
  id_agencia     INTEGER NOT NULL,
  nombre         TEXT NOT NULL,
  FOREIGN KEY (id_agencia) REFERENCES agencia_prensa(id_agencia)
);

CREATE TABLE asignacion_reportero (
  id_evento      INTEGER NOT NULL,
  id_reportero   INTEGER NOT NULL,
  PRIMARY KEY (id_evento, id_reportero),
  FOREIGN KEY (id_evento) REFERENCES evento(id_evento),
  FOREIGN KEY (id_reportero) REFERENCES reportero(id_reportero)
);
