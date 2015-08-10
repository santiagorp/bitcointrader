/**
 * 
 */
package com.srp.trading.core;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class VersionTest {

	/**
	 * Test method for {@link com.srp.trading.core.Version#Version()}.
	 */
	@Test
	public void testVersion() {
		Version v = new Version();
		assertSame(v.getMinor(), 0);
		assertSame(v.getMajor(), 0);
	}

	/**
	 * Test method for {@link com.srp.trading.core.Version#Version(int, int)}.
	 */
	@Test
	public void testVersionIntInt() {
		Version v = new Version(3, 5);
		assertSame(v.getMajor(), 3);
		assertSame(v.getMinor(), 5);		
	}

	/**
	 * Test method for {@link com.srp.trading.core.Version#Version(int)}.
	 */
	@Test
	public void testVersionInt() {
		Version v = new Version(4);
		assertSame(v.getMajor(), 4);
		assertSame(v.getMinor(), 0);
	}

	/**
	 * Test method for {@link com.srp.trading.core.Version#Version(java.lang.String)}.
	 */
	@Test
	public void testVersionString() {
		Version v = new Version("2.4");
		assertSame(v.getMajor(), 2);
		assertSame(v.getMinor(), 4);
		
		v = new Version("1");
		assertSame(v.getMajor(), 1);
		assertSame(v.getMinor(), 0);
		
		v = new Version(".7");
		assertSame(v.getMajor(), 0);
		assertSame(v.getMinor(), 7);
	}

	/**
	 * Test method for {@link com.srp.trading.core.Version#compareTo(com.srp.trading.core.Version)}.
	 */
	@Test
	public void testCompareTo() {
		Version v1 = new Version("3.5");
		Version v2 = new Version("3.0");
		
		assertTrue(v1.compareTo(v2) > 0);
		assertTrue(v2.compareTo(v1) < 0);
		
		Version v3 = new Version(3);
		assertTrue(v3.compareTo(v2) == 0);
	}
	
	@Test
	public void testToString() {
		String versionStr = "2.9";
		Version v = new Version(versionStr);
		assertTrue(v.toString().equals(versionStr));
	}
}
