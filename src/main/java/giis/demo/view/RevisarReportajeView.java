package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.ComentarioRevisionDTO;
import giis.demo.model.MultimediaDTO;
import giis.demo.model.ReportajeDTO;
import giis.demo.model.ReporteroDTO;

public class RevisarReportajeView {

	private JFrame frame;

	// Combo reportero
	private JComboBox<ReporteroDTO> cbReporteros;

	// Tabla de reportajes pendientes (izquierda)
	private JTable            tblReportajes;
	private DefaultTableModel tmReportajes;

	// Cabecera derecha
	private JLabel lblReporteroRegistrado;

	// Contenido del reportaje (derecha arriba)
	private JLabel    lblTitulo;
	private JLabel    lblSubtitulo;
	private JTextArea txtCuerpo;

	// Tabla multimedia (derecha abajo)
	private JTable            tblMultimedia;
	private DefaultTableModel tmMultimedia;

	// Tabla comentarios (centro abajo)
	private JTable            tblComentarios;
	private DefaultTableModel tmComentarios;

	// Controles de comentario y finalizacion
	private JTextField txtComentario;
	private JButton    btnAnadirComentario;
	private JButton    btnFinalizarRevision;

	public RevisarReportajeView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame(" Revisar Reportaje ");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(950, 720);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		// ── Combo reportero (arriba izquierda) ───────────────────────────
		JLabel lblReportero = new JLabel("Reportero:");
		lblReportero.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblReportero.setBounds(20, 15, 90, 20);
		frame.getContentPane().add(lblReportero);

		cbReporteros = new JComboBox<>();
		cbReporteros.setBounds(115, 15, 300, 22);
		frame.getContentPane().add(cbReporteros);

		// ── Reportero registrado (arriba derecha) ─────────────────────────
		lblReporteroRegistrado = new JLabel("Reportero Registrado: ");
		lblReporteroRegistrado.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		lblReporteroRegistrado.setBounds(500, 15, 430, 20);
		frame.getContentPane().add(lblReporteroRegistrado);

		// ── Tabla reportajes pendientes (izquierda) ───────────────────────
		JLabel lblPendientes = new JLabel("Reportajes pendientes de revision");
		lblPendientes.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPendientes.setBounds(20, 50, 270, 20);
		frame.getContentPane().add(lblPendientes);

		tmReportajes = new DefaultTableModel(new Object[]{"Titulo", "id_reportaje"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblReportajes = new JTable(tmReportajes);
		tblReportajes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblReportajes.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane spReportajes = new JScrollPane(tblReportajes);
		spReportajes.setBounds(20, 73, 270, 280);
		frame.getContentPane().add(spReportajes);

		ocultarColumna(tblReportajes, 1);
		tblReportajes.getColumnModel().getColumn(0).setPreferredWidth(250);

		// ── Contenido del reportaje (derecha arriba) ──────────────────────
		lblTitulo = new JLabel("Titulo: ");
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblTitulo.setBounds(310, 50, 620, 22);
		frame.getContentPane().add(lblTitulo);

		lblSubtitulo = new JLabel("Subtitulo: ");
		lblSubtitulo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSubtitulo.setBounds(310, 80, 400, 40);
		frame.getContentPane().add(lblSubtitulo);

		JLabel lblCuerpo = new JLabel("Cuerpo:");
		lblCuerpo.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCuerpo.setBounds(310, 128, 60, 20);
		frame.getContentPane().add(lblCuerpo);

		txtCuerpo = new JTextArea();
		txtCuerpo.setEditable(false);
		txtCuerpo.setLineWrap(true);
		txtCuerpo.setWrapStyleWord(true);
		txtCuerpo.setBackground(new java.awt.Color(245, 245, 245));
		JScrollPane spCuerpo = new JScrollPane(txtCuerpo);
		spCuerpo.setBounds(730, 50, 195, 280);
		frame.getContentPane().add(spCuerpo);

		// ── Tabla multimedia (derecha abajo) ──────────────────────────────
		JLabel lblMultimedia = new JLabel("Contenido Multimedia");
		lblMultimedia.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblMultimedia.setBounds(620, 363, 200, 20);
		frame.getContentPane().add(lblMultimedia);

		tmMultimedia = new DefaultTableModel(new Object[]{"Path", "Tipo", "Estado"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblMultimedia = new JTable(tmMultimedia);
		tblMultimedia.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblMultimedia.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane spMultimedia = new JScrollPane(tblMultimedia);
		spMultimedia.setBounds(620, 385, 305, 200);
		frame.getContentPane().add(spMultimedia);

		tblMultimedia.getColumnModel().getColumn(0).setPreferredWidth(150);
		tblMultimedia.getColumnModel().getColumn(1).setPreferredWidth(60);
		tblMultimedia.getColumnModel().getColumn(2).setPreferredWidth(80);

		// ── Tabla comentarios (centro abajo) ──────────────────────────────
		JLabel lblComentarios = new JLabel("Comentarios");
		lblComentarios.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblComentarios.setBounds(20, 363, 200, 20);
		frame.getContentPane().add(lblComentarios);

		tmComentarios = new DefaultTableModel(new Object[]{"Comentario", "Fecha", "id_comentario"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblComentarios = new JTable(tmComentarios);
		tblComentarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblComentarios.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spComentarios = new JScrollPane(tblComentarios);
		spComentarios.setBounds(20, 385, 580, 200);
		frame.getContentPane().add(spComentarios);

		ocultarColumna(tblComentarios, 2);
		tblComentarios.getColumnModel().getColumn(0).setPreferredWidth(420);
		tblComentarios.getColumnModel().getColumn(1).setPreferredWidth(140);

		// ── Controles inferiores ──────────────────────────────────────────
		btnAnadirComentario = new JButton("Anadir comentario");
		btnAnadirComentario.setBounds(20, 600, 150, 30);
		btnAnadirComentario.setEnabled(false);
		frame.getContentPane().add(btnAnadirComentario);

		txtComentario = new JTextField();
		txtComentario.setBounds(178, 600, 420, 30);
		txtComentario.setEnabled(false);
		frame.getContentPane().add(txtComentario);

		btnFinalizarRevision = new JButton("Finalizar Revision");
		btnFinalizarRevision.setBounds(780, 600, 150, 30);
		btnFinalizarRevision.setEnabled(false);
		frame.getContentPane().add(btnFinalizarRevision);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// ── Utilidades privadas ───────────────────────────────────────────────

	private void ocultarColumna(JTable table, int colIndex) {
		table.getColumnModel().getColumn(colIndex).setMaxWidth(0);
		table.getColumnModel().getColumn(colIndex).setMinWidth(0);
		table.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
	}

	// ── Setters de datos ──────────────────────────────────────────────────

	public void setReporteros(List<ReporteroDTO> reporteros) {
		DefaultComboBoxModel<ReporteroDTO> model = new DefaultComboBoxModel<>();
		for (ReporteroDTO r : reporteros) model.addElement(r);
		cbReporteros.setModel(model);
	}

	public void setReportajesPendientes(List<ReportajeDTO> reportajes) {
		tmReportajes.setRowCount(0);
		for (ReportajeDTO r : reportajes)
			tmReportajes.addRow(new Object[]{r.getTitulo(), r.getIdReportaje()});
		ocultarColumna(tblReportajes, 1);
		tblReportajes.getColumnModel().getColumn(0).setPreferredWidth(250);
	}

	public void setReporteroRegistrado(String nombre) {
		lblReporteroRegistrado.setText("Reportero Registrado: " + nombre);
	}

	public void setTitulo(String titulo) {
		lblTitulo.setText("Titulo: " + titulo);
	}

	public void setSubtitulo(String subtitulo) {
		lblSubtitulo.setText("<html>Subtitulo: " + subtitulo + "</html>");
	}

	public void setCuerpo(String cuerpo) {
		txtCuerpo.setText(cuerpo);
		txtCuerpo.setCaretPosition(0);
	}

	public void setMultimedia(List<MultimediaDTO> lista) {
		tmMultimedia.setRowCount(0);
		for (MultimediaDTO m : lista)
			tmMultimedia.addRow(new Object[]{m.getPath(), m.getTipo(), m.getEstado()});
		tblMultimedia.getColumnModel().getColumn(0).setPreferredWidth(150);
		tblMultimedia.getColumnModel().getColumn(1).setPreferredWidth(60);
		tblMultimedia.getColumnModel().getColumn(2).setPreferredWidth(80);
	}

	public void setComentarios(List<ComentarioRevisionDTO> comentarios) {
		tmComentarios.setRowCount(0);
		for (ComentarioRevisionDTO c : comentarios)
			tmComentarios.addRow(new Object[]{c.getComentario(), c.getFechaHora(), c.getIdComentario()});
		ocultarColumna(tblComentarios, 2);
		tblComentarios.getColumnModel().getColumn(0).setPreferredWidth(420);
		tblComentarios.getColumnModel().getColumn(1).setPreferredWidth(140);
	}

	public void setRevisionEnabled(boolean enabled) {
		btnAnadirComentario.setEnabled(enabled);
		btnFinalizarRevision.setEnabled(enabled);
		txtComentario.setEnabled(enabled);
	}

	public void limpiarPanelDerecho() {
		lblTitulo.setText("Titulo: ");
		lblSubtitulo.setText("Subtitulo: ");
		txtCuerpo.setText("");
		tmMultimedia.setRowCount(0);
		tmComentarios.setRowCount(0);
		txtComentario.setText("");
		setRevisionEnabled(false);
	}

	// ── Getters de datos ──────────────────────────────────────────────────

	public ReporteroDTO getReporteroSeleccionado() {
		return (ReporteroDTO) cbReporteros.getSelectedItem();
	}

	public Integer getIdReportajeSeleccionado() {
		int row = tblReportajes.getSelectedRow();
		if (row < 0) return null;
		return ((Number) tmReportajes.getValueAt(row, 1)).intValue();
	}

	public String getComentario() {
		return txtComentario.getText();
	}

	public void limpiarComentario() {
		txtComentario.setText("");
	}

	public JFrame getFrame() { return frame; }

	// ── Listeners ─────────────────────────────────────────────────────────

	public void addReporteroChangedListener(ActionListener l) {
		cbReporteros.addActionListener(l);
	}

	public void addReportajesSelectionListener(ListSelectionListener l) {
		tblReportajes.getSelectionModel().addListSelectionListener(l);
	}

	public void addAnadirComentarioListener(ActionListener l) {
		btnAnadirComentario.addActionListener(l);
	}

	public void addFinalizarRevisionListener(ActionListener l) {
		btnFinalizarRevision.addActionListener(l);
	}

	// ── Dialogos ──────────────────────────────────────────────────────────

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