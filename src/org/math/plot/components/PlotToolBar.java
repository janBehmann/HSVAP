package org.math.plot.components;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.math.plot.Plot2DPanel;
import org.math.plot.PlotPanel;
import org.math.plot.canvas.Plot3DCanvas;
import org.math.plot.canvas.PlotCanvas;
import org.math.plot.plots.Plot;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class PlotToolBar extends JToolBar {

    // TODO redesign icons...
    private static final long serialVersionUID = 1L;
    protected ButtonGroup buttonGroup;
    protected JToggleButton buttonCenter;
    //protected JToggleButton buttonEdit;
    protected JToggleButton buttonZoom;
    protected JToggleButton buttonRotate;
    //protected JToggleButton buttonViewCoords;
    protected JButton buttonSetScales;
    protected JButton buttonDatas;
    protected JButton buttonSavePNGFile;
    protected JButton buttonReset;
    protected JButton buttonAdjustBounds;
    private boolean denySaveSecurity;
    private JFileChooser pngFileChooser;
    /** the currently selected PlotPanel */
    private PlotCanvas plotCanvas;
    private PlotPanel plotPanel;

    public PlotToolBar(PlotPanel pp) {
        plotPanel = pp;
        plotCanvas = pp.plotCanvas;

        try {
            pngFileChooser = new JFileChooser();
            pngFileChooser.setFileFilter(new FileFilter() {

                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".png");
                }

                public String getDescription() {
                    return "Portable Network Graphic file";
                }
            });
        } catch (AccessControlException ace) {
            denySaveSecurity = true;
        }

        buttonGroup = new ButtonGroup();

        buttonCenter = new JToggleButton(new ImageIcon(PlotPanel.class.getResource("icons/center.png")));
        buttonCenter.setToolTipText("Center axis");
        buttonCenter.setSelected(plotCanvas.ActionMode == PlotCanvas.TRANSLATION);

        buttonZoom = new JToggleButton(new ImageIcon(PlotPanel.class.getResource("icons/zoom.png")));
        buttonZoom.setToolTipText("Zoom");
        buttonZoom.setSelected(plotCanvas.ActionMode == PlotCanvas.ZOOM);

        //buttonEdit = new JToggleButton(new ImageIcon(PlotPanel.class.getResource("icons/edit.png")));
        //buttonEdit.setToolTipText("Edit mode");

        //buttonViewCoords = new JToggleButton(new ImageIcon(PlotPanel.class.getResource("icons/position.png")));
        //buttonViewCoords.setToolTipText("Highlight coordinates / Highlight plot");

        buttonSetScales = new JButton(new ImageIcon(PlotPanel.class.getResource("icons/scale.png")));
        buttonSetScales.setToolTipText("Edit axis scales");

        buttonDatas = new JButton(new ImageIcon(PlotPanel.class.getResource("icons/data.png")));
        buttonDatas.setToolTipText("Get data");

        buttonSavePNGFile = new JButton(new ImageIcon(PlotPanel.class.getResource("icons/topngfile.png")));
        buttonSavePNGFile.setToolTipText("Save graphics in a .PNG File");

        buttonReset = new JButton(new ImageIcon(PlotPanel.class.getResource("icons/back.png")));
        buttonReset.setToolTipText("Reset zoom & axis");

        buttonAdjustBounds = new JButton(new ImageIcon(PlotPanel.class.getResource(plotCanvas.getAdjustBounds() ? "icons/adjustbounds.png" : "icons/noadjustbounds.png")));
        buttonAdjustBounds.setToolTipText("Auto-update/fix bounds");

        /*buttonEdit.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        plotCanvas.ActionMode = PlotCanvas.EDIT;
        }
        });*/

        buttonZoom.setSelected(true);
        buttonZoom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                plotCanvas.ActionMode = PlotCanvas.ZOOM;
            }
        });

        buttonCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                plotCanvas.ActionMode = PlotCanvas.TRANSLATION;
            }
        });

        /*buttonViewCoords.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        plotCanvas.setNoteCoords(buttonViewCoords.isSelected());
        }
        });*/

        buttonSetScales.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                plotCanvas.displayScalesFrame();
            }
        });

        buttonDatas.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                plotCanvas.displayDataFrame();
            }
        });

        buttonSavePNGFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
//                choosePNGFile();
                saveGraphicFile();
            }
        });

        buttonReset.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                plotCanvas.resetBase();
            }
        });

        buttonAdjustBounds.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                plotCanvas.setAdjustBounds(!plotCanvas.getAdjustBounds());
                ajustBoundsChanged();
            }
        });

        buttonGroup.add(buttonCenter);
        buttonGroup.add(buttonZoom);
        //buttonGroup.add(buttonEdit);

        add(buttonCenter, null);
        add(buttonZoom, null);
        add(buttonReset, null);
        //add(buttonViewCoords, null);
        add(buttonSetScales, null);
        if (adjustBoundsVisible) {
            add(buttonAdjustBounds, null);
        }
        //add(buttonEdit, null);
        add(buttonSavePNGFile, null);
        add(buttonDatas, null);

        if (!denySaveSecurity) {
            pngFileChooser.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    saveGraphicFile();
                }
            });
        } else {
            buttonSavePNGFile.setEnabled(false);
        }

        //buttonEdit.setEnabled(plotCanvas.getEditable());

        //buttonViewCoords.setEnabled(plotCanvas.getNotable());

        // allow mixed (2D/3D) plots managed by one toolbar
        if (plotCanvas instanceof Plot3DCanvas) {
            if (buttonRotate == null) {
                buttonRotate = new JToggleButton(new ImageIcon(PlotPanel.class.getResource("icons/rotation.png")));
                buttonRotate.setToolTipText("Rotate axes");

                buttonRotate.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        plotCanvas.ActionMode = Plot3DCanvas.ROTATION;
                    }
                });
                buttonGroup.add(buttonRotate);
                add(buttonRotate, null, 2);
                buttonRotate.setSelected(plotCanvas.ActionMode == Plot3DCanvas.ROTATION);
            } else {
                buttonRotate.setEnabled(true);
            }
        } else {
            if (buttonRotate != null) {
                // no removal/disabling just disable
                if (plotCanvas.ActionMode == Plot3DCanvas.ROTATION) {
                    plotCanvas.ActionMode = PlotCanvas.ZOOM;
                }
                buttonRotate.setEnabled(false);
            }
        }
    }

    void choosePNGFile() {
        pngFileChooser.showSaveDialog(this);
    }
    /**
     * Adapted by jbehmann to Fullscreen plot
     */
    void saveGraphicFile() {
    	try {
			// open save dialog
			JFileChooser chooser = new JFileChooser(new File("logos"));
			chooser.setSelectedFile(new File("plot.png"));
			chooser.showSaveDialog(null);

			// open a new window to show plot in fullscreen (higher resolution)
			JFrame fullScreen = new JFrame();
			fullScreen.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
			Plot2DPanel plotFS = new Plot2DPanel();
			fullScreen.add(plotFS);
			fullScreen.setVisible(true);
			

//			// add plot elements and title to the fullscreen plot and scale the axis
			double minx=Double.MAX_VALUE;
			double maxx=Double.MIN_VALUE;
			double miny=Double.MAX_VALUE;
			double maxy=Double.MIN_VALUE;
			
			for(Plot linePlot:plotPanel.getPlots()){
				plotFS.addPlot(linePlot); // Linien
				if(minx>linePlot.getData()[0][0])
					minx= linePlot.getData()[0][0];
				if(maxx<linePlot.getData()[linePlot.getData().length-1][0])
					maxx= linePlot.getData()[linePlot.getData().length-1][0];
				
				double max = linePlot.getData()[0][1];
				double min = linePlot.getData()[0][1];							
			      for (int j = 1; j < linePlot.getData().length; j++) {
			          if (linePlot.getData()[j][1] > max) {
			              max = linePlot.getData()[j][1];
			          }
			          if (linePlot.getData()[j][1] < min) {
			              min = linePlot.getData()[j][1];
			          }
			      }
			      if(miny>min)
						miny= min;
					if(maxy<max)
						maxy= max;
				
			}
			plotFS.setFixedBounds(0,minx, maxx);
			plotFS.setFixedBounds(1, miny, maxy);
//			plotFS.setAxisLabel(0,"Wavelength [nm]");
			plotFS.setAxisLabel(1,plotPanel.getAxis(1).getLegend());
			plotFS.setAxisLabel(0,plotPanel.getAxis(0).getLegend());
			
			plotFS.getAxis(0).setLabelPosition(plotPanel.getAxis(0).getLegendCoord());
			plotFS.getAxis(1).setLabelPosition(plotPanel.getAxis(1).getLegendCoord());
			plotFS.getAxis(0).setLabelAngle(plotPanel.getAxis(0).getLegendAngle());
			plotFS.getAxis(1).setLabelAngle(plotPanel.getAxis(1).getLegendAngle());
			
					

			// save the fullscreen plot
			plotFS.plotToolBar.setVisible(false);
			plotFS.toGraphicFile(new File(chooser.getSelectedFile().toString()));
			fullScreen.setVisible(false);
			
		} catch (IOException ei) {
			ei.printStackTrace();
			 JOptionPane.showConfirmDialog(null, "Save failed : " + ei.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
		}
    	
    	
    	
    	//Alte Implementierung 
//        java.io.File file = pngFileChooser.getSelectedFile();
//        try {
//            plotPanel.toGraphicFile(file);
//        } catch (IOException e) {
//            JOptionPane.showConfirmDialog(null, "Save failed : " + e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
//        }
    }
    boolean adjustBoundsVisible = true;

    public void viewAdjustBounds(boolean visible) {
        if (visible && !adjustBoundsVisible) {
            add(buttonAdjustBounds, null);
            adjustBoundsVisible = true;
        }
        if (!visible && adjustBoundsVisible) {
            remove(buttonAdjustBounds);
            adjustBoundsVisible = false;
        }
        ajustBoundsChanged();
    }

    public void ajustBoundsChanged() {
        buttonAdjustBounds.setIcon(new ImageIcon(PlotPanel.class.getResource(plotCanvas.getAdjustBounds() ? "icons/adjustbounds.png" : "icons/noadjustbounds.png")));
    }
}