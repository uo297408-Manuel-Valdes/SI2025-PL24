package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.EventoDTO;

public class EntregarReportajesDeEventosView {

	private JFrame frame;

	// Tabla de eventos
	private JTable            tblEventos;
	private DefaultTableModel tmEventos;

	// Cabecera derecha
	private JLabel lblEventoSeleccionado;

	// Campos del formulario
	private JTextField txtAutor;       // Solo lectura — nombre del reportero logueado
	private JTextField txtTitulo;
	private JButton    btnValidarTitulo;
	private JTextArea  txtSubtitulo;
	private JTextArea  txtCuerpo;
	private JTextField txtCambios;     // Descripción de cambios de esta versión

	// Botón principal
	private JButton btnEntregar;

	public EntregarReportajesDeEventosView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame(" Redactar Reportaje ");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(780, 640);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		
		JLabel lblEventos = new JLabel("Lista de Eventos");
		lblEventos.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventos.setBounds(20, 15, 250, 20);
		frame.getContentPane().add(lblEventos);

		tmEventos = new DefaultTableModel(new Object[]{"Nombre", "Fecha", "id_evento"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblEventos = new JTable(tmEventos);
		tblEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblEventos.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spEventos = new JScrollPane(tblEventos);
		spEventos.setBounds(20, 40, 280, 520);
		frame.getContentPane().add(spEventos);

		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(170);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(90);

		
		// Evento seleccionado (cabecera)
		lblEventoSeleccionado = new JLabel("Evento seleccionado: (ninguno)");
		lblEventoSeleccionado.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventoSeleccionado.setBounds(320, 15, 430, 20);
		frame.getContentPane().add(lblEventoSeleccionado);

		// Autor (solo lectura — nombre del reportero)
		JLabel lblAutor = new JLabel("Autor:");
		lblAutor.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblAutor.setBounds(320, 50, 60, 22);
		frame.getContentPane().add(lblAutor);

		txtAutor = new JTextField();
		txtAutor.setEditable(false);
		txtAutor.setBackground(new java.awt.Color(240, 240, 240));
		txtAutor.setBounds(390, 50, 350, 22);
		frame.getContentPane().add(txtAutor);

		// Titulo
		JLabel lblTitulo = new JLabel("Titulo:");
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblTitulo.setBounds(320, 85, 60, 22);
		frame.getContentPane().add(lblTitulo);

		txtTitulo = new JTextField();
		txtTitulo.setBounds(390, 85, 280, 22);
		frame.getContentPane().add(txtTitulo);

		btnValidarTitulo = new JButton("Validar");
		btnValidarTitulo.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnValidarTitulo.setForeground(new java.awt.Color(0, 150, 0));
		btnValidarTitulo.setToolTipText("Validar título");
		btnValidarTitulo.setBounds(676, 83, 80, 26);
		frame.getContentPane().add(btnValidarTitulo);

		// Subtitulo
		JLabel lblSubtitulo = new JLabel("Subtitulo");
		lblSubtitulo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSubtitulo.setBounds(320, 122, 80, 20);
		frame.getContentPane().add(lblSubtitulo);

		txtSubtitulo = new JTextArea();
		txtSubtitulo.setLineWrap(true);
		txtSubtitulo.setWrapStyleWord(true);
		JScrollPane spSubtitulo = new JScrollPane(txtSubtitulo);
		spSubtitulo.setBounds(320, 145, 420, 75);
		frame.getContentPane().add(spSubtitulo);

		// Cuerpo
		JLabel lblCuerpo = new JLabel("Cuerpo");
		lblCuerpo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCuerpo.setBounds(320, 232, 80, 20);
		frame.getContentPane().add(lblCuerpo);

		txtCuerpo = new JTextArea();
		txtCuerpo.setLineWrap(true);
		txtCuerpo.setWrapStyleWord(true);
		JScrollPane spCuerpo = new JScrollPane(txtCuerpo);
		spCuerpo.setBounds(320, 255, 420, 185);
		frame.getContentPane().add(spCuerpo);

		// Cambios (descripción de la versión — obligatorio en VERSION_REPORTAJE)
		JLabel lblCambios = new JLabel("Cambios:");
		lblCambios.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCambios.setBounds(320, 452, 65, 22);
		frame.getContentPane().add(lblCambios);

		txtCambios = new JTextField();
		txtCambios.setToolTipText("Describe brevemente los cambios de esta versión");
		txtCambios.setBounds(390, 452, 350, 22);
		frame.getContentPane().add(txtCambios);

		// Botón Entregar
		btnEntregar = new JButton("Entregar");
		btnEntregar.setBounds(560, 495, 140, 30);
		frame.getContentPane().add(btnEntregar);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	
	private void ocultarColumna(JTable table, int colIndex) {
		table.getColumnModel().getColumn(colIndex).setMaxWidth(0);
		table.getColumnModel().getColumn(colIndex).setMinWidth(0);
		table.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
	}

	
	public void setEventos(List<EventoDTO> eventos) {
		tmEventos.setRowCount(0);
		for (EventoDTO e : eventos) {
			tmEventos.addRow(new Object[]{e.getNombre(), e.getFechaEvento(), e.getIdEvento()});
		}
		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(170);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(90);
	}

	public void setLabelEventoSeleccionado(String nombreEvento) {
		lblEventoSeleccionado.setText("Evento seleccionado: " + nombreEvento);
	}

	/** Muestra el nombre del reportero en el campo Autor (solo lectura). */
	public void setAutor(String nombre)    { txtAutor.setText(nombre); }
	public void setTitulo(String v)        { txtTitulo.setText(v); }
	public void setSubtitulo(String v)     { txtSubtitulo.setText(v); }
	public void setCuerpo(String v)        { txtCuerpo.setText(v); }
	public void setCambios(String v)       { txtCambios.setText(v); }

	/** Limpia los campos editables al cambiar de evento. */
	public void limpiarFormulario() {
		txtTitulo.setText("");
		txtSubtitulo.setText("");
		txtCuerpo.setText("");
		txtCambios.setText("");
		lblEventoSeleccionado.setText("Evento seleccionado: (ninguno)");
	}

	
	public Integer getIdEventoSeleccionado() {
		int row = tblEventos.getSelectedRow();
		if (row < 0) return null;
		return ((Number) tmEventos.getValueAt(row, 2)).intValue();
	}

	public String getNombreEventoSeleccionado() {
		int row = tblEventos.getSelectedRow();
		if (row < 0) return null;
		return (String) tmEventos.getValueAt(row, 0);
	}

	public String getFechaEventoSeleccionado() {
		int row = tblEventos.getSelectedRow();
		if (row < 0) return null;
		return (String) tmEventos.getValueAt(row, 1);
	}

	public String getTitulo()    { return txtTitulo.getText(); }
	public String getSubtitulo() { return txtSubtitulo.getText(); }
	public String getCuerpo()    { return txtCuerpo.getText(); }
	public String getCambios()   { return txtCambios.getText(); }

	public JFrame getFrame() { return frame; }

	
	public void addEventosSelectionListener(ListSelectionListener l) {
		tblEventos.getSelectionModel().addListSelectionListener(l);
	}

	public void addValidarTituloListener(ActionListener l) {
		btnValidarTitulo.addActionListener(l);
	}

	public void addEntregarListener(ActionListener l) {
		btnEntregar.addActionListener(l);
	}

	
	public void showInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
	}

	public void showError(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public boolean confirm(String msg, String title) {
		return JOptionPane.showConfirmDialog(frame, msg, title,
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
}