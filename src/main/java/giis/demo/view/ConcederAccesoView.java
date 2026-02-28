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

public class ConcederAccesoView {

	private JFrame frame;

	
	private JTable            tblEventos;
	private DefaultTableModel tmEventos;

	private JComboBox<AgenciaDTO> cbAgencias;
	
	private JLabel            lblEmpresasAceptantes;
	private JTable            tblAceptantes;
	private DefaultTableModel tmAceptantes;


	private JTable            tblSeleccionadas;
	private DefaultTableModel tmSeleccionadas;


	private JButton btnConcederAcceso;

	
	public ConcederAccesoView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame(" Distribuir Reportaje ");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(780, 610);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		JLabel lblAgencia = new JLabel("Agencia de prensa:");
		lblAgencia.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblAgencia.setBounds(20, 15, 190, 20);
		frame.getContentPane().add(lblAgencia);

		cbAgencias = new JComboBox<>();
		cbAgencias.setBounds(160, 15, 350, 22);
		frame.getContentPane().add(cbAgencias);
		
		
		
		JLabel lblEventos = new JLabel("Eventos Cubiertos");
		lblEventos.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventos.setBounds(20, 50, 200, 20);
		frame.getContentPane().add(lblEventos);

		tmEventos = new DefaultTableModel(new Object[]{"Nombre", "Fecha", "id_evento"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblEventos = new JTable(tmEventos);
		tblEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblEventos.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spEventos = new JScrollPane(tblEventos);
		spEventos.setBounds(20, 90, 310, 190);
		frame.getContentPane().add(spEventos);

		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(190);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(100);

		
		

		lblEmpresasAceptantes = new JLabel("Empresas Aceptantes de: (ninguno)");
		lblEmpresasAceptantes.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEmpresasAceptantes.setBounds(360, 50, 390, 20);
		frame.getContentPane().add(lblEmpresasAceptantes);

		tmAceptantes = new DefaultTableModel(new Object[]{"Nombre de empresa", "id_empresa"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblAceptantes = new JTable(tmAceptantes);
		tblAceptantes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tblAceptantes.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane spAceptantes = new JScrollPane(tblAceptantes);
		spAceptantes.setBounds(360, 90, 390, 190);
		frame.getContentPane().add(spAceptantes);

		ocultarColumna(tblAceptantes, 1);
		instalarTooltipsTabla(tblAceptantes);

		
		

		JLabel lblSeleccionadas = new JLabel("Empresas seleccionadas");
		lblSeleccionadas.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblSeleccionadas.setBounds(20, 260, 200, 20);
		frame.getContentPane().add(lblSeleccionadas);

		tmSeleccionadas = new DefaultTableModel(new Object[]{"Nombre de empresa", "id_empresa"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		tblSeleccionadas = new JTable(tmSeleccionadas);
		tblSeleccionadas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblSeleccionadas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane spSeleccionadas = new JScrollPane(tblSeleccionadas);
		spSeleccionadas.setBounds(20, 285, 730, 170);
		frame.getContentPane().add(spSeleccionadas);

		ocultarColumna(tblSeleccionadas, 1);

		

		btnConcederAcceso = new JButton("Conceder Acceso");
		btnConcederAcceso.setBounds(300, 468, 180, 30);
		frame.getContentPane().add(btnConcederAcceso);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}


	public void setAgencias(List<AgenciaDTO> agencias) {
	    DefaultComboBoxModel<AgenciaDTO> model = new DefaultComboBoxModel<>();
	    for (AgenciaDTO a : agencias) model.addElement(a);
	    cbAgencias.setModel(model);
	}

	public AgenciaDTO getAgenciaSeleccionada() {
	    return (AgenciaDTO) cbAgencias.getSelectedItem();
	}

	public void addAgenciaChangedListener(ActionListener l) {
	    cbAgencias.addActionListener(l);
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


	

	public void setEventos(List<EventoDTO> eventos) {
		tmEventos.setRowCount(0);
		for (EventoDTO e : eventos)
			tmEventos.addRow(new Object[]{e.getNombre(), e.getFechaEvento(), e.getIdEvento()});
		ocultarColumna(tblEventos, 2);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(190);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(100);
	}

	public void setEmpresasAceptantes(List<EmpresaDTO> empresas, String nombreEvento) {
		lblEmpresasAceptantes.setText("Empresas Aceptantes de: " + nombreEvento);
		tmAceptantes.setRowCount(0);
		for (EmpresaDTO emp : empresas)
			tmAceptantes.addRow(new Object[]{emp.getNombre(), emp.getIdEmpresa()});
		ocultarColumna(tblAceptantes, 1);
	}

	public void setEmpresasSeleccionadas(List<EmpresaDTO> empresas) {
		tmSeleccionadas.setRowCount(0);
		for (EmpresaDTO emp : empresas)
			tmSeleccionadas.addRow(new Object[]{emp.getNombre(), emp.getIdEmpresa()});
		ocultarColumna(tblSeleccionadas, 1);
	}

	public void limpiarPanelDerecho() {
		lblEmpresasAceptantes.setText("Empresas Aceptantes de: (ninguno)");
		tmAceptantes.setRowCount(0);
		tmSeleccionadas.setRowCount(0);
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

	public int[] getFilasAceptantesSeleccionadas() {
		return tblAceptantes.getSelectedRows();
	}

	public EmpresaDTO getEmpresaAceptanteEnFila(int row) {
		String nombre  = (String) tmAceptantes.getValueAt(row, 0);
		int    idEmp   = ((Number) tmAceptantes.getValueAt(row, 1)).intValue();
		return new EmpresaDTO(idEmp, nombre);
	}

	public JFrame getFrame() { return frame; }

	
	public void addEventosSelectionListener(ListSelectionListener l) {
		tblEventos.getSelectionModel().addListSelectionListener(l);
	}


	public void addAceptantesSelectionListener(ListSelectionListener l) {
		tblAceptantes.getSelectionModel().addListSelectionListener(l);
	}

	public void addConcederAccesoListener(ActionListener l) {
		btnConcederAcceso.addActionListener(l);
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
