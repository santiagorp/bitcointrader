/**
 * 
 */
package com.srp.geometry;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

import org.junit.Test;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class UtilTest {

	@Test
	public final void testGetSlopeIntercept() {
		// y = x
		double x1 = 1;
		double y1 = 1;
		double x2 = 2;
		double y2 = 2;
				
		Point2D result = Util.getSlopeIntercept(x1, y1, x2, y2);
		Point2D expected = new Point2D.Double(1, 0);
		assertTrue(result.distance(expected) == 0);
		
		// y = -2x + 3
		x1 = 3;
		y1 = -3;
		x2 = 5;
		y2 = -7;
		result = Util.getSlopeIntercept(x1, y1, x2, y2);
		expected = new Point2D.Double(-2, 3);
		assertTrue(result.distance(expected) == 0);
	}
}
