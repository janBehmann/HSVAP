package data;

/**
 * FeaturRoles are used by Views to specify semantic information 
 * a column (="feature") represents for every row (=example, pixel).
 * 
 * This enum type provides 5 roles:
 * 
 * X = the corresponding column of the View represents the x coordinate of a pixel
 * 			There must be at most one such column in a view.
 * Y = the corresponding column of the View represents the y coordinate of a pixel
 * 			There must be at most one such column in a view.
 * LABEL = the corresponding column of the View represents the label of a pixel, 
 * 			e.g. foreground or background. Supervised methods use this column as 
 * 			target attribute. There must be at most one label in each view.
 * RGB = the corresponding column of the View represents the rgb value of a pixel.
 * 			There must be at most one such column in a view.
 * FEATURE = There may be an arbitrary number of features in each view. These can be e.g.
 * 			hyperspectral bands, cluster values, foreground/background, etc.
 * PROB = Probabilies of a certain label. This is a vector with length equal to the number of labels.
 *  
 * @author pwelke
 *
 */
public enum FeatureRole {
	LABEL, X, Y, FEATURE, RGB, PROB
}
