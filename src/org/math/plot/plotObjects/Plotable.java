package org.math.plot.plotObjects;

import java.awt.Color;

import org.math.plot.render.AbstractDrawer;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public interface Plotable {
	public void plot(AbstractDrawer draw);

	public void setVisible(boolean v);

	public boolean getVisible();

	public void setColor(Color c);

	public Color getColor();

}