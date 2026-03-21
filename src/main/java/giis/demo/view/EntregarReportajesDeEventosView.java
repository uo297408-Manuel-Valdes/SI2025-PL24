package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.EventoDTO;
import giis.demo.model.MultimediaDTO;
import giis.demo.model.ReporteroDTO;

public class EntregarReportajesDeEventosView {

	private JFrame frame;

	// Combo reportero (arriba izquierda)
	private JComboBox<ReporteroDTO> cbReporteros;

	// Combo filtro
	private JComboBox<String> cbFiltro;

	// Tabla de eventos (izquierda arriba)
	private JTable            tblEventos;
	private DefaultTableModel tmEventos;

	// Lista multimedia (izquierda abajo)
	private JTable            tblMultimedia;
	private DefaultTableModel tmMultimedia;
	private JButton           btnAnadir;
	private JButton           btnEliminar;
	
	// Cabecera derecha
	private JLabel lblEventoSeleccionado;

	// Campos del formulario (derecha)
	private JTextField txtAutor;
	private JTextField txtTitulo;
	private JButton    btnValidarTitulo;
	private JTextArea  txtSubtitulo;
	private JTextArea  txtCuerpo;

	// Boton principal
	private JButton btnEntregar;
	private JButton btnCambiarEstado;
	private JButton btnSolicitarRevision;
	public EntregarReportajesDeEventosView() {
		initialize();
	}

	private void initialize() {
		// FRAME PRIMERO
		frame = new JFrame(" Entregar Reportajes De Eventos ");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(780, 760);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		JLabel lblReportero = new JLabel("Reportero:");
		lblReportero.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblReportero.setBounds(20, 15, 90, 20);
		frame.getContentPane().add(lblReportero);

		cbReporteros = new JComboBox<>();
		cbReporteros.setBounds(115, 15, 350, 22);
		frame.getContentPane().add(cbReporteros);

		cbFiltro = new JComboBox<>(new String[]{"Eventos SIN reportaje", "Eventos CON reportaje"});
		cbFiltro.setBounds(20, 45, 240, 22);
		frame.getContentPane().add(cbFiltro);

		JLabel lblEventos = new JLabel("Lista de Eventos");
		lblEventos.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventos.setBounds(20, 78, 250, 20);
		frame.getContentPane().add(lblEventos);

		tmEventos = new DefaultTableModel(new Object[]{"Nombre", "Fecha", "id_evento"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblEventos = new JTable(tmEventos);
		tblEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblEventos.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spEventos = new JScrollPane(tblEventos);
		spEventos.setBounds(20, 100, 280, 200);
		frame.getContentPane().add(spEventos);

		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(170);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(90);

		JLabel lblMultimedia = new JLabel("Lista de contenidos multimedia");
		lblMultimedia.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblMultimedia.setBounds(20, 315, 280, 20);
		frame.getContentPane().add(lblMultimedia);

		tmMultimedia = new DefaultTableModel(new Object[]{"Path", "Tipo", "Estado", "id_multimedia"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblMultimedia = new JTable(tmMultimedia);
		tblMultimedia.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblMultimedia.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spMultimedia = new JScrollPane(tblMultimedia);
		spMultimedia.setBounds(20, 338, 280, 180);
		frame.getContentPane().add(spMultimedia);

		ocultarColumna(tblMultimedia, 3);
		tblMultimedia.getColumnModel().getColumn(0).setPreferredWidth(200);
		tblMultimedia.getColumnModel().getColumn(1).setPreferredWidth(60);

		btnAnadir = new JButton("Añadir");
		btnAnadir.setBounds(20, 528, 130, 26);
		frame.getContentPane().add(btnAnadir);

		btnEliminar = new JButton("Eliminar");
		btnEliminar.setBounds(165, 528, 130, 26);
		frame.getContentPane().add(btnEliminar);


		// Evento seleccionado
		lblEventoSeleccionado = new JLabel("Evento seleccionado: (ninguno)");
		lblEventoSeleccionado.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventoSeleccionado.setBounds(320, 78, 430, 20);
		frame.getContentPane().add(lblEventoSeleccionado);

		// Autor (solo lectura)
		JLabel lblAutor = new JLabel("Autor:");
		lblAutor.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblAutor.setBounds(320, 108, 60, 22);
		frame.getContentPane().add(lblAutor);

		txtAutor = new JTextField();
		txtAutor.setEditable(false);
		txtAutor.setBackground(new java.awt.Color(240, 240, 240));
		txtAutor.setBounds(390, 108, 350, 22);
		frame.getContentPane().add(txtAutor);

		// Titulo
		JLabel lblTitulo = new JLabel("Titulo:");
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblTitulo.setBounds(320, 145, 60, 22);
		frame.getContentPane().add(lblTitulo);

		txtTitulo = new JTextField();
		txtTitulo.setBounds(390, 145, 253, 22);
		frame.getContentPane().add(txtTitulo);

		btnValidarTitulo = new JButton("Validar");
		btnValidarTitulo.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnValidarTitulo.setForeground(new java.awt.Color(0, 150, 0));
		btnValidarTitulo.setToolTipText("Validar titulo");
		btnValidarTitulo.setBounds(649, 143, 91, 26);
		frame.getContentPane().add(btnValidarTitulo);

		// Subtitulo
		JLabel lblSubtitulo = new JLabel("Subtitulo");
		lblSubtitulo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSubtitulo.setBounds(320, 182, 80, 20);
		frame.getContentPane().add(lblSubtitulo);

		txtSubtitulo = new JTextArea();
		txtSubtitulo.setLineWrap(true);
		txtSubtitulo.setWrapStyleWord(true);
		JScrollPane spSubtitulo = new JScrollPane(txtSubtitulo);
		spSubtitulo.setBounds(320, 205, 420, 80);
		frame.getContentPane().add(spSubtitulo);

		// Cuerpo
		JLabel lblCuerpo = new JLabel("Cuerpo");
		lblCuerpo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCuerpo.setBounds(320, 298, 80, 20);
		frame.getContentPane().add(lblCuerpo);

		txtCuerpo = new JTextArea();
		txtCuerpo.setLineWrap(true);
		txtCuerpo.setWrapStyleWord(true);
		JScrollPane spCuerpo = new JScrollPane(txtCuerpo);
		spCuerpo.setBounds(320, 320, 420, 265);
		frame.getContentPane().add(spCuerpo);

		// Boton Entregar
		btnEntregar = new JButton("Entregar");
		btnEntregar.setBounds(560, 600, 140, 30);
		frame.getContentPane().add(btnEntregar);

		btnCambiarEstado = new JButton("Cambiar Estado");
		btnCambiarEstado.setBounds(20, 558, 280, 26);
		frame.getContentPane().add(btnCambiarEstado);
		
		btnSolicitarRevision = new JButton("Solicitar Revision");
		btnSolicitarRevision.setBounds(320, 600, 180, 30);
		btnSolicitarRevision.setEnabled(false);
		frame.getContentPane().add(btnSolicitarRevision);
		
		
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		
		
		
	}


	private void ocultarColumna(JTable table, int colIndex) {
		table.getColumnModel().getColumn(colIndex).setMaxWidth(0);
		table.getColumnModel().getColumn(colIndex).setMinWidth(0);
		table.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
	}


	public void setReporteros(List<ReporteroDTO> reporteros) {
		DefaultComboBoxModel<ReporteroDTO> model = new DefaultComboBoxModel<>();
		for (ReporteroDTO r : reporteros) model.addElement(r);
		cbReporteros.setModel(model);
	}

	public void setEventos(List<EventoDTO> eventos) {
		tmEventos.setRowCount(0);
		for (EventoDTO e : eventos)
			tmEventos.addRow(new Object[]{e.getNombre(), e.getFechaEvento(), e.getIdEvento()});
		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(170);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(90);
	}

	public void setMultimedia(List<MultimediaDTO> lista) {
		tmMultimedia.setRowCount(0);
		for (MultimediaDTO m : lista)
			tmMultimedia.addRow(new Object[]{m.getPath(), m.getTipo(), m.getEstado(), m.getId_multimedia()});
		ocultarColumna(tblMultimedia, 3);
		tblMultimedia.getColumnModel().getColumn(0).setPreferredWidth(200);
		tblMultimedia.getColumnModel().getColumn(1).setPreferredWidth(60);
	}

	public void setLabelEventoSeleccionado(String nombreEvento) {
		lblEventoSeleccionado.setText("Evento seleccionado: " + nombreEvento);
	}

	public void setAutor(String nombre)    { txtAutor.setText(nombre); }
	public void setTitulo(String v)        { txtTitulo.setText(v); }
	public void setSubtitulo(String v)     { txtSubtitulo.setText(v); }
	public void setCuerpo(String v)        { txtCuerpo.setText(v); }

	public void setTituloEditable(boolean editable) {
		txtTitulo.setEditable(editable);
		txtTitulo.setBackground(editable
			? java.awt.Color.WHITE
			: new java.awt.Color(240, 240, 240));
	}

	/** Habilita o deshabilita los botones de multimedia. */
	public void setMultimediaEnabled(boolean enabled) {
		btnAnadir.setEnabled(enabled);
		btnEliminar.setEnabled(enabled);
		btnCambiarEstado.setEnabled(enabled);
	}

	public void limpiarFormulario() {
		txtTitulo.setText("");
		txtSubtitulo.setText("");
		txtCuerpo.setText("");
		setTituloEditable(true);
		setMultimediaEnabled(false);
		tmMultimedia.setRowCount(0);
		lblEventoSeleccionado.setText("Evento seleccionado: (ninguno)");
	}


	public ReporteroDTO getReporteroSeleccionado() {
		return (ReporteroDTO) cbReporteros.getSelectedItem();
	}

	public String getFiltroSeleccionado() {
		return (String) cbFiltro.getSelectedItem();
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

	/** Devuelve el id_multimedia de la fila seleccionada, o -1 si no hay ninguna. */
	public int getIdMultimediaSeleccionado() {
		int row = tblMultimedia.getSelectedRow();
		if (row < 0) return -1;
		return ((Number) tmMultimedia.getValueAt(row, 3)).intValue();
	}

	public String getTitulo()    { return txtTitulo.getText(); }
	public String getSubtitulo() { return txtSubtitulo.getText(); }
	public String getCuerpo()    { return txtCuerpo.getText(); }

	public JFrame getFrame() { return frame; }


	/**
	 * Muestra un dialogo para introducir path y tipo de multimedia.
	 * Devuelve String[]{path, tipo} o null si se cancela.
	 */
	public String[] mostrarDialogoAnadir() {
		JTextField txtPath = new JTextField(30);
		JComboBox<String> cbTipo = new JComboBox<>(new String[]{"IMAGEN", "VIDEO"});

		JPanel panel = new JPanel();
		panel.setLayout(new java.awt.GridLayout(2, 2, 6, 6));
		panel.add(new JLabel("Path:"));
		panel.add(txtPath);
		panel.add(new JLabel("Tipo:"));
		panel.add(cbTipo);

		int result = JOptionPane.showConfirmDialog(frame, panel,
			"Añadir contenido multimedia", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result != JOptionPane.OK_OPTION) return null;

		String path = txtPath.getText().trim();
		String tipo = (String) cbTipo.getSelectedItem();
		return new String[]{path, tipo};
	}


	
	
	public void addReporteroChangedListener(ActionListener l) {
		cbReporteros.addActionListener(l);
	}

	public void addFiltroChangedListener(ActionListener l) {
		cbFiltro.addActionListener(l);
	}

	public void addEventosSelectionListener(ListSelectionListener l) {
		tblEventos.getSelectionModel().addListSelectionListener(l);
	}

	public void addValidarTituloListener(ActionListener l) {
		btnValidarTitulo.addActionListener(l);
	}

	public void addEntregarListener(ActionListener l) {
		btnEntregar.addActionListener(l);
	}

	public void addAnadirMultimediaListener(ActionListener l) {
		btnAnadir.addActionListener(l);
	}

	public void addEliminarMultimediaListener(ActionListener l) {
		btnEliminar.addActionListener(l);
	}
	
	// Nuevo getter
	public String getEstadoMultimediaSeleccionado() {
	    int row = tblMultimedia.getSelectedRow();
	    if (row < 0) return null;
	    return (String) tmMultimedia.getValueAt(row, 2);
	}

	// Nuevo listener
	public void addCambiarEstadoListener(ActionListener l) {
	    btnCambiarEstado.addActionListener(l);
	}
	
	public void setPendienteRevision(boolean pendiente) {
	    btnSolicitarRevision.setEnabled(!pendiente);
	    btnSolicitarRevision.setText(pendiente ? "Pendiente de revision" : "Solicitar Revision");
	}
	
	public void setFiltroSeleccionado(String valor) {
	    cbFiltro.setSelectedItem(valor);
	}
	
	public void addSolicitarRevisionListener(ActionListener l) {
	    btnSolicitarRevision.addActionListener(l);
	}
	

	public void showInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Informacion", JOptionPane.INFORMATION_MESSAGE);
	}

	public void showError(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public boolean confirm(String msg, String title) {
		return JOptionPane.showConfirmDialog(frame, msg, title,
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
}


