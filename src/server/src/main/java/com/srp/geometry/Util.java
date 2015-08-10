/**
 * 
 */
package com.srp.geometry;

import java.awt.geom.Point2D;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class Util {
	/**
	 * Get the slope and intercept of the line resulting from joining points (x1, y1) and (x2, y2)
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static Point2D getSlopeIntercept(double x1, double y1, double x2, double y2) {
		double m = (y1 - y2) / (x1 - x2);
		double n = y1 - m * x1 ;
		Point2D result = new Point2D.Double(m, n);
		return result;
	}
	
	/**
	 * Get the intersection of two lines defines by slope-intercept formula
	 * @param line1 x = slope, y = intercept
	 * @param line2 x = slope, y = intercept
	 * @return
	 */
	public static Point2D getIntersection(Point2D line1, Point2D line2) {
		double x = (line2.getY() - line1.getY()) / (line1.getX() - line2.getX());
		double y = line1.getX() * x + line1.getY();
		Point2D result = new Point2D.Double(x, y);
		return result;
	}
	
	/**
	 * Get the intersection by giving two lines: one line by 2 points (x1, y1),(x2,y2) and the other line by (x3, y3), (x4, y4)
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param x4
	 * @param y4
	 * @return
	 */
	public static Point2D getIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		Point2D l1 = getSlopeIntercept(x1, y1, x2, y2);
		Point2D l2 = getSlopeIntercept(x3, y3, x4, y4);
		Point2D intersect = getIntersection(l1,  l2);
		return intersect;
	}
}
