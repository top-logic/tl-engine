/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package  test.com.top_logic.basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Random;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import test.com.top_logic.basic.ReflectionUtils.ReflectionException;
import test.com.top_logic.basic.junit.DirectoryLocalTestCollector;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.IDRangeIterator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.equal.EqualitySpecification;
import com.top_logic.basic.col.equal.ValueEqualitySpecification;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.RegExpUtil;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.xml.XMLCompare;
import com.top_logic.basic.xml.sax.SAXUtil;

/**
 * Adds some useful functions to measure and eventually log times.
 * 
 * As time went on more useful Tests have been accumulated here.
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BasicTestCase extends TestCase implements InContext {

	private static final String SPECIAL_CHARACTER_REPLACEMENT = "_";

	private static final Pattern SPECIAL_CHARACTERS = Pattern.compile("[^ _.$a-zA-Z0-9]+");

	private static final String LATIN_CHARS = "öäüÖÄÜßáàéèíìóòúù€";

	/** Use with {@link Assert#assertEquals(double, double, double)} */
    public static final double EPSILON = 1E-20;
    
    /** 
     * Project local temporary directory for tests. 
     * 
     * Must only be used through:
     * <ul>
     * <li>{@link #createNamedTestFile(String)}</li>
     * <li>{@link #createTestFile(String, String)}</li>
     * <li>{@link #createdCleanTestDir(String)}</li>
     * <li>{@link #deleteTestDir()}</li>
     * </ul>
     */
    private static final File TMP = new File("tmp");

    /** Use this to pretty print the Bytes used in log Space */
    protected static final NumberFormat   NUMF    = NumberFormat.getInstance();

    /** Set to true to enable output of time needed */
    protected static boolean    SHOW_TIME   = false;

    /** Set to true to enable output of memory needed */
    protected static boolean    SHOW_SPACE   = false;

    /** time when time measurement started */
    private long    start;
 
    /** memory found when space measurement started */
    private long    space;

	private String discriminatingSuffix = "";

    /**
     * Empty constructor.
     */
    public BasicTestCase () {
        super();
    }

    /**
     * Default constructor.
     *
     * @param name of test to execute.
     */
    public BasicTestCase (String name) {
        super (name);
    }
    
    @Override
	public void runBare() throws Throwable {
		try {
			ThreadContextManager.inInteraction(testContextId(getClass()), this);
		} catch (WrappedThrowable ex) {
			throw ex.getCause();
		}
    }
    
    @Override
	public void inContext() {
		try {
			superRunBare();
		} catch (Throwable ex) {
			throw new WrappedThrowable(ex);
		}
	}

	/**
	 * See {@link TestCase#runBare()}
	 */
	protected final void superRunBare() throws Throwable {
		super.runBare();
	}

	private static final class WrappedThrowable extends RuntimeException {
		public WrappedThrowable(Throwable ex) {
			super(ex);
		}
    }

    /** Start measuring the time */
    protected void startTime() {
        start = System.nanoTime();
    }
    
    /** Start measuring the space */
    protected void startSpace() {
        if (SHOW_SPACE) {
            Runtime rt = Runtime.getRuntime();
            rt.gc();
            rt.runFinalization();
            rt.gc();
            space = rt.freeMemory();
        }
    }
    
    /**
     * Log the measured time, implies {@link #startTime()}.
     *
     * @return the difference in milliseconds from last call to {@link #startTime()}.
     */
    protected long logTime(String message) {
        long elapsedNanos = logNanoTime(message);
        return elapsedNanos / 1000000;
    }

    /**
     * Log the measured time, implies {@link #startTime()}.
     *
     * @return the difference in nanoseconds from last call to {@link #startTime()}.
     */
	protected long logNanoTime(String message) {
		long elapsedNanos = System.nanoTime() - start;
        if (SHOW_TIME) {
            System.out.println();
            System.out.println(" Time for " + message + ' ' + StopWatch.toStringNanos(elapsedNanos));
        }
        start = System.nanoTime();
		return elapsedNanos;
	}
    
    /** Start measuring the space */
    protected void logSpace(String message) {
        if (SHOW_SPACE) {
            Runtime rt = Runtime.getRuntime();
            long free1 =  rt.freeMemory();
            rt.gc();
            rt.runFinalization();
            rt.gc();
            long free2 =  rt.freeMemory();
            long garbage = free2 - free1;
            long lost    = free2 - space; // Cannot be recycled ...
            space = rt.freeMemory();
            System.out.println();
            System.out.print("Space for " + message + ' '); 
            System.out.print("recyled: "  + NUMF.format(garbage));
            System.out.print("\tlost:"    + NUMF.format(lost )); 
            System.out.print("\t(total "  + NUMF.format(rt.totalMemory()) + ')'); 
        }
    }
    
    public static AssertionFailedError fail(String message, Throwable cause) {
		throw (AssertionFailedError) new AssertionFailedError(message).initCause(cause);
	}

    public static void arrayEquals(String msg, int a[], int b[]) {
        if (a.length != b.length) {
            fail(msg + " Size differs " + a.length + "!=" + b.length);
        }
        for (int i=0; i < a.length; i++) {
            if (a[i] != b[i])
            fail(msg + " Arrays differ [" + i + "] " + a[i] + "!=" + b[i]);
        }
        
    }

    public static void arrayEquals(int a[], int b[]) {
        arrayEquals("", a, b);
    }

    public static void assertNotEquals(String msg, Object notExpected, Object actual) {
		if (CollectionUtil.equals(notExpected, actual)) {
			fail(msg + " notExpected=" + notExpected);
		}
		
		// Check for symmetry!
		if (CollectionUtil.equals(actual, notExpected)) {
			fail(msg + " actual=" + actual);
		}
	}
    
	public static void assertNotEquals(Object notExpected, Object actual) {
		assertNotEquals("Object are equal.", notExpected, actual);
	}
    
	/**
	 * @see #assertLinesEqual(String, File, File)
	 */
	public static void assertLinesEqual(File f1, File f2) throws IOException {
		assertLinesEqual(null, f1, f2);
	}
	
	/**
	 * Check that the lines of two (text-) Files are equal.
	 */
	public static void assertLinesEqual(String message, File f1, File f2) throws IOException {
		final BufferedReader r1 = new BufferedReader(new FileReader(f1));
		try {
			final BufferedReader r2 = new BufferedReader(new FileReader(f2));
			try {
				assertLinesEqual(message, r1, r2);
			} finally {
				r2.close();
			}
		} finally {
			r1.close();
		}
	}

	/**
	 * @see #assertLinesEqual(String, BufferedReader, BufferedReader)
	 */
	public static void assertLinesEqual(final BufferedReader r1, final BufferedReader r2) {
		assertLinesEqual(null, r1, r2);
	}

	/**
	 * Check that the lines of two (text-) Files are equal.
	 */
	public static void assertLinesEqual(String message, final BufferedReader r1, final BufferedReader r2) {
		try {
			int lineNo = 0;
			
			String line1 = r1.readLine();
			String line2 = r2.readLine();
			
			while (true) {
				lineNo++;
				line1 = r1.readLine();
				line2 = r2.readLine();
				
				if (line1 == null) {
					if (line2 == null) {
						// Files are equal.
						return;
					} else {
						throw new AssertionFailedError(optionalMessagePrefix(message) +
							"Unexpected additional line " + lineNo + " '" + line2 + "'.");
					}
				} else {
					if (line2 == null) {
						throw new AssertionFailedError(optionalMessagePrefix(message) +
							"Missing line " + lineNo + ". Expected contents: '" + line1 + "'.");
					} else {
						assertEquals(optionalMessagePrefix(message) +
							"Mismatch in line " + lineNo + ".", line1, line2);
					}
				}
			}
		} catch (IOException ex) {
			fail(optionalMessagePrefix(message) + "Reading failed.", ex);
		}
	}
	
    /** 
     * Special assertion to validate that some Collection is sorted.
     * 
     * This will not allow equals objects.
     * 
     * @param aCol  The Collection to be checked.
     * @param aComp The Comparator used for checking
     */
	public static void assertSorted(Collection aCol, Comparator aComp) {
		assertSorted(null, aCol, aComp);
	}
    
    /** 
     * Special assertion to validate that some Collection is sorted
     * 
     * This will not allow equals objects.
      * 
     * @param aCol  The Collection to be checked.
     */
	public static void assertSorted(Collection<?> aCol) {
        assertSorted(null, aCol);
    }

	/**
	 * @see #assertEquals(String, ConfigurationItem, ConfigurationItem)
	 */
	public static void assertEquals(ConfigurationItem expected, ConfigurationItem actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Compares the given {@link ConfigurationItem}s with
	 * {@link ConfigEquality#INSTANCE_ALL_BUT_DERIVED}, which is the most useful comparison mode.
	 */
	public static void assertEquals(String message, ConfigurationItem expected, ConfigurationItem actual) {
		if (expected == actual) {
			return;
		}
		String detailMessage =
			" Configurations are expected to be equal, but are not. Expected: " + expected + " Actual: " + actual;
		if (expected == null || actual == null) {
			fail(message + detailMessage);
		}
		assertTrue(message + detailMessage, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(expected, actual));
	}
    
	/**
	 * @see #assertEquals(String, List, List)
	 */
	public static void assertConfigEquals(List<? extends ConfigurationItem> expected,
			List<? extends ConfigurationItem> actual) {
		assertConfigEquals(null, expected, actual);
	}

	/** 
	 * Show where two lists differ.
	 * 
	 * @param message
	 *        The message to add to a potential failure description.
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	public static void assertConfigEquals(String message, List<? extends ConfigurationItem> expected,
			List<? extends ConfigurationItem> actual) {
		assertListEquals(message, ConfigEquality.INSTANCE_ALL_BUT_DERIVED, expected, actual);
	}

    /**
     * @see #assertEquals(String, List, List)
     */
    public static void assertEquals(List<?> expected, List<?> actual) {
    	assertEquals(null, expected, actual);
    }
    
    /** 
     * Show where two lists differ.
     * 
	 * @param message
	 *        The message to add to a potential failure description.
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
     */
    public static void assertEquals(String message, List<?> expected, List<?> actual) {
		assertListEquals(message, ValueEqualitySpecification.INSTANCE, expected, actual);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void assertListEquals(String message, EqualitySpecification equality, List expected, List actual) {
		checkNull(message, expected, actual);
		if (expected == null) {
			return;
		}

        int size = expected.size();
        if (size != actual.size()) {
			fail(optionalMessagePrefix(message) +
				"List size differs. " +
				"Size expected: " + size + ", size actual: " + actual.size() + ", " +
				"list expected: " + expected + ", list actual: " + actual + ".");
        }
        for (int i=0; i < size; i++) {
            Object o1 = expected.get(i);
            Object o2 = actual.get(i);
			if (!equality.equals(o1, o2)) {
				fail(optionalMessagePrefix(message) +
					"Lists differ at index " + i + ". " +
					"Element expected '" + o1 + "', element actual '" + o2 + "', " +
					"list expected: " + expected + ", list actual: " + actual + ".");
            }
        }
	}

	/**
	 * Asserts that two objects are equal. If they are not an {@link AssertionFailedError} is
	 * thrown.
	 */
	public static void assertEquals(Object o1, Object o2) {
		assertEquals(null, o1, o2);
	}

	/**
	 * Asserts that two objects are equal. If they are not an {@link AssertionFailedError} is thrown
	 * with the given message.
	 * 
	 * <p>
	 * Note: This method is redeclared to use special handling for special types.
	 * </p>
	 */
	public static void assertEquals(String message, Object expected, Object actual) {
		if (expected instanceof Date && actual instanceof Date) {
			assertEquals(message, (Date) expected, (Date) actual);
		} else if (expected instanceof ConfigurationItem && actual instanceof ConfigurationItem) {
			assertEquals(message, (ConfigurationItem) expected, (ConfigurationItem) actual);
		} else {
			Assert.assertEquals(message, expected, actual);
		}
	}

	/**
	 * Asserts that two {@link Date} are equal. If they are not an {@link AssertionFailedError} is
	 * thrown with the given message.
	 * 
	 * <p>
	 * Note: This method does not use {@link Date#toString()} in message but a representation also
	 * containing milliseconds.
	 * </p>
	 */
	public static void assertEquals(String message, Date expected, Date actual) {
		if (expected == null && actual == null)
			return;
		if (expected != null && expected.equals(actual))
			return;
		StringBuilder formatted = new StringBuilder();
		if (message != null) {
			formatted.append(message);
			formatted.append(StringServices.BLANK_CHAR);
		}
		// Format like Date#toString() but enhanced with millis.
		SimpleDateFormat format = CalendarUtil.newSimpleDateFormat("EEE MMM dd HH:mm:ss.SSS zzz yyyy", Locale.ENGLISH);
		formatted.append("expected:<");
		if (expected == null) {
			formatted.append("null");
		} else {
			formatted.append(format.format(expected));
		}
		formatted.append("> but was:<");
		if (actual == null) {
			formatted.append("null");
		} else {
			formatted.append(format.format(actual));
		}
		formatted.append(">");
		fail(formatted.toString());

	}

	/**
	 * @see #assertEquals(String, boolean[], boolean[])
	 */
	public static void assertEquals(boolean[] expected, boolean[] actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Show where two object arrays differ.
	 * 
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	public static void assertEquals(String message, boolean[] expected, boolean[] actual) {
		checkNull(message, expected, actual);
		if (expected == null) {
			return;
		}
		assertTrue(message, Arrays.equals(expected, actual));
	}

	/**
	 * @see #assertEquals(String, byte[], byte[])
	 */
	public static void assertEquals(byte[] expected, byte[] actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Show where two object arrays differ.
	 * 
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	public static void assertEquals(String message, byte[] expected, byte[] actual) {
		checkNull(message, expected, actual);
		if (expected == null) {
			return;
		}
		assertTrue(message, Arrays.equals(expected, actual));
	}

	/**
	 * @see #assertEquals(String, short[], short[])
	 */
	public static void assertEquals(short[] expected, short[] actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Show where two object arrays differ.
	 * 
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	public static void assertEquals(String message, short[] expected, short[] actual) {
		checkNull(message, expected, actual);
		if (expected == null) {
			return;
		}
		assertTrue(message, Arrays.equals(expected, actual));
	}

	/**
	 * @see #assertEquals(String, long[], long[])
	 */
	public static void assertEquals(long[] expected, long[] actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Show where two object arrays differ.
	 * 
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	public static void assertEquals(String message, long[] expected, long[] actual) {
		checkNull(message, expected, actual);
		if (expected == null) {
			return;
		}
		assertTrue(message, Arrays.equals(expected, actual));
	}

	/**
	 * @see #assertEquals(String, double[], double[])
	 */
	public static void assertEquals(double[] expected, double[] actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Show where two object arrays differ.
	 * 
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	public static void assertEquals(String message, double[] expected, double[] actual) {
		checkNull(message, expected, actual);
		if (expected == null) {
			return;
		}
		assertTrue(message, Arrays.equals(expected, actual));
	}

	/**
	 * @see #assertEquals(String, float[], float[])
	 */
	public static void assertEquals(float[] expected, float[] actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Show where two object arrays differ.
	 * 
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	public static void assertEquals(String message, float[] expected, float[] actual) {
		checkNull(message, expected, actual);
		if (expected == null) {
			return;
		}
		assertTrue(message, Arrays.equals(expected, actual));
	}

	/**
	 * @see #assertEquals(String, Object[], Object[])
	 */
    public static void assertEquals(Object[] expected, Object[] actual) {
    	assertEquals(null, expected, actual);
    }
    
    /** 
     * Show where two object arrays differ.
	 * 
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
     */
    public static void assertEquals(String message, Object[] expected, Object[] actual) {
    	checkNull(message, expected, actual);
    	if (expected == null) {
    		return;
    	}
    	assertEquals(message, Arrays.asList(expected), Arrays.asList(actual));
    }

	/**
	 * Make sure that actual is <code>null</code> only if expected is
	 * <code>null</code>.
	 * 
	 * @param message
	 *        The message to add to a potential failure description.
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	private static void checkNull(String message, Object expected, Object actual) {
		if (expected == null) {
    		assertNull(message, actual);
    		return;
    	} else {
    		assertNotNull(message, actual);
    	}
	}
    
	/**
	 * @see #assertEquals(String, int[], int[])
	 */
	public static void assertEquals(int[] expected, int[] actual) {
		assertEquals(null, expected, actual);
	}
	
	/** 
	 * Show where two array differ.
	 * 
	 * @param expected
	 *        The expected value.
	 * @param actual
	 *        The actual value to test.
	 */
	public static void assertEquals(String message, int[] expected, int[] actual) {
		checkNull(message, expected, actual);
		if (expected == null) {
			return;
		}
		if (expected.length != actual.length) {
			fail(optionalMessagePrefix(message) +
				"Array size missmatch. " +
				"Expected length: " + expected.length + ", actual length:" + actual.length + ", " +
				"expected array: " + Arrays.toString(expected) + ", actual array: " + Arrays.toString(actual) + ".");
		}
		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				fail(optionalMessagePrefix(message) +
					"Array content mismatch at position " + i + ". " +
					"Expected value: " + expected[i] + ", actualValue: " + actual[i] + ", " +
					"expected array: " + Arrays.toString(expected) + ", actual array: " + Arrays.toString(actual) + ".");
			}
		}
	}


    /** 
     * Special assertion to validate that some List is sorted
     *
     * This will not allow equals objects.
     *
     * @param aCol  The Collection to be checked.
     * @param aComp The Comparator used for checking
     */
	public static void assertSorted(String message, Collection aCol, Comparator aComp)
	{
		int size = aCol.size();
		if (size < 2)
			return;	// nothing to compare here
		Iterator it = aCol.iterator();
		Object prev = it.next();
		for (int i=1; i<size; i++) {
			Object next = it.next();
			if (aComp.compare(prev, next) >= 0) {
				fail(optionalMessagePrefix(message) +
					"Collection not sorted according to given comparator at position " + i + ": " + aCol);
            }
			prev = next;
		}
	}

    /** 
     * Special assertion to validate that some List is sorted
     * 
     * This will not allow equals objects.
     *
     * @param aCol  The Collection to be checked.
     */
	public static void assertSorted(String message, Collection<?> aCol)
    {
        int size = aCol.size();
        if (size < 2)
            return; // nothing to compare here
		Iterator<?> it = aCol.iterator();
        Comparable prev = (Comparable) it.next();
        for (int i=1; i<size; i++) {
            Comparable next = (Comparable) it.next();
            if (prev.compareTo(next) >= 0) {
				fail(optionalMessagePrefix(message) +
					"Collection not sorted according to given comparator at position " + i + ": " + aCol);
            }
            prev = next;
        }
    }

    /**
     * @see #assertInIterator(String, Object, Iterator)
     */
	public static void assertInIterator(Object o, Iterator<?> iter) {
    	assertInIterator(null, o, iter);
    }
    
    /** 
     * Check the Iterator that object is contained.
     * 
     *  @param o    The Object to be checked.
     *  @param iter The Iterator used for checking
     */
	public static void assertInIterator(String message, Object o, Iterator<?> iter) {
		while (iter.hasNext()) {
			if (o == iter.next())
				return;
		}
		fail(optionalMessagePrefix(message) + "Expected object '" + o + "' not contained in iterator.");
	}
	
	/**
	 * @see #assertNotInIterator(String, Object, Iterator)
	 */
	public static void assertNotInIterator(Object o, Iterator<?> iter) {
		assertNotInIterator(null, o, iter);
	}
	
    /** 
     * Check the Iterator that object is contained.
     * 
     * @param o    The Object to be checked.
     * @param iter The Iterator used for checking
     */
	public static void assertNotInIterator(String message, Object o, Iterator<?> iter) {
        while (iter.hasNext()) {
            if (o == iter.next())
				fail(optionalMessagePrefix(message) + "Unexpected object '" + o + "' contained in iterator.");
        }
    }

    /**
     * @see #assertCountIterator(String, int, Iterator)
     */
	public static void assertCountIterator(int count, Iterator<?> iter) {
    	assertCountIterator(null, count, iter);
    }
    
    /** 
     * Check the number of Object in the Iterator.
     * 
     *  @param count The Expected number of entries
     *  @param iter  The Iterator exhausted to count the entries.
     */
	public static void assertCountIterator(String message, int count, Iterator<?> iter) {
        assertEquals(message, count,countIterator(iter));
    }

    /** 
     * Return the number of Object in the Iterator.
     */
	public static int countIterator(Iterator<?> iter) {
        int test = 0;
        while (iter.hasNext()) {
            iter.next();
            test++;
        }
        return test;
    }

    /**
     * @see #assertCountRange(String, int, IDRangeIterator)
     */
    public static void assertCountRange(int count, IDRangeIterator iter) {
    	assertCountRange(null, count, iter);
    }
    
    /** 
     * Check the number of Object int the IDRangeIterator.
     * 
     *  @param count The Expected number of entries
     *  @param iter  The Iterator exhausted to count the entries.
     */
    public static void assertCountRange(String message, int count, IDRangeIterator iter) {
        int test = 0;
        while (iter.nextObject() != null) {
            test++;
        }
        assertEquals(message, count, test);
    }

    /**
	 * Assertion to check, if an object is {@link Serializable}.
	 * 
	 * @param anObject
	 *        The Object to be serialized.
	 * @return The object read back from the serialization of the given object.
	 */
    public static Object assertSerializable(Object anObject) 
    	throws IOException, ClassNotFoundException {
    	File   theTemp = null;
    	Object theResult;

        try {
			theTemp = createTestFile("Ser", ".dat");

            FileOutputStream   out  = new FileOutputStream(theTemp);
	    	ObjectOutputStream oout = new ObjectOutputStream(out);
	    	oout.writeObject(anObject);
	    	oout.close();
	    	FileInputStream   in  = new FileInputStream(theTemp);
	    	ObjectInputStream oin = new ObjectInputStream(in);
	    	theResult = oin.readObject();
	    	oin.close();
    	}
    	finally {
    		if (theTemp != null)
    			theTemp.delete();
    	}
		return theResult;
    }

    /**
     * @see #assertLongStringEquals(String, String, String)
     */
    public static void assertLongStringEquals(String s1, String s2) {
    	assertLongStringEquals(null, s1, s2);
    }
    
    /** 
     * Special Way to compare long Strings.
     * 
     * If these differ, only the first position where they differ will be tracked
     */
	public static void assertLongStringEquals(String message, String expected, String actual) {
		int expectedLength = expected.length();
		int actualLength = actual.length();
		if (expectedLength != actualLength) {
			fail(optionalMessagePrefix(message) +
				"String length differs. Expected length: " + expectedLength + ", actual length: " + actualLength + ".");
        }
        
		for (int i = 0; i < expectedLength; i++) {
			char expectedChar = expected.charAt(i);
			char actualChar = actual.charAt(i);
			if (expectedChar != actualChar) {
				fail(optionalMessagePrefix(message) +
					"Strings differ at position " + i + ". " +
					"Expected char: '" + expectedChar + "' (code " + (int) expectedChar + "), " +
					"actual char: '" + actualChar + "' (code " + (int) actualChar + ").");
            }
        }
    }
    
    /**
     * @see #assertStartsWith(String, String, String)
     */
    public static void assertStartsWith(String aString, String aStart) {
    	assertStartsWith(null, aString, aStart);
    }
    
    /**
	 * Check if the first string starts with the second.
	 * 
	 * @param aString
	 *        the tested string
	 * @param expectedPrefix
	 *        the expected start
	 */
	public static void assertStartsWith(String message, String aString, String expectedPrefix) {
		if (expectedPrefix == null)
			return; // anything starts with null
        if (aString == null) {
			throw new AssertionFailedError(optionalMessagePrefix(message) +
				"String is null, but is expected to start with '" + expectedPrefix + "'.");
        }
		if (!aString.startsWith(expectedPrefix)) {
			fail(optionalMessagePrefix(message) +
				"String '" + aString + "' does not start with expected prefix '" + expectedPrefix + "'.");
        }
    }
    
    /**
	 * @see #assertContains(String, String, String)
	 * 
	 * @see #assertErrorMessage(String, Pattern, Throwable)
	 * @see #assertContains(String, Pattern, CharSequence)
	 * @see #assertContains(String, String, String)
	 */
    public static void assertContains(String part, String all) {
    	assertContains(null, part, all);
    }
    
    /**
	 * Check that one String contains some other String
	 * 
	 * @see #assertErrorMessage(String, Pattern, Throwable)
	 * @see #assertContains(String, Pattern, CharSequence)
	 * @see #assertContains(String, String)
	 */
    public static void assertContains(String message, String part, String all) {
        int l = all.indexOf(part);
        if (l < 0) {
			fail(optionalMessagePrefix(message) + "Expecting >>>" + all + "<<< to contain >>>" + part + "<<<.");
        }
    }

    /**
     * @see #assertInstanceof(String, Object, Class...)
     */
	public static void assertInstanceof(Object obj, Class<?>... classes) {
		assertInstanceof(null, obj, classes);
	}

    /**
	 * Asserts that the given object is assignable to one of the classes in the
	 * given array.
	 */
	public static void assertInstanceof(String message, Object obj, Class<?>... classes) {
        if (obj == null) {
			throw new AssertionFailedError(optionalMessagePrefix(message) +
				"Null is not instanceof " + classesToString(classes) + ".");
        }

        Class<?> classOfObject = obj.getClass();
        for (int cnt = classes.length, n = 0; n < cnt; n++) {
			if (classes[n].isAssignableFrom(classOfObject))
				return;
        }
		fail(optionalMessagePrefix(message) +
			"Class of value '" + obj + "' is not subtype of " + classesToString(classes)
			+ ". Actual class: " + obj.getClass().getName());
    }
    
    /** 
     * Assert that given File Object exists and is actually a file.
     */
    public static void assertFileExists(String message, File aFile) {
        if (!aFile.exists()) {
			fail(optionalMessagePrefix(message) + "File not found: " + aFile);
        }
        if (!aFile.isFile()) {
			fail(optionalMessagePrefix(message) + "Not a file: " + aFile);
        }
    }
    
    /** 
     * Assert that given File Object exists and is actually a file.
     */
    public static void assertFileExists(File aFile) {
        assertFileExists(null, aFile);
    }
    
    /** 
     * Helper method to trigger a SaxParser with a String.
     */
    public static void parseXML(String xml, DefaultHandler handler)
            throws FactoryConfigurationError, ParserConfigurationException,
            SAXNotRecognizedException, SAXNotSupportedException, SAXException, IOException {

		SAXParser theParser = SAXUtil.newSAXParserNamespaceAware();
        theParser.parse(new InputSource(new StringReader(xml)), handler);
    }

    /** 
     * Helper method to trigger a SaxParser with a File.
     */
    public static void parseXML(File xml, DefaultHandler handler)
            throws FactoryConfigurationError, ParserConfigurationException,
            SAXNotRecognizedException, SAXNotSupportedException, SAXException, IOException {

		SAXParser theParser = SAXUtil.newSAXParserNamespaceAware();
        theParser.parse(xml, handler);
    }

    
    private static String classesToString(Class<?>[] classes) {
        StringBuilder result = new StringBuilder(classes.length << 6);
		for (int cnt = classes.length, n = 0; n < cnt; n++) {
			if (n > 0) result.append(" or ");
			result.append(classes[n].getName());
		}
		return result.toString();
	}

	/**
     * Write some debug-info about a class.
     * 
     * Its super-classes, interfaces, methods, fields, etc.
     *
     * @param aClass    the Class in question.
     */
    public static void debugClass (Class<?> aClass) {
        System.out.println ("Class: " + aClass.getName ());

        printClassList ("getClasses"    , aClass.getClasses ());
        printClassList ("getInterfaces" , aClass.getInterfaces ());
        printClassList ("getMethods"    , aClass.getMethods ());
        printClassList ("getFields"     , aClass.getFields ());
        printClassList ("getDeclaredClasses",
                             aClass.getDeclaredClasses ());
        printClassList ("getDeclaredMethods",
                             aClass.getDeclaredMethods ());
        printClassList ("getDeclaredFields",
                             aClass.getDeclaredFields ());
    }

    /**
     * Helper function to write out a list of Objects.
     *
     * @param aText will be written as "BEGIN &lt;aText&gt;():"
     * @param someClasses an array of objects taht will be printed.
     */
    public static void printClassList (String aText, Object[] someClasses) {

        int theLen = someClasses.length;
        System.out.println (aText + "(): " + theLen);

        for (int thePos = 0; thePos < theLen; thePos++) {
            System.out.print('\t');
            System.out.println (someClasses [ thePos ]);
        }

        System.out.println ("END " + aText);
        System.out.println();
    }
  
	
	/**
	 * {@link Runnable} that may throw any exceptions.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Execution {
		public void run() throws Exception;
	}

	/**
	 * Factory for {@link BasicTestCase.Execution}s.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface ExecutionFactory {
		Execution createExecution(int id);
	}

	/**
	 * Deferred result of a parallel test execution, see
	 * {@link BasicTestCase#inParallel(BasicTestCase.Execution)}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface TestFuture {
		public static final long DEFAULT_TIMEOUT = 2 * 60 * 1000;

		/**
		 * Throws errors as {@link AssertionError}s after the parallel test has completed.
		 * 
		 * @throws InterruptedException
		 *         If the checking thread was interrupted before the parallel
		 *         test has completed.
		 */
    	void check() throws InterruptedException;

		/**
		 * Throws errors as {@link AssertionError}s after the parallel test has
		 * completed.
		 * 
		 * @param timeout
		 *        the timeout to wait for test execution.
		 * @throws InterruptedException
		 *         If the checking thread was interrupted before the parallel
		 *         test has completed.
		 */
    	void check(long timeout) throws InterruptedException;

		/**
		 * Throws errors as {@link AssertionError}s after the parallel test has
		 * completed.
		 * 
		 * @param message
		 *        The additional message to report, if an error occurred.
		 * @param timeout
		 *        the timeout to wait for test execution.
		 * @throws InterruptedException
		 *         If the checking thread was interrupted before the parallel
		 *         test has completed.
		 */
    	void check(String message, long timeout) throws InterruptedException;
	}
	
	/**
	 * Helper class to execute a given number of threads sequential.
	 * 
	 * <p>
	 * The main method of a {@link SequentialBarrier} is {@link #step(int)} (resp.
	 * {@link #step(int, long)}) which causes the current thread to wait until the given step is
	 * reached.
	 * </p>
	 * 
	 * <p>
	 * The first thread that can pass the barrier is the thread that reaches {@link #step(int)
	 * step(0)}. The thread passes the barrier until it reaches the next call of a
	 * {@link #step(int)} method. Then the thread waits until the step with the specified number is
	 * reached. The thread that called {@link #step(int) step(1)} is woken up and executed until the
	 * next {@link #step(int)} method is called, and so on.
	 * </p>
	 * 
	 * <p>
	 * When the barrier is {@link #close() closed}, all threads are woken up; no call to
	 * {@link #step(int)} causes a thread to wait.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SequentialBarrier implements AutoCloseable {

		private Thread _workerThread;

		private int _nextStep = 0;

		private final long _defaultTimeout;

		/**
		 * Creates a new {@link SequentialBarrier}.
		 */
		public SequentialBarrier() {
			this(0);
		}

		/**
		 * Creates a new {@link SequentialBarrier} with given value as {@link #defaultTimeOut()
		 * default timeout}.
		 */
		public SequentialBarrier(long timeout) {
			if (timeout < 0) {
				throw new IllegalArgumentException("Timeout must not be < 0: " + timeout);
			}
			_defaultTimeout = timeout;
		}

		/**
		 * Time in milliseconds how long a {@link Thread} waits.
		 */
		public long defaultTimeOut() {
			return _defaultTimeout;
		}

		/**
		 * Pauses the current {@link Thread} for {@link #defaultTimeOut()} and executes the next
		 * step.
		 * 
		 * @param step
		 *        The step in the control flow at which the {@link Thread#currentThread() current
		 *        thread} must be re-activated.
		 * @see #step(int, long)
		 */
		public void step(int step) throws InterruptedException {
			step(step, defaultTimeOut());
		}

		/**
		 * Pauses the current {@link Thread} for {@link #defaultTimeOut()} and executes the next
		 * step.
		 * 
		 * @param step
		 *        The step in the control flow at which the {@link Thread#currentThread() current
		 *        thread} must be re-activated.
		 * @param timeout
		 *        How long the current thread waits to be reactivated. If the timeout is over and
		 *        the current {@link Thread} was not woke up, an {@link IllegalStateException} is
		 *        thrown.
		 */
		public synchronized void step(int step, long timeout) throws InterruptedException {
			while (true) {
				if (isClosed()) {
					return;
				}
				Thread caller = Thread.currentThread();
				if (_workerThread == caller) {
					// worker thread re-entered. Work is done.
					_workerThread = null;
					// Wake-up next thread.
					_nextStep++;
					this.notifyAll();
				}

				if (step == _nextStep) {
					if (_workerThread == null) {
						_workerThread = caller;
						break;
					} else {
						throw new IllegalArgumentException(
							"Multiple Threads entered barrier with same step number " + step);
					}
				} else if (step > _nextStep) {
					waitForWakeUp(timeout);
				} else {
					throw new IllegalArgumentException(
						"Step " + step + " was already processed. Next step: " + _nextStep);
				}
			}
		}

		private void waitForWakeUp(long timeout) throws InterruptedException {
			if (timeout <= 0) {
				wait(timeout);
			} else {
				long now = System.currentTimeMillis();
				wait(timeout);
				if (System.currentTimeMillis() - now >= timeout) {
					// thread was not woke up; timeout reached
					throw new IllegalStateException("Timeout '" + timeout + "' reached.");
				}
			}
		}

		@Override
		public synchronized void close() {
			_nextStep = -1;
			notifyAll();
		}

		private boolean isClosed() {
			return _nextStep == -1;
		}

	}

	/**
	 * A thread barrier.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Barrier {
		/**
		 * The number of threads that must {@link #enter(long)} this
		 * {@link BasicTestCase.Barrier} before {@link #enter(long)} and
		 * {@link #await(long)} return.
		 */
		int size();

		/**
		 * Enters this {@link BasicTestCase.Barrier} and wait until all other
		 * threads have also {@link #enter(long) entered}.
		 * 
		 * @param timeout
		 *        The number of milliseconds to wait.
		 * @return Whether all other threads have entered in time.
		 * @throws InterruptedException
		 *         If this thread was interrupted while waiting.
		 */
		boolean enter(long timeout) throws InterruptedException;

		/**
		 * Wait until all threads have {@link #enter(long) entered}.
		 * 
		 * @param timeout
		 *        The number of milliseconds to wait.
		 * @return Whether all other threads have entered in time.
		 * @throws InterruptedException
		 *         If this thread was interrupted while waiting.
		 */
		boolean await(long timeout) throws InterruptedException;
	}

	/**
	 * Creates a simple {@link Barrier} for one time usage.
	 * 
	 * @param size
	 *        The {@link Barrier#size()} of the barrier.
	 * @return A new barrier for testing.
	 */
	public static Barrier createBarrier(final int size) {
		return new Barrier() {
			int entered = 0;
			
			@Override
			public int size() {
				return size;
			}
			
			@Override
			public synchronized boolean enter(long timeout) throws InterruptedException {
				if (entered >= size) {
					throw new IllegalStateException("All expected threads have already entered this barrier.");
				}
				entered++;
				if (entered == size) {
					this.notifyAll();
					return true;
				} else {
					return internalWait(timeout);
				}
			}

			@Override
			public synchronized boolean await(long timeout) throws InterruptedException {
				return internalWait(timeout);
			}
			
			private boolean internalWait(long timeout) throws InterruptedException {
				while (entered < size) {
					long elapsed = -System.currentTimeMillis();
					this.wait(timeout);
					elapsed += System.currentTimeMillis();
					
					timeout -= elapsed;
					if (timeout <= 0) {
						// Timeout reached.
						return false;
					}
				}
				return true;
			}
		};
	}

	/**
	 * Synchronization object that can be explicitly {@link #lock() locked} and
	 * {@link #unlock() unlocked} for testing.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Semaphore {
		/**
		 * Enters this {@link BasicTestCase.Semaphore}, if it is currently not
		 * {@link #lock()}. If this {@link BasicTestCase.Semaphore} is locked,
		 * the entering thread waits until {@link #unlock()} is called.
		 */
		void enter() throws InterruptedException;
		
		/**
		 * Locks this {@link BasicTestCase.Semaphore}.
		 * 
		 * <p>
		 * Calls to {@link #enter()} wait until {@link #unlock()} is called.
		 * </p>
		 */
		void lock();
		
		/**
		 * Unlocks this {@link BasicTestCase.Semaphore}.
		 * 
		 * <p>
		 * Waiting calls to {@link #enter()} can proceed.
		 * </p>
		 */
		void unlock();
	}
	
	/**
	 * Creates a new unlocked {@link Semaphore} for testing.
	 */
	public static Semaphore createSemaphore() {
		return new Semaphore() {
			private boolean locked;

			@Override
			public synchronized void enter() throws InterruptedException {
				while (this.locked) {
					wait();
				}
			}
			
			@Override
			public synchronized void lock() {
				this.locked = true;
			}

			@Override
			public synchronized void unlock() {
				this.locked = false;
				this.notifyAll();
			}
			
		};
	}
	
	/**
	 * Invoke the given {@link Execution} in a separate thread, wait for
	 * completion and forward any exceptions that may have occurred to the
	 * calling thread.
	 */
    public void inThread(final Execution runnable) {
    	inThread(runnable, 0);
	}

	/**
	 * Invoke the given {@link Execution} in a separate thread, wait for
	 * completion and forward any exceptions that may have occurred to the
	 * calling thread.
	 * 
	 * @param millis
	 *        number of milliseconds to wait for the separate thread. If an
	 *        error occurred that problem is reported. If no error occurs but
	 *        the thread has not been finished within the given time an
	 *        {@link AssertionError} is reported.
	 */
    public void inThread(final Execution runnable, long millis) {
    	// Create buffer to comumnicate exceptions with the caller.
    	final Throwable[] problem = new Throwable[1];
    	boolean otherTerminated;
    	
		final String ctxId = testContextId(getClass());
		Thread other = new Thread() {

			@Override
			public void run() {
				super.run();
				ThreadContextManager.inInteraction(ctxId, new InContext() {

					@Override
					public void inContext() {
						try {
							runnable.run();
						} catch (Throwable ex) {
							problem[0] = ex;
						}
					}
				});
			}
    	};
    	
    	// Execute in separate thread.
    	other.start();
    	try {
    		other.join(millis);
			otherTerminated = !other.isAlive();
    	} catch (InterruptedException e) {
    		throw new UnreachableAssertion(e);
    	}
    	
    	if (problem[0] != null) {
			if (problem[0] instanceof AssertionFailedError) {
				throw (AssertionFailedError) problem[0];
			} else {
				throw (AssertionError) new AssertionError("Execution in other thread terminated exceptionally.")
					.initCause(problem[0]);
			}
    	}
    	if (!otherTerminated) {
    		assert millis != 0 : "This Thread waits until the other thread terminates."; 
    		throw new AssertionError("Other thread has not terminated within " + millis + " milliseconds");
    	}
    }
    
	/**
	 * Runs the given test in a separate thread and returns a {@link TestFuture}
	 * that must be {@link TestFuture#check() checked} after all parallel tests
	 * have been started.
	 * 
	 * <p>
	 * The parallel task is run in a newly created {@link ThreadContext}.
	 * </p>
	 * 
	 * @param runnable The test to execute.
	 * @return the deferred test result.
	 */
    public TestFuture inParallel(final Execution runnable) {
    	return inParallel(null, runnable);
    }
    
	/**
	 * Runs the given test in a separate thread and returns a {@link TestFuture}
	 * that must be {@link TestFuture#check() checked} after all parallel tests
	 * have been started.
	 * 
	 * @param context The thread context to use.
	 * @param runnable The test to execute.
	 * @return the deferred test result.
	 */
    public TestFuture inParallel(final ThreadContext context, final Execution runnable) {
    	class Result implements TestFuture {
    		private Throwable problem;
			private boolean completed;
			private Thread executor;

			public synchronized void complete(Throwable ex) {
				this.problem = ex;
				this.completed = true;
				this.notifyAll();
			}

			public synchronized void complete() {
				this.completed = true;
				this.notifyAll();
			}
			
			@Override
			public void check() throws InterruptedException {
				check(TestFuture.DEFAULT_TIMEOUT);
			}

			@Override
			public synchronized void check(long timeout) throws InterruptedException {
				check(null, timeout);
			}
			
			@Override
			public synchronized void check(String message, long timeout) throws InterruptedException {
				long waitTime = timeout;
				while (! completed) {
					long elapsed = - System.currentTimeMillis();
					wait(waitTime);

					// Do not consider wait time, if the result is available.
					// Otherwise, timout = 0 (wait forever) does not work.
					if (completed) {
						break;
					}
					
					elapsed += System.currentTimeMillis();
					waitTime -= elapsed;
					if (waitTime <= 0) {
						throw new AssertionFailedError(optionalMessagePrefix(message) +
							"Test execution did not terminate in time.");
					}
				}
				assertTrue("Test execution did not terminate in time.", completed);
				
				if (this.problem != null) {
					throw (AssertionFailedError) new AssertionFailedError(optionalMessagePrefix(message) +
						"Parallel execution failed.").initCause(this.problem);
				}
			}

			public void start(Thread executor) {
				this.executor = executor;
			}
    	}
    	
    	final Result result = new Result();
    	
    	// Create buffer to communicate exceptions with the caller.
    	
		final String testContextId = testContextId(getClass());
		final InContext job = new InContext() {

			@Override
			public void inContext() {
				try {
					result.start(Thread.currentThread());

					runnable.run();

					result.complete();
				} catch (Throwable ex) {
					result.complete(ex);
				}
			}
		};
		String threadName = "inParallel:" + runnable.getClass().getSimpleName();
		Thread other = new Thread(threadName) {

			@Override
			public void run() {
				super.run();
				if (context != null) {
					ThreadContextManager.inContext(context, job);
				} else {
					ThreadContextManager.inInteraction(testContextId, job);
				}
			}
    	};
    	
    	// Execute in separate thread.
    	other.start();
    	
    	return result;
    }

	/**
	 * Checks all given deferred test results.
	 * 
	 * <p>
	 * The deferred test results are created by executing a test
	 * {@link #inParallel(BasicTestCase.Execution)}.
	 * </p>
	 * 
	 * @throws InterruptedException
	 *         If the checking thread was interrupted before the parallel
	 *         tests have completed.
	 */
    public void checkAll(TestFuture... results) throws InterruptedException {
    	checkAll(results, TestFuture.DEFAULT_TIMEOUT);
    }
    
	/**
	 * Checks all given deferred test results.
	 * 
	 * <p>
	 * The deferred test results are created by executing a test
	 * {@link #inParallel(BasicTestCase.Execution)}.
	 * </p>
	 * 
	 * @param timeout
	 *        the timeout to wait for all test executions.
	 *        
	 * @throws InterruptedException
	 *         If the checking thread was interrupted before the parallel
	 *         tests have completed.
	 */
    public void checkAll(TestFuture[] results, long timeout) throws InterruptedException {
    	Throwable firstProblem = null;
        for (int n = 0; n < results.length; n++) {
        	try {
            	long waitTime = -System.currentTimeMillis();
            	try {
            		results[n].check(timeout);
            	} finally {
					// Reduce timeout about the time waited for the first
					// executor.
            		waitTime += System.currentTimeMillis();
            		timeout -= waitTime;
            		
            		if (timeout <= 0) {
            			timeout = 1;
            		}
            	}
        	} catch (Error ex) {
        		if (firstProblem == null) {
        			firstProblem = ex;
        		} else {
        			// Ignore any further exceptions.
        		}
        	} catch (RuntimeException ex) {
        		if (firstProblem == null) {
        			firstProblem = ex;
        		} else {
        			// Ignore any further exceptions.
        		}
        	}
        }
        if (firstProblem != null) {
        	if (firstProblem instanceof Error) {
        		throw (Error) firstProblem;
        	}
        	if (firstProblem instanceof RuntimeException) {
        		throw (RuntimeException) firstProblem;
        	}
        	throw new UnreachableAssertion(firstProblem);
        }
    }
    
    /**
     * Execute a test with the given parallelism.
     * 
     * @param parallelTests the number of parallel runs.
     * @param executionFactory The factory to create the test {@link Execution}s.
     */
	public void parallelTest(int parallelTests, ExecutionFactory executionFactory) throws InterruptedException {
		parallelTest(parallelTests, TestFuture.DEFAULT_TIMEOUT, executionFactory);
	}

    /**
     * Execute a test with the given parallelism.
     * 
     * @param parallelTests the number of parallel runs.
     * @param timeout milliseconds to wait for all test executions to return.
     * @param executionFactory The factory to create the test {@link Execution}s.
     */
	public void parallelTest(int parallelTests, long timeout, ExecutionFactory executionFactory) throws InterruptedException {
		TestFuture[] results = new TestFuture[parallelTests];
		for (int n = 0; n < parallelTests; n++) {
			results[n] = inParallel(executionFactory.createExecution(n));
		}
		checkAll(results, timeout);
	}

	/**
	 * Creates a {@link SubSessionContext#getContextId()} for test cases.
	 * 
	 * @param testClass
	 *        The test class.
	 * @return The new context id .
	 */
	public static String testContextId(Class<?> testClass) {
		return "test:" + testClass.getName();
	}

	/**
	 * @see #assertEquals(String, Mapping, Set, Set)
	 */
	public static <S> void assertEquals(Mapping<? super S, ?> mapping, Set<S> expectedValues, Set<S> givenValues) {
		assertEquals(null, mapping, expectedValues, givenValues);
	}
	
	/**
	 * Tests that both given sets contains the same elements with respect to the given mapping.
	 * 
	 * <p>Report detailed difference if sets differ.</p>
	 * 
	 * @param mapping
	 *        The mapping to apply to both expected and given values. The result of the mapping is compared.
	 * @param expectedValues
	 *        The expected values.
	 * @param givenValues
	 *        The given values.
	 */
	public static <S> void assertEquals(String message, Mapping<? super S, ?> mapping, Set<S> expectedValues, Set<S> givenValues) {
		HashSet<Object> expectedMapped = CollectionUtil.newSet(expectedValues.size());
		CollectionUtil.map(expectedValues.iterator(), expectedMapped, mapping);
		
		HashSet<Object> givenMapped = CollectionUtil.newSet(givenValues.size());
		CollectionUtil.map(givenValues.iterator(), givenMapped, mapping);
		
		assertEquals(message, expectedMapped, givenMapped);
	}

	/**
	 * @see #assertEquals(String, Set, Set)
	 */
	public static void assertEquals(Set<?> expectedValues, Set<?> givenValues) {
		assertEquals(null, expectedValues, givenValues);
	}

	/**
	 * Tests that both given sets contains the same elements and report detailed
	 * difference, if not.
	 * 
	 * @param message
	 *        The message to add to a potential failure description.
	 * @param expectedValues
	 *        The expected values.
	 * @param givenValues
	 *        The given values.
	 */
	public static void assertEquals(String message, Set<?> expectedValues, Set<?> givenValues) {
		Set<?> missingElements = null;
		if (! givenValues.containsAll(expectedValues)) {
			missingElements = CollectionUtil.difference(expectedValues, givenValues);
		}
		Set<?> unexpectedElements = null;
		if (! expectedValues.containsAll(givenValues)) {
			unexpectedElements = CollectionUtil.difference(givenValues, expectedValues);
		}
		if (missingElements != null || unexpectedElements != null) {
			StringBuilder failure = new StringBuilder();
			failure.append(optionalMessagePrefix(message));
			failure.append("Sets differ.");
			if (missingElements != null) {
				failure.append(" Missing elements " + missingElements + " in " + givenValues + ".");
			}
			if (unexpectedElements != null) {
				if (missingElements != null) {
					failure.append(",");
				}
				failure.append(" Unexpected elements " + unexpectedElements + " in " + givenValues + ".");
			}
			fail(failure.toString());
		}
		
		// Test regular equals methods.
		assertEquals(message, (Object) expectedValues, (Object) givenValues);
	}

	static String optionalMessagePrefix(String message) {
		if (StringServices.isEmpty(message)) {
			return "";
		} else {
			return (message.endsWith(".") ? message.substring(0, message.length() - 1) : message) + ": ";
		}
	}

	public static <T> List<T> toList(Iterable<T> values) {
		ArrayList<T> result = new ArrayList<>();
		for (T value : values) {
			result.add(value);
		}
		return result;
	}

    public static <T> Set<T> toSet(Iterable<T> values) {
    	HashSet<T> result = new HashSet<>();
		for (T value : values) {
			result.add(value);
		}
		return result;
    }
    
	public static <T> Set<T> toSet(Iterator<T> it) {
		return new HashSet<>(CollectionUtil.toList(it));
	}
	
	public static <T> T toSingleton(Iterator<T> it) {
		if (it.hasNext()) {
			T result = it.next();
			if (it.hasNext()) {
				fail("Unexpected element: " + it.next());
			}
			return result;
		} else {
			return null;
		}
	}
	
	public static <T> List<T> toList(Iterator<T> it) {
		return CollectionUtil.toList(it);
	}
    
    
	/**
	 * Creates a set of the given literal values.
	 */
	@SafeVarargs
	public static <T> Set<T> set(T... values) {
		return new HashSet<>(Arrays.asList(values));
	}

	/**
	 * Creates a set of the given literal values.
	 */
	@SafeVarargs
	public static <T> MultiSet<T> multiSet(T... values) {
		return new HashMultiSet<>(Arrays.asList(values));
	}

	/**
	 * Creates an immutable list of the given literal values.
	 */
	@SafeVarargs
	public static <T> List<T> list(T... values) {
		return Arrays.asList(values);
	}

	/**
	 * Create a random string that contains characters form the selected
	 * character classes.
	 * 
	 * @param size
	 *        The size in code points of the result.
	 * @param ascii
	 *        Include ASCII characters.
	 * @param latin
	 *        Include Latin characters.
	 * @param bmp
	 *        Included characters from the base multilingual plane of the
	 *        Unicode character set.
	 * @param supplementary
	 *        Included supplementary Unicode characters (which cannot be
	 *        represented by a single UTF-16 encoded character.
	 * @return A random string of the given length in code points.
	 */
	public static String randomString(int size, boolean ascii, boolean latin, boolean bmp, boolean supplementary) {
		Random rnd = new Random(42);
		return randomString(rnd, size, ascii, latin, bmp, supplementary);
	}

	/**
	 * Create a random string that contains characters form the selected
	 * character classes.
	 * 
	 * @param rnd
	 *        The random source
	 * @param size
	 *        The size in code points of the result.
	 * @param ascii
	 *        Include ASCII characters.
	 * @param latin
	 *        Include Latin characters.
	 * @param bmp
	 *        Included characters from the base multilingual plane of the
	 *        Unicode character set.
	 * @param supplementary
	 *        Included supplementary Unicode characters (which cannot be
	 *        represented by a single UTF-16 encoded character.
	 * @return A random string of the given length in code points.
	 */
	public static String randomString(Random rnd, int size, boolean ascii, boolean latin, boolean bmp, boolean supplementary) {
		StringBuilder buffer = new StringBuilder(supplementary ? 2 * size : size);
		for (int codePointLength = 0; codePointLength < size; codePointLength++) {
			int codePoint = randomChar(rnd, ascii, latin, bmp, supplementary);
			buffer.appendCodePoint(codePoint);
		}
		return buffer.toString();
	}

	/**
	 * Constructs a random string whose UTF-16 encoding fits into a buffer of
	 * the given size in UTF-16 encoded characters.
	 * 
	 * @param size The buffer size in UTF-16 encoded characters
	 * 
	 * @see #randomString(int, boolean, boolean, boolean, boolean)
	 */
	public static String randomUTF16String(int size, boolean ascii, boolean latin, boolean bmp, boolean supplementary) {
		Random rnd = new Random(42);
		return randomUTF16String(rnd, size, ascii, latin, bmp, supplementary);
	}

	/**
	 * Constructs a random string whose UTF-16 encoding fits into a buffer of
	 * the given size in UTF-16 encoded characters.
	 * 
	 * @param size The buffer size in UTF-16 encoded characters
	 * 
	 * @see #randomString(Random, int, boolean, boolean, boolean, boolean)
	 */
	public static String randomUTF16String(Random rnd, int size, boolean ascii, boolean latin, boolean bmp, boolean supplementary) {
		StringBuilder buffer = new StringBuilder(size);
		for (int length = 0; length < size; ) {
			int codePoint = randomChar(rnd, ascii, latin, bmp, supplementary);
			
			int encodedLength = (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 1 : 2;
			if (length + encodedLength > size) {
				if (ascii || latin || bmp) {
					supplementary = false;
					// Choose another character.
					continue;
				} else {
					// No chance to fill the space.
					break;
				}
			}
			length += encodedLength;
			buffer.appendCodePoint(codePoint);
		}

		String result = buffer.toString();
		assert result.length() <= size;
		assert result.length() >= size - 1;
		return result;
	}

	/**
	 * Creates a random <code>char</code> from the given random source that
	 * adheres to the given specification.
	 * 
	 * @param rnd
	 *        The random source.
	 * @param ascii
	 *        Whether plain ASCII characters could be returned.
	 * @param latin
	 *        Whether characters from the latin character set can be returned.
	 * @param bmp
	 *        Whether characters from the base multilingual plane from the UTF
	 *        character set can be returned.
	 * @param supplementary
	 *        Whether characters from the supplementary UTF character set can be
	 *        returned. Such characters consist of two 
	 * @return The code point of the created random character.
	 */
	private static int randomChar(Random rnd, boolean ascii, boolean latin, boolean bmp, boolean supplementary) {
		while (true) {
			int kind = rnd.nextInt(4);
			switch (kind) {
			case 0:
				if (! ascii) {
					continue;
				}
				return randomAsciiChar(rnd);
			case 1:
				if (! latin) {
					continue;
				}
				return randomLatinChar(rnd);
			case 2:
				if (! bmp) {
					continue;
				}
				return randomBaseMultilingualPlaneChar(rnd);
			case 3:
				if (! supplementary) {
					continue;
				}
				return randomSupplementaryChar(rnd);
			default:
				throw new UnreachableAssertion("All values covered.");
			}
		}
	}
	
	private static char randomAsciiChar(Random rnd) {
		return (char) (32 + rnd.nextInt(128 - 32));
	}

	private static char randomLatinChar(Random rnd) {
		return LATIN_CHARS.charAt(rnd.nextInt(LATIN_CHARS.length()));
	}
	
	private static int randomSupplementaryChar(Random rnd) {
		int type;
		int codePoint;
		do {
			codePoint = Character.MIN_SUPPLEMENTARY_CODE_POINT + rnd.nextInt(0x110000 - Character.MIN_SUPPLEMENTARY_CODE_POINT);
			type = Character.getType(codePoint);
		} while (
			type == Character.UNASSIGNED || type == Character.SURROGATE || type == Character.PRIVATE_USE 
		);
		return codePoint;
	}
	
	private static int randomBaseMultilingualPlaneChar(Random rnd) {
		int type;
		int codePoint;
		do {
			codePoint = (1 + rnd.nextInt(Character.MIN_SUPPLEMENTARY_CODE_POINT - 1));
			type = Character.getType(codePoint);
		} while (
				type == Character.UNASSIGNED || type == Character.SURROGATE 
		);
		return codePoint;
	}

	public static File createdCleanTestDir(String name) {
		File dir = createNamedTestFile(name);
		if (dir.exists()) {
			assertTrue(FileUtilities.deleteR(dir));
		}
		assertTrue(dir.mkdir());
		return dir;
	}

    /**
     * Creates a temporary file in the project local temporary directory.
     */
    public static File createNamedTestFile(String fileName) {
    	ensureTempDirExists();
    	return new File(TMP, fileName);
    }

	/**
	 * Creates a temporary file with a unique name in the project local
	 * temporary directory.
	 * 
	 * @see File#createTempFile(String, String)
	 */
	public static File createTestFile(String prefix, String suffix) throws IOException {
		ensureTempDirExists();
		return File.createTempFile(prefix, suffix, TMP);
	}

	private static void ensureTempDirExists() {
		if (! TMP.exists()) {
			assertTrue(TMP.mkdir());
		} else {
			assertTrue(TMP.isDirectory());
		}
	}

	public static void deleteTestDir() {
		if (TMP.exists()) {
			assertTrue(FileUtilities.deleteR(TMP));
		}
	}
	
	/**
	 * Returns the value of the field with name <code>fieldName</code> declared in a superclass of
	 * the type of the object <code>valueHolder</code>
	 * 
	 * @param valueHolder
	 *            an object which has a field named <code>fieldName</code> whose value is needed
	 * @param fieldName
	 *            the name of the field whose value is needed
	 * @throws ReflectionException
	 *             if it is not possible to get the value due to any reason
	 */
	@Deprecated
	public static Object getValueByReflection(Object valueHolder, String fieldName) throws ReflectionException {
		return ReflectionUtils.getValue(valueHolder, fieldName);
	}
	
	/**
	 * Calls {@link DirectoryLocalTestCollector#createTests(String)}.
	 */
	public static Test createTests(String pckgName, Protocol log, boolean includeSubPckgs) {
		return new DirectoryLocalTestCollector(log, includeSubPckgs).createTests(pckgName);
	}

	/**
	 * @see #assertEqualsTextStream(String, InputStream, InputStream)
	 */
	public static void assertEqualsTextStream(InputStream patternStream, InputStream testStream) throws IOException {
		assertEqualsTextStream(null, patternStream, testStream);
	}

    /**
	 * Checks, whether the stream passed as second argument contains the same textual contents as
	 * the stream passed as first argument.
	 * 
	 * This method is not identical to
	 * {@link StreamUtilities#equalsStreamContents(java.io.InputStream, java.io.InputStream)},
	 * because the streams are parsed into lines ignoring different flavors of line terminators.
	 * 
	 * @param patternStream
	 *        The stream that contains the expected result.
	 * @param testStream
	 *        The stream that is tested for equality to the pattern stream.
	 * @throws IOException
	 *         If there is a problem during reading one of the two streams.
	 */
	public static void assertEqualsTextStream(String message, InputStream patternStream, InputStream testStream)
			throws IOException {

                BufferedReader patternReader = new BufferedReader(new InputStreamReader(patternStream));
            	BufferedReader testReader    = new BufferedReader(new InputStreamReader(testStream));
            	
            	int lineNr = 0;
            	
            	while (true) {
            	    String patternLine = patternReader.readLine();
            	    String testLine    = testReader.readLine();
            		lineNr++;
            
            		// Both at end, fine
            		if (patternLine == null) {
            		    if (testLine == null) {// both at end, find
            		        return;
            		    }
                        fail(optionalMessagePrefix(message) + 
                        	"Missing line " + lineNr + ". Expected: '" + patternLine + "'.");
            		}
            		
            		if (testLine == null) {
                        fail(optionalMessagePrefix(message) + 
                        	"Superfluous line " + lineNr + ". Unexpected: '" + testLine + "'.");
            		}
            		
            		if (! patternLine.equals(testLine)) {
						fail(optionalMessagePrefix(message) +
							"Difference in line " + lineNr + ". Expected: '" + patternLine + "', found: '" + testLine + "'.");
            		}
            	}
            }

    /**
     * Asserts that the given collection is empty.
     * 
     * @param message Is prepended to the rest of the message. Can be null.
     * @param printCollection Print the collection and its content or just the size of the collection?
     *        Choose <code>false</code> for large collections or
     *        collections containing objects with a large String representation.
     * @param collection Must not be <code>null</code>.
     */
	public static void assertEmpty(String message, boolean printCollection, Collection<?> collection) {
		if (collection.isEmpty()) {
			return;
		}
		
		String prependedMessage = optionalMessagePrefix(message) + "Collection is not empty. ";
		if (printCollection) {
			fail(prependedMessage + "Content of collection: [" + StringServices.toString(collection) + "]");
		} else {
			fail(prependedMessage + "Size of collection: " + collection.size());
		}
	}
	
	/**
	 * @see #assertEmpty(String, boolean, Collection)
	 */
	public static void assertEmpty(boolean printCollection, Collection<?> collection) {
		assertEmpty("", printCollection, collection);
	}

	/**
	 * @see #assertEmpty(String, boolean, Collection)
	 */
	public static void assertEmpty(String message, boolean printArray, Object[] array) {
		assertEmpty(message, printArray, Arrays.asList(array));
	}

	/**
	 * @see #assertEmpty(String, boolean, Collection)
	 */
	public static void assertEmpty(boolean printArray, Object[] array) {
		assertEmpty("", printArray, Arrays.asList(array));
	}

	/**
	 * Tests that the given document is semantically equal to the expected document.
	 */
	public static void assertEqualsDocument(Document expected, Document given) {
		assertEqualsDocument((String) null, expected, given);
	}
	
	/**
	 * Tests that the given document is semantically equal to the expected document.
	 */
	public static void assertEqualsDocument(String message, Document expected, Document given) {
		assertEqualsDocument(message, FilterFactory.trueFilter(), expected, given);
	}
	
	/**
	 * Tests that the given document is semantically equal to the expected document ignoring all elements of 
	 * namespaces not matching the given filter.
	 */
	public static void assertEqualsDocument(Filter<? super String> namespaceFilter, Document expected, Document given) {
		assertEqualsDocument(null, namespaceFilter, expected, given);
	}
	
	/**
	 * Tests that the given document is semantically equal to the expected document ignoring all elements of 
	 * namespaces not matching the given filter.
	 */
	public static void assertEqualsDocument(String message, Filter<? super String> namespaceFilter, Document expected, Document given) {
		new XMLCompare(new AssertProtocol(message), false, namespaceFilter).assertEqualsNode(expected, given);
	}

	/**
	 * Tests that the given document is not semantically equal to the expected document.
	 */
	public static void assertNotEqualsDocument(Document expected, Document given) {
		assertNotEqualsDocument((String)null, expected, given);
	}
	
	/**
	 * Tests that the given document is not semantically equal to the expected document.
	 */
	public static void assertNotEqualsDocument(String message, Document expected, Document given) {
		assertNotEqualsDocument(message, FilterFactory.trueFilter(), expected, given);
	}

	/**
	 * Tests that the given document is not semantically equal to the expected document ignoring all elements of 
	 * namespaces not matching the given filter.
	 */
	public static void assertNotEqualsDocument(Filter<? super String> namespaceFilter, Document expected, Document given) {
		assertNotEqualsDocument(null, namespaceFilter, expected, given);
	}
	
	/**
	 * Tests that the given document is not semantically equal to the expected document ignoring all elements of 
	 * namespaces not matching the given filter.
	 */
	public static void assertNotEqualsDocument(String message, Filter<? super String> namespaceFilter, Document expected, Document given) {
		try {
			new XMLCompare(new ExpectedFailureProtocol(), false, namespaceFilter).assertEqualsNode(expected, given);
			fail(optionalMessagePrefix(message) + "Expected difference in documents.");
		} catch (ExpectedFailure ex) {
			// Expected.
		}
	}
	
	/**
	 * Returns the resource with the name
	 * <code>clazz.getSimpleName() + "." + fileSuffix</code> near given class.
	 * 
	 * @return never <code>null</code>
	 * 
	 * @throws IOException
	 *         iff there is no such resource
	 */
	public static InputStream getResource(Class<?> clazz, String fileSuffix) throws IOException {
		final String resourceName = clazz.getSimpleName() + "." + fileSuffix;
		return getResourceByName(clazz, resourceName);
	}

	/**
	 * Returns the resource with the given name near given class.
	 * 
	 * @return never <code>null</code>
	 * 
	 * @throws IOException
	 *         iff there is no such resource
	 */
	public static InputStream getResourceByName(Class<?> clazz, String resourceName) throws IOException {
		final InputStream resource = clazz.getResourceAsStream(resourceName);
		if (resource == null) {
			throw new IOException("Unable to find resource " + resourceName);
		}
		return resource;
	}
	
	/**
	 * Provokes (and catches) an {@link OutOfMemoryError}. Useful to force
	 * garbage collector to run.
	 */
	// Deprecated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	@Deprecated
	public static void provokeOutOfMemory() {
		for (int i = 0; i < 3; i++) {
			System.runFinalization();
			System.gc();
			internalProvokeOOM(128 * 1024);
		}
	}

	private static void internalProvokeOOM(int stepSize) {
		if (Long.MAX_VALUE == Runtime.getRuntime().maxMemory()) {
			fail("No inherent memory limit. Out of memory could not be simulated.");
		}

		try {
			ArrayList<Object> list = new ArrayList<>();

			while (true) {
				list.add(allocateMemory(stepSize));
			}
		} catch (OutOfMemoryError ex) {
			// Expected.
		}
	}

	private static byte[] allocateMemory(int size) {
		byte[] x = new byte[size];
		// ensure that array is de facto allocated
		x[0] = Byte.MAX_VALUE;
		x[size - 1] = Byte.MAX_VALUE;
		return x;
	}
	/**
	 * The "discriminating name suffix" is a information appended to the name of the test.
	 * Useful, if the same test is run multiple times with just the context being a little bit different.
	 * For example, if a database test is run with MySQL and Oracle.
	 * The "discriminating name suffix" can be used to append the database name to the original test name,
	 * making it clear which database causes the test to fail and which not.
	 */
	public void appendDiscriminatingNameSuffix(String contextSuffix) {
		discriminatingSuffix += sanitizeForEclipse(contextSuffix);
	}

	/**
	 * The JUnit GUI in Eclipse cuts off the name of the tests at some special characters.
	 * Therefore, everything matching {@link #SPECIAL_CHARACTERS} is replaced with {@link #SPECIAL_CHARACTER_REPLACEMENT}
	 */
	public static String sanitizeForEclipse(String content) {
		return SPECIAL_CHARACTERS.matcher(content).replaceAll(SPECIAL_CHARACTER_REPLACEMENT);
	}

	/**
	 * Appends the "context name suffix" to the original name delivered by: {@link TestCase#getName()}
	 * The "context name suffix" can be changed via: {@link #appendDiscriminatingNameSuffix(String)}
	 * 
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return super.getName() + discriminatingSuffix;
	}

	/**
	 * Asserts that the contents of the two {@link File}s are equal.
	 * <p>
	 * Both files must not be <code>null</code> and both files must {@link File#exists() exist}.
	 * </p>
	 * 
	 * @throws NullPointerException
	 *         If one or both files are <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If one or both files does not {@link File#exists() exist}.
	 */
	public static void assertEqualsFileContent(String message, File expected, File actual) {
		// Fail early to ensure documented semantics and give helpful error messages.
		if (expected == null) {
			throw new NullPointerException("The 'expected' file is null.");
		}
		if (actual == null) {
			throw new NullPointerException("The 'actual' file is null.");
		}
		if (!expected.exists()) {
			throw new IllegalArgumentException("The 'expected' file does not exist. File: '" + expected + "'");
		}
		if (!actual.exists()) {
			throw new IllegalArgumentException("The 'actual' file does not exist. File: '" + actual + "'");
		}
		try {
			assertTrue(optionalMessagePrefix(message) + " File content is not equal. Expected content: '"
				+ expected
				+ "', actual content: '" + actual + "'.", FileUtilities.equalsFile(expected, actual));
		}
		catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Asserts that the {@link File} is not <code>null</code>,
	 * does {@link File#exists() exist} and is not {@link File#length() empty}.
	 */
	public static void assertFileNotEmpty(String message, File file) {
		assertNotNull(optionalMessagePrefix(message) + "File is null: '" + file + "'", file);
		assertTrue(optionalMessagePrefix(message) + "File does not exist: '" + file + "'", file.exists());
		assertTrue(optionalMessagePrefix(message) + "File is empty: '" + file + "'", file.length() > 0);
	}

	/**
	 * Asserts that the given {@link CharSequence} contains a part of text that matches the given
	 * {@link Pattern}.
	 * <p>
	 * Neither of the arguments is allowed to be <code>null</code>.
	 * </p>
	 * 
	 * @see #assertErrorMessage(String, Pattern, Throwable)
	 * @see #assertContains(String, String, String)
	 * @see #assertContains(String, String)
	 * 
	 * @see RegExpUtil#contains(CharSequence, Pattern)
	 */
	public static void assertContains(String message, Pattern expectedPart, CharSequence actual) {
		if (expectedPart == null) {
			throw new NullPointerException("The pattern is null.");
		}
		if (actual == null) {
			throw new NullPointerException("The given text to search in is null.");
		}
		String fullMessage = optionalMessagePrefix(message) +
			"String does not contain matching content. Pattern: '" + expectedPart + "', content: '" + actual + "'.";
		assertTrue(fullMessage, RegExpUtil.contains(actual, expectedPart));
	}

	/**
	 * Asserts that the {@link Throwable#getMessage() message} of the given {@link Throwable} or one
	 * of it's {@link Throwable#getCause() causes} matches the given {@link Pattern}.
	 * <p>
	 * More precisely: The {@link Pattern} has to match a part of one of the messages. To enforce,
	 * that the whole message has to be matched by the pattern, surround it with "\A" and "\z", for
	 * example.
	 * </p>
	 * 
	 * @see #assertContains(String, Pattern, CharSequence)
	 * @see #assertContains(String, String, String)
	 * @see #assertContains(String, String)
	 * 
	 * @param message
	 *        If the assert fails, this message starts the error message.
	 * @param errorMessagePattern
	 *        Is not allowed to be null.
	 * @param error
	 *        The {@link Throwable} whose {@link Throwable#getMessage() message} (or one of its
	 *        causes messages) has to be matched by the given {@link Pattern}. Is allowed to be
	 *        null, which causes the assert to fail.
	 */
	public static void assertErrorMessage(String message, Pattern errorMessagePattern, Throwable error) {
		for (Throwable throwable : ExceptionUtil.getCauseChain(error)) {
			if (throwable.getMessage() != null && errorMessagePattern.matcher(throwable.getMessage()).find()) {
				return;
			}
		}
		BasicTestCase.fail(optionalMessagePrefix(message) + " Expected error message matched by the regex '"
			+ errorMessagePattern.pattern() + "', but got: " + error.getMessage(), error);
	}

	/**
	 * Executes the given {@link Computation} with the given stream <code>sysOut</code> as new
	 * {@link System#out}, and the stream <code>sysErr</code> as new {@link System#err}.
	 * 
	 * @param sysOut
	 *        The new value of {@link System#out}.
	 * @param sysErr
	 *        The new value of {@link System#err}.
	 * @param callback
	 *        The computation to execute with the given streams.
	 * @return Return value of given {@link Computation}.
	 * 
	 * @see #runWithSystemOut(PrintStream, Computation)
	 * @see #runWithSystemErr(PrintStream, Computation)
	 */
	public static <T> T runWithSystemOutAndErr(PrintStream sysOut, PrintStream sysErr, Computation<T> callback) {
		PrintStream oldOut = System.out;
		System.setOut(sysOut);
		try {
			PrintStream oldErr = System.err;
			System.setErr(sysErr);
			try {
				return callback.run();
			} finally {
				System.setErr(oldErr);
			}
		} finally {
			System.setOut(oldOut);
		}
	}

	/**
	 * Executes the given {@link Computation} with the given stream <code>sysOut</code> as new
	 * {@link System#out}.
	 * 
	 * @param sysOut
	 *        The new value of {@link System#out}.
	 * @param callback
	 *        The computation to execute with the given streams.
	 * @return Return value of given {@link Computation}.
	 * 
	 * @see #runWithSystemErr(PrintStream, Computation)
	 * @see #runWithSystemOutAndErr(PrintStream, PrintStream, Computation)
	 */
	public static <T> T runWithSystemOut(PrintStream sysOut, Computation<T> callback) {
		PrintStream oldOut = System.out;
		System.setOut(sysOut);
		try {
			return callback.run();
		} finally {
			System.setOut(oldOut);
		}
	}

	/**
	 * Executes the given {@link Computation} with the given stream <code>sysErr</code> as new
	 * {@link System#err}.
	 * 
	 * @param sysErr
	 *        The new value of {@link System#err}.
	 * @param callback
	 *        The computation to execute with the given streams.
	 * @return Return value of given {@link Computation}.
	 * 
	 * @see #runWithSystemOut(PrintStream, Computation)
	 * @see #runWithSystemOutAndErr(PrintStream, PrintStream, Computation)
	 */
	public static <T> T runWithSystemErr(PrintStream sysErr, Computation<T> callback) {
		PrintStream oldErr = System.err;
		System.setErr(sysErr);
		try {
			return callback.run();
		} finally {
			System.setErr(oldErr);
		}
	}

	/**
	 * Executes the given {@link Computation} with the given <code>value</code> as system property
	 * for the <code>property</code>.
	 * 
	 * @param prop
	 *        The system property to set.
	 * @param value
	 *        The new system property value of <code>prop</code>.
	 * @param callback
	 *        The computation to execute with the given system property.
	 * @return Return value of given {@link Computation}.
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T runWithSystemProperty(String prop, String value,
			ComputationEx2<T, E1, E2> callback) throws E1, E2 {
		String oldValue = System.getProperty(prop);
		System.setProperty(prop, value);
		try {
			return callback.run();
		} finally {
			if (oldValue == null) {
				System.clearProperty(prop);
			} else {
				System.setProperty(prop, oldValue);
			}
		}
	}

	/**
	 * Returns the {@link TimeZone} with the given ID and checks that it exists.
	 */
	public static TimeZone getTimeZone(String id) {
		assertTrue("Use existing time zone for correct tests.", Arrays.asList(TimeZone.getAvailableIDs()).contains(id));
		return TimeZone.getTimeZone(id);
	}

	/**
	 * Returns a {@link TimeZone} with given offset in hours, no daylight saving.
	 */
	public static TimeZone getTimeZone(int offset) {
		return getTimeZone(offset, TimeUnit.HOURS);
	}

	/**
	 * Returns a {@link TimeZone} with given offset in given {@link TimeUnit}, no daylight saving.
	 */
	public static TimeZone getTimeZone(int offset, TimeUnit unit) {
		long millis = unit.toMillis(offset);
		if (millis > Integer.MAX_VALUE || millis < Integer.MIN_VALUE) {
			throw new IndexOutOfBoundsException();
		}
		return new SimpleTimeZone((int) millis, randomString(5, true, false, false, false));
	}

	/**
	 * Returns the {@link TimeZone} for Berlin: GMT+1 in winter time, GMT+2 in summer time.
	 */
	public static TimeZone getTimeZoneBerlin() {
		return getTimeZone("Europe/Berlin");
	}

	/**
	 * Returns the {@link TimeZone} for LA: GMT-8 in winter time, GMT-7 in summer time.
	 */
	public static TimeZone getTimeZoneLosAngeles() {
		return getTimeZone("America/Los_Angeles");
	}

	/**
	 * Returns the {@link TimeZone} for Auckland: GMT+12 in winter time, GMT+13 in summer time.
	 */
	public static TimeZone getTimeZoneAuckland() {
		return getTimeZone("Pacific/Auckland");
	}

	/**
	 * Returns the {@link TimeZone} for Samoa: GMT-11.
	 */
	public static TimeZone getTimeZoneSamoa() {
		return getTimeZone("Pacific/Samoa");
	}

	/**
	 * Executes the given {@link Execution job} with the given {@link Locale} as
	 * {@link Locale#getDefault()}.
	 */
	public static <T> T executeInDefaultLocale(Locale newLocale, Supplier<T> job) {
		/* Setting the Locale also changes the Locale's for each category. So each Locale must be
		 * saved. */
		Locale defaultLocale = Locale.getDefault();
		Locale displayLocale = Locale.getDefault(Category.DISPLAY);
		Locale formatLocale = Locale.getDefault(Category.FORMAT);
		Locale.setDefault(newLocale);
		try {
			return job.get();
		} finally {
			/* Setting default Locale must occur first because it overrides the Locale's for all
			 * categories. */
			Locale.setDefault(defaultLocale);
			Locale.setDefault(Category.DISPLAY, displayLocale);
			Locale.setDefault(Category.FORMAT, formatLocale);
		}
	}

	/**
	 * Executes the given {@link Execution job} with the given {@link Locale} as
	 * {@link SubSessionContext#getCurrentLocale()}.
	 */
	public static void executeInLocale(Locale locale, Execution job) throws Exception {
		ThreadContext sessionContext = ThreadContext.getThreadContext();
		Locale currentLocale = sessionContext.getCurrentLocale();
		try {
			sessionContext.setCurrentLocale(locale);
			job.run();
		} finally {
			sessionContext.setCurrentLocale(currentLocale);
		}
	}

	/**
	 * Executes the given {@link Execution job} with the given {@link TimeZone} as
	 * {@link SubSessionContext#getCurrentTimeZone()}.
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T executeInTimeZone(TimeZone timeZone,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		ThreadContext subsession = ThreadContext.getThreadContext();
		TimeZone currentTimeZone = subsession.getCurrentTimeZone();
		try {
			subsession.setCurrentTimeZone(timeZone);
			return job.run();
		} finally {
			subsession.setCurrentTimeZone(currentTimeZone);
		}
	}

	/**
	 * Executes the given {@link Execution job} with the given {@link TimeZone} as
	 * {@link TimeZones#getSystemTimeZone() system TimeZone}.
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T executeInSystemTimeZone(TimeZone timeZone,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		TimeZones timeZones = TimeZones.getInstance();
		Object before = ReflectionUtils.setValue(timeZones, "_systemTimeZone", timeZone);
		try {
			return job.run();
		} finally {
			ReflectionUtils.setValue(timeZones, "_systemTimeZone", before);
		}
	}

	/**
	 * If the given {@link Runnable} does not throw a {@link Throwable} of the given type, the
	 * assert {@link #fail(String)}s.
	 * 
	 * @param expectedType
	 *        Is not allowed to be null.
	 * @param thrower
	 *        Is not allowed to be null.
	 */
	public static void assertThrows(Class<? extends Throwable> expectedType, Runnable thrower) {
		assertThrows(null, expectedType, thrower);
	}

	/**
	 * If the given {@link Runnable} does not throw a {@link Throwable} of the given type, the
	 * assert {@link #fail(String)}s.
	 * 
	 * @param message
	 *        Is allowed to be null.
	 * @param expectedType
	 *        Is not allowed to be null.
	 * @param thrower
	 *        Is not allowed to be null.
	 */
	public static void assertThrows(String message, Class<? extends Throwable> expectedType, Runnable thrower) {
		String fullMessage = StringServicesShared.nonNull(message) + " Expected Throwable type: " + expectedType;
		assertThrows(fullMessage, expectedType::isInstance, thrower);
	}

	/**
	 * If the given {@link Runnable} does not throw a {@link Throwable} that matches the given
	 * {@link Predicate}, the assert {@link #fail(String)}s.
	 * 
	 * @param test
	 *        Is not allowed to be null.
	 * @param thrower
	 *        Is not allowed to be null.
	 */
	public static void assertThrows(Predicate<? super Throwable> test, Runnable thrower) {
		assertThrows(null, test, thrower);
	}

	/**
	 * If the given {@link Runnable} does not throw a {@link Throwable} that matches the given
	 * {@link Predicate}, the assert {@link #fail(String)}s.
	 * 
	 * @param message
	 *        Is allowed to be null.
	 * @param test
	 *        Is not allowed to be null.
	 * @param thrower
	 *        Is not allowed to be null.
	 */
	public static void assertThrows(String message, Predicate<? super Throwable> test, Runnable thrower) {
		try {
			thrower.run();
		} catch (Throwable throwable) {
			if (test.test(throwable)) {
				return;
			}
			fail(StringServicesShared.nonNull(message) + " The Throwable did not pass the test."
				+ " Actual Throwable: " + throwable);
		}
		fail(StringServicesShared.nonNull(message) + " Expected that something is thrown, but nothing was thrown.");
	}

}