package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.ReporteroDTO;

public class ConsultarImporteEventoView {

	private JFrame frame;

	private JComboBox<ReporteroDTO> cbReporteros;

	private JTable tblEventos;
	private DefaultTableModel tmEventos;

	private JTextArea txtInfo;
	private JTextArea txtDesglose;

	public ConsultarImporteEventoView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame("HU 34366 - Consulta de importe de evento");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1000, 650);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		JLabel lblTitulo = new JLabel("Consulta de importe de evento");
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblTitulo.setBounds(20, 15, 350, 25);
		frame.getContentPane().add(lblTitulo);

		JLabel lblReportero = new JLabel("Reportero:");
		lblReportero.setBounds(20, 55, 100, 20);
		frame.getContentPane().add(lblReportero);

		cbReporteros = new JComboBox<>();
		cbReporteros.setBounds(100, 55, 300, 22);
		frame.getContentPane().add(cbReporteros);

		JLabel lblEventos = new JLabel("Eventos asignados");
		lblEventos.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventos.setBounds(20, 95, 200, 20);
		frame.getContentPane().add(lblEventos);

		tmEventos = new DefaultTableModel(
				new Object[] {"Nombre", "Fecha inicio", "Fecha fin", "Provincia", "País", "id_evento"}, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		tblEventos = new JTable(tmEventos);
		tblEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblEventos.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spEventos = new JScrollPane(tblEventos);
		spEventos.setBounds(20, 120, 940, 200);
		frame.getContentPane().add(spEventos);

		ocultarColumna(tblEventos, 5);

		JLabel lblDetalle = new JLabel("Detalle del importe");
		lblDetalle.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblDetalle.setBounds(20, 330, 200, 20);
		frame.getContentPane().add(lblDetalle);

		JLabel lblInfo = new JLabel("Datos evento / reportero");
		lblInfo.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblInfo.setBounds(20, 360, 220, 20);
		frame.getContentPane().add(lblInfo);

		txtInfo = new JTextArea();
		txtInfo.setEditable(false);
		txtInfo.setLineWrap(true);
		txtInfo.setWrapStyleWord(true);
		txtInfo.setFont(new Font("Monospaced", Font.BOLD, 14));
		txtInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JScrollPane spInfo = new JScrollPane(txtInfo);
		spInfo.setBounds(20, 390, 450, 180);
		frame.getContentPane().add(spInfo);

		JLabel lblDesglose = new JLabel("Desglose económico");
		lblDesglose.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblDesglose.setBounds(510, 360, 220, 20);
		frame.getContentPane().add(lblDesglose);

		txtDesglose = new JTextArea();
		txtDesglose.setEditable(false);
		txtDesglose.setLineWrap(true);
		txtDesglose.setWrapStyleWord(true);
		txtDesglose.setFont(new Font("Monospaced", Font.BOLD, 14));
		txtDesglose.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JScrollPane spDesglose = new JScrollPane(txtDesglose);
		spDesglose.setBounds(510, 390, 450, 180);
		frame.getContentPane().add(spDesglose);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void ocultarColumna(JTable table, int colIndex) {
		table.getColumnModel().getColumn(colIndex).setMaxWidth(0);
		table.getColumnModel().getColumn(colIndex).setMinWidth(0);
		table.getColumnModel().getColumn(colIndex).setPreferredWidth(0);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setReporteros(List<ReporteroDTO> reporteros) {
		DefaultComboBoxModel<ReporteroDTO> model = new DefaultComboBoxModel<>();
		for (ReporteroDTO r : reporteros) {
			model.addElement(r);
		}
		cbReporteros.setModel(model);
	}

	public ReporteroDTO getReporteroSeleccionado() {
		return (ReporteroDTO) cbReporteros.getSelectedItem();
	}

	public void setEventos(List<Object[]> eventos) {
		tmEventos.setRowCount(0);
		for (Object[] e : eventos) {
			tmEventos.addRow(e);
		}
		ocultarColumna(tblEventos, 5);
	}

	public Integer getIdEventoSeleccionado() {
		int row = tblEventos.getSelectedRow();
		if (row < 0) return null;
		return ((Number) tmEventos.getValueAt(row, 5)).intValue();
	}

	public void setDetalle(String info, String desglose) {
		txtInfo.setText(info == null ? "" : info);
		txtDesglose.setText(desglose == null ? "" : desglose);
	}

	public void clearDetalle() {
		txtInfo.setText("");
		txtDesglose.setText("");
	}

	public void addReporteroChangedListener(ActionListener l) {
		cbReporteros.addActionListener(l);
	}

	public void addEventosSelectionListener(ListSelectionListener l) {
		tblEventos.getSelectionModel().addListSelectionListener(l);
	}
}