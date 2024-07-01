/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.col.HashMapWeak;

/**
 * Test class to check the {@link com.top_logic.basic.col.HashMapWeak} class.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestHashMapWeak extends AbstractTestMap {

    /** The amount of longs allocated to "waste" Memory. */
    static final int LOAD = 0x1000;

    /** Internal Loops will count that much */
    static final int MAX  = 100;
    
    /** The number of objects to be saved */
    static final int MAX_SAVED = 80;

	private Object[] stableSampleKeys;

	private Object[] stableSampleValues;

    /**
     * Constructor to conduct a special test.
     *
     * @param name name of the test to execute.
     */
    public TestHashMapWeak (String name) {
        super (name);
    }
    
    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	
    	this.stableSampleKeys = super.getSampleKeys();
    	this.stableSampleValues = super.getSampleValues();
    }
    
    @Override
    public void tearDown() throws Exception {
    	this.stableSampleKeys = null;
    	this.stableSampleValues = null;

    	super.tearDown();
    }

    // Test methodes

    /** 
     * Main Testcase
     */
    public void testMain () throws Exception
    {
        Runtime rt = Runtime.getRuntime();
        HashMapWeak hmw = new HashMapWeak();
        for (int i=0; i < MAX; i++) {
            String key = Integer.toString(i);
            hmw.put(key, new BigValue(key));
        }
        rt.gc();
        Thread.sleep(100);
        // In fact we want some Object to have gone !
        int size = hmw.size();
        assertTrue("No Object is gone ? " + size + ',' + MAX , size < MAX);
    }
    
    /** 
     * Check the {@link Map#containsKey(Object)} method.
     */
    public void testContainsKey() throws Exception
    {
        HashMapWeak<String, BigValue> hmw       = new HashMapWeak<>();
        ArrayList<BigValue> allValues = new ArrayList<>();
        int         testindex = MAX >> 1;
        BigValue    testValue = null;
        String      testKey   = null;
        for (int i=0; i < MAX; i++) {
            String key = Integer.toString(i);
            BigValue value = new BigValue(key);
			hmw.put(key, value);
			allValues.add(value);
            if (i == testindex) {
                testValue = new BigValue("testContainsKey");
                hmw.put(testKey = key, testValue);
            }
        }
        for (int i=0; i < MAX; i++) {
            assertTrue(hmw.containsKey(Integer.toString(i)));
        }
        // Drop values.
        allValues = null;
        
        Runtime     rt        = Runtime.getRuntime();

        rt.gc();
        Thread.sleep(100);
        // Shoud still be there since we still refer to it.
        assertTrue(hmw.containsKey(testKey));
        testValue = null;
        int size  = hmw.size();
        rt.gc();
        Thread.sleep(100);
        assertTrue(hmw.size() < size);
    }

    
    /** 
     * Check the ContainsValue method.
     */
    public void testContainsValue() throws Exception
    {
        Runtime     rt        = Runtime.getRuntime();
        HashMapWeak hmw       = new HashMapWeak();
        int         testindex = MAX >> 1;
        BigValue    testValue = null;
        for (int i=0; i < MAX; i++) {
            String key = Integer.toString(i);
            hmw.put(key, new BigValue(key));
            if (i == testindex) {
            	testValue = new BigValue("testContainsValue");
                hmw.put(key, testValue);
            }
        }
        assertTrue(hmw.containsValue(testValue));

        rt.gc();
        Thread.sleep(100);
        // Shoud still be there since we still refer to it.
        assertTrue(hmw.containsValue(testValue));
        testValue = null;
        int size  = hmw.size();
        rt.gc();
        Thread.sleep(100);
        assertTrue(hmw.size() < size);
    }

    /** 
     * Test with a preallocation.
     */
    public void testPreset() throws Exception
    {
        Runtime rt = Runtime.getRuntime();
        HashMapWeak hmw = new HashMapWeak(MAX);
        for (int i=0; i < MAX; i++) {
            String key = Integer.toString(i);
            hmw.put(key, new BigValue(key));
        }
        rt.gc();
        Thread.sleep(100);
        // In fact we want some Object to have gone !
        int size = hmw.size();
        assertTrue("No Object is gone ? " + size + ',' + MAX , size < MAX);
    }
    
    /** 
     * Test with a preallocation and Load Factor
     */
    public void testPreset2() throws Exception
    {
        Runtime rt = Runtime.getRuntime();
        HashMapWeak hmw = new HashMapWeak(MAX, 0.66f);
        for (int i=0; i < MAX; i++) {
            String key = Integer.toString(i);
            hmw.put(key, new BigValue(key));
        }
        rt.gc();
        Thread.sleep(100);
        // In fact we want some Object to have gone !
        int size = hmw.size();
        assertTrue("No Object is gone ? " + size + ',' + MAX , size < MAX);
    }

    /** 
     * test if the saved objects are still in the HashMapWeak
     */
    public void testSavedObjects () throws Exception
    {
        Runtime rt = Runtime.getRuntime();
        HashMapWeak hmw = new HashMapWeak();
        Object[] keys = new Object[MAX_SAVED];   // the keys of the saved objects
        Object[] refs = new Object[MAX_SAVED];   // the references of the saved objects
        Random rand = new Random(0x80938402);
        int saveCount = 0;  // counts the saved objects
        for (int i=0; i < MAX; i++) {
            String   key = Integer.toString(i);
            BigValue val = new BigValue(key);
            hmw.put(key, val);
            if (saveCount < MAX_SAVED && rand.nextInt(10) < 1) {
                keys[saveCount] = key;
                refs[saveCount] = val;
                saveCount++;
            }
        }
        // run garbage collector
        rt.gc();
        
        // Need to sleep on HyperThreading machines.
        Thread.sleep(50);
        
        // In fact we want some Object to have gone !
        int size = hmw.size();
        assertTrue("No Object is gone ?", size < MAX);
        
        // Are the MAX_SAVED saved objects still in the HashMapWeak?
        for (int i = 0; i < saveCount; i++)  {
            assertEquals(refs[i], hmw.get(keys[i]));
        }
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestHashMapWeak.class);

        // TestSuite suite = new TestSuite()
        // suite.addTest(new TestSuite("testMain");
        return suite;
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
    
    // Utility Objects used for Testing

    /** Simulate a Big Payload for the Map, so that GC will eventually remove it */
    static class BigValue {

        String  key;

        long    paylod[] = new long[LOAD];

        public BigValue(String aKey) {
            this.key = aKey;
        }
    }

	@Override
	public Map makeEmptyMap() {
		return new HashMapWeak<>();
	}
	
	@Override
	public Object[] getSampleKeys() {
		return stableSampleKeys;
	}
	
	@Override
	public Object[] getSampleValues() {
		return stableSampleValues;
	}
	
	@Override
	public boolean isAllowNullValue() {
		return false;
	}
	
}


