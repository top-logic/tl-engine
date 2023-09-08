/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.ControlFlowAssertion;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Test case for {@link ConfigUtil}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigUtil extends BasicTestCase {

	/**
	 * Singleton class.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class A {
		public static final Object INSTANCE = new A();
		
		protected A() {
			// Singleton constructor.
		}
	}
	
	/**
	 * Non-singleton class.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class B extends A {
		public B() {
			super();
		}
	}
	
	/**
	 * Non-singleton class.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class F {
		public F() {
			super();
		}
	}
	
	/**
	 * Singleton class.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class C {
		private static C singletonInstance;
		
		public static C getInstance() {
			if (singletonInstance == null) {
				singletonInstance = new C();
			}
			return singletonInstance;
		}
	}

	/**
	 * Class without default constructor.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class D {
		private final int x;

		public D(int x) {
			this.x = x;
			
		}
		
		public int getX() {
			return x;
		}
	}

	/**
	 * Class without default constructor.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class E {
		
		private final String x;

		public E(String x) {
			this.x = x;
			
		}
		
		public String getX() {
			return x;
		}
	}

	/**
	 * Marker interface
	 */
	public static interface X {
		/* Pure Marker Interface */
	}
	
	/**
	 * Implementation of X
	 */
	public static class Y implements X {
		public static final X INSTANCE = new Y();
	}
	
	/**
	 * Extension of Y, but declaring INSTANCE as of interface X.
	 */
	public static class Z extends Y {
		public static final X INSTANCE = new Y();
	}
	
	/**
	 * Extension of Y, but declaring getInstance() as of interface X and returning an instance of Y (and not of ZZ) 
	 */
	public static class ZZ extends Y {
		private static X singleton;
		
		public static X getInstance() {
			if (singleton == null) {
				singleton = new Y();
			}
			return singleton;
		}
	}
	
	private static final Object DEFAULT = new NamedConstant("DEFAULT");

	private static final Class<?>[] INT_PARAM = { int.class };
	private static final Object[] INT_ARG = {Integer.valueOf(42)};

	public void testGetClassForNameMandatory() throws ConfigurationException {
		assertEquals(A.class, ConfigUtil.getClassForNameMandatory(Object.class, "name", A.class.getName()));
		assertEquals(A.class, ConfigUtil.getClassForName(Object.class, "name", A.class.getName(), B.class));
		assertEquals(B.class, ConfigUtil.getClassForName(Object.class, "name", "", B.class));
		assertNull(ConfigUtil.getClassForName(Object.class, "name", "", null));
		
		try {
			ConfigUtil.getClassForNameMandatory(Object.class, "name", null);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		
		try {
			ConfigUtil.getClassForNameMandatory(Object.class, "name", "");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
	}


	public void testGetInstanceMandatory() throws ConfigurationException {
		Object instance1 = ConfigUtil.getNewInstanceMandatory(Object.class, "name", B.class.getName());
		
		assertNotNull(instance1);
		assertNotEquals(A.INSTANCE, instance1);
		assertInstanceof(instance1, B.class);
		
		Object instance2 = ConfigUtil.getInstanceMandatory(Object.class, "name", B.class.getName());
		assertNotNull(instance2);
		assertNotEquals(A.INSTANCE, instance2);
		assertInstanceof(instance2, B.class);
		
		Object instance3 = ConfigUtil.getInstanceMandatory(Object.class, "name", A.class.getName());
		assertNotNull(instance3);
		assertEquals(A.INSTANCE, instance3);
		
		try {
			ConfigUtil.getNewInstanceMandatory(Object.class, "name", "");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		
		try {
			ConfigUtil.getNewInstanceMandatory(Object.class, "name", null);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		
		try {
			ConfigUtil.getNewInstanceMandatory(Object.class, "name", A.class.getName());
			fail("Constructor not accessible.");
		} catch (ConfigurationException ex) {/*expected*/}
	}
		
	public void testGetInstanceWithClassDefault() throws ConfigurationException {
		Object aInstance = ConfigUtil.getNewInstanceWithClassDefault(Object.class, "name", F.class.getName(), B.class);
		
		assertNotNull(aInstance);
		assertInstanceof(aInstance, F.class);
		
		Object bInstance1 = ConfigUtil.getNewInstanceWithClassDefault(Object.class, "name", "", B.class);
		
		assertNotNull(bInstance1);
		assertInstanceof(bInstance1, B.class);
		
		Object bInstance2 = ConfigUtil.getNewInstanceWithClassDefault(Object.class, "name", null, B.class);
		
		assertNotNull(bInstance2);
		assertInstanceof(bInstance2, B.class);

		try {
			ConfigUtil.getNewInstanceWithClassDefault(Object.class, "name", "", A.class);
			fail("A is a singleton class.");
		} catch (ConfigurationException ex) {/*expected*/}
		
		Object aInstance3 = ConfigUtil.getInstanceWithClassDefault(Object.class, "name", B.class.getName(), A.class);
		assertNotNull(aInstance3);
		assertInstanceof(aInstance3, B.class);
		
		Object aInstance4 = ConfigUtil.getInstanceWithClassDefault(Object.class, "name", "", A.class);
		assertNotNull(aInstance4);
		assertEquals(A.INSTANCE, aInstance4);
		
		try {
			ConfigUtil.getNewInstanceWithClassDefault(Object.class, "name", "", null);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		
		try {
			ConfigUtil.getNewInstanceWithClassDefault(Object.class, "name", null, null);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		
		// test expected return type
		X x = ConfigUtil.getInstanceWithClassDefault(X.class, "name", Z.class.getName(), Z.class);
		Y y = ConfigUtil.getInstanceWithClassDefault(Y.class, "name", Z.class.getName(), Z.class);
		assertSame(x, y);
		assertSame(x, Z.INSTANCE);
		try {
			ConfigUtil.getInstanceWithClassDefault(Z.class, "name", Z.class.getName(), Z.class);
		} catch (ConfigurationException ex) {/*expected*/}
		
		// test expected return type
		x = ConfigUtil.getInstanceWithClassDefault(X.class, "name", ZZ.class.getName(), ZZ.class);
		y = ConfigUtil.getInstanceWithClassDefault(Y.class, "name", ZZ.class.getName(), ZZ.class);
		assertSame(x, y);
		assertSame(x, ZZ.getInstance());
		try {
			ConfigUtil.getInstanceWithClassDefault(ZZ.class, "name", ZZ.class.getName(), ZZ.class);
		} catch (ConfigurationException ex) {/*expected*/}
		
	}

	public void testGetInstanceWithInstanceDefault() throws ConfigurationException {
		Object aInstance = ConfigUtil.getNewInstanceWithInstanceDefault(Object.class, "name", B.class.getName(), DEFAULT);
		
		assertNotNull(aInstance);
		assertNotEquals(DEFAULT, aInstance);
		assertInstanceof(aInstance, B.class);
		
		Object bInstance1 = ConfigUtil.getNewInstanceWithInstanceDefault(Object.class, "name", "", DEFAULT);
		
		assertNotNull(bInstance1);
		assertEquals(bInstance1, DEFAULT);
		
		Object bInstance2 = ConfigUtil.getNewInstanceWithInstanceDefault(Object.class, "name", null, DEFAULT);
		
		assertNotNull(bInstance2);
		assertEquals(bInstance2, DEFAULT);

		assertNull(ConfigUtil.getNewInstanceWithInstanceDefault(Object.class, "name", null, null));
		assertNull(ConfigUtil.getNewInstanceWithInstanceDefault(Object.class, "name", "", null));
		
		assertEquals(A.INSTANCE, ConfigUtil.getInstanceWithInstanceDefault(Object.class, "name", A.class.getName(), DEFAULT));
		assertInstanceof(ConfigUtil.getInstanceWithInstanceDefault(Object.class, "name", B.class.getName(), DEFAULT), B.class);
		assertEquals(DEFAULT, ConfigUtil.getInstanceWithInstanceDefault(Object.class, "name", "", DEFAULT));
	}
	
	public void testGetInstanceSpecialConstructor() throws ConfigurationException {
		D dInstance = (D) ConfigUtil.getNewInstance(Object.class, "name", D.class.getName(), null, INT_PARAM, INT_ARG);
		assertEquals(((Integer) INT_ARG[0]).intValue(), dInstance.getX());
		
		try {
			ConfigUtil.getNewInstance(Object.class, "name", "", null, INT_PARAM, INT_ARG);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}

		try {
			ConfigUtil.getNewInstance(Object.class, "name", null, null, INT_PARAM, INT_ARG);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		
		try {
			ConfigUtil.getNewInstance(Object.class, "name", E.class.getName(), D.class, INT_PARAM, INT_ARG);
			fail("Wrong signature.");
		} catch (ConfigurationException ex) {/*expected*/}
	}

	public void testGetSingletonMandatory() throws ConfigurationException {
		assertEquals(A.INSTANCE, ConfigUtil.getSingletonMandatory(Object.class, "name", A.class.getName()));
		assertEquals(C.getInstance(), ConfigUtil.getSingletonMandatory(Object.class, "name", C.class.getName()));
		try {
			ConfigUtil.getSingletonMandatory(Object.class, "name", "");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		try {
			ConfigUtil.getSingletonMandatory(Object.class, "name", null);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		try {
			ConfigUtil.getSingletonMandatory(Object.class, "name", B.class.getName());
			fail("Not a singleton class.");
		} catch (ConfigurationException ex) {/*expected*/}
		try {
			ConfigUtil.getSingletonMandatory(Object.class, "name", F.class.getName());
			fail("Not a singleton class.");
		} catch (ConfigurationException ex) {/*expected*/}
	}
	
	public void testGetSingletonWithClassDefault() throws ConfigurationException {
		assertEquals(A.INSTANCE, ConfigUtil.getSingletonWithClassDefault(Object.class, "name", A.class.getName(), C.class));
		assertEquals(C.getInstance(), ConfigUtil.getSingletonWithClassDefault(Object.class, "name", C.class.getName(), A.class));
		
		assertEquals(C.getInstance(), ConfigUtil.getSingletonWithClassDefault(Object.class, "name", "", C.class));
		assertEquals(A.INSTANCE, ConfigUtil.getSingletonWithClassDefault(Object.class, "name", null, A.class));
		
		try {
			ConfigUtil.getSingletonWithClassDefault(Object.class, "name", "", null);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
		
		try {
			ConfigUtil.getSingletonWithClassDefault(Object.class, "name", null, null);
			fail();
		} catch (ConfigurationException ex) {/*expected*/}

		ControlFlowAssertion unreachable = new ControlFlowAssertion();
		try {
			ConfigUtil.getSingletonWithClassDefault(Object.class, "name", null, B.class);
			unreachable.touch();
		} catch (AssertionError ex) {/*expected*/}
		
		try {
			assertEquals(C.getInstance(), ConfigUtil.getSingletonWithClassDefault(Object.class, "name", "", F.class));
			unreachable.touch();
		} catch (AssertionError ex) {/*expected*/}
		
		unreachable.assertNotReached("No singleton classes.");
	}
	
	public void testGetSingletonWithInstanceDefault() throws ConfigurationException {
		assertEquals(A.INSTANCE, ConfigUtil.getSingletonWithInstanceDefault(Object.class, "name", A.class.getName(), C.getInstance()));
		assertEquals(C.getInstance(), ConfigUtil.getSingletonWithInstanceDefault(Object.class, "name", C.class.getName(), A.INSTANCE));
		
		assertEquals(C.getInstance(), ConfigUtil.getSingletonWithInstanceDefault(Object.class, "name", "", C.getInstance()));
		assertEquals(A.INSTANCE, ConfigUtil.getSingletonWithInstanceDefault(Object.class, "name", null, A.INSTANCE));
		
		assertNull(ConfigUtil.getSingletonWithInstanceDefault(Object.class, "name", "", null));
		assertNull(ConfigUtil.getSingletonWithInstanceDefault(Object.class, "name", null, null));
	}

	public void testGetDouble() throws ConfigurationException {
		assertEquals(Double.valueOf(42.13), ConfigUtil.getDouble(Collections.singletonMap("name", "42.13"), "name"));
		assertEquals(42.13, ConfigUtil.getDoubleValue(Collections.singletonMap("name", "42.13"), "name"));
		assertEquals(42.13, ConfigUtil.getDoubleValue(Collections.emptyMap(), "name", 42.13));
		assertEquals(42.13, ConfigUtil.getDoubleValue("name", "42.13"), 0.0);
		assertEquals(42.13, ConfigUtil.getDoubleValue("name", "42.13", 13.42), 0.0);
		assertEquals(42.13, ConfigUtil.getDoubleValue("name", "", 42.13), 0.0);
		assertEquals(Double.valueOf(42.13), ConfigUtil.getDouble("name", "42.13"));
		assertNull(ConfigUtil.getDouble("name", ""));
		assertNull(ConfigUtil.getDouble("name", null));
		
		try {
			ConfigUtil.getDouble("name", "illegal");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
	}

	public void testGetInteger() throws ConfigurationException {
		assertEquals(Integer.valueOf(42), ConfigUtil.getInteger(Collections.singletonMap("name", "42"), "name"));
		assertEquals(42, ConfigUtil.getIntValue(Collections.singletonMap("name", "42"), "name"));
		assertEquals(42, ConfigUtil.getIntValue(Collections.emptyMap(), "name", 42));
		assertEquals(42, ConfigUtil.getIntValue("name", "42"));
		assertEquals(42, ConfigUtil.getIntValue("name", "42", 13));
		assertEquals(42, ConfigUtil.getIntValue("name", "", 42));
		assertEquals(Integer.valueOf(42), ConfigUtil.getInteger("name", "42"));
		assertNull(ConfigUtil.getInteger("name", ""));
		assertNull(ConfigUtil.getInteger("name", null));
		
		try {
			ConfigUtil.getInteger("name", "illegal");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
	}

	public void testGetLong() throws ConfigurationException {
		assertEquals(Long.valueOf(42L), ConfigUtil.getLong(Collections.singletonMap("name", "42"), "name"));
		assertEquals(42L, ConfigUtil.getLongValue(Collections.singletonMap("name", "42"), "name"));
		assertEquals(42L, ConfigUtil.getLongValue(Collections.emptyMap(), "name", 42L));
		assertEquals(42L, ConfigUtil.getLongValue("name", "42"));
		assertEquals(42L, ConfigUtil.getLongValue("name", "42", 13));
		assertEquals(13L, ConfigUtil.getLongValue("name", "", 13));
		assertEquals(Long.valueOf(42), ConfigUtil.getLong("name", "42"));
		assertNull(ConfigUtil.getLong("name", ""));
		assertNull(ConfigUtil.getLong("name", null));
		
		try {
			ConfigUtil.getLong("name", "illegal");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
	}

	public void testGetFloat() throws ConfigurationException {
		assertEquals(Float.valueOf(42.13f), ConfigUtil.getFloat(Collections.singletonMap("name", "42.13"), "name"));
		assertEquals(42.13f, ConfigUtil.getFloatValue(Collections.singletonMap("name", "42.13"), "name"));
		assertEquals(42.13f, ConfigUtil.getFloatValue(Collections.emptyMap(), "name", 42.13f));
		assertEquals(42.13f, ConfigUtil.getFloatValue("name", "42.13"), 0.0);
		assertEquals(42.13f, ConfigUtil.getFloatValue("name", "42.13", 13.42f), 0.0);
		assertEquals(13.42f, ConfigUtil.getFloatValue("name", "", 13.42f), 0.0);
		assertEquals(Float.valueOf(42.13f), ConfigUtil.getFloat("name", "42.13"));
		assertNull(ConfigUtil.getFloat("name", ""));
		assertNull(ConfigUtil.getFloat("name", null));
		
		try {
			ConfigUtil.getFloat("name", "illegal");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
	}

	public void testGetByte() throws ConfigurationException {
		byte byte42 = (byte) 42;
		assertEquals(Byte.valueOf(byte42), ConfigUtil.getByte(Collections.singletonMap("name", "42"), "name"));
		assertEquals(byte42, ConfigUtil.getByteValue(Collections.singletonMap("name", "42"), "name"));
		assertEquals(byte42, ConfigUtil.getByteValue(Collections.emptyMap(), "name", byte42));
		assertEquals(42, ConfigUtil.getByteValue("name", "42"));
		assertEquals(42, ConfigUtil.getByteValue("name", "42", (byte) 13));
		assertEquals(13, ConfigUtil.getByteValue("name", "", (byte) 13));
		assertEquals(Byte.valueOf(byte42), ConfigUtil.getByte("name", "42"));
		assertNull(ConfigUtil.getByte("name", ""));
		assertNull(ConfigUtil.getByte("name", null));
		
		try {
			ConfigUtil.getByte("name", "illegal");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
	}

	public void testGetShort() throws ConfigurationException {
		short short42 = (short) 42;
		assertEquals(Short.valueOf(short42), ConfigUtil.getShort(Collections.singletonMap("name", "42"), "name"));
		assertEquals(short42, ConfigUtil.getShortValue(Collections.singletonMap("name", "42"), "name"));
		assertEquals(short42, ConfigUtil.getShortValue(Collections.emptyMap(), "name", short42));
		assertEquals(42, ConfigUtil.getShortValue("name", "42"));
		assertEquals(42, ConfigUtil.getShortValue("name", "42", (short) 13));
		assertEquals(13, ConfigUtil.getShortValue("name", "", (short) 13));
		assertEquals(Short.valueOf(short42), ConfigUtil.getShort("name", "42"));
		assertNull(ConfigUtil.getShort("name", ""));
		assertNull(ConfigUtil.getShort("name", null));
		
		try {
			ConfigUtil.getShort("name", "illegal");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
	}

	public void testGetCharacter() throws ConfigurationException {
		assertEquals(Character.valueOf('a'), ConfigUtil.getCharacter(Collections.singletonMap("name", "a"), "name"));
		assertEquals('a', ConfigUtil.getCharValue(Collections.singletonMap("name", "a"), "name"));
		assertEquals('a', ConfigUtil.getCharValue(Collections.emptyMap(), "name", 'a'));
		assertEquals('a', ConfigUtil.getCharValue("name", "a"));
		assertEquals('a', ConfigUtil.getCharValue("name", "a", 'b'));
		assertEquals('b', ConfigUtil.getCharValue("name", "", 'b'));
		assertEquals(Character.valueOf('a'), ConfigUtil.getCharacter("name", "a"));
		assertNull(ConfigUtil.getCharacter("name", ""));
		assertNull(ConfigUtil.getCharacter("name", null));
		
		try {
			ConfigUtil.getCharacter("name", "ab");
			fail();
		} catch (ConfigurationException ex) {/*expected*/}
	}

	public void testGetBoolean() throws ConfigurationException {
		assertEquals(Boolean.TRUE, ConfigUtil.getBoolean(Collections.singletonMap("name", "true"), "name"));
		assertEquals(Boolean.FALSE, ConfigUtil.getBoolean(Collections.singletonMap("name", "false"), "name"));
		assertEquals(true, ConfigUtil.getBooleanValue(Collections.singletonMap("name", "true"), "name"));
		assertEquals(false, ConfigUtil.getBooleanValue(Collections.singletonMap("name", "false"), "name"));
		assertEquals(true, ConfigUtil.getBooleanValue(Collections.emptyMap(), "name", true));
		assertEquals(false, ConfigUtil.getBooleanValue(Collections.emptyMap(), "name", false));

		assertTrue(ConfigUtil.getBooleanValue("name", "true"));
		assertFalse(ConfigUtil.getBooleanValue("name", "false"));
		
		assertTrue(ConfigUtil.getBooleanValue("name", "true", false));
		assertFalse(ConfigUtil.getBooleanValue("name", "false", true));
		
		assertFalse(ConfigUtil.getBooleanValue("name", "", false));
		assertTrue(ConfigUtil.getBooleanValue("name", "", true));
		
		assertEquals(Boolean.TRUE, ConfigUtil.getBoolean("name", "true"));
		assertEquals(Boolean.FALSE, ConfigUtil.getBoolean("name", "false"));
		
		assertNull(ConfigUtil.getBoolean("name", ""));
		assertNull(ConfigUtil.getBoolean("name", null));
		
		
		String[] illegalValues = {"t", "T", "True", "y", "yes", "Yes", "n", "no", "No", "f", "F", "False"};
		for (Iterator<String> it = Arrays.asList(illegalValues).iterator(); it.hasNext();) {
			String value = it.next();
			try {
				ConfigUtil.getBoolean("name", value);
				fail();
			} catch (ConfigurationException ex) {/*expected*/}
			try {
				ConfigUtil.getBooleanValue("name", value, true);
				fail();
			} catch (ConfigurationException ex) {/*expected*/}
		}

	}


	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestConfigUtil.class));
	}

}
