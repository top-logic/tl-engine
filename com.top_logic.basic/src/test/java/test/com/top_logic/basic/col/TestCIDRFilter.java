/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.filter.CIDRFilter;

/**
 * Test the {@link CIDRFilter}
 * 
 * @author    Steffen Kopmeier
 */
public class TestCIDRFilter extends BasicTestCase {

	//@Test
	public void testParse() throws ParseException {
		CIDRFilter filter = CIDRFilter.fromString("10.49.8.102/24");
		
		assertTrue(filter.matches(new byte[] {10, 49, 8, 102}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 8, 13}));
		Assert.assertFalse(filter.matches(new byte[] {10, 49, 9, 13}));
		Assert.assertFalse(filter.matches(new byte[] {10, 50, 8, 13}));
		Assert.assertFalse(filter.matches(new byte[] {11, 49, 8, 13}));
	}

	//@Test
	public void testWithoutMask() throws ParseException {
		CIDRFilter filter = CIDRFilter.fromString("127.0.0.1");

		Assert.assertTrue(filter.matches(new byte[] {127, 0, 0, 1}));
		Assert.assertFalse(filter.matches(new byte[] {127, 0, 0, 2}));
		Assert.assertFalse(filter.matches(new byte[] {(byte) 128, 0, 0, 1}));
		Assert.assertFalse(filter.matches(new byte[] {126, 0, 0, 1}));
	}
	
	//@Test
	public void testMask() throws ParseException {
		CIDRFilter filter = CIDRFilter.fromString("10.49.12.0/21");
		
		Assert.assertFalse(filter.matches(new byte[] {10, 49, 5, 1}));
		Assert.assertFalse(filter.matches(new byte[] {10, 49, 6, 1}));
		Assert.assertFalse(filter.matches(new byte[] {10, 49, 7, 1}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 8, 1}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 9, 1}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 10, 1}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 11, 1}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 12, 1}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 13, 1}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 14, 1}));
		Assert.assertTrue(filter.matches(new byte[] {10, 49, 15, 1}));
		Assert.assertFalse(filter.matches(new byte[] {10, 49, 16, 1}));
		Assert.assertFalse(filter.matches(new byte[] {10, 49, 17, 1}));
		Assert.assertFalse(filter.matches(new byte[] {10, 49, 18, 1}));
	}

	//@Test
	public void testFormat() {
		try {
			CIDRFilter.fromString("10.49.12/21");
			Assert.fail("Pattern to short.");
		} catch (ParseException ex) {
			// Expected.
		}
		try {
			CIDRFilter.fromString("10.49.12.3.4/21");
			Assert.fail("Pattern to long.");
		} catch (ParseException ex) {
			// Expected.
		}

		try {
			CIDRFilter.fromString("10.49.-1.3/21");
			Assert.fail("Pattern out of range.");
		} catch (ParseException ex) {
			// Expected.
		}
		
		try {
			CIDRFilter.fromString("10.256.1.3/21");
			Assert.fail("Pattern out of range.");
		} catch (ParseException ex) {
			// Expected.
		}
		
		try {
			CIDRFilter.fromString("10.2.1.3/-2");
			Assert.fail("Mask out of range.");
		} catch (ParseException ex) {
			// Expected.
		}

		try {
			CIDRFilter.fromString("10.2.1.3/33");
			Assert.fail("Mask out of range.");
		} catch (ParseException ex) {
			// Expected.
		}

		try {
			CIDRFilter.fromString("10.2x.1.3/21");
			Assert.fail("Illegal pattern.");
		} catch (ParseException ex) {
			// Expected.
		}

		try {
			CIDRFilter.fromString("10.2.1.3/3y");
			Assert.fail("Illegal mask.");
		} catch (ParseException ex) {
			// Expected.
		}
	}
	
	//@Test
	public void testIPAddress() throws ParseException, UnknownHostException {
		
		CIDRFilter filter = CIDRFilter.fromString("10.49.12.0/21");
		Assert.assertTrue(filter.matches(InetAddress.getByName("10.49.8.1").getAddress()));
		
	}
    
	public void testToString() throws ParseException {
        
        CIDRFilter filter = CIDRFilter.fromString("10.49.12.0/21");
        assertEquals("com.top_logic.basic.col.filter.CIDRFilter [10.49.8.0/21]",filter.toString());
        
    }
    
    
 	/**
	 * IPv6 ranges and addresses, and that IPv4 and IPv6 never match each other.
	 */
	public void testIPv6() throws ParseException, UnknownHostException {
		CIDRFilter filter = CIDRFilter.fromString("fd00:1234::/32");
		assertTrue(filter.matches(InetAddress.getByName("fd00:1234::1")));
		assertTrue(filter.matches(InetAddress.getByName("fd00:1234:ffff::1")));
		assertFalse(filter.matches(InetAddress.getByName("fd00:1235::1")));
		assertFalse(filter.matches(InetAddress.getByName("10.0.0.1")));
		assertTrue(filter.accept("fd00:1234::abcd"));

		CIDRFilter single = CIDRFilter.fromString("::1");
		assertTrue(single.matches(InetAddress.getByName("::1")));
		assertFalse(single.matches(InetAddress.getByName("::2")));
		assertEquals(CIDRFilter.class.getName() + " [fd00:1234:0:0:0:0:0:0/32]", filter.toString());

		CIDRFilter ipv4 = CIDRFilter.fromString("10.0.0.0/8");
		assertTrue(ipv4.matches(InetAddress.getByName("10.1.2.3")));
		assertFalse(ipv4.matches(InetAddress.getByName("fd00::1")));
		assertFalse(ipv4.matches((InetAddress) null));
	}

	/**
	 * Invalid IPv6 ranges are refused without looking up a host name.
	 */
	public void testIPv6Format() {
		for (String invalid : new String[] { "fd00::/129", "fd00::/-1", "example.com:80", "fd00::g" }) {
			try {
				CIDRFilter.fromString(invalid);
				fail("Accepted: " + invalid);
			} catch (ParseException ex) {
				// Expected.
			}
		}
	}

    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestCIDRFilter.class));
    }


    /**
     * The main program for executing this test also from console.
     *
     * @param    args    Will be ignored.
     */
    public static void main(String[] args) {

        junit.textui.TestRunner.run (suite());
    }

}
