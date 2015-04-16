import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;


/**
 * Homework 3 assignment solution.
 * Main class and controller.
 * 
 * @author Shayan Motevalli
 * @version 1
 */
public class PR1 extends JFrame implements ActionListener { 
	private static final long serialVersionUID = 1L;
	private final static String TITLE = "PR1: smoteval";
	private final static String HELP = "Project 1 version 1.";
	private JMenuBar menuBar = null;
	private JMenu fileMenu = null;
	private JMenu editMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem openMenuItem = null;
	private JMenuItem closeMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JMenuItem quitMenuItem = null;
	private JMenuItem copyMenuItem = null;
	private JMenuItem pasteMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private PR1Model model = null;
	private PR1View view1 = null;
	private PR1View view2 = null;
	private PR1View view3 = null;
	private PR1ViewGUI viewGUI = null;
	private String clipboard = null;
	private File file = null;

	/**
	 * Creates an instance of <code>HW3</code> class.
	 * The default title is used.
	 */
	public PR1() {
		this(TITLE);
	}

	/**
	 * Creates an instance of <code>HW3</code> class.
	 * 
	 * @param title The title of the application window.
	 */
	public PR1(String title) {
		super(title);
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");

		openMenuItem = new JMenuItem("Open");
		openMenuItem.addActionListener(this);
		openMenuItem.setActionCommand("O");
		fileMenu.add(openMenuItem);

		closeMenuItem = new JMenuItem("Close");
		closeMenuItem.addActionListener(this);
		closeMenuItem.setActionCommand("W");
		fileMenu.add(closeMenuItem);

		saveMenuItem = new JMenuItem("Save");
		saveMenuItem.addActionListener(this);
		saveMenuItem.setActionCommand("S");
		fileMenu.add(saveMenuItem);

		quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.addActionListener(this);
		quitMenuItem.setActionCommand("Q");
		fileMenu.add(quitMenuItem);

		menuBar.add(fileMenu);

		editMenu = new JMenu("Edit");

		copyMenuItem = new JMenuItem("Copy");
		copyMenuItem.addActionListener(this);
		copyMenuItem.setActionCommand("C");
		editMenu.add(copyMenuItem);

		pasteMenuItem = new JMenuItem("Paste");
		pasteMenuItem.addActionListener(this);
		pasteMenuItem.setActionCommand("V");
		editMenu.add(pasteMenuItem);

		menuBar.add(editMenu);

		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About Project 1");
		aboutMenuItem.addActionListener(this);
		aboutMenuItem.setActionCommand("A");
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);

		setJMenuBar(menuBar);

		model = new PR1Model();

		viewGUI = new PR1ViewGUI(model);
		view1 = new PR1View(model);
		view2 = new PR1View(model);
		view3 = new PR1View(model);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewGUI, view1);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(400);
		
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, view2, view3);
		splitPane2.setOneTouchExpandable(true);
		splitPane2.setDividerLocation(400);
		
		JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, splitPane2);
		splitPane3.setOneTouchExpandable(true);
		splitPane3.setDividerLocation(350);

		Dimension minimumSize = new Dimension(100, 50);
		viewGUI.setMinimumSize(minimumSize);
		view1.setMinimumSize(minimumSize);
		setLayout(new BorderLayout());
		add(splitPane3, BorderLayout.CENTER);

	}

	/**
	 * The main method.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		final PR1 app = new PR1();
		app.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowevent) {
				app.dispose();
				System.exit(0);
			}
		});
		app.setSize(800, 800);
		app.setVisible(true);
	}

	/**
	 * The action event handler that determines the source of the event 
	 * and processes the event accordingly.
	 * 
	 * @param e The generated event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String command = null;
		switch (source.getClass().getName()) {
		case "javax.swing.JMenuItem":
			command = ((JMenuItem) source).getActionCommand();
			break;
		default:
			command = "";
		}
		switch (command) {

		case "O":
			JFileChooser fileChooser = new JFileChooser();
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fileChooser.getSelectedFile();
				model.setColumnCount(0);
				try {

					//Create input stream (a BufferdReader object) from the file.
					BufferedReader inputStream = new BufferedReader(new FileReader(file));

					//Read the first line to get the column names.
					String line = null;
					if ((line = inputStream.readLine()) != null) {
						Scanner scanner = new Scanner(line);
						scanner.useDelimiter(",");
						while (scanner.hasNext()) {
							model.addColumn(scanner.next());
						}
						scanner.close();
					}

					//Read the remaining lines to get the data.
					while ((line = inputStream.readLine()) != null) {
						Vector<String> tmpVector = new Vector<String>();
						Scanner scanner = new Scanner(line);
						scanner.useDelimiter(",");
						while (scanner.hasNext()) {
							tmpVector.add(scanner.next());
						}
						model.addRow(tmpVector);
						scanner.close();
					}

					//Close the input stream.
					inputStream.close();

					setTitle(TITLE + ": " + file.getName());
					repaint();
				}
				catch (IOException ex) {
					System.err.println(ex);
				}	
			}
			break;
		case "W":
			file = null;

			//Clear the model (erase the model data).
			model.setRowCount(0);
			model.setColumnCount(0);
			setTitle(TITLE);
			break;
		case "S":
			try {
				// Create output stream (a BufferdReader object) from the file.
				BufferedWriter outputStream = new BufferedWriter(new FileWriter(file));

				// Write the first line to store the column names.
				int columnCount = model.getColumnCount();
				int rowCount = model.getRowCount();
				if (columnCount > 0) {
					for (int i = 0; i < columnCount -1; i++) {
						outputStream.write(model.getColumnName(i) + ",");
					}
					outputStream.write(model.getColumnName(columnCount - 1) + "\n");
				}
				for (int i = 0; i < rowCount; i++) {
					for (int j = 0; j < columnCount; j++) {
						outputStream.write((j != 0 ? ", " : "") + model.getValueAt(i, j));			
					}
					outputStream.write("\n");
				}
				outputStream.close();
			}
			catch (IOException ex) {
				System.err.println(ex);
			}
			break;
		case "Q":
			System.exit(0);
			break;
		case "C":
			int row = viewGUI.getEditingRow();
			int col = viewGUI.getEditingColumn();
			if (row >= 0 && col >= 0) {
				clipboard = (String) model.getValueAt(row, col);
			}
			else {
				clipboard = null;
			}
			break;
		case "V":
			row = viewGUI.getEditingRow();
			col = viewGUI.getEditingColumn();
			if (row >= 0 && col >= 0) {
				viewGUI.setEditingColumn((col + 1) % model.getColumnCount());
				model.setValueAt(clipboard, row, col);
			}
			break;
		case "A":
			JOptionPane.showMessageDialog(this, HELP);
			break;
		}
	}

}