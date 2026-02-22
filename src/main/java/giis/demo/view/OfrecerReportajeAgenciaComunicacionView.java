package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import giis.demo.model.AgenciaDTO;
import giis.demo.model.EmpresaDTO;
import giis.demo.model.EventoDTO;


public class OfrecerReportajeAgenciaComunicacionView {

	private JFrame frame;

	private JComboBox<AgenciaDTO> cbAgencias;

	private JTable tblReportajes;
	private DefaultTableModel tmReportajes;

	private JTable tblDisponibles;
	private DefaultTableModel tmDisponibles;

	private JTable tblSeleccionados;
	private DefaultTableModel tmSeleccionados;

	private JButton btnAsignar;
	private JButton btnOfrecer;

	public OfrecerReportajeAgenciaComunicacionView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame(" Ofrecer reportajes a empresas de comunicación ");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(980, 650);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		JLabel lblAgencia = new JLabel("Agencia de prensa:");
		lblAgencia.setBounds(20, 15, 140, 20);
		frame.getContentPane().add(lblAgencia);

		cbAgencias = new JComboBox<>();
		cbAgencias.setBounds(160, 15, 350, 22);
		frame.getContentPane().add(cbAgencias);

		JLabel lblReportajes = new JLabel("Reportajes de eventos");
		lblReportajes.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblReportajes.setBounds(20, 50, 350, 20);
		frame.getContentPane().add(lblReportajes);

		tmReportajes = new DefaultTableModel(new Object[] { "Nombre", "Fecha", "id_evento" }, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};

		tblReportajes = new JTable(tmReportajes);
		tblReportajes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblReportajes.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spReportajes = new JScrollPane(tblReportajes);
		spReportajes.setBounds(20, 75, 300, 400);
		frame.getContentPane().add(spReportajes);

		ocultarColumna(tblReportajes, 2);
		tblReportajes.getColumnModel().getColumn(0).setPreferredWidth(750);
		tblReportajes.getColumnModel().getColumn(1).setPreferredWidth(150);

		JLabel lblDisp = new JLabel("Empresas disponibles");
		lblDisp.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblDisp.setBounds(330, 50, 200, 20);
		frame.getContentPane().add(lblDisp);

		JLabel lblAsig = new JLabel("Empresas seleccionadas");
		lblAsig.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblAsig.setBounds(640, 50, 300, 20);
		frame.getContentPane().add(lblAsig);

		tmDisponibles = new DefaultTableModel(new Object[] { "Nombre", "id_empresa" }, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblDisponibles = new JTable(tmDisponibles);
		tblDisponibles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		tblDisponibles.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane spDisp = new JScrollPane(tblDisponibles);
		spDisp.setBounds(330, 75, 300, 400);
		frame.getContentPane().add(spDisp);
		ocultarColumna(tblDisponibles, 1);

		tmSeleccionados = new DefaultTableModel(new Object[] { "Nombre", "id_empresa" }, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblSeleccionados = new JTable(tmSeleccionados);
		tblSeleccionados.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tblSeleccionados.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane spAsig = new JScrollPane(tblSeleccionados);
		spAsig.setBounds(640, 75, 300, 400);
		frame.getContentPane().add(spAsig);
		ocultarColumna(tblSeleccionados, 1);

		instalarTooltipsTabla(tblDisponibles);
		instalarTooltipsTabla(tblSeleccionados);

		btnAsignar = new JButton("Asignar ->");
		btnAsignar.setBounds(390, 545, 140, 30);
		frame.getContentPane().add(btnAsignar);

		btnOfrecer = new JButton("Ofrecer");
		btnOfrecer.setBounds(810, 545, 140, 30);
		frame.getContentPane().add(btnOfrecer);

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
			@Override public void mouseMoved(java.awt.event.MouseEvent e) {
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


	public void setAgencias(List<AgenciaDTO> agencias) {
		DefaultComboBoxModel<AgenciaDTO> model = new DefaultComboBoxModel<>();
		for (AgenciaDTO a : agencias) model.addElement(a);
		cbAgencias.setModel(model);
	}

	public void setReportajes(List<EventoDTO> Reportaje) {
		tmReportajes.setRowCount(0);
		for (EventoDTO e : Reportaje) {
			tmReportajes.addRow(new Object[] { e.getNombre(), e.getFechaEvento(), e.getIdEvento() });
		}
		ocultarColumna(tblReportajes, 2);
		tblReportajes.getColumnModel().getColumn(0).setPreferredWidth(750);
		tblReportajes.getColumnModel().getColumn(1).setPreferredWidth(150);
	}

	public void setDisponibles(List<EmpresaDTO> reporteros) {
		tmDisponibles.setRowCount(0);
		for (EmpresaDTO r : reporteros) {
			tmDisponibles.addRow(new Object[] { r.getNombre(), r.getIdEmpresa() });
		}
		ocultarColumna(tblDisponibles, 1);
	}

	public void setAsignados(List<EmpresaDTO> reporteros) {
		tmSeleccionados.setRowCount(0);
		for (EmpresaDTO r : reporteros) {
			tmSeleccionados.addRow(new Object[] { r.getNombre(), r.getIdEmpresa() });
		}
		ocultarColumna(tblSeleccionados, 1);
	}


	public AgenciaDTO getAgenciaSeleccionada() {
		return (AgenciaDTO) cbAgencias.getSelectedItem();
	}

	public Integer getIdReportajeSeleccionado() {
		int row = tblReportajes.getSelectedRow();
		if (row < 0) return null;
		return ((Number) tmReportajes.getValueAt(row, 2)).intValue();
	}

	public String getNombreReportajeSeleccionado() {
		int row = tblReportajes.getSelectedRow();
		if (row < 0) return null;
		return (String) tmReportajes.getValueAt(row, 0);
	}

	public String getFechaReportajeSeleccionado() {
		int row = tblReportajes.getSelectedRow();
		if (row < 0) return null;
		return (String) tmReportajes.getValueAt(row, 1);
	}

	public int[] getFilasDisponiblesSeleccionadas() {
		return tblDisponibles.getSelectedRows();
	}

	public EmpresaDTO getEmpresaDisponibleEnFila(int row) {
		String nombre = (String) tmDisponibles.getValueAt(row, 0);
		int id = ((Number) tmDisponibles.getValueAt(row, 1)).intValue();
		return new EmpresaDTO(id, nombre);
	}

	public JFrame getFrame() { return frame; }


	public void addAgenciaChangedListener(ActionListener l) {
		cbAgencias.addActionListener(l);
	}

	public void addReportajesSelectionListener(ListSelectionListener l) {
		tblReportajes.getSelectionModel().addListSelectionListener(l);
	}

	public void addAsignarListener(ActionListener l) {
		btnAsignar.addActionListener(l);
	}

	public void addOfrecerListener(ActionListener l) {
		btnOfrecer.addActionListener(l);
	}


	public void showInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean confirm(String msg, String title) {
		int opt = JOptionPane.showConfirmDialog(frame, msg, title, JOptionPane.YES_NO_OPTION);
		return opt == JOptionPane.YES_OPTION;
	}
}

