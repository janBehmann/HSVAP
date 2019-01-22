package org.math.plot.plotObjects;

import org.math.plot.render.AbstractDrawer;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public interface Noteable {
	public double[] isSelected(int[] screenCoord, AbstractDrawer draw);

	public void note(AbstractDrawer draw);
}