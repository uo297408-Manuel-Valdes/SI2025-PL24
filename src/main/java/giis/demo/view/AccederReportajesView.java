package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.AgenciaDTO;
import giis.demo.model.EmpresaDTO;
import giis.demo.model.EventoDTO;
import giis.demo.model.MultimediaDTO;

public class AccederReportajesView {
	
	private JFrame frame;

	private JComboBox<EmpresaDTO> cbEmpresas;

	private JTable tblReportajes;
	private DefaultTableModel tmReportajes;
	
	private JTable tblMultimedia;
	private DefaultTableModel tmMultimedia;

	private JLabel lblTitulo;
	private JLabel lblSubtitulo;
	private JLabel lblCuerpo;

	private JButton btnFinalizar;
	private JButton btnDescargar;
	
	public AccederReportajesView() {
		initialize();
	}
	
	private void initialize() {
		frame = new JFrame(" Acceder a reportajes de un evento ");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1250, 600);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);
		
		JLabel lblEmpresas = new JLabel("Empresas de comunicación:");
		lblEmpresas.setBounds(20, 15, 170, 20);
		frame.getContentPane().add(lblEmpresas);

		cbEmpresas = new JComboBox<>();
		cbEmpresas.setBounds(200, 15, 350, 22);
		frame.getContentPane().add(cbEmpresas);

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
		spReportajes.setBounds(20, 90, 250, 250);
		frame.getContentPane().add(spReportajes);
		ocultarColumna(tblReportajes, 2);
		
		JLabel lblMultimedia = new JLabel("Contenido Multimdeia");
		lblMultimedia.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblMultimedia.setBounds(850, 50, 350, 20);
		frame.getContentPane().add(lblMultimedia);
		
		tmMultimedia = new DefaultTableModel(new Object[] { "Ruta", "Tipo"}, 0) {
			@Override public boolean isCellEditable(int row, int col) { return false; }
		};
		
		tblMultimedia = new JTable(tmMultimedia);
		tblMultimedia.setRowSelectionAllowed(false);
		tblMultimedia.setColumnSelectionAllowed(false);
		tblMultimedia.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		
		JScrollPane spMultimedia = new JScrollPane(tblMultimedia);
		spMultimedia.setBounds(850, 90, 350, 250);
		frame.getContentPane().add(spMultimedia);

		
		JLabel l1= new JLabel("Título");
		l1.setFont(new Font("Tahoma", Font.BOLD, 12));
		l1.setBounds(350, 90, 350, 20);
		frame.getContentPane().add(l1);
		
		lblTitulo= new JLabel("-");
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblTitulo.setBounds(430, 90, 550, 20);
		frame.getContentPane().add(lblTitulo);
		
		JLabel l2= new JLabel("Subtítulo");
		l2.setFont(new Font("Tahoma", Font.BOLD, 12));
		l2.setBounds(350, 150, 450, 20);
		frame.getContentPane().add(l2);
		
		lblSubtitulo= new JLabel("-");
		lblSubtitulo.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblSubtitulo.setBounds(430, 150, 550, 20);
		frame.getContentPane().add(lblSubtitulo);
		
		JLabel l3= new JLabel("Cuerpo");
		l3.setFont(new Font("Tahoma", Font.BOLD, 12));
		l3.setBounds(350, 220, 550, 20);
		frame.getContentPane().add(l3);
		
		lblCuerpo= new JLabel("-");
		lblCuerpo.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblCuerpo.setBounds(430, 220, 550, 20);
		frame.getContentPane().add(lblCuerpo);
		
		btnFinalizar = new JButton("Finalizar visualización");
		btnFinalizar.setBounds(850, 500, 170, 30);
		frame.getContentPane().add(btnFinalizar);
		
		btnDescargar = new JButton("Descargar reportaje");
		btnDescargar.setBounds(1050, 500, 170, 30);
		frame.getContentPane().add(btnDescargar);
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
	}
	
	public JFrame getFrame() {return frame;}
	
	public void setEmpresas(List<EmpresaDTO> empresas) {
		DefaultComboBoxModel<EmpresaDTO> model = new DefaultComboBoxModel<>();
		for (EmpresaDTO a : empresas) model.addElement(a);
		cbEmpresas.setModel(model);
		
	}

	public void addEmpresaChangedListener(ActionListener l) {
		cbEmpresas.addActionListener(l);
		
	}

	public void addReportajesSelectionListener(ListSelectionListener l) {
		tblReportajes.getSelectionModel().addListSelectionListener(l);
		
	}

	public void addFinalizarListener(ActionListener l) {
		btnFinalizar.addActionListener(l);
		
	}
	
	public void addDescargarListener(ActionListener l) {
		btnDescargar.addActionListener(l);
		
	}
	
	public void setInfo(String titulo, String subtitulo, String cuerpo) {
		lblTitulo.setText(titulo == null ? "-" : titulo);
		lblSubtitulo.setText(subtitulo == null ? "-" : subtitulo);
		lblCuerpo.setText(cuerpo == null ? "-" : cuerpo);
		
	}
	
	private void ocultarColumna(JTable table, int colIndex) {
		table.getColumnModel().getColumn(colIndex).setMaxWidth(0);
		table.getColumnModel().getColumn(colIndex).setMinWidth(0);
		table.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
	}

	public EmpresaDTO getEmpresaSeleccionada() {
		return (EmpresaDTO) cbEmpresas.getSelectedItem();
	}

	public void setReportajes(List<EventoDTO> Reportaje) {
		tmReportajes.setRowCount(0);
		for (EventoDTO e : Reportaje) {
			tmReportajes.addRow(new Object[] { e.getNombre(), e.getFechaEvento(), e.getIdEvento() });
		}
		ocultarColumna(tblReportajes, 1);
		
	}
	
	public void setMultimedia(List<MultimediaDTO> Multimedia) {
		tmMultimedia.setRowCount(0);
		for (MultimediaDTO e : Multimedia) {
			tmMultimedia.addRow(new Object[] { e.getPath(), e.getTipo()});
		}
		
	}
	
	public void setMultimediaEmbargo(String aux) {
		tmMultimedia.setRowCount(0);
		tmMultimedia.addRow(new Object[] { aux, null});
		ocultarColumna(tblMultimedia, 1);
	}

	public void vaciarMultimedia() {
		tmMultimedia.setRowCount(0);
	}
	
	public Integer getIdReportajeSeleccionado() {
		int row = tblReportajes.getSelectedRow();
		if (row < 0) return null;
		return ((Number) tmReportajes.getValueAt(row, 2)).intValue();
	}

	public void showInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
	}
	
	
}
