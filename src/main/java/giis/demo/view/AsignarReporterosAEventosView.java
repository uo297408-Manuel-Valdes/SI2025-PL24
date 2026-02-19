package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.AgenciaDTO;
import giis.demo.model.EventoDTO;
import giis.demo.model.ReporteroDTO;

public class AsignarReporterosAEventosView {

	private JFrame frame;

	private JComboBox<AgenciaDTO> cbAgencias;

	private JTable tblEventos;
	private DefaultTableModel tmEventos;

	private JTable tblDisponibles;
	private DefaultTableModel tmDisponibles;

	private JTable tblAsignados;
	private DefaultTableModel tmAsignados;

	private JButton btnAsignar;
	private JButton btnAceptar;

	public AsignarReporterosAEventosView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame(" Asignar reporteros a eventos ");
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

		JLabel lblEventos = new JLabel("Eventos sin reporteros asignados");
		lblEventos.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventos.setBounds(20, 50, 350, 20);
		frame.getContentPane().add(lblEventos);

		tmEventos = new DefaultTableModel(new Object[] { "Nombre", "Fecha", "id_evento" }, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};

		tblEventos = new JTable(tmEventos);
		tblEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblEventos.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spEventos = new JScrollPane(tblEventos);
		spEventos.setBounds(20, 75, 930, 180);
		frame.getContentPane().add(spEventos);

		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(750);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(150);

		JLabel lblDisp = new JLabel("Reporteros disponibles");
		lblDisp.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblDisp.setBounds(20, 270, 200, 20);
		frame.getContentPane().add(lblDisp);

		JLabel lblAsig = new JLabel("Reporteros asignados (selección)");
		lblAsig.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblAsig.setBounds(500, 270, 300, 20);
		frame.getContentPane().add(lblAsig);

		tmDisponibles = new DefaultTableModel(new Object[] { "Nombre", "id_reportero" }, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblDisponibles = new JTable(tmDisponibles);
		tblDisponibles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		tblDisponibles.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane spDisp = new JScrollPane(tblDisponibles);
		spDisp.setBounds(20, 295, 450, 240);
		frame.getContentPane().add(spDisp);
		ocultarColumna(tblDisponibles, 1);

		tmAsignados = new DefaultTableModel(new Object[] { "Nombre", "id_reportero" }, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblAsignados = new JTable(tmAsignados);
		tblAsignados.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tblAsignados.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane spAsig = new JScrollPane(tblAsignados);
		spAsig.setBounds(500, 295, 450, 240);
		frame.getContentPane().add(spAsig);
		ocultarColumna(tblAsignados, 1);

		instalarTooltipsTabla(tblDisponibles);
		instalarTooltipsTabla(tblAsignados);

		btnAsignar = new JButton("Asignar ->");
		btnAsignar.setBounds(390, 545, 140, 30);
		frame.getContentPane().add(btnAsignar);

		btnAceptar = new JButton("Aceptar");
		btnAceptar.setBounds(810, 545, 140, 30);
		frame.getContentPane().add(btnAceptar);

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

	public void setEventos(List<EventoDTO> eventos) {
		tmEventos.setRowCount(0);
		for (EventoDTO e : eventos) {
			tmEventos.addRow(new Object[] { e.getNombre(), e.getFechaEvento(), e.getIdEvento() });
		}
		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(750);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(150);
	}

	public void setDisponibles(List<ReporteroDTO> reporteros) {
		tmDisponibles.setRowCount(0);
		for (ReporteroDTO r : reporteros) {
			tmDisponibles.addRow(new Object[] { r.getNombre(), r.getIdReportero() });
		}
		ocultarColumna(tblDisponibles, 1);
	}

	public void setAsignados(List<ReporteroDTO> reporteros) {
		tmAsignados.setRowCount(0);
		for (ReporteroDTO r : reporteros) {
			tmAsignados.addRow(new Object[] { r.getNombre(), r.getIdReportero() });
		}
		ocultarColumna(tblAsignados, 1);
	}


	public AgenciaDTO getAgenciaSeleccionada() {
		return (AgenciaDTO) cbAgencias.getSelectedItem();
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

	public int[] getFilasDisponiblesSeleccionadas() {
		return tblDisponibles.getSelectedRows();
	}

	public ReporteroDTO getReporteroDisponibleEnFila(int row) {
		String nombre = (String) tmDisponibles.getValueAt(row, 0);
		int id = ((Number) tmDisponibles.getValueAt(row, 1)).intValue();
		return new ReporteroDTO(id, 0, nombre);
	}

	public JFrame getFrame() { return frame; }


	public void addAgenciaChangedListener(ActionListener l) {
		cbAgencias.addActionListener(l);
	}

	public void addEventosSelectionListener(ListSelectionListener l) {
		tblEventos.getSelectionModel().addListSelectionListener(l);
	}

	public void addAsignarListener(ActionListener l) {
		btnAsignar.addActionListener(l);
	}

	public void addAceptarListener(ActionListener l) {
		btnAceptar.addActionListener(l);
	}


	public void showInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean confirm(String msg, String title) {
		int opt = JOptionPane.showConfirmDialog(frame, msg, title, JOptionPane.YES_NO_OPTION);
		return opt == JOptionPane.YES_OPTION;
	}
}
