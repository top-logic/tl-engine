/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.util;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.mig.util.NumbersToGermanWords;

/**
 * Tests for NumbersToGermanWords.
 * 
 * @author wba
 */
public class TestNumbersToGermanWords extends TestCase {
    
    /**
     * Constructor for having the name of this suite.
     *
     * @param    aName    The name of this suite.
     */
    public TestNumbersToGermanWords (String aName) {
        super (aName);
    }
       
    /**
     * Tests the {@link com.top_logic.mig.util.NumbersToGermanWords#convert(long)}
     * method and the 
     * {@link com.top_logic.mig.util.NumbersToGermanWords#convert(double)} method.
     */
    public void testConvert() throws Exception {
        assertEquals("Eins",
                        NumbersToGermanWords.convert(1));         
        assertEquals("Einhundertsiebenundzwanzig",
                        NumbersToGermanWords.convert(127));         
        assertEquals("Eintausend",
                        NumbersToGermanWords.convert(1000));         
        assertEquals("Einhundertsiebenundzwanzig Milliarden",
                        NumbersToGermanWords.convert(127000000000L));
        assertEquals("Einhundertsiebenundzwanzig Milliarden",
                        NumbersToGermanWords.convert(1.27000000000E11));
        assertEquals("Minus Zwei Trillionen Einhundertsiebenundzwanzig Billiarden",
                        NumbersToGermanWords.convert(-2127000000000000000L));
        String result = NumbersToGermanWords.convert(123456789867542.0);
        assertEquals("Einhundertdreiundzwanzig Billionen Vierhundertsechsundfünfzig Milliarden Siebenhundertneunundachtzig Millionen Achthundertsiebenundsechzigtausendfünfhundertzweiundvierzig",
                     result);
        result = NumbersToGermanWords.convert(1234567898675423196L);
        assertEquals("Eine Trillion Zweihundertvierunddreißig Billiarden Fünfhundertsiebenundsechzig Billionen Achthundertachtundneunzig Milliarden Sechshundertfünfundsiebzig Millionen Vierhundertdreiundzwanzigtausendeinhundertsechsundneunzig",
                     result);
        result = NumbersToGermanWords.convert(-1324657896875423196L);
        assertEquals("Minus Eine Trillion Dreihundertvierundzwanzig Billiarden Sechshundertsiebenundfünfzig Billionen Achthundertsechsundneunzig Milliarden Achthundertfünfundsiebzig Millionen Vierhundertdreiundzwanzigtausendeinhundertsechsundneunzig",
                     result);
        try {
            NumbersToGermanWords.convert(Long.MAX_VALUE * 10.0d);
            fail("Excpected Illegal ArgumentExcpetion here");
		} catch (IllegalArgumentException expected) {
			/* expected */
		}
    }
    
    /**
     * Tests some evil values at the "edges" of allowed values.
     */
    public void testEvil() throws Exception {
        // Check for errors at the border from int to long 
        // 2147483647 / -2147483648
        assertEquals("Zwei Milliarden Einhundertsiebenundvierzig Millionen Vierhundertdreiundachtzigtausendsechshundertsiebenundvierzig",
                     NumbersToGermanWords.convert(Integer.MAX_VALUE));
        assertEquals("Zwei Milliarden Einhundertsiebenundvierzig Millionen Vierhundertdreiundachtzigtausendsechshundertachtundvierzig",
                     NumbersToGermanWords.convert(Integer.MAX_VALUE + 1L));
        assertEquals("Minus Zwei Milliarden Einhundertsiebenundvierzig Millionen Vierhundertdreiundachtzigtausendsechshundertachtundvierzig",
                     NumbersToGermanWords.convert(Integer.MIN_VALUE));
        assertEquals("Minus Zwei Milliarden Einhundertsiebenundvierzig Millionen Vierhundertdreiundachtzigtausendsechshundertneunundvierzig",
                     NumbersToGermanWords.convert(Integer.MIN_VALUE - 1L));

        for (int j=0; j< 1000;j++) {
            assertEquals(NumbersToGermanWords.
                convert((long) (NumbersToGermanWords.MAX_DOUBLE - j )) , 
                NumbersToGermanWords.convert(9007199254740989L - j));
        }

        long maxLong = (long) NumbersToGermanWords.MAX_DOUBLE;
        for (int i=1 ; i < 100; i++) {
            assertEquals(NumbersToGermanWords.convert(NumbersToGermanWords.MAX_DOUBLE - i),
                         NumbersToGermanWords.convert(maxLong - i));
        }        
        for (int i=1 ; i < 100; i++) {
            assertEquals(NumbersToGermanWords.convert(- NumbersToGermanWords.MAX_DOUBLE + i),
                         NumbersToGermanWords.convert(- maxLong + i));
        }  
        assertEquals("Neun Billiarden Sieben Billionen Einhundertneunundneunzig Milliarden Zweihundertvierundfünfzig Millionen Siebenhundertvierzigtausendneunhundertneunundachtzig",
            NumbersToGermanWords.convert(NumbersToGermanWords.MAX_DOUBLE));   
        assertEquals("Neun Billiarden Sieben Billionen Einhundertneunundneunzig Milliarden Zweihundertvierundfünfzig Millionen Siebenhundertvierzigtausendneunhundertachtundachtzig",
            NumbersToGermanWords.convert(NumbersToGermanWords.MAX_DOUBLE - 1));   
        assertEquals("Neun Billiarden Sieben Billionen Einhundertneunundneunzig Milliarden Zweihundertvierundfünfzig Millionen Siebenhundertvierzigtausendneunhundertsiebenundachtzig",
            NumbersToGermanWords.convert(NumbersToGermanWords.MAX_DOUBLE - 2));   
        assertEquals("Minus Neun Billiarden Sieben Billionen Einhundertneunundneunzig Milliarden Zweihundertvierundfünfzig Millionen Siebenhundertvierzigtausendneunhundertneunundachtzig",
            NumbersToGermanWords.convert(- NumbersToGermanWords.MAX_DOUBLE));   
        assertEquals("Minus Neun Billiarden Sieben Billionen Einhundertneunundneunzig Milliarden Zweihundertvierundfünfzig Millionen Siebenhundertvierzigtausendneunhundertachtundachtzig",
            NumbersToGermanWords.convert(- NumbersToGermanWords.MAX_DOUBLE + 1));   
        assertEquals("Minus Neun Billiarden Sieben Billionen Einhundertneunundneunzig Milliarden Zweihundertvierundfünfzig Millionen Siebenhundertvierzigtausendneunhundertsiebenundachtzig",
            NumbersToGermanWords.convert(- NumbersToGermanWords.MAX_DOUBLE + 2)); 
    }

    /**
     * Tests some evil or not allowed values.
     */
    public void test4Vers13() {
        // Check for errors at the border from int to long 
        // Erzeuge Fehler:

        try {
            NumbersToGermanWords.convert( -9223372036854775808L);
            fail("Excpected AssertionError here");
		} catch (AssertionError expected) {
			/* expected */
		}
        
        try {
            System.out.println(NumbersToGermanWords.convert(Long.MAX_VALUE + 1));
            fail("Excpected AssertionError here");
		} catch (AssertionError expected) {
			/* expected */
		}
        
        assertEquals("Zwei", NumbersToGermanWords.convert(Math.E)); 
        
        try {
            System.out.println(NumbersToGermanWords.convert(Double.NEGATIVE_INFINITY)); 
			fail("Excpected IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			/* expected */
		}
        try {
            System.out.println(NumbersToGermanWords.convert(Double.POSITIVE_INFINITY)); 
			fail("Excpected Illegal ArgumentExcpetion here");
		} catch (IllegalArgumentException expected) {
			/* expected */
		}
        try {
            System.out.println(NumbersToGermanWords.convert(Double.NaN)); 
            fail("Excpected Illegal ArgumentExcpetion here");
		} catch (IllegalArgumentException expected) {
			/* expected */
		}
    }

    /**
     * Tests the {@link com.top_logic.mig.util.NumbersToGermanWords#convert(long)}
     * method.
     */
    public void testConvertLong() throws Exception {
        Random aRandomGen = new Random(587879);
        for (int anInt = 1; anInt < 10000; anInt++) {
            long test = aRandomGen.nextLong();
            NumbersToGermanWords.convert(test);
        }
    }
    

    /**    
    * Tests the {@link com.top_logic.mig.util.NumbersToGermanWords#convert(double)}
    * method.
    */
    public void testConvertDouble() throws Exception {
        Random aRandomGen = new Random(58789);
        for (int anInt = 1; anInt < 10000; anInt++) {
            double test = aRandomGen.nextDouble();
            NumbersToGermanWords.convert(test);
        }
    }


    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite () {
        return new TestSuite (TestNumbersToGermanWords.class);
        // return new TestNumbersToGermanWords("testConvert");
    }

    /**
     * The main program for executing this test also from console.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        TestRunner.run (suite ());
    }
}
