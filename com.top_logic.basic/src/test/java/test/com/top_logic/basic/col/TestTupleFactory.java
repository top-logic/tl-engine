/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.TupleFactory.Tuple;

/**
 * Test case for {@link TupleFactory}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestTupleFactory extends TestCase {

    static final Object KEY1[] =  { "aaa" , "bbb" };
    static final Object KEY2[] =  { "bbb" , "aaa" };
    static final Object KEY3[] =  { "aaa" , "bbb" , "ccc" };
    static final Object KEY4[] =  { null  , "bbb" , "ccc" };
    
    static final Object KEY5[] =  { "aaa" , Integer.valueOf(10) };
    static final Object KEY6[] =  { "aaa" , Integer.valueOf(20)  };
    static final Object KEY7[] =  { "aaa" , Integer.valueOf(10)  };

    static final Object KEYS[][] =  {KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7 };
    
    /**
     * Constructor to conduct a special test.
     *
     * @param name name of the test to execute.
     */
    public TestTupleFactory (String name) {
        super (name);
    }

	/**
	 * Tests {@link #hashCode()} in TupleFactory#CompositeObjectKey
	 * 
	 */
	public void testEmptyCompositeKeyHashEquals() {
		final Object[] data = new Object[0];
		Tuple ownTuple = new Tuple() {

			@Override
			public int size() {
				return data.length;
			}

			@Override
			public Object get(int index) {
				return data[index];
			}

			@Override
			public int compareTo(Tuple o) {
				return TupleFactory.compare(this, o);
			}

			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof Tuple)) {
					return false;
				}
				return TupleFactory.equalsTuple(this, (Tuple) obj);
			}

			@Override
			public int hashCode() {
				return TupleFactory.hashCode(this);
			}
		};
		Tuple newTuple = TupleFactory.newTuple(data);
		assertTrue(newTuple.equals(ownTuple));
		assertEquals(ownTuple.hashCode(), newTuple.hashCode());
    }

	/**
	 * Tests equals in internal {@link Pair} class.
	 */
	public void testPairEquals() {
		final Object first = "a";
		final Object second = "b";
		Tuple ownPair = new Tuple() {

			@Override
			public int size() {
				return 2;
			}

			@Override
			public Object get(int index) {
				switch (index) {
					case 0:
						return first;
					case 1:
						return second;
					default:
						throw new IllegalArgumentException();
				}
			}

			@Override
			public int compareTo(Tuple o) {
				return TupleFactory.compare(this, o);
			}
		};
		
		Tuple newTuple = TupleFactory.newTuple(first, second);
		assertTrue("Ticket #9306: Wrong equals method in Pair.", newTuple.equals(ownPair));
	}

    /** Test the HashCode function.
     */
    public void testHashCode () throws Exception
    {
        int len = KEYS.length;
        
        Tuple keys[] = new Tuple[len];
        
        for (int i=0; i < len; i++) {
            keys[i] = TupleFactory.newTuple(KEYS[i]);
        }
        
        for (int i=0; i < len; i++) {
        	Tuple key = keys[i];
            int h1 = key.hashCode();
            for (int j=0; j < len; j++) {
                int h2 = keys[j].hashCode();
                if (h1 != h2)
                    assertTrue(!keys[i].equals(keys[j]));
            }
            assertNotNull(key.toString());
        }
    }

    /** Test the Equals function.
     */
    public void testEquals () throws Exception
    {
        int len = KEYS.length;
        
        Tuple keys[] = new Tuple[len];
        
        for (int i=0; i < len; i++) {
            keys[i] = TupleFactory.newTuple(KEYS[i]);
        }
        
        for (int i=0; i < len; i++) {
            Tuple k1 = keys[i];
            for (int j=i; j < len; j++) {
                Tuple k2 = keys[j];
                if (k1.equals(k2)){
                    assertTrue(k2.equals(k1));
                    assertEquals(k1.hashCode(), k2.hashCode());
                }
            }
        }
    }
    
    /** Test the equalsPart function.
     */
    public void testEqualsPart () throws Exception
    {
        int len = KEYS.length;
        
        Tuple keys[] = new Tuple[len];
        
        for (int i=0; i < len; i++) {
            keys[i] = TupleFactory.newTuple(KEYS[i]);
        }
        
        for (int i=0; i < len; i++) {
            Tuple k1 = keys[i];
            for (int j=i; j < len; j++) {
                Tuple k2 = keys[j];
                if (CollectionUtil.equals(k1.get(0), KEYS[i][0])){
                    assertTrue(CollectionUtil.equals(k2.get(0), KEYS[j][0]));
                }
            }
        }
    }

    /** Test the <code>compareTo()</code> function
     */
    public void testCompareTo () throws Exception
    {
        int len = KEYS.length;
        
        Tuple keys[] = new Tuple[len];
        
        for (int i=0; i < len; i++) {
            keys[i] = TupleFactory.newTuple(KEYS[i]);
        }

        // KEY3 is longer, thus greater        
        assertTrue(keys[0].compareTo(keys[2]) < 0);

        for (int i=0; i < len; i++) {
            Tuple k1 = keys[i];
            for (int j=0; j < len; j++) {
                Tuple k2 = keys[j];
                int c1 = 0;
                try {
                    c1 = k1.compareTo(k2);
                } catch (ClassCastException cce) {
                    // this is quite OK
                    continue;
                }
                String message = "(" + i + ',' + j + ")";
                if (c1 == 0)  {
                    assertTrue("equals" + message   , k1.equals(k2));
                    assertEquals(message            , k1.hashCode(), k2.hashCode());
                    assertEquals(message            , 0            , k2.compareTo(k1));
                } else if (c1 > 0)
                    assertTrue("compareTo" + message, k2.compareTo(k1) < 0);
                  else if (c1 < 0)
                    assertTrue("compareTo" + message, k2.compareTo(k1) > 0);
            }
        }
    }

    /** 
     * Test using small keys only.
     */
    public void testSmall () throws Exception
    {
        Tuple cok0 = TupleFactory.newTuple(new Object[0]);
        assertEquals("()", cok0.toString());
        assertEquals(cok0, cok0);
        
        Tuple cok1 = TupleFactory.newTuple(new Object[1]);
        assertNotNull(cok1.toString());
        assertEquals(cok1, cok1);
    }

    /** 
     * Test some variants of Constructors.
     */
    public void testCTors () throws Exception
    {
        Tuple ckey1 = TupleFactory.newTuple(KEY2);
        Tuple ckey2 = TupleFactory.newTuple(KEY2[0], KEY2[1]);
        assertEquals(ckey1, ckey2);
        
        Object[] o1 = new Object[2];
        Object[] o2 = new Object[3];
        Object[] o3 = new Object[3];
        o3[0] = "0";
        o3[1] = "1";
        o3[2] = "2";
        Object[] o4 = o3.clone();
        
        // test comparison of null values
        Tuple t1      = TupleFactory.newTuple(o1);
        Tuple t1_null = TupleFactory.newTuple(null, null);
        assertEquals(t1, t1_null);
        
        // test comparison of null values
        Tuple t2      = TupleFactory.newTuple(o2);
        Tuple t2_null = TupleFactory.newTuple(null, null, null);
        assertEquals(t2, t2_null);
        assertNotEquals(t1, t2);
        assertNotEquals(t1_null, t2_null);
        
        // test comparison of objects
        Tuple t3      = TupleFactory.newTuple(o3);
        Tuple t3_share= TupleFactory.newTuple(o3);
        Tuple t4      = TupleFactory.newTuple(o4);
        Tuple t4_copy = TupleFactory.newTupleCopy(o4);
        assertEquals(t3, t3_share);
        assertEquals(t3_share, t4);
        assertEquals(t4, t4_copy);
        
        // If o3 changes, the tuple t3_share based o3 should change, too
        o3[0] = "1";
        assertEquals(t3, t3_share);
        assertEquals(t3.get(0), "1");
        assertEquals(t3_share.get(0), "1");
        assertNotEquals(t3, t4);
        
        // If o4 changes, the copied tuple t4_copy must not change 
        o4[0] = "1";
        assertNotEquals(t4, t4_copy);
        assertEquals(t3, t4);
    }

    public static void assertNotEquals(Object o1, Object o2) {
        assertFalse(o1.equals(o2));
    }
    
    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite (TestTupleFactory.class);
        // return new TestCompositeObjectKey("testEqualsPart");
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
