package gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import data.View;
import data.inmemory.DoubleMatrixView;
import filter.BoxFilter;

/**
 * In this panel, all settings for filtering can be set. Different filtering
 * algorithms are put into different tabs of the TabbedPane.
 * 
 * @author Axel Forsch
 * @date 18.02.2015
 *
 */
public class FilterPanel extends JTabbedPane implements ActionListener {

	/**
	 * Default serial ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Components for the JPanel
	 */
	private MainGui master;
	private JPanel boxFilterPanel;

	/**
	 * Components for the JPanel boxFilter which allows to specify parameters
	 * for box filtering
	 */
	private JComboBox<String> filterSelection;
	private JPanel boxSizePanel;
	private JSpinner boxSize;
	private JTextField nameOfNewViewBox;
	private JButton boxFilterButton;

	public FilterPanel(MainGui main) {
		super();
		master = main;
		this.initialize();
	}

	/**
	 * All GUI elements are initialized and the layout is set through a
	 * GridBadLayout
	 */
	private void initialize() {

		boxFilterPanel = new JPanel();
		GridBagLayout gblBox = new GridBagLayout();
		boxFilterPanel.setLayout(gblBox);

		// Auswahlmöglichkeit zum filtern
		String[] selections = { "Features", "Labels", "Probabilities" };
		filterSelection = new JComboBox<String>(selections);

		// Elements to set box size
		boxSizePanel = new JPanel();
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(3, 3, 9999, 2);
		boxSize = new JSpinner(spinnerModel);

		boxSizePanel.add(boxSize);
		boxSizePanel.setBorder(BorderFactory.createTitledBorder("Box size:"));

		// Textfield to enter name
		nameOfNewViewBox = new JTextField();
		nameOfNewViewBox.setHorizontalAlignment(JTextField.CENTER);
		nameOfNewViewBox.setBorder(BorderFactory.createTitledBorder("Name of new view:"));

		// Button to activate filtering
		boxFilterButton = new JButton("Box Filter");
		boxFilterButton.addActionListener(this);

		addComponent(boxFilterPanel, gblBox, filterSelection, 0, 0, 1, 1, 0, 0);
		addComponent(boxFilterPanel, gblBox, boxSizePanel, 0, 1, 1, 3, 0, 0);
		addComponent(boxFilterPanel, gblBox, boxFilterButton, 1, 3, 1, 1, 0, 0);
		addComponent(boxFilterPanel, gblBox, nameOfNewViewBox, 0, 4, 2, 1, 0, 0);

		this.add(boxFilterPanel, "Box filter");
	}

	/**
	 * The ActionListener for the buttons
	 * 
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == boxFilterButton) {
			if (!nameOfNewViewBox.getText().equals("")) { // A name has been
															// inserted

				View selectedView = master.getProject().getActiveView();

				int box = (Integer) boxSize.getValue();
				BoxFilter filter = new BoxFilter(selectedView, box,
						nameOfNewViewBox.getText());

				DoubleMatrixView newView = null;
				if (filterSelection.getSelectedItem().equals("Features"))
					newView = filter.filter("feature");
				else if (filterSelection.getSelectedItem().equals("Labels"))
					try {
						newView = filter.filter("label");
					} catch ( Exception ex ) { 
						JOptionPane.showMessageDialog(master, "View has no labels!", "Error",
								JOptionPane.INFORMATION_MESSAGE);
					}
				else if (filterSelection.getSelectedItem().equals("Probabilities"))
					try {
						newView = filter.filter("prob");
					} catch ( Exception ex ) {
						JOptionPane.showMessageDialog(master, "View has no probabilities!", "Error",
								JOptionPane.INFORMATION_MESSAGE);
					}
				if ( newView != null ) {
					master.getProject().add(newView);
				}

			} else { // No name for the new view is inserted
				JOptionPane.showMessageDialog(master, "Please insert name!", "Error",
						JOptionPane.INFORMATION_MESSAGE);
			}
			master.getDataManagementPanel().actualiseListen();
			nameOfNewViewBox.setText("");
		}
	}

	/**
	 * Utility class for a GridBagLayout to add components
	 * 
	 * @param cont
	 *            Container, to which the component should be added.
	 * @param gbl
	 *            For each Component needs to be defined a seperate
	 *            GridBagLayout.
	 * @param c
	 *            The Component to add to the Container
	 * @param x
	 *            position
	 * @param y
	 *            position
	 * @param width
	 * @param height
	 * @param weightx
	 *            1 if component should be as wide as possible, 0 if as
	 *            narrow/small as possible
	 * @param weighty
	 *            1 if component should be as high as possible, 0 if as
	 *            narrow/small as possible
	 */
	static void addComponent(Container cont, GridBagLayout gbl, Component c, int x,
			int y, int width, int height, double weightx, double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}

	protected void setDefaultName(String name) {
		this.nameOfNewViewBox.setText(name);
	}
}
