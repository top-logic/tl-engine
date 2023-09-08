/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.equal.EqualityByValue;

/**
 * Test case for equality of {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationEquality extends AbstractTypedConfigurationTestCase implements Scenario2 {

	public interface Example extends ConfigPart {

		int getValue();

		void setValue(int newValue);

		Example getEntry();

		void setEntry(Example newValue);

		@Container
		Example getContainer();

		PolymorphicConfiguration<CI> getAConfig();

		@InstanceFormat
		CI getA();

	}

	public static class CI extends AbstractConfiguredInstance<CI.Config<?>> {

		/**
		 * Configuration options for {@link CI}.
		 */
		public interface Config<I extends CI> extends PolymorphicConfiguration<I> {
			String getFoo();
		}

		/**
		 * Creates a {@link CI} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public CI(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

	}

	public interface ExampleEqualityByValue extends EqualityByValue {

		int getValue();

		void setValue(int value);
	}

	public void testEqualityByValue() {
		ExampleEqualityByValue e1 = TypedConfiguration.newConfigItem(ExampleEqualityByValue.class);
		ExampleEqualityByValue e2 = TypedConfiguration.newConfigItem(ExampleEqualityByValue.class);

		assertTrue("Newly created objects must be equal.", e1.equals(e2));
		assertEquals("Equal objects must have same hashCode.", e1.hashCode(), e2.hashCode());

		e1.setValue(15);
		assertFalse("Value of one property are not equal.", e1.equals(e2));

		e2.setValue(15);
		assertTrue("All properties are equal.", e1.equals(e2));
		assertEquals("Equal objects must have same hashCode.", e1.hashCode(), e2.hashCode());

		assertFalse("Comparison with null must not fail.", e1.equals(null));
		assertFalse("Comparison with non configItem must not fail.", e1.equals(new Object()));
	}

	/**
	 * Tests {@link ConfigurationItem} methods inherited from {@link Object}.
	 */
	public void testEquals() {
		A a1 = TypedConfiguration.newConfigItem(A.class);
		A a2 = TypedConfiguration.newConfigItem(A.class);
		assertNotSame(a1, a2);
		assertEquals(a1, a2);
		{
			B b1 = TypedConfiguration.newConfigItem(B.class);
			b1.setX(42);
			
			a1.setParent(b1);
		}
		assertNotEquals(a2, a1);
		BasicTestCase.assertNotEquals(a2.toString(), a1.toString());
		
		{
			B b2 = TypedConfiguration.newConfigItem(B.class);
			b2.setX(43);
			
			a2.setParent(b2);
		}
		assertNotEquals(a2, a1);
		BasicTestCase.assertNotEquals(a2.toString(), a1.toString());
		
		((B) a2.getParent()).setX(42);
		assertEquals(a2, a1);
		assertEquals(a2.toString(), a1.toString());
	}

	public void testContainerEqual() {
		Example firstContainer = createContainerWithEntry();
		Example secondContainer = createContainerWithEntry();

		try {
			assertFalse(firstContainer.equals(secondContainer));
			assertFalse(firstContainer.getEntry().equals(secondContainer.getEntry()));
			assertEquals(firstContainer, secondContainer);
			assertEquals(firstContainer.getEntry(), secondContainer.getEntry());
		} catch (StackOverflowError ex) {
			String message = "When a ConfigItem has a container property, the equals() method causes a stackoverflow.";
			BasicTestCase.fail(message, ex);
		}
	}

	public void testContainerEqualNull() {
		Example firstContainer = createContainerWithEntry();
		Example secondContainer = createContainerWithEntry();

		Example secondEntry = secondContainer.getEntry();
		secondContainer.setEntry(null);

		assertEquals(firstContainer.getEntry(), secondEntry);
		assertEquals(secondEntry, firstContainer.getEntry());
	}

	public void testContainerNotEqual() {
		Example firstContainer = createContainerWithEntry();
		firstContainer.setValue(1);
		Example secondContainer = createContainerWithEntry();
		secondContainer.setValue(2);

		try {
			assertNotEquals(firstContainer, secondContainer);
			assertEquals(firstContainer.getEntry(), secondContainer.getEntry());
		} catch (StackOverflowError ex) {
			String message = "When a ConfigItem has a container property, the equals() method causes a stackoverflow.";
			BasicTestCase.fail(message, ex);
		}
	}

	public void testPolymorphicConfigurationEquals() throws ConfigurationException {
		ConfigurationItem c1 =
			read("<example><a-config class='" + CI.class.getName() + "'/></example>");
		ConfigurationItem c2 =
			read("<example><a-config class='" + CI.class.getName() + "'/></example>");

		assertNotSame(((Example) c1).getAConfig(), ((Example) c2).getAConfig());
		assertEquals(c1, c2);
	}

	public void testConfiguredInstanceEquals() throws ConfigurationException {
		ConfigurationItem c1 =
			read("<example><a class='" + CI.class.getName() + "'/></example>");
		ConfigurationItem c2 =
			read("<example><a class='" + CI.class.getName() + "'/></example>");
		assertNotSame(((Example) c1).getA(), ((Example) c2).getA());
		assertEquals(c1, c2);
	}

	public void testConfiguredInstanceNotEquals() throws ConfigurationException {
		ConfigurationItem c1 =
			read("<example><a class='" + CI.class.getName() + "'/></example>");
		ConfigurationItem c2 =
			read("<example><a class='" + CI.class.getName() + "' foo='bar'/></example>");

		assertNotSame(((Example) c1).getA(), ((Example) c2).getA());
		assertNotEquals(c1, c2);
	}

	public void testContainerHashCode() {
		Example firstContainer = createContainerWithEntry();

		try {
			firstContainer.hashCode();
			firstContainer.getEntry().hashCode();
		} catch (StackOverflowError ex) {
			String message = "When a ConfigItem has a container property, the hashCode() method causes a stackoverflow.";
			BasicTestCase.fail(message, ex);
		}
	}

	private Example createContainerWithEntry() {
		Example container = create(Example.class);
		Example entry = create(Example.class);
		container.setEntry(entry);
		return container;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("example", TypedConfiguration.getConfigurationDescriptor(Example.class));
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfigurationEquality.class);
	}

}
