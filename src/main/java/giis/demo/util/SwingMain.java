package giis.demo.util;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import giis.demo.controller.AsignarReporterosAEventosController;
import giis.demo.controller.OfrecerReportajeAgenciaComunicacionController;
import giis.demo.model.AsignarReporterosAEventosModel;
import giis.demo.model.OfrecerReportajeAgenciaComunicacionModel;
import giis.demo.tkrun.*;
import giis.demo.view.AsignarReporterosAEventosView;
import giis.demo.view.OfrecerReportajeAgenciaComunicacionView;

/**
 * Punto de entrada principal que incluye botones para la ejecucion de las pantallas 
 * de las aplicaciones de ejemplo
 * y acciones de inicializacion de la base de datos.
 * No sigue MVC pues es solamente temporal para que durante el desarrollo se tenga posibilidad
 * de realizar acciones de inicializacion
 */
public class SwingMain {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() { //NOSONAR codigo autogenerado
			public void run() {
				try {
					SwingMain window = new SwingMain();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace(); //NOSONAR codigo autogenerado
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SwingMain() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Main");
		frame.setBounds(0, 0, 287, 185);
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		/*JButton btnEjecutarTkrun = new JButton("Ejecutar giis.demo.tkrun");
		btnEjecutarTkrun.addActionListener(new ActionListener() { //NOSONAR codigo autogenerado
			public void actionPerformed(ActionEvent e) {
				CarrerasController controller=new CarrerasController(new CarrerasModel(), new CarrerasView());
				controller.initController();
			}
		});
		frame.getContentPane().add(btnEjecutarTkrun);
		*/
		
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		JButton btnInicializarBaseDeDatos = new JButton("Inicializar Base de Datos en Blanco");
		btnInicializarBaseDeDatos.addActionListener(new ActionListener() { //NOSONAR codigo autogenerado
			public void actionPerformed(ActionEvent e) {
				Database db=new Database();
				db.createDatabase(false);
			}
		});
		frame.getContentPane().add(btnInicializarBaseDeDatos);
			
		JButton btnCargarDatosIniciales = new JButton("Cargar Datos Iniciales para Pruebas");
		btnCargarDatosIniciales.addActionListener(new ActionListener() { //NOSONAR codigo autogenerado
			public void actionPerformed(ActionEvent e) {
				Database db=new Database();
				db.createDatabase(false);
				db.loadDatabase();
			}
		});
		frame.getContentPane().add(btnCargarDatosIniciales);

		
		JButton btnHU33550 = new JButton("HU 33550 - Asignar reporteros");
		btnHU33550.addActionListener(e -> {
			AsignarReporterosAEventosController controller =
				new AsignarReporterosAEventosController(
					new AsignarReporterosAEventosModel(),
					new AsignarReporterosAEventosView()
				);
			controller.initController();
		});
		frame.getContentPane().add(btnHU33550);

		JButton btnHU33552 = new JButton("HU 33552 - Ofrecer reportajes");
		btnHU33552.addActionListener(e -> {
			OfrecerReportajeAgenciaComunicacionController controller =
				new OfrecerReportajeAgenciaComunicacionController(
					new OfrecerReportajeAgenciaComunicacionModel(),
					new OfrecerReportajeAgenciaComunicacionView()
				);
			controller.initController();
		});
		frame.getContentPane().add(btnHU33552);
			
	}

	public JFrame getFrame() { return this.frame; }
	
}
