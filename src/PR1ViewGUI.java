import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Project 1
 * Table (Swing) view.
 * 
 * @author Shayan Motevalli
 * @version 1
 */
public class PR1ViewGUI extends JComponent implements ChangeListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;
	private PR1Model model = null;
	private JTable table = null;

	/**
	 * Creates an instance of <code>HW3ViewGUI</code> class.
	 * 
	 * @param m The data model for this instance.
	 */
	public PR1ViewGUI(PR1Model m) {
		// Set the model and listen to its events
		model = m;
		model.addChangeListener(this);
		
		// Create the table
		table = new JTable(model);

		// Configure the color scheme.
		table.setGridColor(Color.BLUE);
		table.getTableHeader().setBackground(Color.YELLOW);
		table.setSelectionBackground(Color.RED);

		// Configure the selection model.
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);	
		table.getSelectionModel().addListSelectionListener(this);

		// Assemble the GUI components
		JScrollPane scrollPane = new JScrollPane(table);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Provides the row that is currently edited in the table (wrapper method).
	 * 
	 * @return The editing row.
	 */
	public int getEditingRow() {
		return table.getEditingRow();
	}

	/**
	 * Provides the column that is currently edited in the table (wrapper method).
	 * 
	 * @return The editing column.
	 */
	public int getEditingColumn() {
		return table.getEditingColumn();
	}

	/**
	 * Provides the column that is currently edited in the table (wrapper method).
	 * 
	 * @return The editing column.
	 */
	public ListSelectionModel getSelectionModel() {
		return table.getSelectionModel();
	}

	/**
	 * Sets the currently edited column in the table (wrapper method).
	 * 
	 * @param c The editing column.
	 */
	public void setEditingColumn(int c) {
		table.setEditingColumn(c);
	}

	/**
	 * Listens to the updates in the table selection and stores in the model the currently selected rows in the table.
	 * 
	 * @param e The list selection event.
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		model.setSelectedRows(table.getSelectedRows());
	}

	/**
	 * Listens to the updates in the model selection and stores in the table the currently selected rows in the model.
	 * 
	 * @param e The list selection event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		int[] s = model.getSelectedRows();
		if (s != null) {
			for (int i = 0; i < s.length; i++) {
				table.getSelectionModel().addSelectionInterval(s[i], s[i]);					
			}
		}
		else {
			table.getSelectionModel().clearSelection();
		}
	}

}