package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.EmpresaDTO;
import giis.demo.model.OfrecimientoDTO;

public class GestionarOfrecimientosRecibidosView {

	private JFrame frame;

	private JComboBox<EmpresaDTO> cbEmpresas;

	private JTable tblOfrecimientos;
	private DefaultTableModel tmOfrecimientos;

	private JLabel lblEvento;
	private JLabel lblFecha;
	private JLabel lblAgencia;
	private JLabel lblDecision;

	private JButton btnAceptar;
	private JButton btnRechazar;

	public GestionarOfrecimientosRecibidosView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame("HU 33553 - Gestionar ofrecimientos recibidos (Empresa de comunicación)");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(980, 600);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		// ---------------- Cabecera ----------------
		JLabel lblEmpresa = new JLabel("Empresa de comunicación:");
		lblEmpresa.setBounds(20, 15, 170, 20);
		frame.getContentPane().add(lblEmpresa);

		cbEmpresas = new JComboBox<>();
		cbEmpresas.setBounds(190, 15, 360, 22);
		frame.getContentPane().add(cbEmpresas);

		JLabel lblInfo = new JLabel("Mostrando solo ofrecimientos PENDIENTES (sin decisión)");
		lblInfo.setFont(new Font("Tahoma", Font.ITALIC, 12));
		lblInfo.setBounds(20, 45, 400, 18);
		frame.getContentPane().add(lblInfo);

		// ---------------- Tabla ofrecimientos ----------------
		JLabel lblTabla = new JLabel("Ofrecimientos pendientes");
		lblTabla.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblTabla.setBounds(20, 70, 300, 20);
		frame.getContentPane().add(lblTabla);

		// Columnas visibles: Evento, Fecha, Agencia
		// Columnas ocultas: id_ofrecimiento, id_evento, id_agencia, id_empresa
		tmOfrecimientos = new DefaultTableModel(
				new Object[] { "Evento", "Fecha", "Agencia", "id_ofrecimiento", "id_evento", "id_agencia", "id_empresa" }, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};

		tblOfrecimientos = new JTable(tmOfrecimientos);
		tblOfrecimientos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblOfrecimientos.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane sp = new JScrollPane(tblOfrecimientos);
		sp.setBounds(20, 95, 930, 230);
		frame.getContentPane().add(sp);

		// Ocultar ids
		ocultarColumna(tblOfrecimientos, 3);
		ocultarColumna(tblOfrecimientos, 4);
		ocultarColumna(tblOfrecimientos, 5);
		ocultarColumna(tblOfrecimientos, 6);

		// Ajuste ancho
		tblOfrecimientos.getColumnModel().getColumn(0).setPreferredWidth(520);
		tblOfrecimientos.getColumnModel().getColumn(1).setPreferredWidth(150);
		tblOfrecimientos.getColumnModel().getColumn(2).setPreferredWidth(260);

		// ---------------- Panel detalle ----------------
		JPanel detail = new JPanel();
		detail.setLayout(null);
		detail.setBounds(20, 340, 930, 150);
		detail.setBorder(BorderFactory.createTitledBorder("Detalle del ofrecimiento seleccionado"));
		frame.getContentPane().add(detail);

		JLabel l1 = new JLabel("Evento:");
		l1.setBounds(20, 30, 80, 20);
		detail.add(l1);
		lblEvento = new JLabel("-");
		lblEvento.setBounds(90, 30, 400, 20);
		detail.add(lblEvento);

		JLabel l2 = new JLabel("Fecha:");
		l2.setBounds(520, 30, 80, 20);
		detail.add(l2);
		lblFecha = new JLabel("-");
		lblFecha.setBounds(580, 30, 200, 20);
		detail.add(lblFecha);

		JLabel l3 = new JLabel("Agencia:");
		l3.setBounds(20, 65, 80, 20);
		detail.add(l3);
		lblAgencia = new JLabel("-");
		lblAgencia.setBounds(90, 65, 400, 20);
		detail.add(lblAgencia);

		JLabel l4 = new JLabel("Decisión actual:");
		l4.setBounds(520, 65, 110, 20);
		detail.add(l4);
		lblDecision = new JLabel("Pendiente");
		lblDecision.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblDecision.setBounds(630, 65, 150, 20);
		detail.add(lblDecision);

		// ---------------- Botones ----------------
		btnAceptar = new JButton("Aceptar ofrecimiento");
		btnAceptar.setBounds(520, 105, 190, 28);
		detail.add(btnAceptar);

		btnRechazar = new JButton("Rechazar ofrecimiento");
		btnRechazar.setBounds(720, 105, 190, 28);
		detail.add(btnRechazar);

		// Al inicio deshabilitados hasta que selecciones una fila
		setDecisionButtonsEnabled(false);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void ocultarColumna(JTable table, int colIndex) {
		table.getColumnModel().getColumn(colIndex).setMaxWidth(0);
		table.getColumnModel().getColumn(colIndex).setMinWidth(0);
		table.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
	}

	// ---------------- setters (cargar datos) ----------------

	public void setEmpresas(List<EmpresaDTO> empresas) {
		DefaultComboBoxModel<EmpresaDTO> model = new DefaultComboBoxModel<>();
		for (EmpresaDTO e : empresas) model.addElement(e);
		cbEmpresas.setModel(model);
	}

	public void setOfrecimientos(List<OfrecimientoDTO> ofrecimientos) {
		tmOfrecimientos.setRowCount(0);
		for (OfrecimientoDTO o : ofrecimientos) {
			tmOfrecimientos.addRow(new Object[] {
					o.getNombreEvento(),
					o.getFechaEvento(),
					o.getNombreAgencia(),
					o.getIdOfrecimiento(),
					o.getIdEvento(),
					o.getIdAgencia(),
					o.getIdEmpresa()
			});
		}
		// re-ocultar por si se recrea modelo
		ocultarColumna(tblOfrecimientos, 3);
		ocultarColumna(tblOfrecimientos, 4);
		ocultarColumna(tblOfrecimientos, 5);
		ocultarColumna(tblOfrecimientos, 6);
	}

	// ---------------- getters selección ----------------

	public EmpresaDTO getEmpresaSeleccionada() {
		return (EmpresaDTO) cbEmpresas.getSelectedItem();
	}

	public Integer getIdOfrecimientoSeleccionado() {
		int row = tblOfrecimientos.getSelectedRow();
		if (row < 0) return null;
		return ((Number) tmOfrecimientos.getValueAt(row, 3)).intValue();
	}

	public String getEventoSeleccionado() {
		int row = tblOfrecimientos.getSelectedRow();
		if (row < 0) return null;
		return (String) tmOfrecimientos.getValueAt(row, 0);
	}

	public String getFechaEventoSeleccionado() {
		int row = tblOfrecimientos.getSelectedRow();
		if (row < 0) return null;
		return (String) tmOfrecimientos.getValueAt(row, 1);
	}

	public String getAgenciaSeleccionadaEnTabla() {
		int row = tblOfrecimientos.getSelectedRow();
		if (row < 0) return null;
		return (String) tmOfrecimientos.getValueAt(row, 2);
	}

	public JFrame getFrame() { return frame; }

	// ---------------- detalle UI ----------------

	public void setDetalle(String evento, String fecha, String agencia, String decision) {
		lblEvento.setText(evento == null ? "-" : evento);
		lblFecha.setText(fecha == null ? "-" : fecha);
		lblAgencia.setText(agencia == null ? "-" : agencia);
		lblDecision.setText(decision == null ? "Pendiente" : decision);
	}

	public void setDecisionButtonsEnabled(boolean enabled) {
		btnAceptar.setEnabled(enabled);
		btnRechazar.setEnabled(enabled);
	}

	// ---------------- listeners ----------------

	public void addEmpresaChangedListener(ActionListener l) {
		cbEmpresas.addActionListener(l);
	}

	public void addOfrecimientosSelectionListener(ListSelectionListener l) {
		tblOfrecimientos.getSelectionModel().addListSelectionListener(l);
	}

	public void addAceptarListener(ActionListener l) {
		btnAceptar.addActionListener(l);
	}

	public void addRechazarListener(ActionListener l) {
		btnRechazar.addActionListener(l);
	}

	// ---------------- dialogs ----------------

	public void showInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean confirm(String msg, String title) {
		int opt = JOptionPane.showConfirmDialog(frame, msg, title, JOptionPane.YES_NO_OPTION);
		return opt == JOptionPane.YES_OPTION;
	}
}
