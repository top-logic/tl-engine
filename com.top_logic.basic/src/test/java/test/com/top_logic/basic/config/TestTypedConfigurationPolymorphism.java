/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * Test case for {@link TypedConfiguration} testing configuration interface inheritance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTypedConfigurationPolymorphism extends BasicTestCase {
	
	public abstract static class A {
		// Base configuration.
		public interface Config extends ConfigurationItem {
			
			@ClassDefault(A.class)
			@Name("class")
			Class getImplClass();
			
		}
	}
	
	public static class B extends A {
		// Simple extension of A
		public interface Config extends A.Config {
			
			@Override
			@ClassDefault(B.class)
			Class getImplClass();

			int X_DEFAULT_VALUE = 31;
			@IntDefault(X_DEFAULT_VALUE)
			int getX();
			
		}
	}
	
	public static class C extends A {
		// Simple extension of A
		public interface Config extends A.Config {

			@Override
			@ClassDefault(C.class)
			Class getImplClass();
			
			int Y_DEFAULT_VALUE = 42;
			@IntDefault(Y_DEFAULT_VALUE)
			int getY();
			
		}
	}

	public static class D extends A {
		// Invalid declaration with ambiguous declaration of implementation class.
		public interface Config extends B.Config, C.Config {
			
			@IntDefault(7)
			int getZ();
		}
	}
	
	public static class E extends A {
		// Multiple inheritance by making ambiguous declarations unambiguous.
		public interface Config extends B.Config, C.Config {
			@Override
			@ClassDefault(E.class)
			Class getImplClass();
		}
	}

	public void testAConfig() {
		A.Config config = TypedConfiguration.newConfigItem(A.Config.class);
		assertEquals(A.class, config.getImplClass());
		checkImplClassProperty(config);
	}

	public void testBConfig() {
		B.Config config = TypedConfiguration.newConfigItem(B.Config.class);
		assertEquals(B.Config.X_DEFAULT_VALUE, config.getX());
		assertEquals(B.class, config.getImplClass());
		checkImplClassProperty(config);
	}
	
	public void testCConfig() {
		C.Config config = TypedConfiguration.newConfigItem(C.Config.class);
		assertEquals(C.Config.Y_DEFAULT_VALUE, config.getY());
		assertEquals(C.class, config.getImplClass());
		checkImplClassProperty(config);
	}

	/**
	 * It is to complex to check conflicts in default values in different super interfaces.
	 * 
	 * <p>
	 * Due to configured defaults (Ticket 14459), the default values in the
	 * {@link PropertyDescriptor} are changed after programmatic reading of configuration interface.
	 * If configuration is "invalid" (i.e. default values does not match) this problem can only be
	 * solved by changing the configuration interface. This is not acceptable. Therefore only a
	 * warning is logged.
	 * </p>
	 */
	public void _testDConfig() {
		try {
			TypedConfiguration.newConfigItem(D.Config.class);
			fail("Invalid configuration not detected: Ambiguous default value for getImplClass().");
		} catch (Exception e) {
			// Expected.
			Logger.debug("Config creation rejected.", e, TestTypedConfigurationPolymorphism.class);
		}
	}
	
	public void testEConfig() {
		E.Config config = TypedConfiguration.newConfigItem(E.Config.class);
		assertEquals(E.Config.X_DEFAULT_VALUE, config.getX());
		assertEquals(E.Config.Y_DEFAULT_VALUE, config.getY());
		assertEquals(E.class, config.getImplClass());
		checkImplClassProperty(config);
	}
	
	private void checkImplClassProperty(A.Config config) {
		PropertyDescriptor implClassProperty = config.descriptor().getProperty("class");
		assertNotNull(implClassProperty);
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTypedConfigurationPolymorphism.class));
	}

}
