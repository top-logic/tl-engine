/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.NumberComparator;


/**
 * This Class tests the NumberComparator 
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestNumberComparator extends TestCase{
    
    Comparator<Object> nc = (Comparator) NumberComparator.INSTANCE;
    
    public void testClassCastException() {
        String s1 = "abc";
        String s2 = "xyz";
        try{
            nc.compare(s1, s2);
            fail("Expected IllegalArgumentException");
        } catch (ClassCastException expected){/* expected*/}
    }
    
    public void testMaxDoubleValue() {
        Double n1 = Double.valueOf(Double.MAX_VALUE/2);
        Double n2 = Double.valueOf(Double.MAX_VALUE);
        
        assertTrue(nc.compare(n1, n2)<0);
    }

    public void testMinDoubleValue() {
        Double n1 = Double.valueOf(Double.MIN_VALUE);
        Double n2 = Double.valueOf(Double.MIN_VALUE*2);
        assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testSmallDoubleValue() {
    	Double n1 = Double.valueOf(0.00000000000001);
    	Double n2 = Double.valueOf(0.00000000000002);
    	assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testSmallDoubleFloatValue() {
    	Double n1 = Double.valueOf(0.00000000000001);
		Float n2 = Float.valueOf(0.00000000000002f);
    	assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testMaxFloatValue() {
		Float n1 = Float.valueOf(10);
		Float n2 = Float.valueOf(20);
        assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testMinFloatValue() {
		Float n1 = Float.valueOf(Float.MIN_VALUE);
		Float n2 = Float.valueOf(Float.MIN_VALUE * 2);
        assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testMaxLongValue() {
		Long n1 = Long.valueOf(Long.MAX_VALUE - Long.MIN_VALUE);
		Long n2 = Long.valueOf(Long.MAX_VALUE);
        assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testMinLongValue() {
		Long n1 = Long.valueOf(Long.MIN_VALUE);
		Long n2 = Long.valueOf(Long.MIN_VALUE * 2);
        assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testEqualIntValues() {
		/* Intentional use of constructors to check if different instances for the same value are
		 * handled correctly. */
		Integer n1 = Integer.valueOf(Integer.MAX_VALUE);
		Integer n2 = Integer.valueOf(Integer.MAX_VALUE);
        assertTrue(nc.compare(n1, n2)==0);
    }
    
    public void testEqualDoubleValues() {
		Double n1 = Double.valueOf(Double.MAX_VALUE);
		Double n2 = Double.valueOf(Double.MAX_VALUE);
        assertTrue(nc.compare(n1, n2)==0);
    }
    
    public void testEqualDoubleIntValues() {
		Double n1 = Double.valueOf(1000.00);
		Integer n2 = Integer.valueOf(1000);
        assertTrue(nc.compare(n1, n2)==0);
    }
    
    public void testIntDouble() {
		Double n1 = Double.valueOf(Double.MIN_VALUE);
		Integer n2 = Integer.valueOf(Integer.MAX_VALUE);
        assertTrue(nc.compare(n2, n1)>0);
    }
    
    public void testLongFloat() {
		Long n1 = Long.valueOf(Long.MIN_VALUE);
		Float n2 = Float.valueOf(Float.MAX_VALUE);
        assertTrue(nc.compare(n2, n1)>0);
    }
    
    public void testMaxBigDecimalValue() {
		BigDecimal n1 = BigDecimal.valueOf(Double.MIN_VALUE);
		BigDecimal n2 = BigDecimal.valueOf(Double.MAX_VALUE);
        assertTrue(nc.compare(n2, n1)>0);
    }
    
    public void testMinBigDecimalValue() {
		BigDecimal n1 = BigDecimal.valueOf(Double.MIN_VALUE);
		BigDecimal n2 = BigDecimal.valueOf(Double.MIN_VALUE * 2);
        assertTrue(nc.compare(n2, n1)>0);
    }
    
    public void testFirstBigDecimalValue() {
		BigDecimal n1 = BigDecimal.valueOf(Integer.MIN_VALUE);
		Integer n2 = Integer.valueOf(Integer.MIN_VALUE * 2);
        assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testSecondBigDecimalValue() {
		Integer n1 = Integer.valueOf(Integer.MIN_VALUE);
		BigDecimal n2 = BigDecimal.valueOf(Double.MIN_VALUE * 2);
        assertTrue(nc.compare(n1, n2)<0);
    }
    
    public void testOneBigInteger(){
        BigInteger n1 = new BigInteger("100",10);
		Integer n2 = Integer.valueOf(Integer.MIN_VALUE);
        assertTrue(nc.compare(n1, n2) > 0);
    }
    
    public void testEqualBigIntegerValues(){
        BigInteger d1 = new BigInteger("1000000");
        BigInteger d2 = new BigInteger("1000000");
        assertTrue(nc.compare(d1, d2)==0);
    }
    
	public void testCompareBigIntegerWithOtherType() {
		BigInteger d1 = new BigInteger("1000000");
		Integer d2 = Integer.valueOf("1000000");
		assertTrue(nc.compare(d1, d2) == 0);
		assertTrue(nc.compare(d2, d1) == 0);
	}

    /** 
     * Returns a suite of tests.
     */
    public static Test suite() {
        return new TestSuite(TestNumberComparator.class);
    }

    /**
     * This main function is for direct testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite ());
    }    
    
}
