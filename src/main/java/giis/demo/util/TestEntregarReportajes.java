package giis.demo.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import giis.demo.model.ComentarioRevisionDTO;
import giis.demo.model.EntregarReportajesDeEventosModel;
import giis.demo.model.MultimediaDTO;
import giis.demo.model.ReportajeDTO;
import giis.demo.model.RevisarReportajeModel;
import giis.demo.model.VersionReportajeDTO;
import giis.demo.util.ApplicationException;
import giis.demo.util.Database;

/**
 * Pruebas unitarias de los procesos de negocio de entrega, revision
 * y finalizacion de reportajes (HU 33551, 34030, 34031, 34359).
 *
 * Procesos de negocio probados:
 *   P1 - Entrega de reportaje (primera entrega y modificacion)
 *   P2 - Solicitud de revision de un reportaje
 *   P3 - Finalizacion individual de revision por reportero
 *   P4 - Finalizacion del reportaje por el reportero responsable
 *   P5 - Gestion de contenido multimedia de un reportaje
 */
@DisplayName("Pruebas de entrega, revision y finalizacion de reportajes")
public class TestEntregarReportajes {

    private static final Database db = new Database();
    private EntregarReportajesDeEventosModel model;
    private RevisarReportajeModel revisarModel;

    // Evento 1: asignados rep1 (responsable) y rep2 (no responsable)
    private static final int EVENTO_1    = 1;
    // Evento 2: asignado solo rep3 (responsable), sin rep no-responsable
    private static final int EVENTO_2    = 2;

    private static final int REP_1       = 1;  // responsable del evento 1
    private static final int REP_2       = 2;  // no responsable del evento 1
    private static final int REP_3       = 3;  // responsable del evento 2, no asignado al evento 1
    private static final int AGENCIA_1   = 1;
    private static final int PROVINCIA_1 = 1;

    @BeforeEach
    void setUp() {
        db.createDatabase(true);
        cargarDatosLimpios();
        model       = new EntregarReportajesDeEventosModel();
        revisarModel = new RevisarReportajeModel();
    }

    /**
     * Carga un estado conocido y minimal para las pruebas:
     *   - Agencia 1, provincias, 3 reporteros
     *   - Evento 1: rep1 responsable, rep2 no responsable
     *   - Evento 2: rep3 responsable (sin no-responsables, para probar
     *               finalizacion directa sin revision)
     */
    private void cargarDatosLimpios() {
        db.executeBatch(new String[]{
            // Base
            "DELETE FROM comentario_revision",
            "DELETE FROM version_reportaje",
            "DELETE FROM multimedia_reportaje",
            "DELETE FROM reportaje",
            "DELETE FROM asignacion_reportero",
            "DELETE FROM evento",
            "DELETE FROM reportero",
            "DELETE FROM provincia",
            "DELETE FROM pais",
            "DELETE FROM agencia_prensa",

            // Agencia
            "INSERT INTO agencia_prensa(id_agencia,nombre) VALUES (1,'Agencia Test')",

            // Pais y provincia
            "INSERT INTO pais(id_pais,nombre,dieta_manutencion) VALUES (1,'España',50.0)",
            "INSERT INTO provincia(id_provincia,id_pais,nombre,dieta_alojamiento) VALUES (1,1,'Asturias',50.0)",

            // Reporteros: 1=Ana(resp ev1), 2=Luis(no-resp ev1), 3=Marta(resp ev2)
            "INSERT INTO reportero(id_reportero,id_agencia,id_provincia,nombre,tipo_reportero) VALUES (1,1,1,'Ana Perez','Básico')",
            "INSERT INTO reportero(id_reportero,id_agencia,id_provincia,nombre,tipo_reportero) VALUES (2,1,1,'Luis Gomez','Gráfico')",
            "INSERT INTO reportero(id_reportero,id_agencia,id_provincia,nombre,tipo_reportero) VALUES (3,1,1,'Marta Ruiz','Camarógrafo')",

            // Evento 1: rep1 responsable, rep2 no responsable
            "INSERT INTO evento(id_evento,id_agencia,id_provincia,nombre,fecha_inicio,fecha_fin,finalizada) VALUES (1,1,1,'Congreso Test','2026-03-10','2026-03-12',0)",
            "INSERT INTO asignacion_reportero(id_evento,id_reportero,es_responsable) VALUES (1,1,1)",
            "INSERT INTO asignacion_reportero(id_evento,id_reportero,es_responsable) VALUES (1,2,0)",

            // Evento 2: rep3 responsable unico (sin no-responsables)
            "INSERT INTO evento(id_evento,id_agencia,id_provincia,nombre,fecha_inicio,fecha_fin,finalizada) VALUES (2,1,1,'Rueda de prensa','2026-03-15','2026-03-15',0)",
            "INSERT INTO asignacion_reportero(id_evento,id_reportero,es_responsable) VALUES (2,3,1)"
        });
    }

    // ── Helpers reutilizables ─────────────────────────────────────────────

    /** Entrega un reportaje valido en el evento indicado. */
    private void entregarReportaje(int idEvento, int idReportero, String titulo) {
        model.entregarReportaje(idEvento, idReportero, titulo, "Sub de " + titulo, "Cuerpo de " + titulo);
    }

    /** Obtiene el id del reportaje del evento (asume que existe). */
    private int getIdReportaje(int idEvento) {
        return model.getReportaje(idEvento).getIdReportaje();
    }

    /** Inserta una solicitud de revision para el reportaje del evento. */
    private void solicitarRevision(int idEvento, int idReportero) {
        model.solicitarRevision(idEvento, idReportero);
    }

    /** Finaliza la revision del reportero para el reportaje indicado. */
    private void finalizarRevision(int idReportaje, int idReportero) {
        revisarModel.finalizarRevision(idReportaje, idReportero);
    }

    // ====================================================================
    // P1 — ENTREGA DE REPORTAJE
    // ====================================================================
    @Nested
    @DisplayName("P1 - Entrega de reportaje")
    class EntregaReportaje {

        /**
         * CE1: Primera entrega correcta.
         * Dado un evento sin reportaje y un reportero asignado,
         * al entregar se crea el reportaje y su primera version.
         */
        @Test
        @DisplayName("CE1.1: Primera entrega crea reportaje y version")
        void primeraEntregaCreaReportajeYVersion() {
            assertNull(model.getReportaje(EVENTO_1), "No debe existir reportaje antes de entregar");

            entregarReportaje(EVENTO_1, REP_1, "Titulo Original");

            ReportajeDTO rep = model.getReportaje(EVENTO_1);
            assertNotNull(rep, "Debe crearse el reportaje");
            assertEquals("Titulo Original", rep.getTitulo());
            assertEquals(REP_1, rep.getIdReporteroEntrega());

            VersionReportajeDTO v = model.getUltimaVersion(rep.getIdReportaje());
            assertNotNull(v, "Debe crearse la primera version");
            assertEquals("Sub de Titulo Original", v.getSubtitulo());
            assertTrue(v.getCambios().startsWith("Primera entrega."), "Los cambios deben indicar primera entrega");
        }

        /**
         * CE2: Modificacion por el mismo reportero.
         * Tras la primera entrega, el autor puede modificar el contenido;
         * se crea una segunda version con los cambios detectados.
         */
        @Test
        @DisplayName("CE1.2: Modificacion por el autor genera nueva version con cambios")
        void modificacionPorAutorGeneraNuevaVersion() {
            entregarReportaje(EVENTO_1, REP_1, "Titulo");
            int idRep = getIdReportaje(EVENTO_1);

            model.entregarReportaje(EVENTO_1, REP_1, "Titulo", "Subtitulo Nuevo", "Cuerpo nuevo");

            VersionReportajeDTO v = model.getUltimaVersion(idRep);
            assertEquals("Subtitulo Nuevo", v.getSubtitulo());
            assertTrue(v.getCambios().contains("subtitulo"), "Debe registrar cambio de subtitulo");
            assertTrue(v.getCambios().contains("cuerpo"),    "Debe registrar cambio de cuerpo");
        }

        /**
         * CE3: Modificacion por reportero diferente al autor.
         * Un reportero asignado pero que no hizo la entrega original
         * no puede modificar el contenido textual del reportaje.
         */
        @Test
        @DisplayName("CE1.3: Modificacion por reportero no autor lanza excepcion")
        void modificacionPorNoAutorLanzaExcepcion() {
            entregarReportaje(EVENTO_1, REP_1, "Titulo");

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.entregarReportaje(EVENTO_1, REP_2, "Titulo", "Otro sub", "Otro cuerpo")
            );
            assertTrue(ex.getMessage().contains("entrega original"),
                "El mensaje debe indicar que solo el autor puede modificar");
        }

        /**
         * CE4: Titulo duplicado en primera entrega.
         * Si ya existe un reportaje con el mismo titulo en el sistema,
         * la entrega debe rechazarse.
         */
        @Test
        @DisplayName("CE1.4: Titulo duplicado en primera entrega lanza excepcion")
        void tituloDuplicadoEnPrimeraEntregaLanzaExcepcion() {
            entregarReportaje(EVENTO_1, REP_1, "Titulo Unico");

            // Entregar en evento 2 con el mismo titulo
            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.entregarReportaje(EVENTO_2, REP_3, "Titulo Unico", "Sub", "Cuerpo")
            );
            assertTrue(ex.getMessage().contains("titulo"), "Debe indicar titulo duplicado");
        }

        /**
         * CE5: Modificacion bloqueada por revision pendiente.
         * Cuando el reportaje esta pendiente de revision, el autor
         * no puede modificarlo hasta que todos finalicen la revision.
         */
        @Test
        @DisplayName("CE1.5: Modificacion bloqueada si hay revision pendiente")
        void modificacionBloqueadaPorRevisionPendiente() {
            entregarReportaje(EVENTO_1, REP_1, "Titulo Rev");
            solicitarRevision(EVENTO_1, REP_1);

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.entregarReportaje(EVENTO_1, REP_1, "Titulo Rev", "Sub mod", "Cuerpo mod")
            );
            assertTrue(ex.getMessage().contains("pendiente de revision"),
                "Debe indicar que el reportaje esta pendiente de revision");
        }

        /**
         * CE6: Reportero no asignado no puede entregar.
         */
        @Test
        @DisplayName("CE1.6: Reportero sin asignacion no puede entregar")
        void reporteroSinAsignacionNoEntrega() {
            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.entregarReportaje(EVENTO_1, REP_3, "Titulo", "Sub", "Cuerpo")
            );
            assertTrue(ex.getMessage().contains("asignacion"),
                "Debe indicar que el reportero no tiene asignacion");
        }
    }

    // ====================================================================
    // P2 — SOLICITUD DE REVISION
    // ====================================================================
    @Nested
    @DisplayName("P2 - Solicitud de revision")
    class SolicitudRevision {

        /**
         * CE1: Solicitud correcta.
         * El autor del reportaje puede solicitar revision.
         * Se inserta un comentario con es_finalizacion=0.
         */
        @Test
        @DisplayName("CE2.1: Solicitud correcta genera comentario de solicitud")
        void solicitudCorrectaGeneraComentario() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Para Revision");
            int idRep = getIdReportaje(EVENTO_1);

            assertFalse(model.isPendienteRevision(idRep), "No debe estar pendiente antes de solicitar");

            solicitarRevision(EVENTO_1, REP_1);

            assertTrue(model.isPendienteRevision(idRep), "Debe estar pendiente tras solicitar");
            List<ComentarioRevisionDTO> comentarios = model.getComentariosRevision(idRep);
            // La solicitud es un comentario con es_finalizacion=0
            assertFalse(comentarios.isEmpty(), "Debe existir al menos un comentario de solicitud");
        }

        /**
         * CE2: Solo el autor puede solicitar la revision.
         * Un reportero asignado pero que no hizo la entrega no puede
         * solicitar la revision.
         */
        @Test
        @DisplayName("CE2.2: Solicitud por no autor lanza excepcion")
        void solicitudPorNoAutorLanzaExcepcion() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Solicitud");

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                solicitarRevision(EVENTO_1, REP_2)
            );
            assertTrue(ex.getMessage().contains("entrega"),
                "Debe indicar que solo el autor puede solicitar");
        }

        /**
         * CE3: No se puede solicitar si ya hay revision pendiente.
         */
        @Test
        @DisplayName("CE2.3: Solicitud duplicada lanza excepcion")
        void solicitudDuplicadaLanzaExcepcion() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Doble Sol");
            solicitarRevision(EVENTO_1, REP_1);

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                solicitarRevision(EVENTO_1, REP_1)
            );
            assertTrue(ex.getMessage().contains("pendiente"),
                "Debe indicar que ya esta pendiente");
        }

        /**
         * CE4: No se puede solicitar si no existe reportaje.
         */
        @Test
        @DisplayName("CE2.4: Solicitud sin reportaje existente lanza excepcion")
        void solicitudSinReportajeLanzaExcepcion() {
            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                solicitarRevision(EVENTO_1, REP_1)
            );
            assertTrue(ex.getMessage().contains("reportaje"),
                "Debe indicar que no existe reportaje");
        }
    }

    // ====================================================================
    // P3 — FINALIZACION INDIVIDUAL DE REVISION
    // ====================================================================
    @Nested
    @DisplayName("P3 - Finalizacion individual de revision por reportero")
    class FinalizacionIndividualRevision {

        /**
         * CE1: Finalizacion correcta.
         * Un reportero asignado puede finalizar su revision cuando
         * hay una solicitud activa y no ha finalizado previamente.
         */
        @Test
        @DisplayName("CE3.1: Finalizacion correcta inserta es_finalizacion=1")
        void finalizacionCorrectaInsertaRegistro() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Fin Rev");
            int idRep = getIdReportaje(EVENTO_1);
            solicitarRevision(EVENTO_1, REP_1);

            assertFalse(revisarModel.haFinalizadoRevision(idRep, REP_2),
                "Rep2 no debe haber finalizado aun");

            finalizarRevision(idRep, REP_2);

            assertTrue(revisarModel.haFinalizadoRevision(idRep, REP_2),
                "Rep2 debe haber finalizado tras llamar a finalizarRevision");
        }

        /**
         * CE2: No se puede finalizar sin solicitud activa.
         * Si no existe ninguna solicitud de revision, no se puede finalizar.
         */
        @Test
        @DisplayName("CE3.2: Finalizacion sin solicitud activa lanza excepcion")
        void finalizacionSinSolicitudLanzaExcepcion() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Sin Sol");
            int idRep = getIdReportaje(EVENTO_1);

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                finalizarRevision(idRep, REP_2)
            );
            assertTrue(ex.getMessage().contains("solicitud"),
                "Debe indicar que no hay solicitud activa");
        }

        /**
         * CE3: No se puede finalizar dos veces.
         * Si el reportero ya finalizo su revision, no puede volver a hacerlo.
         */
        @Test
        @DisplayName("CE3.3: Finalizacion duplicada lanza excepcion")
        void finalizacionDuplicadaLanzaExcepcion() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Doble Fin");
            int idRep = getIdReportaje(EVENTO_1);
            solicitarRevision(EVENTO_1, REP_1);
            finalizarRevision(idRep, REP_2);

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                finalizarRevision(idRep, REP_2)
            );
            assertTrue(ex.getMessage().contains("finalizado"),
                "Debe indicar que ya ha finalizado");
        }

        /**
         * CE4: Reportero no asignado no puede finalizar.
         */
        @Test
        @DisplayName("CE3.4: Reportero no asignado no puede finalizar revision")
        void reporteroNoAsignadoNoPuedeFinalizar() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Asig");
            int idRep = getIdReportaje(EVENTO_1);
            solicitarRevision(EVENTO_1, REP_1);

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                finalizarRevision(idRep, REP_3) // rep3 no esta asignado al evento 1
            );
            assertTrue(ex.getMessage().contains("asignado"),
                "Debe indicar que el reportero no esta asignado");
        }

        /**
         * CE5: Varios reporteros finalizan independientemente.
         * rep1 y rep2 finalizan de forma individual; todosHanFinalizadoRevision
         * solo devuelve true cuando ambos han finalizado.
         */
        @Test
        @DisplayName("CE3.5: Todos finalizan individualmente hasta completar la revision")
        void todosFinalizanIndividualmente() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Todos Fin");
            int idRep = getIdReportaje(EVENTO_1);
            solicitarRevision(EVENTO_1, REP_1);

            assertFalse(model.todosHanFinalizadoRevision(EVENTO_1),
                "No todos han finalizado al inicio");

            finalizarRevision(idRep, REP_1);
            assertFalse(model.todosHanFinalizadoRevision(EVENTO_1),
                "Aun falta rep2 por finalizar");

            finalizarRevision(idRep, REP_2);
            assertTrue(model.todosHanFinalizadoRevision(EVENTO_1),
                "Todos han finalizado tras que rep1 y rep2 completen");
        }

        /**
         * CE6: Anadir comentario de revision antes de finalizar.
         * Un reportero puede anadir comentarios mientras no haya finalizado.
         */
        @Test
        @DisplayName("CE3.6: Anadir comentario es posible antes de finalizar")
        void anadirComentarioAntesDeFinalizarEsValido() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Comentarios");
            int idRep = getIdReportaje(EVENTO_1);
            solicitarRevision(EVENTO_1, REP_1);

            revisarModel.addComentarioRevision(idRep, REP_2, "Falta ortografia en el parrafo 2");

            List<ComentarioRevisionDTO> comentarios = revisarModel.getComentariosRevision(idRep);
            assertEquals(1, comentarios.size(), "Debe existir 1 comentario de revision");
            assertEquals("Falta ortografia en el parrafo 2", comentarios.get(0).getComentario());
        }

        /**
         * CE7: No se puede anadir comentario tras finalizar revision propia.
         */
        @Test
        @DisplayName("CE3.7: Anadir comentario tras finalizar lanza excepcion")
        void anadirComentarioTrasFinalizar() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Coment Fin");
            int idRep = getIdReportaje(EVENTO_1);
            solicitarRevision(EVENTO_1, REP_1);
            finalizarRevision(idRep, REP_2);

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                revisarModel.addComentarioRevision(idRep, REP_2, "Comentario tardio")
            );
            assertTrue(ex.getMessage().contains("finalizado"),
                "Debe indicar que ya ha finalizado");
        }
    }

    // ====================================================================
    // P4 — FINALIZACION DEL REPORTAJE POR EL RESPONSABLE
    // ====================================================================
    @Nested
    @DisplayName("P4 - Finalizacion del reportaje por el responsable")
    class FinalizacionPorResponsable {

        /**
         * CE1: Finalizacion directa sin revision previa.
         * Si no se ha solicitado ninguna revision, el responsable puede
         * finalizar el reportaje directamente.
         */
        @Test
        @DisplayName("CE4.1: Finalizacion directa sin revision previa")
        void finalizacionDirectaSinRevision() {
            // Evento 2: rep3 es el unico, responsable, sin no-responsables
            entregarReportaje(EVENTO_2, REP_3, "Rep Fin Directo");
            int idRep = getIdReportaje(EVENTO_2);

            assertFalse(model.estaFinalizadoPorResponsableDeReportaje(idRep),
                "No debe estar finalizado antes");

            model.finalizarReportajeResponsable(EVENTO_2, REP_3);

            assertTrue(model.estaFinalizadoPorResponsableDeReportaje(idRep),
                "Debe estar finalizado tras la llamada");
        }

        /**
         * CE2: Finalizacion tras revision completa.
         * El responsable puede finalizar cuando todos los no-responsables
         * han finalizado su revision individual.
         */
        @Test
        @DisplayName("CE4.2: Finalizacion valida cuando todos han finalizado revision")
        void finalizacionValidaTrasRevisionCompleta() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Fin Completo");
            int idRep = getIdReportaje(EVENTO_1);
            solicitarRevision(EVENTO_1, REP_1);
            finalizarRevision(idRep, REP_1);
            finalizarRevision(idRep, REP_2);

            assertDoesNotThrow(() ->
                model.finalizarReportajeResponsable(EVENTO_1, REP_1),
                "No debe lanzar excepcion cuando todos han finalizado"
            );

            assertTrue(model.estaFinalizadoPorResponsableDeReportaje(idRep),
                "Debe quedar marcado como finalizado por el responsable");
        }

        /**
         * CE3: No se puede finalizar si hay revision pendiente.
         * Si alguno de los reporteros asignados no ha finalizado su revision,
         * el responsable no puede finalizar el reportaje.
         */
        @Test
        @DisplayName("CE4.3: Finalizacion bloqueada si revision pendiente")
        void finalizacionBloqueadaConRevisionPendiente() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Pendiente");
            solicitarRevision(EVENTO_1, REP_1);
            // rep2 NO finaliza su revision

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.finalizarReportajeResponsable(EVENTO_1, REP_1)
            );
            assertTrue(ex.getMessage().contains("revision"),
                "Debe indicar que no todos han finalizado la revision");
        }

        /**
         * CE4: Solo el responsable puede finalizar.
         * Un reportero no responsable no puede llamar a finalizarReportajeResponsable.
         */
        @Test
        @DisplayName("CE4.4: Finalizacion por no responsable lanza excepcion")
        void finalizacionPorNoResponsableLanzaExcepcion() {
            entregarReportaje(EVENTO_1, REP_1, "Rep No Resp");

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.finalizarReportajeResponsable(EVENTO_1, REP_2)
            );
            assertTrue(ex.getMessage().contains("responsable"),
                "Debe indicar que solo el responsable puede finalizar");
        }

        /**
         * CE5: No se puede finalizar dos veces.
         */
        @Test
        @DisplayName("CE4.5: Doble finalizacion por responsable lanza excepcion")
        void dobleFinalizacionLanzaExcepcion() {
            entregarReportaje(EVENTO_2, REP_3, "Rep Doble");
            model.finalizarReportajeResponsable(EVENTO_2, REP_3);

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.finalizarReportajeResponsable(EVENTO_2, REP_3)
            );
            assertTrue(ex.getMessage().contains("finalizado"),
                "Debe indicar que ya ha sido finalizado");
        }

        /**
         * CE6: El reportaje finalizado desaparece de la lista de trabajo.
         * Tras finalizarReportajeResponsable, el evento ya no debe aparecer
         * en getEventosAsignadosAReportero para ninguno de los reporteros.
         */
        @Test
        @DisplayName("CE4.6: Evento finalizado desaparece de la lista de trabajo")
        void eventoFinalizadoDesaparaceDeListaDeTrabajo() {
            entregarReportaje(EVENTO_2, REP_3, "Rep Desaparece");

            // Antes de finalizar, aparece en lista CON reportaje
            long antes = model.getEventosAsignadosAReportero(REP_3, true)
                .stream().filter(e -> e.getIdEvento() == EVENTO_2).count();
            assertEquals(1, antes, "Debe aparecer antes de finalizar");

            model.finalizarReportajeResponsable(EVENTO_2, REP_3);

            long despues = model.getEventosAsignadosAReportero(REP_3, true)
                .stream().filter(e -> e.getIdEvento() == EVENTO_2).count();
            assertEquals(0, despues, "No debe aparecer tras finalizar");
        }

        /**
         * CE7: isPendienteRevision devuelve false tras finalizacion del responsable.
         * El estado pendiente debe resolverse cuando el responsable finaliza.
         */
        @Test
        @DisplayName("CE4.7: isPendienteRevision es false tras finalizacion completa")
        void isPendienteRevisionFalseTrasFinalizacion() {
            entregarReportaje(EVENTO_1, REP_1, "Rep isPendiente");
            int idRep = getIdReportaje(EVENTO_1);
            solicitarRevision(EVENTO_1, REP_1);

            assertTrue(model.isPendienteRevision(idRep), "Debe estar pendiente tras solicitar");

            finalizarRevision(idRep, REP_1);
            finalizarRevision(idRep, REP_2);
            model.finalizarReportajeResponsable(EVENTO_1, REP_1);

            assertFalse(model.isPendienteRevision(idRep),
                "No debe estar pendiente tras finalizacion del responsable");
        }
    }

    // ====================================================================
    // P5 — GESTION DE MULTIMEDIA
    // ====================================================================
    @Nested
    @DisplayName("P5 - Gestion de contenido multimedia")
    class GestionMultimedia {

        private int idReportaje;

        @BeforeEach
        void prepararReportaje() {
            entregarReportaje(EVENTO_1, REP_1, "Rep Multimedia");
            idReportaje = getIdReportaje(EVENTO_1);
        }

        /**
         * CE1: Anadir IMAGEN correctamente.
         * Cualquier reportero asignado puede anadir multimedia.
         * El elemento se crea con estado BORRADOR.
         */
        @Test
        @DisplayName("CE5.1: Anadir imagen valida la crea en estado BORRADOR")
        void anadirImagenValidaEstadoBorrador() {
            model.addMultimedia(idReportaje, REP_2, EVENTO_1, "img/foto.jpg", "IMAGEN");

            List<MultimediaDTO> lista = model.getMultimedia(idReportaje);
            assertEquals(1, lista.size(), "Debe existir 1 elemento multimedia");
            assertEquals("img/foto.jpg", lista.get(0).getPath());
            assertEquals("IMAGEN", lista.get(0).getTipo());
            assertEquals("BORRADOR", lista.get(0).getEstado());
        }

        /**
         * CE2: Anadir VIDEO correctamente.
         */
        @Test
        @DisplayName("CE5.2: Anadir video valido crea elemento multimedia")
        void anadirVideoValido() {
            model.addMultimedia(idReportaje, REP_1, EVENTO_1, "vid/video.mp4", "VIDEO");

            List<MultimediaDTO> lista = model.getMultimedia(idReportaje);
            assertEquals(1, lista.size());
            assertEquals("VIDEO", lista.get(0).getTipo());
        }

        /**
         * CE3: Path duplicado en cualquier reportaje del sistema.
         * El path debe ser unico globalmente.
         */
        @Test
        @DisplayName("CE5.3: Path duplicado lanza excepcion")
        void pathDuplicadoLanzaExcepcion() {
            model.addMultimedia(idReportaje, REP_1, EVENTO_1, "img/unico.jpg", "IMAGEN");

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.addMultimedia(idReportaje, REP_2, EVENTO_1, "img/unico.jpg", "IMAGEN")
            );
            assertTrue(ex.getMessage().contains("path") || ex.getMessage().contains("existe"),
                "Debe indicar que el path ya existe");
        }

        /**
         * CE4: Reportero no asignado no puede anadir multimedia.
         */
        @Test
        @DisplayName("CE5.4: Reportero no asignado no puede anadir multimedia")
        void reporteroNoAsignadoNoAnade() {
            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.addMultimedia(idReportaje, REP_3, EVENTO_1, "img/otro.jpg", "IMAGEN")
            );
            assertTrue(ex.getMessage().contains("asignacion"),
                "Debe indicar que el reportero no tiene asignacion");
        }

        /**
         * CE5: Eliminar elemento propio en estado BORRADOR.
         * El autor del multimedia puede eliminar sus propios BORRADOR.
         */
        @Test
        @DisplayName("CE5.5: Eliminar propio BORRADOR es correcto")
        void eliminarPropioBorradorCorrecto() {
            model.addMultimedia(idReportaje, REP_2, EVENTO_1, "img/borrar.jpg", "IMAGEN");
            int idMult = model.getMultimedia(idReportaje).get(0).getId_multimedia();

            model.removeMultimedia(idMult, REP_2);

            assertTrue(model.getMultimedia(idReportaje).isEmpty(),
                "La lista debe estar vacia tras eliminar");
        }

        /**
         * CE6: No se puede eliminar multimedia ajeno.
         */
        @Test
        @DisplayName("CE5.6: Eliminar multimedia ajeno lanza excepcion")
        void eliminarMultimediaAjenoLanzaExcepcion() {
            model.addMultimedia(idReportaje, REP_1, EVENTO_1, "img/rep1.jpg", "IMAGEN");
            int idMult = model.getMultimedia(idReportaje).get(0).getId_multimedia();

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.removeMultimedia(idMult, REP_2) // rep2 no es el autor
            );
            assertTrue(ex.getMessage().contains("subio") || ex.getMessage().contains("autor"),
                "Debe indicar que no es el autor del contenido");
        }

        /**
         * CE7: No se puede eliminar multimedia en estado DEFINITIVO.
         */
        @Test
        @DisplayName("CE5.7: Eliminar DEFINITIVO lanza excepcion")
        void eliminarDefinitivoLanzaExcepcion() {
            model.addMultimedia(idReportaje, REP_1, EVENTO_1, "img/definitivo.jpg", "IMAGEN");
            int idMult = model.getMultimedia(idReportaje).get(0).getId_multimedia();
            model.cambiarEstadoMultimedia(idMult, REP_1, "DEFINITIVO");

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.removeMultimedia(idMult, REP_1)
            );
            assertTrue(ex.getMessage().contains("BORRADOR"),
                "Debe indicar que solo se eliminan elementos en BORRADOR");
        }

        /**
         * CE8: Cambio de estado de BORRADOR a DEFINITIVO por el autor.
         */
        @Test
        @DisplayName("CE5.8: Cambio de estado BORRADOR a DEFINITIVO por el autor")
        void cambioEstadoBorradorADefinitivo() {
            model.addMultimedia(idReportaje, REP_1, EVENTO_1, "img/cambio.jpg", "IMAGEN");
            int idMult = model.getMultimedia(idReportaje).get(0).getId_multimedia();
            assertEquals("BORRADOR", model.getMultimedia(idReportaje).get(0).getEstado());

            model.cambiarEstadoMultimedia(idMult, REP_1, "DEFINITIVO");

            assertEquals("DEFINITIVO", model.getMultimedia(idReportaje).get(0).getEstado());
        }

        /**
         * CE9: No se puede cambiar el estado de un elemento DEFINITIVO.
         */
        @Test
        @DisplayName("CE5.9: Cambio de estado en DEFINITIVO lanza excepcion")
        void cambioEstadoEnDefinitivoLanzaExcepcion() {
            model.addMultimedia(idReportaje, REP_1, EVENTO_1, "img/def.jpg", "IMAGEN");
            int idMult = model.getMultimedia(idReportaje).get(0).getId_multimedia();
            model.cambiarEstadoMultimedia(idMult, REP_1, "DEFINITIVO");

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.cambiarEstadoMultimedia(idMult, REP_1, "BORRADOR")
            );
            assertTrue(ex.getMessage().contains("BORRADOR"),
                "Debe indicar que solo se cambia estado de elementos en BORRADOR");
        }

        /**
         * CE10: No se puede cambiar el estado de multimedia ajeno.
         */
        @Test
        @DisplayName("CE5.10: Cambio de estado de multimedia ajeno lanza excepcion")
        void cambioEstadoAjenoLanzaExcepcion() {
            model.addMultimedia(idReportaje, REP_1, EVENTO_1, "img/ajeno.jpg", "IMAGEN");
            int idMult = model.getMultimedia(idReportaje).get(0).getId_multimedia();

            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                model.cambiarEstadoMultimedia(idMult, REP_2, "DEFINITIVO")
            );
            assertTrue(ex.getMessage().contains("subio") || ex.getMessage().contains("autor"),
                "Debe indicar que no es el autor");
        }
    }
}