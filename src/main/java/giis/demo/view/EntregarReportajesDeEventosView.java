package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.EventoDTO;
import giis.demo.model.ReporteroDTO;

public class EntregarReportajesDeEventosView {

	private JFrame frame;


	private JComboBox<ReporteroDTO> cbReporteros;


	private JComboBox<String> cbFiltro;


	private JTable            tblEventos;
	private DefaultTableModel tmEventos;


	private JLabel lblEventoSeleccionado;

	private JTextField txtAutor;       
	private JTextField txtTitulo;      
	private JButton    btnValidarTitulo;
	private JTextArea  txtSubtitulo;
	private JTextArea  txtCuerpo;
	
	private JButton btnEntregar;

	public EntregarReportajesDeEventosView() {
		initialize();
	}

	private void initialize() {
	
		frame = new JFrame(" Entregar Reportajes De Eventos ");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(780, 680);
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
		spEventos.setBounds(20, 102, 280, 510);
		frame.getContentPane().add(spEventos);

		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(170);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(90);

	
		lblEventoSeleccionado = new JLabel("Evento seleccionado: (ninguno)");
		lblEventoSeleccionado.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventoSeleccionado.setBounds(320, 78, 430, 20);
		frame.getContentPane().add(lblEventoSeleccionado);

		JLabel lblAutor = new JLabel("Autor:");
		lblAutor.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblAutor.setBounds(320, 108, 60, 22);
		frame.getContentPane().add(lblAutor);

		txtAutor = new JTextField();
		txtAutor.setEditable(false);
		txtAutor.setBackground(new java.awt.Color(240, 240, 240));
		txtAutor.setBounds(390, 108, 350, 22);
		frame.getContentPane().add(txtAutor);

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


		JLabel lblCuerpo = new JLabel("Cuerpo");
		lblCuerpo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCuerpo.setBounds(320, 298, 80, 20);
		frame.getContentPane().add(lblCuerpo);

		txtCuerpo = new JTextArea();
		txtCuerpo.setLineWrap(true);
		txtCuerpo.setWrapStyleWord(true);
		JScrollPane spCuerpo = new JScrollPane(txtCuerpo);
		spCuerpo.setBounds(320, 320, 420, 270);
		frame.getContentPane().add(spCuerpo);


		btnEntregar = new JButton("Entregar");
		btnEntregar.setBounds(560, 608, 140, 30);
		frame.getContentPane().add(btnEntregar);

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

	public void limpiarFormulario() {
		txtTitulo.setText("");
		txtSubtitulo.setText("");
		txtCuerpo.setText("");
		setTituloEditable(true);
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

	public String getTitulo()    { return txtTitulo.getText(); }
	public String getSubtitulo() { return txtSubtitulo.getText(); }
	public String getCuerpo()    { return txtCuerpo.getText(); }

	public JFrame getFrame() { return frame; }


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