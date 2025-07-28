import java.util.*;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.event.*;
import javax.swing.table.TableModel;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;


class OutputDev extends UserOutput {
	private static JTextArea txt;
	
	OutputDev(JTextArea txt) {
		this.txt = txt;
	}
	
	public void putInt(int val) {}
	public void putHex(int val) {}
	public void putChar(char val) {}
	public void putString(String val) {
		txt.append(val);
		String s = txt.getText();
		int pos = s.length();
		txt.setCaretPosition(pos);
	}
}

class InputDev extends UserInput {
	private String input = "";
	private char data1, data2;
	private int retval;
	
	public int getInt() {
		input = JOptionPane.showInputDialog("Integer value: ");
		retval = Integer.decode(input);
		return retval;
	}
	public int getHex() {
		input = JOptionPane.showInputDialog("Hex value: ");
		retval = Integer.parseInt(input, 16);
		return retval;
	}
	public char getChar() {
		input = JOptionPane.showInputDialog("Char value: ");
		retval = Integer.parseInt(String.valueOf(input.charAt(0)));
		return (char)(retval & 0xff);
	}
	public void getString(int[] memory, int addr) {
		addr >>= 1;
		input = JOptionPane.showInputDialog("String value: ");
		input = input + "\0\0";
		int i = 0;
		while (true) {
			data1 = (char)(input.charAt(i));
			data2 = (char)(input.charAt(i + 1));
			memory[addr] = (data1 << 8) | data2;
			if (data1 == 0 || data2 == 0) break;
			addr++;
			i += 2;
		}
	}
}

class Gui extends JFrame implements ActionListener {
	private static Assembler asm;
	private static Simulator sim;
	private static UserOutput out;
	private static UserInput in;
	
	private volatile int cycles = 0;
	private volatile boolean simrunning = false;
	
	private static List<String> program;
	private static List<String> obj_code;
	
	private static JMenuItem m2_1, m2_2, m2_3, m2_4, m2_5;
	private static JLabel label;
	private static JButton button1, button2, button3, button4, button5;
	private static JButton bpAdd, bpRem, bpClr;
	private static JTextArea textArea, textArea3, textArea4;
	private static JTable table, symtable, memtable;
	private static DefaultTableModel tabmodel, symmodel, memmodel, regmodel;
	private static DefaultListModel<String> model;
	private static JList<String> list;
	private static Thread simThread;
	private static File file;
	
	protected void createWindow() {
		JFrame frame = new JFrame("Tiny RISC Machine");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenu(frame);
		createUI(frame);
		
		frame.setSize(1024, 768);            
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		createAsmSim();
	}
	
	private void createAsmSim() {
		out = new OutputDev(textArea3);
		in = new InputDev();
		asm = new Assembler();
		sim = new Simulator(in, out);
	}

	private void createMenu(JFrame frame) {
		JMenuBar mb = new JMenuBar();
		
		JMenu m1 = new JMenu("File");
		JMenu m2 = new JMenu("Program");
		JMenu m3 = new JMenu("Help");
		mb.add(m1);
		mb.add(m2);
		mb.add(m3);
		
		JMenuItem m1_1 = new JMenuItem("New");
		JMenuItem m1_2 = new JMenuItem("Open");
		JMenuItem m1_3 = new JMenuItem("Save as");
		JMenuItem m1_4 = new JMenuItem("Quit");
		m1_1.setActionCommand("file_new");
		m1_2.setActionCommand("file_openasm");
		m1_3.setActionCommand("file_saveasm");
		m1_4.setActionCommand("file_quit");
		m1_1.addActionListener(this);
		m1_2.addActionListener(this);
		m1_3.addActionListener(this);
		m1_4.addActionListener(this);
		m1.add(m1_1);
		m1.add(m1_2);
		m1.add(m1_3);
		m1.addSeparator();
		m1.add(m1_4);
		
		m2_1 = new JMenuItem("Assemble/Exec");
		m2_2 = new JMenuItem("Reset");
		m2_3 = new JMenuItem("Run");
		m2_4 = new JMenuItem("Auto Step");
		m2_5 = new JMenuItem("Stop/Step");
		
		m2_1.setEnabled(true);
		m2_2.setEnabled(false);
		m2_3.setEnabled(false);
		m2_4.setEnabled(false);
		m2_5.setEnabled(false);
		
		m2_1.setActionCommand("prog_assemble");
		m2_2.setActionCommand("prog_simstart");
		m2_3.setActionCommand("prog_simrun");
		m2_4.setActionCommand("prog_simauto");
		m2_5.setActionCommand("prog_simstep");
		m2_1.addActionListener(this);
		m2_2.addActionListener(this);
		m2_3.addActionListener(this);
		m2_4.addActionListener(this);
		m2_5.addActionListener(this);
		m2.add(m2_1);
		m2.addSeparator();
		m2.add(m2_2);
		m2.add(m2_3);
		m2.add(m2_4);
		m2.add(m2_5);
		
		JMenuItem m3_1 = new JMenuItem("RTFM");
		m3_1.setEnabled(false);
		m3.add(m3_1);
		
		frame.getContentPane().add(BorderLayout.NORTH, mb);
	}

	private void createUI(JFrame frame) {
		JTabbedPane tabbedPane = new JTabbedPane();
		JPanel panel1 = new JPanel(new BorderLayout());
		JPanel panel2 = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel2.setLayout(layout);
		
		textArea = new JTextArea(30, 80);
		JLabel lineLabel = new JLabel("Lin: 1, Col: 0");
		textArea.addCaretListener(new CaretListener() {
		    public void caretUpdate(CaretEvent e) {
			JTextArea editArea = (JTextArea)e.getSource();
			int lin = 1;
			int col = 0;

			try {
			    int caretpos = editArea.getCaretPosition();
			    lin = editArea.getLineOfOffset(caretpos);
			    col = caretpos - editArea.getLineStartOffset(lin);
			    lineLabel.setText("Lin: " + (lin + 1) + ", Col: " + col);
			} catch(Exception ex) {
			}
		    }
		});

		textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setColumnHeaderView(lineLabel);
		panel1.add(scrollPane, BorderLayout.CENTER);
		
		Object[][] data = {};
		String[] columnNames = {"Address", "Instruction", "Program disassembly"};
		tabmodel = new DefaultTableModel(data, columnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new JTable(tabmodel);
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(1).setMaxWidth(100);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(240);
		table.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane1 = new JScrollPane(table);
		table.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = table.rowAtPoint(evt.getPoint());
				int col = table.columnAtPoint(evt.getPoint());
				symtable.clearSelection();
				if (row >= 0 && col >= 0) {
					for (int i = 0; i < symmodel.getRowCount(); i++) {
						Object value = symmodel.getValueAt(i, 0);
						if (tabmodel.getValueAt(row, 0).equals(value)) {
							symtable.scrollRectToVisible(symtable.getCellRect(i, i, false));
							symtable.setRowSelectionInterval(i, i);
						}
					}
				}
			}
		});
		
		Object[][] symdata = {};
		String[] symColumnNames = {"Address", "Symbol"};
		symmodel = new DefaultTableModel(symdata, symColumnNames){
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		symtable = new JTable(symmodel);
		symtable.getColumnModel().getColumn(0).setMaxWidth(100);
		symtable.getColumnModel().getColumn(0).setMinWidth(100);
		symtable.getColumnModel().getColumn(1).setPreferredWidth(120);
		symtable.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane2 = new JScrollPane(symtable);
		symtable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = symtable.rowAtPoint(evt.getPoint());
				int col = symtable.columnAtPoint(evt.getPoint());
				table.clearSelection();
				if (row >= 0 && col >= 0) {
					for (int i = 0; i < tabmodel.getRowCount(); i++) {
						Object value = tabmodel.getValueAt(i, 0);
						if (symmodel.getValueAt(row, 0).equals(value)) {
							table.scrollRectToVisible(table.getCellRect(i, i, false));
							table.setRowSelectionInterval(i, i);
						}
					}
				}
			}
		});

		textArea3 = new JTextArea(10, 10);
		textArea3.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea3.setEditable(false);
		JScrollPane scrollPane3 = new JScrollPane(textArea3);
		
		Object[][] mem = {};
		String[] memColumnNames = {"Address", "0", "2", "4", "6", "8", "A", "C", "E"};
		memmodel = new DefaultTableModel(mem, memColumnNames){
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		memtable = new JTable(memmodel);
		memtable.setCellSelectionEnabled(true);
		memtable.getColumnModel().getColumn(0).setMaxWidth(100);
		memtable.getColumnModel().getColumn(0).setMinWidth(100);
		memtable.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane4 = new JScrollPane(memtable);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 2.0; gbc.weighty = 2.0;
		gbc.gridheight = 1;
		gbc.gridx = 0; gbc.gridy = 0; panel2.add(scrollPane1, gbc);
		gbc.weightx = 1.0; gbc.weighty = 1.0;
		gbc.gridx = 1; gbc.gridy = 0; panel2.add(scrollPane2, gbc);
		gbc.weightx = 1.0; gbc.weighty = 1.0;
		gbc.gridx = 0; gbc.gridy = 1; panel2.add(scrollPane4, gbc);
		gbc.gridwidth = 3;
		gbc.weightx = 1.0; gbc.weighty = 1.0;
		gbc.gridx = 0; gbc.gridy = 2; panel2.add(scrollPane3, gbc);
		
		Object[][] regs = {
		    {"r0", "zr", "0000"},
		    {"r1", "a0", "0000"},
		    {"r2", "a1", "0000"},
		    {"r3", "a2", "0000"},
		    {"r4", "a3", "0000"},
		    {"r5", "v0", "0000"},
		    {"r6", "v1", "0000"},
		    {"r7", "v2", "0000"},
		    {"r8", "v3", "0000"},
		    {"r9", "v4", "0000"},
		    {"r10", "v5", "0000"},
		    {"r11", "v6", "0000"},
		    {"r12", "v7", "0000"},
		    {"r13", "fp", "0000"},
		    {"r14", "sp", "0000"},
		    {"r15", "lr", "0000"},
		    {"", "", ""},
		    {"pc", "", "0000"}
		};
		String[] regColumnNames = {"Register", "Name", "Value"};
		regmodel = new DefaultTableModel(regs, regColumnNames){
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable regtable = new JTable(regmodel);
		regtable.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane5 = new JScrollPane(regtable);
		
		regtable.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				scrollPane5.revalidate();
				scrollPane5.repaint();
			}
		});
		
		gbc.weightx = 0.6;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 2; gbc.gridy = 0;
		panel2.add(scrollPane5, gbc);

		JLabel label2 = new JLabel("Program:");
		label = new JLabel("CPU cycle: 0");
		button1 = new JButton("Assemble/Exec");
		button2 = new JButton("Reset");
		button3 = new JButton("Run");
		button4 = new JButton("Auto Step");
		button5 = new JButton("Stop/Step");
		
		button1.setEnabled(true);
		button2.setEnabled(false);
		button3.setEnabled(false);
		button4.setEnabled(false);
		button5.setEnabled(false);
		
		button1.setActionCommand("prog_assemble");
		button1.addActionListener(this);
		button2.setActionCommand("prog_simstart");
		button2.addActionListener(this);
		button3.setActionCommand("prog_simrun");
		button3.addActionListener(this);
		button4.setActionCommand("prog_simauto");
		button4.addActionListener(this);
		button5.setActionCommand("prog_simstep");
		button5.addActionListener(this);

		JPanel controlPanel = new JPanel();
		controlPanel.setMinimumSize(new Dimension(20, 60));
		controlPanel.setMaximumSize(new Dimension(20, 60));
		controlPanel.setPreferredSize(new Dimension(20, 60));
		GridBagLayout layoutControl = new GridBagLayout();
		GridBagConstraints gbcControl = new GridBagConstraints();
		controlPanel.setLayout(layoutControl);
		gbcControl.fill = GridBagConstraints.BOTH;
		gbcControl.weightx = 1.0; gbcControl.weighty = 1.0;
		gbcControl.gridx = 0;
		gbcControl.gridy = 0;

		gbcControl.gridy = 0;
		controlPanel.add(label2, gbcControl);
		gbcControl.gridy = 1;
		controlPanel.add(label, gbcControl);
		gbcControl.gridy = 2;
		controlPanel.add(button1, gbcControl);
		gbcControl.gridy = 3;
		controlPanel.add(button2, gbcControl);
		gbcControl.gridy = 4;
		controlPanel.add(button3, gbcControl);
		gbcControl.gridy = 5;
		controlPanel.add(button4, gbcControl);
		gbcControl.gridy = 6;
		controlPanel.add(button5, gbcControl);
		
		gbc.weightx = 0.6;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 2; gbc.gridy = 1;
		panel2.add(controlPanel, gbc);
		
		JPanel bpPanel = new JPanel();
		bpPanel.setMinimumSize(new Dimension(20, 60));
		bpPanel.setMaximumSize(new Dimension(20, 60));
		bpPanel.setPreferredSize(new Dimension(20, 60));
		GridBagLayout layoutBp = new GridBagLayout();
		GridBagConstraints gbcBp = new GridBagConstraints();
		bpPanel.setLayout(layoutBp);
		gbcBp.fill = GridBagConstraints.BOTH;
		gbcBp.weightx = 1.0; gbcBp.weighty = 1.0;
		gbcBp.gridheight = 1;
		gbcBp.gridwidth = 1;
		gbcBp.gridx = 0;
		gbcBp.gridy = 0;
	
		JLabel bplabel = new JLabel("Breakpoints:");
		gbcBp.gridx = 0;
		gbcBp.gridy = 0;
		bpPanel.add(bplabel, gbcBp);
		bpAdd = new JButton("Add");
		gbcBp.gridx = 0;
		gbcBp.gridy = 3;
		bpPanel.add(bpAdd, gbcBp);
		bpRem = new JButton("Remove");
		gbcBp.gridx = 1;
		gbcBp.gridy = 3;
		bpPanel.add(bpRem, gbcBp);
		bpClr = new JButton("Clear");
		gbcBp.gridx = 2;
		gbcBp.gridy = 3;
		bpPanel.add(bpClr, gbcBp);
		
		bpAdd.setActionCommand("bp_add");
		bpAdd.addActionListener(this);
		bpRem.setActionCommand("bp_rem");
		bpRem.addActionListener(this);
		bpClr.setActionCommand("bp_clr");
		bpClr.addActionListener(this);
		
		model = new DefaultListModel<String>();
		list = new JList<String>(model);
		list.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane listScroller = new JScrollPane(list);

		gbcBp.gridx = 0;
		gbcBp.gridy = 1;
		gbcBp.gridheight = 2;
		gbcBp.gridwidth = 3;
		bpPanel.add(listScroller, gbcBp);
		
		gbc.weightx = 1.0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 1; gbc.gridy = 1;
		panel2.add(bpPanel, gbc);

		tabbedPane.addTab("Code Editor", panel1);
		tabbedPane.addTab("Assembler and Simulator", panel2);
		
		frame.add(tabbedPane);
	}
	
	private void loadAsm() {
		JFileChooser fc = new JFileChooser();
		
		fc.setFileFilter(new FileFilter() {
			public String getDescription() {
				return "TRM Assembly Files (*.s; *.asm)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					String filename = f.getName();
					return filename.endsWith(".s") || filename.endsWith(".asm") ;
				}
			}
		});
		
		int returnVal = fc.showOpenDialog(Gui.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String filedata;

				textArea.setText("");
				while ((filedata = br.readLine()) != null) {
					textArea.append(filedata + "\n");
				}
				br.close();
			} catch (Exception error) {
			}
		}
	}
	
	private void saveAsm() {
		JFileChooser fc = new JFileChooser();
		
		fc.setFileFilter(new FileFilter() {
			public String getDescription() {
				return "TRM Assembly Files (*.s; *.asm)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					String filename = f.getName();
					return filename.endsWith(".s") || filename.endsWith(".asm") ;
				}
			}
		});
		
		int returnVal = fc.showOpenDialog(Gui.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				String filedata;
				int lines = textArea.getLineCount();
				
				for (int i = 0; i < lines; i++) {
					int start = textArea.getLineStartOffset(i);
					int end = textArea.getLineEndOffset(i);
					String line = textArea.getText(start, end - start);
					bw.write(line);
				}
				bw.close();
			} catch (Exception error) {
			}
		}
	}
		
	private void assembler() {
		program = new ArrayList<>();
		obj_code = new ArrayList<>();
		
		try {
			int lines = textArea.getLineCount();
					
			for (int i = 0; i < lines; i++) {
				int start = textArea.getLineStartOffset(i);
				int end = textArea.getLineEndOffset(i);
				String line = textArea.getText(start, end - start);
				program.add(line);
			}
		} catch (Exception e) {
			return;
		}
		
		tabmodel.setRowCount(0);
		symmodel.setRowCount(0);
		textArea3.append("[assembling program...");
		
		asm.pass1(program);
		asm.pass2(program);
		asm.pass3(program, obj_code);

		boolean asm_ok = true;
		for (String lin : obj_code) {
			if (lin.contains("****")) {
				if (asm_ok == true) {
					textArea3.append(" failed.]\n");
					asm_ok = false;
				}
				textArea3.append(lin + "\n");
			}
		}
		
		if (asm_ok == true) {
			boolean literal = false;
			String[] tmpfields = {"", "", ""};
			for (String lin : obj_code) {
				String[] fields = lin.split("\\s+", 3);
				if (fields.length == 3) {
					fields[2] = fields[2].replace("(", "");
					fields[2] = fields[2].replace(")", "");
					
					String[] params = fields[2].split(",");
					if (params.length > 2) {
						tmpfields = fields;
						literal = true;
					} else {
						tabmodel.addRow(new Object[]{fields[0], fields[1], fields[2]});
					}
				} else if (fields.length == 2) {
					if (literal == true) {
						tabmodel.addRow(new Object[]{tmpfields[0], tmpfields[1] + " " + fields[1], tmpfields[2]});
						literal = false;
					} else {
						tabmodel.addRow(new Object[]{fields[0], fields[1], ""});
					}
				}
			}
			
			List<String> symbols;
			symbols = asm.getsymbols();
			for (String sym : symbols) {
				String[] fields = sym.split("\\s+", 2);
				symmodel.addRow(new Object[]{fields[0], fields[1]});
			}

			textArea3.append(" ok, program size: " + Integer.toString(obj_code.size() * 2) + " bytes.]\n");
			boolean ok = simulatorStart();
			
			if (ok == true) {
				m2_2.setEnabled(true);
				m2_3.setEnabled(true);
				m2_4.setEnabled(true);
				m2_5.setEnabled(true);
				button2.setEnabled(true);
				button3.setEnabled(true);
				button4.setEnabled(true);
				button5.setEnabled(true);
			}
		} else {
			m2_2.setEnabled(false);
			m2_3.setEnabled(false);
			m2_4.setEnabled(false);
			m2_5.setEnabled(false);
			button2.setEnabled(false);
			button3.setEnabled(false);
			button4.setEnabled(false);
			button5.setEnabled(false);
		}
	}
	
	
	private int rowIndexForValue(final String value) {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0) != null && table.getValueAt(i, 0).equals(value))
				return i;
		}
		return -1;
	}
	
	private void updateRegs() {
		int[] context;
		
		try {
			context = sim.getcontext();
			for (int i = 0; i < context.length; i++)
				regmodel.setValueAt(String.format("%04x", context[i]), i, 2);
			regmodel.setValueAt(String.format("%04x", sim.getpc()), context.length + 1, 2);
		} catch (Exception e) {}
	}
	
	private void updateCode() {
		try {
			int rowIdx = rowIndexForValue(String.format("%04x", sim.getpc()));
			table.clearSelection();
			if (rowIdx >= 0) {
				table.scrollRectToVisible(table.getCellRect(rowIdx, rowIdx, false));
				table.setRowSelectionInterval(rowIdx, rowIdx);
			}
		} catch (Exception e) {}
	}
	
	private void updateMem() {
		int lsaddr, store;
		
		lsaddr = sim.getlastlsaddr() & 0xffff;
		store = sim.getlastlsaddr() >> 16;
		
		try {
			if (lsaddr >= 0 && lsaddr < 0xf000) {
				int row = lsaddr / 16;
				int col = ((lsaddr / 2) % 8) + 1;
				
				memtable.clearSelection();
				memtable.scrollRectToVisible(memtable.getCellRect(row, row, false));
				memtable.setRowSelectionInterval(row, row);
				memtable.setColumnSelectionInterval(col, col);
				
				if (store == 1) {
					int[] mem = sim.getmemory();
					int data = mem[lsaddr >> 1];
					memmodel.setValueAt(String.format("%04x", data), row, col);
				}
			} else {
				memtable.clearSelection();
			}
		} catch (Exception e) {}
	}
	
	private void fillMem() {
		memmodel.setRowCount(0);
		int[] mem = sim.getmemory();
		
		try {
			for (int i = 0; i < mem.length; i += 8) {
				memmodel.addRow(new Object[]{String.format("%04x", i * 2),
					String.format("%04x", mem[i]), String.format("%04x", mem[i + 1]), 
					String.format("%04x", mem[i + 2]), String.format("%04x", mem[i + 3]), 
					String.format("%04x", mem[i + 4]), String.format("%04x", mem[i + 5]), 
					String.format("%04x", mem[i + 6]), String.format("%04x", mem[i + 7]) 
				});
			}
		} catch (Exception e) {}
	}
	
	private boolean simulatorStart() {
		textArea3.append("[starting simulator...");
		
		cycles = 0;
		label.setText("CPU cycle: " + Integer.toString(cycles));
		if (sim.check(obj_code)) {
			textArea3.append(" failed, program has errors.]\n");
		} else {
			textArea3.append(" ok.]\n");
			
			int ok = sim.load(obj_code);
			
			if (ok == 0) {
				fillMem();
				updateCode();
				model.removeAllElements();
				
				return true;
			}
		}
		return false;
	}
	
	private void simulatorRun(boolean auto) {
		simThread = new Thread(() -> {
			boolean go;
			int[] context;
			
			m2_1.setEnabled(false);
			m2_2.setEnabled(false);
			m2_3.setEnabled(false);
			m2_4.setEnabled(false);
			button1.setEnabled(false);
			button2.setEnabled(false);
			button3.setEnabled(false);
			button4.setEnabled(false);
			
			simrunning = true;
			
			while (simrunning == true) {
				if (auto == true) {
					updateCode();
					updateMem();
					updateRegs();
				}
				
				label.setText("CPU cycle: " + Integer.toString(cycles));

				if (breakpointCheck()) {
					go = false;
					updateCode();
					updateMem();
					updateRegs();
				} else {
					try {
						go = sim.step();
					} catch (Exception e) {
						break;
					}
				}

				if (auto == true) {
					try {
						Thread.sleep(200);
					} catch(Exception e) {}
				}
				
				if (go) {
					cycles++;
				} else {
					m2_1.setEnabled(true);
					m2_2.setEnabled(true);
					m2_3.setEnabled(true);
					m2_4.setEnabled(true);
					button1.setEnabled(true);
					button2.setEnabled(true);
					button3.setEnabled(true);
					button4.setEnabled(true);
					break;
				}
			}
			
			if (auto == false) {
				updateRegs();
				fillMem();
			}
		});
		simThread.start();
	}
	
	private void simulatorStep() {
		boolean go = false;
		
		simrunning = false;
		m2_1.setEnabled(true);
		m2_2.setEnabled(true);
		m2_3.setEnabled(true);
		m2_4.setEnabled(true);
		button1.setEnabled(true);
		button2.setEnabled(true);
		button3.setEnabled(true);
		button4.setEnabled(true);
		
		try {
			go = sim.step();
		} catch (Exception e) {}		

		if (go)
			cycles++;

		updateCode();
		updateMem();
		updateRegs();
		label.setText("CPU cycle: " + Integer.toString(cycles));
	}
	
	private boolean breakpointCheck() {
		int i;
		String pcval = String.format("%04x", sim.getpc());
		
		for (i = 0; i < model.getSize(); i++)
			if (model.getElementAt(i).equals(pcval)) break;
		
		return i >= model.getSize() ? false : true;
	}
	
	private void breakpointAdd() {
		int i;
		int row = table.getSelectedRow();
		
		if (row < 0) return;
		
		String value = table.getModel().getValueAt(row, 0).toString();
		for (i = 0; i < model.getSize(); i++)
			if (model.getElementAt(i).equals(value)) break;
		
		if (i >= model.getSize()) {
			model.addElement(value);
		}
	}
	
	private void breakpointRemove() {
		int selectedIndex = list.getSelectedIndex();
		if (selectedIndex != -1) {
		    model.remove(selectedIndex);
		}
	}
	
	private void breakpointClear() {
		model.removeAllElements();
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if ("file_new".equals(command))
			textArea.setText("");
		
		if ("file_openasm".equals(command))
			loadAsm();

		if ("file_saveasm".equals(command))
			saveAsm();
		
		if ("file_quit".equals(command))
			System.exit(0);
			
		if ("prog_assemble".equals(command))
			assembler();
			
		if ("prog_simstart".equals(command))
			simulatorStart();
			
		if ("prog_simrun".equals(command))
			simulatorRun(false);

		if ("prog_simauto".equals(command))
			simulatorRun(true);
			
		if ("prog_simstep".equals(command))
			simulatorStep();
			
		if ("bp_add".equals(command))
			breakpointAdd();
			
		if ("bp_rem".equals(command))
			breakpointRemove();
			
		if ("bp_clr".equals(command))
			breakpointClear();
	}
}

public class TrmGUI {
	public static void main(String[] args) {
		Gui appInterface = new Gui();
		
		appInterface.createWindow();
	}
}
