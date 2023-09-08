/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * The class {@link TestConfigurationAbstract} tests abstract configurations.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigurationAbstract extends AbstractTypedConfigurationTestCase {

	private static final Map<String, ConfigurationDescriptor> GLOBAL_DESCRIPTORS_BY_NAME =
		Collections.singletonMap(
			"abstract-test",
			TypedConfiguration.getConfigurationDescriptor(ExampleAbstractBaseClass.Config.class));

	public abstract static class ExampleAbstractBaseClass {

		private final Config config;

		public interface Config extends PolymorphicConfiguration<ExampleAbstractBaseClass> {

			int getIntValueOne();

			int getIntValueTwo();

			int getIntValueThree();

			int getIntValueFour();

			int getIntValueFive();

			int getIntValueSix();

		}

		/**
		 * Config-constructor as required by the typed configuration.
		 */
		@SuppressWarnings("unused")
		public ExampleAbstractBaseClass(InstantiationContext context, Config config) {
			this.config = config;
		}

		public Config getConfig() {
			return config;
		}

	}

	public abstract static class ExampleAbstractSubClass extends ExampleAbstractBaseClass {

		public ExampleAbstractSubClass(InstantiationContext context, Config config) {
			super(context, config);
		}

	}

	public static class ExampleConcreteSubSubClass extends ExampleAbstractSubClass {

		public ExampleConcreteSubSubClass(InstantiationContext context, Config config) {
			super(context, config);
		}

	}

	public abstract static class ExampleAbstractConcreteAbstractClass extends ExampleAbstractSubClass {
		
		public ExampleAbstractConcreteAbstractClass(InstantiationContext context, Config config) {
			super(context, config);
		}
		
	}
	
	public static class ExampleConcreteAbstractConcreteAbstractClass extends ExampleAbstractSubClass {
		
		public ExampleConcreteAbstractConcreteAbstractClass(InstantiationContext context, Config config) {
			super(context, config);
		}
		
	}
	
	public void testDeclaredAbstract() throws Throwable {
		ConfigurationItem configUntyped = readConfiguration("declaredAbstract.xml");
		assertInstanceof(configUntyped, ExampleAbstractBaseClass.Config.class);
		ExampleAbstractBaseClass.Config config = (ExampleAbstractBaseClass.Config) configUntyped;
		assertEquals(100, config.getIntValueOne());
		assertEquals(200, config.getIntValueTwo());
		assertEquals(300, config.getIntValueThree());
		assertEquals(400, config.getIntValueFour());
		assertEquals(500, config.getIntValueFive());
		assertEquals(600, config.getIntValueSix());
		assertEquals(ExampleAbstractBaseClass.class, config.getImplementationClass());
	}

	public void testNotDeclaredAbstract() {
		Throwable actualException = null;
		try {
			readConfiguration("notDeclaredAbstract.xml");
		} catch (Throwable exception) {
			actualException = exception;
		}
		assertNotNull(actualException);
	}

	public void testOverrideDeclaredAbstract() throws Throwable {
		ConfigurationItem configUntyped = readConfigurationStacked(
			"declaredAbstract.xml",
			"overrideDeclaredAbstract.xml");
		assertInstanceof(configUntyped, ExampleAbstractBaseClass.Config.class);
		ExampleAbstractBaseClass.Config config = (ExampleAbstractBaseClass.Config) configUntyped;
		assertEquals(100, config.getIntValueOne());
		assertEquals(210, config.getIntValueTwo());
		assertEquals(300, config.getIntValueThree());
		assertEquals(400, config.getIntValueFour());
		assertEquals(500, config.getIntValueFive());
		assertEquals(600, config.getIntValueSix());
		assertEquals(ExampleAbstractBaseClass.class, config.getImplementationClass());
	}

	public void testOverrideNotDeclaredAbstract() {
		Throwable actualException = null;
		try {
			readConfigurationStacked(
				"declaredAbstract.xml",
				"overrideNotDeclaredAbstract.xml");
		} catch (Throwable exception) {
			actualException = exception;
		}
		assertNotNull(actualException);
	}

	public void testAbstractSubClassDeclaredAbstract() throws Throwable {
		ConfigurationItem configUntyped = readConfigurationStacked(
			"declaredAbstract.xml",
			"overrideDeclaredAbstract.xml",
			"abstractSubClassDeclaredAbstract.xml");
		assertInstanceof(configUntyped, ExampleAbstractBaseClass.Config.class);
		ExampleAbstractBaseClass.Config config = (ExampleAbstractBaseClass.Config) configUntyped;
		assertEquals(100, config.getIntValueOne());
		assertEquals(210, config.getIntValueTwo());
		assertEquals(320, config.getIntValueThree());
		assertEquals(400, config.getIntValueFour());
		assertEquals(500, config.getIntValueFive());
		assertEquals(600, config.getIntValueSix());
		assertEquals(ExampleAbstractSubClass.class, config.getImplementationClass());
	}

	public void testAbstractSubClassNotDeclaredAbstract() {
		Throwable actualException = null;
		try {
			readConfigurationStacked(
				"declaredAbstract.xml",
				"overrideDeclaredAbstract.xml",
				"abstractSubClassNotDeclaredAbstract.xml");
		} catch (Throwable exception) {
			actualException = exception;
		}
		assertNotNull(actualException);
	}

	public void testConcreteSubSubClassDeclaredAbstract() throws Throwable {
		ConfigurationItem configUntyped = readConfigurationStacked(
			"declaredAbstract.xml",
			"overrideDeclaredAbstract.xml",
			"abstractSubClassDeclaredAbstract.xml",
			"concreteSubSubClassDeclaredAbstract.xml");
		assertInstanceof(configUntyped, ExampleAbstractBaseClass.Config.class);
		ExampleAbstractBaseClass.Config config = (ExampleAbstractBaseClass.Config) configUntyped;
		assertEquals(100, config.getIntValueOne());
		assertEquals(210, config.getIntValueTwo());
		assertEquals(320, config.getIntValueThree());
		assertEquals(430, config.getIntValueFour());
		assertEquals(500, config.getIntValueFive());
		assertEquals(600, config.getIntValueSix());
		assertEquals(ExampleConcreteSubSubClass.class, config.getImplementationClass());
	}

	public void testConcreteSubSubClassNotDeclaredAbstract() throws Throwable {
		ConfigurationItem configUntyped = readConfigurationStacked(
			"declaredAbstract.xml",
			"overrideDeclaredAbstract.xml",
			"abstractSubClassDeclaredAbstract.xml",
			"concreteSubSubClassNotDeclaredAbstract.xml");
		assertInstanceof(configUntyped, ExampleAbstractBaseClass.Config.class);
		ExampleAbstractBaseClass.Config config = (ExampleAbstractBaseClass.Config) configUntyped;
		assertEquals(100, config.getIntValueOne());
		assertEquals(210, config.getIntValueTwo());
		assertEquals(320, config.getIntValueThree());
		assertEquals(431, config.getIntValueFour());
		assertEquals(500, config.getIntValueFive());
		assertEquals(600, config.getIntValueSix());
		assertEquals(ExampleConcreteSubSubClass.class, config.getImplementationClass());
	}

	public void testAbstractConcreteAbstractDeclaredAbstract() throws Throwable {
		ConfigurationItem configUntyped = readConfigurationStacked(
			"declaredAbstract.xml",
			"overrideDeclaredAbstract.xml",
			"abstractSubClassDeclaredAbstract.xml",
			"concreteSubSubClassNotDeclaredAbstract.xml",
			"abstractConcreteAbstractDeclaredAbstract.xml");
		assertInstanceof(configUntyped, ExampleAbstractBaseClass.Config.class);
		ExampleAbstractBaseClass.Config config = (ExampleAbstractBaseClass.Config) configUntyped;
		assertEquals(100, config.getIntValueOne());
		assertEquals(210, config.getIntValueTwo());
		assertEquals(320, config.getIntValueThree());
		assertEquals(431, config.getIntValueFour());
		assertEquals(540, config.getIntValueFive());
		assertEquals(600, config.getIntValueSix());
		assertEquals(ExampleAbstractConcreteAbstractClass.class, config.getImplementationClass());
	}

	public void testAbstractConcreteAbstractNotDeclaredAbstract() {
		Throwable actualException = null;
		try {
			readConfigurationStacked(
				"declaredAbstract.xml",
				"overrideDeclaredAbstract.xml",
				"abstractSubClassDeclaredAbstract.xml",
				"concreteSubSubClassNotDeclaredAbstract.xml",
				"abstractConcreteAbstractNotDeclaredAbstract.xml");
		} catch (Throwable exception) {
			actualException = exception;
		}
		assertNotNull(actualException);
	}

	public void testConcreteAbstractConcreteAbstractDeclaredAbstract() throws Throwable {
		ConfigurationItem configUntyped = readConfigurationStacked(
			"declaredAbstract.xml",
			"overrideDeclaredAbstract.xml",
			"abstractSubClassDeclaredAbstract.xml",
			"concreteSubSubClassNotDeclaredAbstract.xml",
			"abstractConcreteAbstractDeclaredAbstract.xml",
			"concreteAbstractConcreteAbstractDeclaredAbstract.xml");
		assertInstanceof(configUntyped, ExampleAbstractBaseClass.Config.class);
		ExampleAbstractBaseClass.Config config = (ExampleAbstractBaseClass.Config) configUntyped;
		assertEquals(100, config.getIntValueOne());
		assertEquals(210, config.getIntValueTwo());
		assertEquals(320, config.getIntValueThree());
		assertEquals(431, config.getIntValueFour());
		assertEquals(540, config.getIntValueFive());
		assertEquals(650, config.getIntValueSix());
		assertEquals(ExampleConcreteAbstractConcreteAbstractClass.class, config.getImplementationClass());
	}

	public void testConcreteAbstractConcreteAbstractNotDeclaredAbstract() throws Throwable {
		ConfigurationItem configUntyped = readConfigurationStacked(
			"declaredAbstract.xml",
			"overrideDeclaredAbstract.xml",
			"abstractSubClassDeclaredAbstract.xml",
			"concreteSubSubClassNotDeclaredAbstract.xml",
			"abstractConcreteAbstractDeclaredAbstract.xml",
			"concreteAbstractConcreteAbstractNotDeclaredAbstract.xml");
		assertInstanceof(configUntyped, ExampleAbstractBaseClass.Config.class);
		ExampleAbstractBaseClass.Config config = (ExampleAbstractBaseClass.Config) configUntyped;
		assertEquals(100, config.getIntValueOne());
		assertEquals(210, config.getIntValueTwo());
		assertEquals(320, config.getIntValueThree());
		assertEquals(431, config.getIntValueFour());
		assertEquals(540, config.getIntValueFive());
		assertEquals(651, config.getIntValueSix());
		assertEquals(ExampleConcreteAbstractConcreteAbstractClass.class, config.getImplementationClass());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return GLOBAL_DESCRIPTORS_BY_NAME;
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestConfigurationAbstract.class));
	}

}
