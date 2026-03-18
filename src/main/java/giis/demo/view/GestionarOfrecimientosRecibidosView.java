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
	private JLabel lblEspecialidadesEmpresa;
	private JCheckBox chkSoloCoincidentes;
	private JComboBox<String> cbTematicas;

	private JTable tblOfrecimientos;
	private DefaultTableModel tmOfrecimientos;

	private JLabel lblEvento;
	private JLabel lblFecha;
	private JLabel lblAgencia;
	private JLabel lblDecision;
	private JLabel lblTematicas;

	private JButton btnAceptar;
	private JButton btnRechazar;

	public GestionarOfrecimientosRecibidosView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame("HU 34029 - Filtrar ofrecimientos por temática");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1080, 650);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		JLabel lblEmpresa = new JLabel("Empresa de comunicación:");
		lblEmpresa.setBounds(20, 15, 170, 20);
		frame.getContentPane().add(lblEmpresa);

		cbEmpresas = new JComboBox<>();
		cbEmpresas.setBounds(190, 15, 300, 22);
		frame.getContentPane().add(cbEmpresas);

		JLabel lblEsp = new JLabel("Especialidades:");
		lblEsp.setBounds(520, 15, 100, 20);
		frame.getContentPane().add(lblEsp);

		lblEspecialidadesEmpresa = new JLabel("-");
		lblEspecialidadesEmpresa.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEspecialidadesEmpresa.setBounds(620, 15, 420, 20);
		frame.getContentPane().add(lblEspecialidadesEmpresa);

		chkSoloCoincidentes = new JCheckBox("Mostrar solo eventos con temática coincidente con la empresa");
		chkSoloCoincidentes.setBounds(20, 50, 380, 22);
		frame.getContentPane().add(chkSoloCoincidentes);

		JLabel lblFiltroTematica = new JLabel("Filtrar por temática:");
		lblFiltroTematica.setBounds(430, 50, 120, 20);
		frame.getContentPane().add(lblFiltroTematica);

		cbTematicas = new JComboBox<>();
		cbTematicas.setBounds(550, 50, 250, 22);
		frame.getContentPane().add(cbTematicas);

		JLabel lblTabla = new JLabel("Ofrecimientos recibidos");
		lblTabla.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblTabla.setBounds(20, 90, 300, 20);
		frame.getContentPane().add(lblTabla);

		tmOfrecimientos = new DefaultTableModel(
				new Object[] { "Evento", "Fecha", "Agencia", "Temáticas", "id_ofrecimiento", "id_evento", "id_agencia", "id_empresa" }, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		tblOfrecimientos = new JTable(tmOfrecimientos);
		tblOfrecimientos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblOfrecimientos.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane sp = new JScrollPane(tblOfrecimientos);
		sp.setBounds(20, 115, 1020, 250);
		frame.getContentPane().add(sp);

		ocultarColumna(tblOfrecimientos, 4);
		ocultarColumna(tblOfrecimientos, 5);
		ocultarColumna(tblOfrecimientos, 6);
		ocultarColumna(tblOfrecimientos, 7);

		tblOfrecimientos.getColumnModel().getColumn(0).setPreferredWidth(300);
		tblOfrecimientos.getColumnModel().getColumn(1).setPreferredWidth(120);
		tblOfrecimientos.getColumnModel().getColumn(2).setPreferredWidth(180);
		tblOfrecimientos.getColumnModel().getColumn(3).setPreferredWidth(360);

		instalarTooltipsTabla(tblOfrecimientos);

		JPanel detail = new JPanel();
		detail.setLayout(null);
		detail.setBounds(20, 385, 1020, 170);
		detail.setBorder(BorderFactory.createTitledBorder("Detalle del ofrecimiento seleccionado"));
		frame.getContentPane().add(detail);

		JLabel l1 = new JLabel("Evento:");
		l1.setBounds(20, 30, 80, 20);
		detail.add(l1);
		lblEvento = new JLabel("-");
		lblEvento.setBounds(90, 30, 380, 20);
		detail.add(lblEvento);

		JLabel l2 = new JLabel("Fecha:");
		l2.setBounds(520, 30, 80, 20);
		detail.add(l2);
		lblFecha = new JLabel("-");
		lblFecha.setBounds(580, 30, 180, 20);
		detail.add(lblFecha);

		JLabel l3 = new JLabel("Agencia:");
		l3.setBounds(20, 65, 80, 20);
		detail.add(l3);
		lblAgencia = new JLabel("-");
		lblAgencia.setBounds(90, 65, 380, 20);
		detail.add(lblAgencia);

		JLabel l4 = new JLabel("Decisión actual:");
		l4.setBounds(520, 65, 110, 20);
		detail.add(l4);
		lblDecision = new JLabel("Pendiente");
		lblDecision.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblDecision.setBounds(630, 65, 150, 20);
		detail.add(lblDecision);

		JLabel l5 = new JLabel("Temáticas:");
		l5.setBounds(20, 100, 80, 20);
		detail.add(l5);
		lblTematicas = new JLabel("-");
		lblTematicas.setBounds(90, 100, 670, 20);
		detail.add(lblTematicas);

		btnAceptar = new JButton("Aceptar ofrecimiento");
		btnAceptar.setBounds(620, 130, 180, 28);
		detail.add(btnAceptar);

		btnRechazar = new JButton("Rechazar ofrecimiento");
		btnRechazar.setBounds(820, 130, 180, 28);
		detail.add(btnRechazar);

		setDecisionButtonsEnabled(false);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void ocultarColumna(JTable table, int colIndex) {
		table.getColumnModel().getColumn(colIndex).setMaxWidth(0);
		table.getColumnModel().getColumn(colIndex).setMinWidth(0);
		table.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
	}

	private void instalarTooltipsTabla(JTable table) {
		table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override
			public void mouseMoved(java.awt.event.MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				if (row >= 0 && col >= 0) {
					Object v = table.getValueAt(row, col);
					table.setToolTipText(v == null ? null : v.toString());
				} else {
					table.setToolTipText(null);
				}
			}
		});
	}

	public void setEmpresas(List<EmpresaDTO> empresas) {
		DefaultComboBoxModel<EmpresaDTO> model = new DefaultComboBoxModel<>();
		for (EmpresaDTO e : empresas) {
			model.addElement(e);
		}
		cbEmpresas.setModel(model);
	}

	public void setEspecialidadesEmpresa(String tematicas) {
		if (tematicas == null || tematicas.isBlank()) {
			lblEspecialidadesEmpresa.setText("-");
		} else {
			lblEspecialidadesEmpresa.setText(tematicas);
		}
	}

	public void setTematicas(List<String> tematicas) {
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		model.addElement("(Todas)");
		if (tematicas != null) {
			for (String t : tematicas) {
				model.addElement(t);
			}
		}
		cbTematicas.setModel(model);
	}

	public void setOfrecimientos(List<OfrecimientoDTO> ofrecimientos) {
		tmOfrecimientos.setRowCount(0);
		for (OfrecimientoDTO o : ofrecimientos) {
			tmOfrecimientos.addRow(new Object[] {
					o.getNombreEvento(),
					o.getFechaEvento(),
					o.getNombreAgencia(),
					o.getTematicasTexto(),
					o.getIdOfrecimiento(),
					o.getIdEvento(),
					o.getIdAgencia(),
					o.getIdEmpresa()
			});
		}
		ocultarColumna(tblOfrecimientos, 4);
		ocultarColumna(tblOfrecimientos, 5);
		ocultarColumna(tblOfrecimientos, 6);
		ocultarColumna(tblOfrecimientos, 7);
	}

	public EmpresaDTO getEmpresaSeleccionada() {
		return (EmpresaDTO) cbEmpresas.getSelectedItem();
	}

	public boolean isFiltroCoincidentesActivo() {
		return chkSoloCoincidentes.isSelected();
	}

	public String getTematicaSeleccionada() {
		Object selected = cbTematicas.getSelectedItem();
		if (selected == null) return null;
		String value = selected.toString();
		return "(Todas)".equals(value) ? null : value;
	}

	public Integer getIdOfrecimientoSeleccionado() {
		int row = tblOfrecimientos.getSelectedRow();
		if (row < 0) return null;
		return ((Number) tmOfrecimientos.getValueAt(row, 4)).intValue();
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

	public String getTematicasEventoSeleccionado() {
		int row = tblOfrecimientos.getSelectedRow();
		if (row < 0) return null;
		return (String) tmOfrecimientos.getValueAt(row, 3);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setDetalle(String evento, String fecha, String agencia, String decision, String tematicas) {
		lblEvento.setText(evento == null ? "-" : evento);
		lblFecha.setText(fecha == null ? "-" : fecha);
		lblAgencia.setText(agencia == null ? "-" : agencia);
		lblDecision.setText(decision == null || decision.isBlank() ? "Pendiente" : decision);
		lblTematicas.setText(tematicas == null || tematicas.isBlank() ? "-" : tematicas);
	}

	public void clearDetalle() {
		setDetalle(null, null, null, null, null);
	}

	public void setDecisionButtonsEnabled(boolean enabled) {
		btnAceptar.setEnabled(enabled);
		btnRechazar.setEnabled(enabled);
	}

	public void addEmpresaChangedListener(ActionListener l) {
		cbEmpresas.addActionListener(l);
	}

	public void addFiltroCoincidentesChangedListener(ActionListener l) {
		chkSoloCoincidentes.addActionListener(l);
	}

	public void addTematicaChangedListener(ActionListener l) {
		cbTematicas.addActionListener(l);
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

	public void showInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean confirm(String msg, String title) {
		int opt = JOptionPane.showConfirmDialog(frame, msg, title, JOptionPane.YES_NO_OPTION);
		return opt == JOptionPane.YES_OPTION;
	}

	public void removeFilaSeleccionada() {
		int row = tblOfrecimientos.getSelectedRow();
		if (row >= 0) {
			tmOfrecimientos.removeRow(row);
		}
	}

	public boolean hayFilasEnTabla() {
		return tmOfrecimientos.getRowCount() > 0;
	}
}