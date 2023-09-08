/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * The class {@link TestRecursiveConfigDefinitions} tests that recursive
 * relationships between {@link ConfigurationItem} are possible.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestRecursiveConfigDefinitions extends TestCase {

	public interface UnusualPropName extends ConfigurationItem {

		String IDENTIFIER_PROPERTY_NAME = "unusualPropertyName";

		@Name(IDENTIFIER_PROPERTY_NAME)
		String getIdentifier();

		@Key(IDENTIFIER_PROPERTY_NAME)
		Map<String, UnusualPropName> getAs();
	}

	public interface A extends ConfigurationItem {

		List<B> getB();

	}

	public interface AExtension extends A {

		public static final String IDENTIFIER_PROP_NAME = "identifier";

		@Name(IDENTIFIER_PROP_NAME)
		String getIdentifier();

	}

	public interface B extends ConfigurationItem {

		@Key(AExtension.IDENTIFIER_PROP_NAME)
		Map<String, AExtension> getAExtension();

	}
	
	public interface C extends ConfigurationItem {
		@Key(D.IDENTIFIER_PROP_NAME)
		Map<String, D> getDs();
	}

	public interface D extends ConfigurationItem {
		String IDENTIFIER_PROP_NAME = "identifier";
		@Name(IDENTIFIER_PROP_NAME)
		String getIdentifier();
	}
	
	public interface E extends PolymorphicConfiguration<Object> {
		static final String DEFAULT_IDENTIFIER = "default";
		
		String getIdentifier();
		void setIdentifier(String id);
		
		static final int INT_DEFAULT_VALUE = 666;

		@IntDefault(INT_DEFAULT_VALUE)
		Integer getIntDefault();
	}

	public interface F extends ConfigurationItem {

		@InstanceDefault(EImpl.class)
		@InstanceFormat
		EImpl getE();

	}

	public static class EImpl {

		static final String INDEX_MUST_NOT_BE_NULL = "Index must not be null.";

		public EImpl(InstantiationContext context, E config) throws ConfigurationException {
			assertEquals("Ticket #11659: Default value of property unresolved.", Integer.valueOf(E.INT_DEFAULT_VALUE),
				config.getIntDefault());
		}
	}
	
	public void testDefaultValue() {
		TypedConfiguration.newConfigItem(F.class);
	}

	public void testSimpleReference() {
		TypedConfiguration.newConfigItem(C.class);
	}
	
	public void testRecursion() {
		TypedConfiguration.newConfigItem(AExtension.class);
	}
	
	/**
	 * Tests that a name for a property can be "unusual", and the property can
	 * be used as key property
	 */
	public void testUnusualKeyPropertyName() {
		TypedConfiguration.newConfigItem(UnusualPropName.class);
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestRecursiveConfigDefinitions.class);
	}
}
