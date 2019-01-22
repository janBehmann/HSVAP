package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.util.Collections;


//import models.PolynomialApproximationModel;

import models.PolynomialApproximationModel;

import org.apache.commons.math3.util.Pair;
import org.jblas.DoubleMatrix;
import org.math.plot.Plot2DPanel;


import data.FeatureSelection;
import data.Utilities;
import data.View;

public class ApproximationWindow extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Plot2DPanel plotApproximationSingle;
	private Plot2DPanel plotResiduals;

	private double[][] spectren;
	private double[][] coords;
	private double[] label;
	
	private FeatureSelectionPanel master;
	
	private JCheckBox cbIndex;
	private JCheckBox absolute;
	private JCheckBox pointPlot;
	private JTable approxTable;
	
	private JTextField modelName;

	
	private View featureView;
	private ArrayList<Pair<Integer, Integer>> featureRanges;

	private JComboBox<JLabel> classBox;
	private JComboBox<JLabel> sampleBox;
	private ArrayList<Integer> classOrderInClassBox;

	private LinkedHashMap<Integer, String> map;
	
	/**
	 * For each class (index) the list of column indices is saved
	 */
	private HashMap<Integer,ArrayList<Integer>> coordsOfEachClass;
	

	private Point point;


	// enthält für jeden Approximationsbereich einen boolean, ob die Werte from, to und degree geändert wurden, ohne dass auf den Button "Approximate" gedrückt wurde
	// definiert, wann die approximierten Kurven geplottet werden sollen (nur wenn Button gedrückt wurde)
	private ArrayList<Boolean> committed;

	// speichert für jede Zeile in der Tabelle (Approximationsbereich) den index des samples, welches am schlectesten approximiert wurde
	private ArrayList<Integer> maxSOSIndizes;
	
	
	public ApproximationWindow(boolean modal, FeatureSelectionPanel master, View view) {

		// Daten abgreifen
		this.featureView = view;
		this.master = master;

		map = new LinkedHashMap<Integer, String>(featureView.getLabelMap());
		spectren = Utilities.materializeFeatures(featureView, FeatureSelection.getNonSpecialFeatures(featureView));
		coords = Utilities.materializeFeatures(featureView, FeatureSelection.getCoordinates(featureView));
		label = Utilities.materializeFeature(featureView, FeatureSelection.getLabel(featureView));
		
		// Welche samples gehören zu welcher Klasse
		coordsOfEachClass = new HashMap<Integer,ArrayList<Integer>>();
		for(int i = 0; i<coords.length; i++){
			ArrayList<Integer> mm = coordsOfEachClass.get((int)(label[i]));
			if(mm == null){
				mm = new ArrayList<Integer>();
			}
			mm.add(i);
			coordsOfEachClass.put((int)(label[i]), mm);
		}


		this.setModal(true);
		
		this.setSize(500, 500);
		
		
		
		this.setResizable(true);
		this.setTitle("Polynomial Approximation");
		this.setSize(new Dimension(1200,600));

		this.setLocation(50, 50);

		JPanel coeffPanel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		coeffPanel.setLayout(gbl);
		
		JPanel residualsPanel = new JPanel();
		GridBagLayout gblRes = new GridBagLayout();
		residualsPanel.setLayout(gblRes);
		residualsPanel.setPreferredSize(new Dimension(250,250));
		
		plotApproximationSingle = new Plot2DPanel();
		plotApproximationSingle.setFixedBounds(0, 0, 1);
		plotApproximationSingle.setFixedBounds(1, 0, 1);
		plotApproximationSingle.setBorder(BorderFactory.createTitledBorder("Single samples"));
		plotApproximationSingle.setOpaque(false);
		
		plotResiduals = new Plot2DPanel();
		plotResiduals.setFixedBounds(0, 0, 1);
		plotResiduals.setFixedBounds(1, 0, 1);
		plotResiduals.setBorder(BorderFactory.createTitledBorder("Residuals"));
		plotResiduals.setOpaque(false);
		
		// enthält für jeden Approximationsbereich (Zeilen der Tabelle) die Indizes des Feature range
		featureRanges = new ArrayList<Pair<Integer,Integer>>();
		
		// enthält für jeden Approximationsbereich einen boolean, ob die Werte from, to und degree geändert wurden, ohne dass auf den Button "Approximate" gedrückt wurde
		// definiert, wann die approximierten Kurven geplottet werden sollen (nur wenn Button gedrückt wurde)
		committed = new ArrayList<Boolean>();
		
		// speichert für jede Zeile in der Tabelle (Approximationsbereich) den index des samples, welches am schlectesten approximiert wurde
		maxSOSIndizes = new ArrayList<Integer>();

		
		// JComboboxen füllen
		classBox = new JComboBox<JLabel>();
		JLabel all = new JLabel("all classes");
		Color cl = new Color(0, 0, 0, 0); 
		int clrInt = cl.getRGB();
		int[] clrImage = {	clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt};
		ImageProducer p = new MemoryImageSource( 10, 10, clrImage, 0, 10 );
		Image image = all.createImage( p );
		ImageIcon icon = new ImageIcon(image,"class color");
		all.setIcon(icon);
		classBox.addItem(all);
		
		classOrderInClassBox = new ArrayList<Integer>();
		for(Map.Entry<Integer, String> l: map.entrySet()){
			int clrInt1 = Color.decode(l.getValue().substring(l.getValue().length()-7, l.getValue().length())).getRGB();
			int[] clrImage1 = {	clrInt1, clrInt1,clrInt1,clrInt1,clrInt1, clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1, clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1, clrInt1, clrInt1,clrInt1,clrInt1,clrInt1};
			JLabel akku = new JLabel(l.getValue().substring(0, l.getValue().length()-8).toString());
			ImageProducer p1 = new MemoryImageSource( 10, 10, clrImage1, 0, 10 );
			Image image1 = akku.createImage( p1 );
			ImageIcon icon1 = new ImageIcon(image1,"class color");
			akku.setIcon(icon1);
			classBox.addItem(akku);
			classOrderInClassBox.add(l.getKey());
		}

		classBox.setRenderer(new IconRenderer2());
		classBox.setBorder(BorderFactory.createTitledBorder("label"));
		classBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int indCB = classBox.getSelectedIndex();
				fillSampleBox(indCB);
			}
		});
		
		sampleBox = new JComboBox<JLabel>();
		fillSampleBox(0);
		
		sampleBox.setRenderer(new IconRenderer2());
		sampleBox.setBorder(BorderFactory.createTitledBorder("sample"));
		sampleBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int indCB = classBox.getSelectedIndex();
				int indSB = sampleBox.getSelectedIndex();
				plotSampleDetailAndResiduals(indCB,indSB);
				
			}
		});
		

		
		DecimalFormat waveF = new DecimalFormat("#0");
		Object[][] rowData = new Object[featureRanges.size()][7];
		for(int i=0; i< featureRanges.size(); i++){
			rowData[i][0] = String.valueOf(waveF.format(featureRanges.get(i).getFirst()));
			rowData[i][1] = String.valueOf(waveF.format(featureRanges.get(i).getSecond()));
			rowData[i][2] = "2";

			rowData[i][3] = new JButton("");;	//Color
			rowData[i][4] = new JButton("Approximate"); //Approximate
			rowData[i][5] = ""; //SOS
			rowData[i][6] = ""; //worst sample SOS
		}
		DefaultTableModel dm = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;

			@Override 
			public boolean isCellEditable(int row, int column){
				if(column==0 |column==1|column==2){
					return true;
				}else{
					return false;
				}
			}
		};
		dm.setDataVector(rowData, new Object[] {"From","To", "Degree","Color","Approximate","Mean Sum of Squares","Worst Sample MeanSOS"});
		approxTable = new JTable(dm);
		approxTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		approxTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		approxTable.getColumnModel().getColumn(2).setPreferredWidth(45);
		approxTable.getColumnModel().getColumn(3).setPreferredWidth(35);
		approxTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		approxTable.getColumnModel().getColumn(5).setPreferredWidth(100);
		approxTable.getColumnModel().getColumn(6).setPreferredWidth(110);
		
		DefaultTableCellRenderer renderer =new DefaultTableCellRenderer();
		renderer.setToolTipText("Click to show worst sample");
		
//		renderer.setFont(font)
		approxTable.getColumnModel().getColumn(6).setCellRenderer(renderer);
		
		approxTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		approxTable.setRowSelectionAllowed(false);
	
		/**
		 * Einfügen eines neuen Bereiches in die Liste
		 */
		JButton insert = new JButton("Add Range");
		insert.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				JTextField from = new JTextField(5);
				JTextField to = new JTextField(5);
				JTextField order = new JTextField(2);

				JPanel myPanel = new JPanel();
				myPanel.add(new JLabel("from:"));
				myPanel.add(from);
				myPanel.add(Box.createHorizontalStrut(15)); // a spacer
				myPanel.add(new JLabel("to:"));
				myPanel.add(to);
				myPanel.add(Box.createHorizontalStrut(15)); // a spacer
				myPanel.add(new JLabel("order:"));
				myPanel.add(order);
				
				String title = "Please Enter range and order";
				getInsertValues( myPanel, from, to, order, title);
			}

			/**
			 * 
			 * rekursive Formulierung zur Fehlerabfangung
			 */
			private void getInsertValues(JPanel myPanel,JTextField from,JTextField to,JTextField order,String title) {
				int result = JOptionPane.showConfirmDialog(null, myPanel, 
						title, JOptionPane.OK_CANCEL_OPTION);

				if(result==JOptionPane.OK_OPTION){
					
					ArrayList<Double> xx = new ArrayList<Double>();
					double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(featureView);
					for(int i=0; i<x.length; i++){
						xx.add(x[i]);
					}
					
					try{
						double fr = Double.parseDouble(from.getText());
						double too = Double.parseDouble(to.getText());
						int or = Integer.parseInt(order.getText());
						
						if(or<0 | or>15){
							throw new Error("Order must be between 0 and 15!");
						}
						
						if(fr<Collections.min(xx) | fr >Collections.max(xx) | fr>=too | too<Collections.min(xx) | too >Collections.max(xx)){
							throw new Error("Invalid range values!");
						}
						
						DefaultTableModel m =  (DefaultTableModel) approxTable.getModel();
						m.addRow(new Object[]{from.getText(), to.getText(), order.getText(),new JButton(""),new JButton("Approximate"),"",""});

						committed.add(false);
						maxSOSIndizes.add(-1);
					}catch(Error e){
						
						String title2 = e.getMessage();
						getInsertValues( myPanel, from, to, order, title2);
					}catch(NumberFormatException h){
						String title2 = "Only numbers with point separator.\n"+h.getMessage();
						getInsertValues( myPanel, from, to, order, title2);
					}
				}
			}
		});
		
		// bei checkbox aktivieren werden die Spaltennamen der Tabelle geändert sowie die bounds umgerechnet 
		cbIndex = new JCheckBox("real value bounds");
		cbIndex.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(featureView);
				
				if(cbIndex.isSelected()){
					approxTable.getColumnModel().getColumn(0).setHeaderValue("From");
					approxTable.getColumnModel().getColumn(1).setHeaderValue("To");
					approxTable.getColumnModel().getColumn(0).setPreferredWidth(65);
					approxTable.getColumnModel().getColumn(1).setPreferredWidth(65);
					
					// Bounds umrechnen von index zu double value
					for(int row = 0; row<approxTable.getRowCount(); row++){
						int fromInd = Integer.parseInt(approxTable.getValueAt(row, 0).toString());
						int toInd = Integer.parseInt(approxTable.getValueAt(row, 1).toString());
						double from = getFrom(fromInd, x);
						double to = getTo(toInd, x);
						approxTable.setValueAt(from, row, 0);
						approxTable.setValueAt(to, row, 1);
					}
				}else{
					approxTable.getColumnModel().getColumn(0).setHeaderValue("From index");
					approxTable.getColumnModel().getColumn(1).setHeaderValue("To index");
					approxTable.getColumnModel().getColumn(0).setPreferredWidth(65);
					approxTable.getColumnModel().getColumn(1).setPreferredWidth(65);
					
					// Bounds umrechnen von double value zu index
					for(int row = 0; row<approxTable.getRowCount(); row++){
						double from = Double.parseDouble(approxTable.getValueAt(row, 0).toString());
						double to = Double.parseDouble(approxTable.getValueAt(row, 1).toString());
						int fromInd = getFromInd(from, x);
						int toInd = getToInd(to, x);
						approxTable.setValueAt(fromInd, row, 0);
						approxTable.setValueAt(toInd, row, 1);
					}
				
				}
				approxTable.repaint();
				approxTable.getTableHeader().repaint();
			}
		});
		cbIndex.setSelected(true);
		cbIndex.setToolTipText("<html>Define, whether the bounds should be real values or index values,<br>i.e. either from wavelength 400 to 600 or from the 10th feature to the 20th.</html>");


		approxTable.setDefaultRenderer(Object.class, new MyButtonRenderer());
		approxTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = approxTable.rowAtPoint(e.getPoint());
				int column = approxTable.columnAtPoint(e.getPoint());
				try{
					if (approxTable.getValueAt(row, column) instanceof JButton) {
						JButton btn = (JButton) approxTable.getValueAt(row, column);
						if(btn.getText()==""){ //Color-Button: Farbe setzen
							final JColorChooser colorChooser = new JColorChooser();

							ActionListener okActionListener = new ActionListener() {
								public void actionPerformed(ActionEvent actionEvent) {
								}
							};

							ActionListener cancelActionListener = new ActionListener() {
								public void actionPerformed(ActionEvent actionEvent) {
								}
							};

							final JDialog dialog = JColorChooser.createDialog(null, "Choose Color", true,
									colorChooser, okActionListener, cancelActionListener);

							dialog.setVisible(true);
							Color color = colorChooser.getColor();
							btn.setForeground(color);
							btn.setBackground(color);
							btn.setContentAreaFilled(false);
							btn.setBorderPainted(false);
							btn.setFocusPainted(false);
							btn.setOpaque(true);
							approxTable.setValueAt(btn, row, column);
						}else if(btn.getText()=="Approximate"){
							
							//Hier geschieht die Approxation
							int order = Integer.parseInt(approxTable.getValueAt(row, 2).toString());
							double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(featureView);
							
							//Ableiten der Grenzen, Index wird benötigt
							int fromInd;
							int toInd;
							if(cbIndex.isSelected()){
								double from = Double.parseDouble(approxTable.getValueAt(row, 0).toString());
								double to = Double.parseDouble(approxTable.getValueAt(row, 1).toString());
								fromInd = getFromInd(from, x);
								toInd = getToInd(to, x);
							}else{
								fromInd = Integer.parseInt(approxTable.getValueAt(row, 0).toString());
								toInd = Integer.parseInt(approxTable.getValueAt(row, 1).toString());
							}
							int leng = toInd-fromInd+1;
							
							// selected wavelength data
							double[] xData = getXData(fromInd, toInd, x);
							
							// SumOfSquares Error und Indizes für das schlechteste Sample 
							double maxSOS = 0;
							int maxSOSInd = -1;
							
							// SumOfSquares Error über alle Samples aufsummiert
							double totSOS = 0;
							
//							
							// Iterieren über alle Samples des Views
							for(int j=0;j<spectren.length;j++){
								int k=0;
								
								// Abgreifen der Funktonswerte (Intensitätswerte) in den Bereichen für jeden Bereich
								double[] yData = new double[leng]; 
								for(int i=fromInd;i<=toInd;i++){
									yData[k]=spectren[j][i];
									k++;
								}
								
								// Schätzen des Polynoms
								feature_selection.PolynomialRegression pol = new feature_selection.PolynomialRegression(xData, yData, order);
								
								//Residuals sum of squares
								double SOS = pol.getResidualsSOS();
								totSOS = totSOS+SOS;
								if(SOS>maxSOS){
									maxSOS = SOS;
									maxSOSInd = j;
								}
							}

							// Werte in 5. und 6. Spalte setzen
							approxTable.setValueAt(String.format("%.3f",totSOS), row, 5);
							approxTable.setValueAt("<html><u><b><font color=\"#0000FF\">"+String.format("%.3f",maxSOS)+"</font></b></u></html>", row, 6);

							committed.set(row, true);
							maxSOSIndizes.set(row,maxSOSInd);

							plotApproximationSingle.repaint();

						}
					}
					if(column==0 | column==1 | column==2 | column==3){
						
						// Wenn geändert, verschwindet die Approximation aus dem plot, Button muss erneut gedrückt werden
						committed.set(row, false);
						maxSOSIndizes.set(row, -1);
						approxTable.setValueAt("", row, 5);
						approxTable.setValueAt("", row, 6);
					}
					if(column == 6){
						
						// Verlinkung des schlechtesten samples
						// schlechtestes sample darstellen
						if(maxSOSIndizes.get(row)!=-1){
							classBox.setSelectedIndex(0);
							sampleBox.setSelectedIndex(maxSOSIndizes.get(row)+1);
							//					plotSampleDetailAndResiduals(0, );
						}
					}

					// Auslösen, dass untere JComboBox geklickt wird, sodass sich Plots aktualisieren
					for(ActionListener a: sampleBox.getActionListeners()) {
						a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null) {
							private static final long serialVersionUID = 1L;
						});
					}
				}catch(ArrayIndexOutOfBoundsException wfa){
					JOptionPane.showMessageDialog(null, wfa.getMessage(), "Error" , JOptionPane.ERROR_MESSAGE);
				}catch(java.lang.NegativeArraySizeException f){
					JOptionPane.showMessageDialog(null, "Invalid degree or range values!", "Error" , JOptionPane.ERROR_MESSAGE);
				}catch(java.lang.NumberFormatException f){
					//  Matrix is rank deficient.
					JOptionPane.showMessageDialog(null, "Please insert only numbers with point separator.\n"+f.getMessage(), "Error" , JOptionPane.ERROR_MESSAGE);
				}catch(java.lang.RuntimeException f){
					//  Matrix is rank deficient.
					JOptionPane.showMessageDialog(null, f.getMessage()+"\nPlease widen the spectral range or decrease the degree.", "Error" , JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		
		// Rechtsklick definieren, um Löschen zu ermöglichen
		approxTable.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseReleased(MouseEvent e) {
		        int r = approxTable.rowAtPoint(e.getPoint());
		        if (r >= 0 && r < approxTable.getRowCount()) {
		        	approxTable.setRowSelectionInterval(r, r);
		        } else {
		        	approxTable.clearSelection();
		        }

		        int rowindex = approxTable.getSelectedRow();
		        if (rowindex < 0)
		            return;

		        if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
		            
		            point = e.getPoint();
		            
		            JPopupMenu popup = new JPopupMenu();
		            JMenuItem menuItem = new JMenuItem("delete");
		            menuItem.setToolTipText("Delete row of table");
		            Image img = ((ImageIcon) UIManager.getIcon("OptionPane.errorIcon")).getImage();
		            Image newimg = img.getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH);
		            ImageIcon newIcon = new ImageIcon(newimg);
		            menuItem.setIcon(newIcon);
		            menuItem.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent arg0) {
							DefaultTableModel model = (DefaultTableModel) approxTable.getModel();
							int row = approxTable.rowAtPoint(point);
							
							
							// Löschen
							model.removeRow(row);
							committed.remove(row);
							maxSOSIndizes.remove(row);
							
							//repaint auslösen
							for(ActionListener a: sampleBox.getActionListeners()) {
							    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null) {
									private static final long serialVersionUID = 1L;
							    });
							}
						}
					});
		            popup.add( menuItem );
		            popup.show(e.getComponent(), e.getX(), e.getY());
		        }
		    }

		    
		    // Cursor für 6. Spalte auf Hand-Cursor umstellen
			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				if(approxTable.columnAtPoint(e.getPoint())==6){
				    approxTable.setCursor(Cursor.getPredefinedCursor
		                      (Cursor.HAND_CURSOR)); 
				}else{
					approxTable.setCursor(Cursor.getPredefinedCursor
		                      (Cursor.DEFAULT_CURSOR));
				}
				
			}
		    
			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				if(approxTable.columnAtPoint(e.getPoint())==6){
				    approxTable.setCursor(Cursor.getPredefinedCursor
		                      (Cursor.HAND_CURSOR)); 
				}else{
					approxTable.setCursor(Cursor.getPredefinedCursor
		                      (Cursor.DEFAULT_CURSOR));
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				
				if(approxTable.columnAtPoint(e.getPoint())==6){
				    approxTable.setCursor(Cursor.getPredefinedCursor
		                      (Cursor.HAND_CURSOR)); 
				}else{
					approxTable.setCursor(Cursor.getPredefinedCursor
		                      (Cursor.DEFAULT_CURSOR));
				}
			}
		    
		});
		
		
		// Für die beiden Checkboxen einen repaint auslösen, in dem untere Combobox ausgelöst wird
		absolute = new JCheckBox("absolute values",true);
		absolute.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(ActionListener a: sampleBox.getActionListeners()) {
				    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null) {
						private static final long serialVersionUID = 1L;
				    });
				}
				
			}
		});
		pointPlot = new JCheckBox("point plot",true);
		pointPlot.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(ActionListener a: sampleBox.getActionListeners()) {
				    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null) {
						private static final long serialVersionUID = 1L;
				    });
				}
			}
		});
		
		// Comcoboxen in Normalstellung setzen
		plotSampleDetailAndResiduals(0,0);
		
		modelName = new JTextField();

		// Modell abspeichern
		JButton saveModel = new JButton("Create Model");
		saveModel.setToolTipText("Create a model to apply this polynomial approximation on views");
		saveModel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(modelName.getText()!=""){
				
				PolynomialApproximationModel model = new PolynomialApproximationModel("PAmodel_"+modelName.getText(), getThis().featureView.getViewName(), new Date());
				double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(getThis().featureView);
				
				for(int i = 0; i<approxTable.getRowCount();i++){
					if(committed.get(i)){

						int order = Integer.parseInt(approxTable.getValueAt(i, 2).toString());
						int fromInd;
						int toInd;
						if(cbIndex.isSelected()){
							double from = Double.parseDouble(approxTable.getValueAt(i, 0).toString());
							double to = Double.parseDouble(approxTable.getValueAt(i, 1).toString());
							fromInd = getFromInd(from, x);
							toInd = getToInd(to, x);
						}else{
							fromInd = Integer.parseInt(approxTable.getValueAt(i, 0).toString());
							toInd = Integer.parseInt(approxTable.getValueAt(i, 1).toString());
						}

						int[] xDataIndices = getXDataIndices(fromInd, toInd, x);

						model.addAproximation(xDataIndices, order);
						
					}
				}
				

				// Modell der Liste hinzufügen
				getMaster().getMaster().getProject().getModels().add(model);
				// Aktualsieren der Modell-JList
				getMaster().getMaster().getDataManagementPanel().actualiseListen();
				//Fenster schließen
				getThis().dispose();
				
				}
			}
		});

		JScrollPane scrpTable = new JScrollPane();
		scrpTable.setViewportView(approxTable);
		approxTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		approxTable.setFillsViewportHeight(true);
		approxTable.setPreferredScrollableViewportSize(approxTable.getPreferredSize());
//		scrpTable.setPreferredSize(approxTable.getPreferredSize());
		 
		DataManagementPanel.addComponent(coeffPanel, gbl, insert	, 1, 0, 1, 1, 0, 0);
		DataManagementPanel.addComponent(coeffPanel, gbl, cbIndex	, 0, 0, 1, 1, 1, 0);
		DataManagementPanel.addComponent(coeffPanel, gbl, scrpTable	, 0, 1, 2, 1, 1, 1);
		DataManagementPanel.addComponent(coeffPanel, gbl, new JLabel("model name: "), 0, 2, 1, 1, 0, 0);
		DataManagementPanel.addComponent(coeffPanel, gbl, modelName, 1, 2, 1, 1, 1, 0);
		DataManagementPanel.addComponent(coeffPanel, gbl, saveModel, 0, 3, 2, 1, 0, 0);

		DataManagementPanel.addComponent(residualsPanel, gblRes, classBox  , 0, 0, 1, 1, 1, 0);
		DataManagementPanel.addComponent(residualsPanel, gblRes, sampleBox, 0, 1, 1, 1, 1, 0);
		DataManagementPanel.addComponent(residualsPanel, gblRes, absolute, 0, 2, 1, 1, 1, 0);
		DataManagementPanel.addComponent(residualsPanel, gblRes, pointPlot, 0, 3, 1, 1, 1, 0);
		
		JSeparator separator = new JSeparator();
		
		
		
		JPanel oben = new JPanel();
		GridBagLayout gblOben = new GridBagLayout();
		oben.setLayout(gblOben);
		DataManagementPanel.addComponent(oben,gblOben,coeffPanel					,0,0,1,1,0,0);
		DataManagementPanel.addComponent(oben,gblOben,plotApproximationSingle		,1,0,1,1,1,1);
		DataManagementPanel.addComponent(oben,gblOben,separator						,0,1,2,1,1,0);
		DataManagementPanel.addComponent(oben,gblOben,residualsPanel				,0,2,1,1,0,0);
		DataManagementPanel.addComponent(oben,gblOben,plotResiduals					,1,2,1,1,1,1);
		

		this.add(oben);
		this.setVisible(true);

		
	}

/**
 * This method actualizes the approximation plot as well as the residuals plot according to the selected class and feature
 * There are four cases: 	- Classbox selected allclasses & Samplebox selected mean
 * 							- Classbox selected a class & Samplebox selected mean
 * 							- Classbox selected allclasses & Samplebox selected a sample
 * 							- Classbox selected a class & Samplebox selected a sample
 * 
 * @param indCB: Index of the selected entry in classbox: i.e. 0 for all classes, indCB+1 for class indCB
 * @param indSB: Index of the selected entry in samplebox: i.e. 0 for mean, indSB+1 for sample indSB
 */
	private void plotSampleDetailAndResiduals(int indCB, int indSB) {
		plotApproximationSingle.removeAllPlots();
		plotResiduals.removeAllPlots();

		try{

			double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(this.featureView);
			if(indSB==-1){

			}else if(indSB==0){		//einzelne Mittelwerte plotten
				// Classbox selected a class & Samplebox selected mean
				if(indCB!=0){
					ArrayList<Integer> samples = coordsOfEachClass.get(classOrderInClassBox.get(indCB-1));
					int[] rows = new int[samples.size()];
					for(int i=1;i<samples.size();i++){
						rows[i] = samples.get(i);
					}
					double[] mean = new DoubleMatrix(spectren).getRows(rows).columnMeans().toArray();;
					//				double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(this.featureView);


					String classString = map.get(classOrderInClassBox.get(indCB-1));
					Color clr = Color.decode(classString.substring(classString.length()-7, classString.length()));
					plotApproximationSingle.addLinePlot(null, clr, x, mean);
					// Plot auf die Daten skalieren
					plotApproximationSingle.setFixedBounds(0, FeatureSelectionPanel.getMin(x), FeatureSelectionPanel.getMax(x));		
					plotApproximationSingle.setFixedBounds(1,new DoubleMatrix(spectren).min(), new DoubleMatrix(spectren).max());
					for(int i = 0; i<approxTable.getRowCount();i++){
						if(committed.get(i)){

							int order = Integer.parseInt(approxTable.getValueAt(i, 2).toString());
							//							double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(featureView);
							int fromInd;
							int toInd;
							if(cbIndex.isSelected()){
								double from = Double.parseDouble(approxTable.getValueAt(i, 0).toString());
								double to = Double.parseDouble(approxTable.getValueAt(i, 1).toString());
								fromInd = getFromInd(from, x);
								toInd = getToInd(to, x);
							}else{
								fromInd = Integer.parseInt(approxTable.getValueAt(i, 0).toString());
								toInd = Integer.parseInt(approxTable.getValueAt(i, 1).toString());
							}
							int leng = toInd-fromInd+1;
							double[] xData = getXData(fromInd, toInd, x);

							int k=0;
							double[] yData = new double[leng]; 
							for(int l=fromInd;l<=toInd;l++){
								yData[k]=mean[l];
								k++;
							}

							feature_selection.PolynomialRegression polMean = new feature_selection.PolynomialRegression(xData, yData, order);
							double[] plotspect = new double[mean.length];
							for(int j = 0; j<xData.length;j++){
								plotspect[j] = polMean.predict(xData[j]);
							}
							Color clr2 = ((JButton) approxTable.getValueAt(i, 3)).getBackground();

							plotApproximationSingle.addLinePlot(null, clr2, xData, plotspect);
							plotApproximationSingle.repaint();

							double[] res = polMean.getResiduals();
							if(absolute.isSelected()){
								for(int kk=0; kk<res.length;kk++){
									res[kk]=Math.abs(res[kk]);
								}
							}
							if(pointPlot.isSelected()){
								plotResiduals.addScatterPlot(null, clr2, xData, res);
							}else{
								plotResiduals.addLinePlot(null, clr2, xData, res);
							}
							plotResiduals.repaint();

						}
					}

				}else{
					// Classbox selected allclasses & Samplebox selected mean
					//alle Mittelwerte plotten
					plotResiduals.removeAllPlots();
					plotApproximationSingle.removeAllPlots();
					for(Map.Entry<Integer, ArrayList<Integer>> mapEntry : coordsOfEachClass.entrySet()){
						ArrayList<Integer> classIndizes =  mapEntry.getValue();
						int[] indizes = new int[classIndizes.size()];
						for(int i=0; i<classIndizes.size(); i++){
							indizes[i] = classIndizes.get(i);
						}
						double[] mean = new DoubleMatrix(spectren).getRows(indizes).columnMeans().toArray();;
						String classString = map.get(mapEntry.getKey());
						Color clr = Color.decode(classString.substring(classString.length()-7, classString.length()));
						plotApproximationSingle.addLinePlot(null, clr, x, mean);

						for(int i = 0; i<approxTable.getRowCount();i++){
							if(committed.get(i)){

								int order = Integer.parseInt(approxTable.getValueAt(i, 2).toString());
								int fromInd;
								int toInd;
								if(cbIndex.isSelected()){
									double from = Double.parseDouble(approxTable.getValueAt(i, 0).toString());
									double to = Double.parseDouble(approxTable.getValueAt(i, 1).toString());
									fromInd = getFromInd(from, x);
									toInd = getToInd(to, x);
								}else{
									fromInd = Integer.parseInt(approxTable.getValueAt(i, 0).toString());
									toInd = Integer.parseInt(approxTable.getValueAt(i, 1).toString());
								}
								int leng = toInd-fromInd+1;
								double[] xData = getXData(fromInd, toInd, x);

								int k=0;
								double[] yData = new double[leng]; 
								for(int l=fromInd;l<=toInd;l++){
									yData[k]=mean[l];
									k++;
								}

								feature_selection.PolynomialRegression polMean = new feature_selection.PolynomialRegression(xData, yData, order);
								double[] plotspect = new double[mean.length];
								for(int j = 0; j<xData.length;j++){
									plotspect[j] = polMean.predict(xData[j]);
								}

								Color clr2 = ((JButton) approxTable.getValueAt(i, 3)).getBackground();

								plotApproximationSingle.addLinePlot(null, clr2, xData, plotspect);
								plotApproximationSingle.repaint();

								double[] res = polMean.getResiduals();
								if(absolute.isSelected()){
									for(int kk=0; kk<res.length;kk++){
										res[kk]=Math.abs(res[kk]);
									}
								}
								if(pointPlot.isSelected()){
									plotResiduals.addScatterPlot(null, clr, xData, res);
								}else{
									plotResiduals.addLinePlot(null, clr, xData, res);
								}
								plotResiduals.repaint();

							}
						}
					}
					plotApproximationSingle.repaint();
					plotApproximationSingle.setFixedBounds(0, FeatureSelectionPanel.getMin(x), FeatureSelectionPanel.getMax(x));		
					plotApproximationSingle.setFixedBounds(1,new DoubleMatrix(spectren).min(), new DoubleMatrix(spectren).max());
				}
			}else{
				if(indCB==0){
					//Classbox selected allclasses & Samplebox selected a sample
					double[] plotspect = new DoubleMatrix(spectren).getRow(indSB-1).toArray();
					plotResiduals.removeAllPlots();
					plotApproximationSingle.removeAllPlots();
					String classString = map.get((int)(label[indSB-1]));

					Color clr = Color.decode(classString.substring(classString.length()-7, classString.length()));
					plotApproximationSingle.addLinePlot(null, clr, x, plotspect);


					for(int i = 0; i<approxTable.getRowCount();i++){
						if(committed.get(i)){

							int order = Integer.parseInt(approxTable.getValueAt(i, 2).toString());
							int fromInd;
							int toInd;
							if(cbIndex.isSelected()){
								double from = Double.parseDouble(approxTable.getValueAt(i, 0).toString());
								double to = Double.parseDouble(approxTable.getValueAt(i, 1).toString());
								fromInd = getFromInd(from, x);
								toInd = getToInd(to, x);
							}else{
								fromInd = Integer.parseInt(approxTable.getValueAt(i, 0).toString());
								toInd = Integer.parseInt(approxTable.getValueAt(i, 1).toString());
							}
							int leng = toInd-fromInd+1;
							double[] xData = getXData(fromInd, toInd, x);

							int k=0;
							double[] yData = new double[leng]; 
							for(int l=fromInd;l<=toInd;l++){
								yData[k]=plotspect[l];
								k++;
							}
							feature_selection.PolynomialRegression polSample = new feature_selection.PolynomialRegression(xData, yData, order);

							double[] plotspectAppr = new double[yData.length];
							for(int j = 0; j<xData.length;j++){
								plotspectAppr[j] = polSample.predict(xData[j]);
							}
							Color clr2 = ((JButton) approxTable.getValueAt(i, 3)).getBackground();
							//				
							plotApproximationSingle.addLinePlot(null, clr2, xData, plotspectAppr);
							plotApproximationSingle.repaint();

							double[] res = polSample.getResiduals();
							if(absolute.isSelected()){
								for(int kk=0; kk<res.length;kk++){
									res[kk]=Math.abs(res[kk]);
								}
							}
							if(pointPlot.isSelected()){
								plotResiduals.addScatterPlot(null, clr2, xData, res);
							}else{
								plotResiduals.addLinePlot(null, clr2, xData, res);
							}
							plotResiduals.repaint();


						}
					}
					plotApproximationSingle.setFixedBounds(0, FeatureSelectionPanel.getMin(x), FeatureSelectionPanel.getMax(x));		
					plotApproximationSingle.setFixedBounds(1,new DoubleMatrix(spectren).min(), new DoubleMatrix(spectren).max());
				}else{
					// Classbox selected a class & Samplebox selected a sample
					int index = coordsOfEachClass.get(classOrderInClassBox.get(indCB-1)).get(indSB-1);

					//				double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(this.featureView);
					double[] plotspect = new DoubleMatrix(spectren).getRow(index).toArray();
					plotApproximationSingle.removeAllPlots();
					plotResiduals.removeAllPlots();
					String classString = map.get((int)(label[index]));
					//				String classString = map.get(classOrderInClassBox.get(indCB-1));
					Color clr = Color.decode(classString.substring(classString.length()-7, classString.length()));
					plotApproximationSingle.addLinePlot(null, clr, x, plotspect);
					for(int i = 0; i<approxTable.getRowCount();i++){
						if(committed.get(i)){

							int order = Integer.parseInt(approxTable.getValueAt(i, 2).toString());
							//						double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(featureView);
							int fromInd;
							int toInd;
							if(cbIndex.isSelected()){
								double from = Double.parseDouble(approxTable.getValueAt(i, 0).toString());
								double to = Double.parseDouble(approxTable.getValueAt(i, 1).toString());
								fromInd = getFromInd(from, x);
								toInd = getToInd(to, x);
							}else{
								fromInd = Integer.parseInt(approxTable.getValueAt(i, 0).toString());
								toInd = Integer.parseInt(approxTable.getValueAt(i, 1).toString());
							}
							int leng = toInd-fromInd+1;
							double[] xData = getXData(fromInd, toInd, x);
							//						int[] indizesBereich = getIndizesBereich(fromInd, toInd, x);


							int k=0;
							double[] yData = new double[leng]; 
							for(int l=fromInd;l<=toInd;l++){
								yData[k]=plotspect[l];
								k++;
							}
							feature_selection.PolynomialRegression polSample = new feature_selection.PolynomialRegression(xData, yData, order);

							double[] plotspectAppr = new double[yData.length];
							for(int j = 0; j<xData.length;j++){
								plotspectAppr[j] = polSample.predict(xData[j]);
							}
							Color clr2 = ((JButton) approxTable.getValueAt(i, 3)).getBackground();
							//				
							plotApproximationSingle.addLinePlot(null, clr2, xData, plotspectAppr);
							plotApproximationSingle.repaint();

							double[] res = polSample.getResiduals();
							if(absolute.isSelected()){
								for(int kk=0; kk<res.length;kk++){
									res[kk]=Math.abs(res[kk]);
								}
							}
							if(pointPlot.isSelected()){
								plotResiduals.addScatterPlot(null, clr2, xData, res);
							}else{
								plotResiduals.addLinePlot(null, clr2, xData, res);
							}
							plotResiduals.repaint();


						}
					}

				}
			}
			plotApproximationSingle.repaint();
			// Plot auf die Daten skalieren
			plotApproximationSingle.setFixedBounds(0, FeatureSelectionPanel.getMin(x), FeatureSelectionPanel.getMax(x));		
			plotApproximationSingle.setFixedBounds(1,new DoubleMatrix(spectren).min(), new DoubleMatrix(spectren).max());
			plotResiduals.repaint();
			plotResiduals.setFixedBounds(0, FeatureSelectionPanel.getMin(x), FeatureSelectionPanel.getMax(x));

		}catch(Exception exc){

		}
	}


	/**
	 * Put entries into sample box according to pixels (=samples) of the class
	 * 
	 * @param indCB: Index of the selected entry in classbox: i.e. 0 for all classes, indCB+1 for class indCB
	 */
	private void fillSampleBox(int indCB) {
		ArrayList<Integer> samples = new ArrayList<Integer>();
		if(indCB==0){
			for(int i: classOrderInClassBox){
				ArrayList<Integer> val = coordsOfEachClass.get(i);
				if(val!=null){
					samples.addAll(val);
				}
			}
		}else{
			if(coordsOfEachClass.get(classOrderInClassBox.get(indCB-1))==null){
				sampleBox.removeAllItems();
				sampleBox.setEnabled(false);

				plotApproximationSingle.removeAllPlots();
				plotResiduals.removeAllPlots();
				return;
			}
			samples.addAll(coordsOfEachClass.get(classOrderInClassBox.get(indCB-1)));
		}

		sampleBox.removeAllItems();
		//		sampleIndizes = new ArrayList<Integer>();

		JLabel all = new JLabel("all classes");
		Color cl = new Color(0, 0, 0, 0); 
		int clrInt = cl.getRGB();
		int[] clrImage = {	clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
				clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt};
		ImageProducer p = new MemoryImageSource( 10, 10, clrImage, 0, 10 );
		Image image = all.createImage( p );
		ImageIcon icon2 = new ImageIcon(image,"class color");

		JLabel mean = new JLabel("mean");
		mean.setIcon(icon2);
		sampleBox.addItem(mean);
		int anzahl = 0;
		for(int i: samples){
			String classString = map.get((int)(label[i]));
			int clrInt1 = Color.decode(classString.substring(classString.length()-7, classString.length())).getRGB();
			int[] clrImage1 = {	clrInt1, clrInt1,clrInt1,clrInt1,clrInt1, clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1, clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,clrInt1, clrInt1,clrInt1,clrInt1,clrInt1,
					clrInt1, clrInt1,clrInt1,clrInt1,clrInt1, clrInt1, clrInt1,clrInt1,clrInt1,clrInt1};

			JLabel akku = new JLabel("("+coords[i][0]+", "+coords[i][1]+")");
			ImageProducer p1 = new MemoryImageSource( 10, 10, clrImage1, 0, 10 );
			Image image1 = akku.createImage( p1 );
			ImageIcon icon1 = new ImageIcon(image1,"class color");
			akku.setIcon(icon1);
			sampleBox.addItem(akku);
			//				sampleIndizes.add(i);
			anzahl++;

		}
		if(anzahl == 0){
			sampleBox.removeAllItems();
			sampleBox.setEnabled(false);
		}else{
			if(approxTable!=null){
				sampleBox.setEnabled(true);
				plotSampleDetailAndResiduals(indCB,0);
			}
		}
	}
	/**
	 * Retrieve index of the wavelength from double value
	 * 
	 * @param from: Double value
	 * @param x: Feature descriptors, i.e. wavelength values 
	 * @return Index
	 */
	private int getFromInd(double from, double[] x){
		int fromInd = 0;
		double minDiff1 = Double.POSITIVE_INFINITY;
		for(int i=0;i<x.length;i++){
			if(Math.abs(x[i]-from)<minDiff1 & x[i]>=from){
				minDiff1 = Math.abs(x[i]-from);
				fromInd = i;
			}
		}
		return fromInd;
	}

	/**
	 * Retrieve index of the wavelength from double value
	 * 
	 * @param to: Double value
	 * @param x: Feature descriptors, i.e. wavelength values 
	 * @return Index
	 */
	private int getToInd(double to, double[] x){
		double minDiff2 = Double.POSITIVE_INFINITY;
		int toInd = 0;
		for(int i=0;i<x.length;i++){
			if(Math.abs(x[i]-to)< minDiff2 & x[i]<=to){
				minDiff2 =Math.abs(x[i]-to);
				toInd = i;
			}
		}
		return toInd;
	}
	
	/**
	 * Retrieve double value of the wavelength from index
	 * 
	 * @param from: Index
	 * @param x: Feature descriptors, i.e. wavelength values 
	 * @return Double value
	 */
	private double getFrom(int from, double[] x){
		return x[from];
	}
	
	/**
	 * Retrieve double value of the wavelength from index
	 * 
	 * @param to: Index
	 * @param x: Feature descriptors, i.e. wavelength values 
	 * @return Double value
	 */
	private double getTo(int to, double[] x){
		return x[to];
	}
	
	/**
	 * returns the wavelengths inside the indices from and to (index to is included)
	 * 
	 * @param fromInd: Index where approximation area begins
	 * @param toInd: Index where approximation area ends (including this entry) 
	 * @param x: Feature descriptors, i.e. wavelength values 
	 * @return wavelength values of the approximation area
	 */
	public static double[] getXData(int fromInd, int toInd, double[] x){
		int leng = toInd-fromInd+1;
		double[] xData = new double[leng];
		int k=0;
		for(int i=fromInd;i<=toInd;i++){
			xData[k] = x[i];
			k++;
		}
		return xData;
	}
	
	
	/**
	 * returns the wavelengths indices inside the indices from and to (index to is included)
	 * 
	 * @param fromInd: Index where approximation area begins
	 * @param toInd: Index where approximation area ends (including this entry) 
	 * @param x: Feature descriptors, i.e. wavelength values 
	 * @return wavelength indices of the approximation area
	 */
	private int[] getXDataIndices(int fromInd, int toInd, double[] x){
		int leng = toInd-fromInd+1;
		int[] xDataInd = new int[leng];
		int k=0;
		for(int i=fromInd;i<=toInd;i++){
			xDataInd[k] = i;
			k++;
		}
		return xDataInd;
	}


	/**
	 * Initializes plotting of selected data in selected plot window
	 * Only if the double matrix spect can be assigned to the local class variable featureView
	 * @param plotWindow panel where the plot will be printed
	 * @param spect	DoubleMatrix with values for printing
	 * @param remove decides if the old plot should be overwritten or deleted
	 * @param plotColor	sets if all data is plotted or just means / std etc. Needed to print spectra in the right color. 0 : all data is plotted in the right color; 1: mean, std, var; 2: filter results in black
	 * 
	 * @author croemer
	 */
	public void plotData2D(Plot2DPanel plotWindow, DoubleMatrix spect, boolean remove, int plotColor){
		
		if(remove)
			plotWindow.removeAllPlots();
		//either returns double values of feature descriptors or 1..n if there are no double values inside
		double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(this.featureView);
				
		
		
		for (int i = 0; i<spect.rows; i++){
			Color color = Color.BLACK;
			double[] plotspect = spect.getRow(i).toArray();
			if (plotColor == 0){
				double[] label = Utilities.materializeFeature(featureView,FeatureSelection.getLabel(featureView));			
				color = Utilities.getColor(this.featureView.getLabelMap().get((int) label[i]));
			} else if (plotColor==1) {
				//i+1 as label start with 1
				color = Utilities.getColor(this.featureView.getLabelMap().get(i+1));
			}
			plotWindow.addLinePlot(null, color, FeatureSelection.getNonSpecialFeatureDescriptor(this.featureView), plotspect);
			
		}
		// Plot auf die Daten skalieren
		plotWindow.setFixedBounds(0, FeatureSelectionPanel.getMin(x), FeatureSelectionPanel.getMax(x));		
		plotWindow.setFixedBounds(1,spect.min(), spect.max());
	}
	
	private ApproximationWindow getThis(){
		return this;
	}
	
	public FeatureSelectionPanel getMaster(){
		return master;
	}

	/**
	 * List Cell renderer to show icon for each list element in classbox
	 * 
	 * @author Till
	 *
	 */
	class IconRenderer2 extends DefaultListCellRenderer{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) 
		{

			if(value instanceof JLabel)
			{
				this.setText(((JLabel)value).getText());
				this.setIcon(((JLabel)value).getIcon());
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			return this;
		}
	}
	
	class MyButtonRenderer extends DefaultTableCellRenderer {
		 
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			if (value instanceof JButton){
				return (JButton)value;
			}else if(value instanceof JTextField){
				return (JTextField)value;
			}
			Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			
			if(col==5|col==6){
				Color cl = new Color(240,240,240);
				((JComponent)rendererComp).setBackground(cl);
				return rendererComp;
			}else{
				rendererComp.setBackground(Color.WHITE);
				return rendererComp;
			}
		}
	}

	
}