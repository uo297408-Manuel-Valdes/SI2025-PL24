package giis.demo.view;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import giis.demo.model.AgenciaDTO;
import giis.demo.model.EventoDTO;
import giis.demo.model.ReporteroDTO;

public class AsignarReporterosAEventosView {

	private JFrame frame;

	private JComboBox<AgenciaDTO> cbAgencias;
	private JComboBox<String> cbFiltroEventos;
	private JCheckBox chkSoloEspecialistas;

	private JCheckBox chkTipoBasico;
	private JCheckBox chkTipoGrafico;
	private JCheckBox chkTipoCamarografo;

	private JTable tblEventos;
	private DefaultTableModel tmEventos;

	private JTable tblDisponibles;
	private DefaultTableModel tmDisponibles;

	private JTable tblAsignados;
	private DefaultTableModel tmAsignados;

	private JLabel lblEstadoAsignacionValor;

	private JButton btnAsignar;
	private JButton btnEliminar;
	private JButton btnMarcarResponsable;
	private JButton btnFinalizarAsignacion;
	private JButton btnGuardar;

	public AsignarReporterosAEventosView() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame("HU 34358 - Finalizar asignación de reporteros");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1240, 790);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		JLabel lblTitulo = new JLabel("Asignación de reporteros a eventos");
		lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblTitulo.setBounds(20, 10, 450, 24);
		frame.getContentPane().add(lblTitulo);

		JLabel lblAgencia = new JLabel("Agencia de prensa:");
		lblAgencia.setBounds(20, 45, 140, 20);
		frame.getContentPane().add(lblAgencia);

		cbAgencias = new JComboBox<>();
		cbAgencias.setBounds(160, 45, 300, 24);
		frame.getContentPane().add(cbAgencias);

		JLabel lblFiltroEventos = new JLabel("Filtro eventos:");
		lblFiltroEventos.setBounds(490, 45, 90, 20);
		frame.getContentPane().add(lblFiltroEventos);

		cbFiltroEventos = new JComboBox<>(new String[] {
				"Eventos SIN reporteros asignados",
				"Eventos CON reporteros asignados"
		});
		cbFiltroEventos.setBounds(580, 45, 300, 24);
		frame.getContentPane().add(cbFiltroEventos);

		JPanel pnlFiltros = new JPanel();
		pnlFiltros.setLayout(null);
		pnlFiltros.setBorder(new TitledBorder("Filtros de reporteros"));
		pnlFiltros.setBounds(20, 80, 1190, 95);
		frame.getContentPane().add(pnlFiltros);

		chkSoloEspecialistas = new JCheckBox("Mostrar solo reporteros con temática coincidente con el evento");
		chkSoloEspecialistas.setBounds(20, 25, 430, 22);
		pnlFiltros.add(chkSoloEspecialistas);

		JLabel lblTipos = new JLabel("Filtrar por tipo:");
		lblTipos.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblTipos.setBounds(20, 55, 100, 20);
		pnlFiltros.add(lblTipos);

		chkTipoBasico = new JCheckBox("Básico");
		chkTipoBasico.setBounds(130, 55, 90, 22);
		pnlFiltros.add(chkTipoBasico);

		chkTipoGrafico = new JCheckBox("Gráfico");
		chkTipoGrafico.setBounds(230, 55, 90, 22);
		pnlFiltros.add(chkTipoGrafico);

		chkTipoCamarografo = new JCheckBox("Camarógrafo");
		chkTipoCamarografo.setBounds(330, 55, 120, 22);
		pnlFiltros.add(chkTipoCamarografo);

		JLabel lblEventos = new JLabel("Eventos");
		lblEventos.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEventos.setBounds(20, 190, 350, 20);
		frame.getContentPane().add(lblEventos);

		tmEventos = new DefaultTableModel(new Object[] { "Nombre", "Fecha", "Temáticas", "id_evento" }, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		tblEventos = new JTable(tmEventos);
		tblEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblEventos.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spEventos = new JScrollPane(tblEventos);
		spEventos.setBounds(20, 215, 1190, 180);
		frame.getContentPane().add(spEventos);

		ocultarColumna(tblEventos, 3);
		tblEventos.getColumnModel().getColumn(0).setPreferredWidth(360);
		tblEventos.getColumnModel().getColumn(1).setPreferredWidth(120);
		tblEventos.getColumnModel().getColumn(2).setPreferredWidth(620);

		JLabel lblEstadoAsignacion = new JLabel("Estado de la asignación:");
		lblEstadoAsignacion.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEstadoAsignacion.setBounds(20, 405, 160, 20);
		frame.getContentPane().add(lblEstadoAsignacion);

		lblEstadoAsignacionValor = new JLabel("-");
		lblEstadoAsignacionValor.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblEstadoAsignacionValor.setBounds(185, 405, 160, 20);
		frame.getContentPane().add(lblEstadoAsignacionValor);

		JLabel lblDisp = new JLabel("Reporteros disponibles");
		lblDisp.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblDisp.setBounds(20, 435, 200, 20);
		frame.getContentPane().add(lblDisp);

		JLabel lblAsig = new JLabel("Reporteros asignados al evento");
		lblAsig.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblAsig.setBounds(690, 435, 240, 20);
		frame.getContentPane().add(lblAsig);

		tmDisponibles = new DefaultTableModel(
				new Object[] { "Nombre", "Tipo", "Temáticas", "id_reportero" }, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		tblDisponibles = new JTable(tmDisponibles);
		tblDisponibles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tblDisponibles.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spDisp = new JScrollPane(tblDisponibles);
		spDisp.setBounds(20, 460, 500, 240);
		frame.getContentPane().add(spDisp);

		ocultarColumna(tblDisponibles, 3);
		tblDisponibles.getColumnModel().getColumn(0).setPreferredWidth(150);
		tblDisponibles.getColumnModel().getColumn(1).setPreferredWidth(100);
		tblDisponibles.getColumnModel().getColumn(2).setPreferredWidth(230);

		tmAsignados = new DefaultTableModel(
				new Object[] { "Nombre", "Tipo", "Temáticas", "Responsable", "id_reportero" }, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		tblAsignados = new JTable(tmAsignados);
		tblAsignados.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tblAsignados.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		JScrollPane spAsig = new JScrollPane(tblAsignados);
		spAsig.setBounds(710, 460, 500, 240);
		frame.getContentPane().add(spAsig);

		ocultarColumna(tblAsignados, 4);
		tblAsignados.getColumnModel().getColumn(0).setPreferredWidth(140);
		tblAsignados.getColumnModel().getColumn(1).setPreferredWidth(90);
		tblAsignados.getColumnModel().getColumn(2).setPreferredWidth(190);
		tblAsignados.getColumnModel().getColumn(3).setPreferredWidth(80);

		btnAsignar = new JButton("Asignar ->");
		btnAsignar.setBounds(550, 480, 130, 32);
		frame.getContentPane().add(btnAsignar);

		btnEliminar = new JButton("<- Eliminar");
		btnEliminar.setBounds(550, 530, 130, 32);
		frame.getContentPane().add(btnEliminar);

		btnMarcarResponsable = new JButton("Marcar responsable");
		btnMarcarResponsable.setBounds(530, 580, 160, 32);
		frame.getContentPane().add(btnMarcarResponsable);

		btnFinalizarAsignacion = new JButton("Finalizar asignación");
		btnFinalizarAsignacion.setBounds(1010, 415, 200, 30);
		frame.getContentPane().add(btnFinalizarAsignacion);

		btnGuardar = new JButton("Aceptar");
		btnGuardar.setBounds(1070, 720, 140, 30);
		frame.getContentPane().add(btnGuardar);

		instalarTooltipsTabla(tblEventos);
		instalarTooltipsTabla(tblDisponibles);
		instalarTooltipsTabla(tblAsignados);

		setAccionesEnabled(false);
		setResponsableEnabled(false);
		setFinalizarAsignacionEnabled(false);
		setEstadoAsignacion(false);

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

	public void setAgencias(List<AgenciaDTO> agencias) {
		DefaultComboBoxModel<AgenciaDTO> model = new DefaultComboBoxModel<>();
		for (AgenciaDTO a : agencias) {
			model.addElement(a);
		}
		cbAgencias.setModel(model);
		if (model.getSize() > 0) {
			cbAgencias.setSelectedIndex(0);
		}
	}

	public void setEventos(List<EventoDTO> eventos) {
		tmEventos.setRowCount(0);
		for (EventoDTO e : eventos) {
			tmEventos.addRow(new Object[] {
					e.getNombre(),
					e.getFechaEvento(),
					e.getTematicasTexto(),
					e.getIdEvento()
			});
		}
		ocultarColumna(tblEventos, 3);
	}

	public void setDisponibles(List<ReporteroDTO> reporteros) {
		tmDisponibles.setRowCount(0);
		for (ReporteroDTO r : reporteros) {
			tmDisponibles.addRow(new Object[] {
					r.getNombre(),
					r.getTipoReportero(),
					r.getTematicasTexto(),
					r.getIdReportero()
			});
		}
		ocultarColumna(tblDisponibles, 3);
	}

	public void setAsignados(List<ReporteroDTO> reporteros) {
		tmAsignados.setRowCount(0);
		for (ReporteroDTO r : reporteros) {
			tmAsignados.addRow(new Object[] {
					r.getNombre(),
					r.getTipoReportero(),
					r.getTematicasTexto(),
					r.isResponsable() ? "Sí" : "No",
					r.getIdReportero()
			});
		}
		ocultarColumna(tblAsignados, 4);
	}

	public void setEstadoAsignacion(boolean finalizada) {
		lblEstadoAsignacionValor.setText(finalizada ? "FINALIZADA" : "ABIERTA");
	}

	public JFrame getFrame() {
		return frame;
	}

	public AgenciaDTO getAgenciaSeleccionada() {
		return (AgenciaDTO) cbAgencias.getSelectedItem();
	}

	public int getFiltroEventosSeleccionado() {
		return cbFiltroEventos.getSelectedIndex();
	}

	public boolean isFiltroSoloEspecialistasActivo() {
		return chkSoloEspecialistas.isSelected();
	}

	public boolean isFiltroTipoBasicoActivo() {
		return chkTipoBasico.isSelected();
	}

	public boolean isFiltroTipoGraficoActivo() {
		return chkTipoGrafico.isSelected();
	}

	public boolean isFiltroTipoCamarografoActivo() {
		return chkTipoCamarografo.isSelected();
	}

	public Integer getIdEventoSeleccionado() {
		int row = tblEventos.getSelectedRow();
		if (row < 0 || row >= tmEventos.getRowCount()) {
			return null;
		}
		Object value = tmEventos.getValueAt(row, 3);
		return value == null ? null : ((Number) value).intValue();
	}

	public int[] getFilasDisponiblesSeleccionadas() {
		return tblDisponibles.getSelectedRows();
	}

	public int[] getFilasAsignadosSeleccionadas() {
		return tblAsignados.getSelectedRows();
	}

	public Integer getIdReporteroAsignadoSeleccionado() {
		int row = tblAsignados.getSelectedRow();
		if (row < 0 || row >= tmAsignados.getRowCount()) {
			return null;
		}
		return ((Number) tmAsignados.getValueAt(row, 4)).intValue();
	}

	public ReporteroDTO getReporteroDisponibleEnFila(int row) {
		if (row < 0 || row >= tmDisponibles.getRowCount()) {
			return null;
		}
		String nombre = (String) tmDisponibles.getValueAt(row, 0);
		String tipo = (String) tmDisponibles.getValueAt(row, 1);
		String tematicas = (String) tmDisponibles.getValueAt(row, 2);
		int id = ((Number) tmDisponibles.getValueAt(row, 3)).intValue();
		return new ReporteroDTO(id, 0, nombre, tematicas, tipo);
	}

	public ReporteroDTO getReporteroAsignadoEnFila(int row) {
		if (row < 0 || row >= tmAsignados.getRowCount()) {
			return null;
		}
		String nombre = (String) tmAsignados.getValueAt(row, 0);
		String tipo = (String) tmAsignados.getValueAt(row, 1);
		String tematicas = (String) tmAsignados.getValueAt(row, 2);
		int id = ((Number) tmAsignados.getValueAt(row, 4)).intValue();
		return new ReporteroDTO(id, 0, nombre, tematicas, tipo);
	}

	public void clearSeleccionEvento() {
		tblEventos.clearSelection();
	}

	public void clearSeleccionDisponibles() {
		tblDisponibles.clearSelection();
	}

	public void clearSeleccionAsignados() {
		tblAsignados.clearSelection();
	}

	public void setAccionesEnabled(boolean enabled) {
		btnAsignar.setEnabled(enabled);
		btnEliminar.setEnabled(enabled);
		btnMarcarResponsable.setEnabled(enabled);
		btnGuardar.setEnabled(enabled);
	}

	public void setResponsableEnabled(boolean enabled) {
		btnMarcarResponsable.setEnabled(enabled);
	}

	public void setFinalizarAsignacionEnabled(boolean enabled) {
		btnFinalizarAsignacion.setEnabled(enabled);
	}

	public void setEdicionAsignacionEnabled(boolean enabled) {
		btnAsignar.setEnabled(enabled);
		btnEliminar.setEnabled(enabled);
		btnMarcarResponsable.setEnabled(enabled);
		btnGuardar.setEnabled(enabled);
	}

	public void addAgenciaChangedListener(ActionListener l) {
		cbAgencias.addActionListener(l);
	}

	public void addFiltroEventosChangedListener(ActionListener l) {
		cbFiltroEventos.addActionListener(l);
	}

	public void addSoloEspecialistasChangedListener(ActionListener l) {
		chkSoloEspecialistas.addActionListener(l);
	}

	public void addFiltroTipoChangedListener(ActionListener l) {
		chkTipoBasico.addActionListener(l);
		chkTipoGrafico.addActionListener(l);
		chkTipoCamarografo.addActionListener(l);
	}

	public void addEventosSelectionListener(ListSelectionListener l) {
		tblEventos.getSelectionModel().addListSelectionListener(l);
	}

	public void addAsignadosSelectionListener(ListSelectionListener l) {
		tblAsignados.getSelectionModel().addListSelectionListener(l);
	}

	public void addAsignarListener(ActionListener l) {
		btnAsignar.addActionListener(l);
	}

	public void addEliminarListener(ActionListener l) {
		btnEliminar.addActionListener(l);
	}

	public void addMarcarResponsableListener(ActionListener l) {
		btnMarcarResponsable.addActionListener(l);
	}

	public void addFinalizarAsignacionListener(ActionListener l) {
		btnFinalizarAsignacion.addActionListener(l);
	}

	public void addGuardarListener(ActionListener l) {
		btnGuardar.addActionListener(l);
	}

	public void showInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
	}

	public void showError(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public boolean confirm(String msg, String title) {
		int opt = JOptionPane.showConfirmDialog(frame, msg, title, JOptionPane.YES_NO_OPTION);
		return opt == JOptionPane.YES_OPTION;
	}
}