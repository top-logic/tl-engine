/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Test case for {@link Format} annotations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestFormat extends TestCase {

	public interface FormatAnnotatedAtReturnType extends ConfigurationItem {

		String ANNOTATED_CLASS_DEFAULT_ = "XXXX";

		@FormattedDefault(ANNOTATED_CLASS_DEFAULT_)
		FormatAnnotatedClass getAnnotatedFormat();

		@Format(FormatAnnotatedClassFormat.class)
		public class FormatAnnotatedClass {

			private final String _specification;

			public FormatAnnotatedClass(String specification) {
				_specification = specification;
			}

			public String getSpecification() {
				return _specification;
			}

		}

		public class FormatAnnotatedClassFormat extends AbstractConfigurationValueProvider<FormatAnnotatedClass> {

			/** Singleton {@link FormatAnnotatedClassFormat} instance. */
			public static final FormatAnnotatedClassFormat INSTANCE = new FormatAnnotatedClassFormat();

			private FormatAnnotatedClassFormat() {
				super(FormatAnnotatedClass.class);
			}

			@Override
			protected FormatAnnotatedClass getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				return new FormatAnnotatedClass(propertyValue.toString());
			}

			@Override
			protected String getSpecificationNonNull(FormatAnnotatedClass configValue) {
				return configValue.getSpecification();
			}

		}

	}

	@NoImplementationClassGeneration
	public interface FormatTooSpecific extends ConfigurationItem {


		@Format(BarFormat.class)
		Foo getFoo();

		void setFoo(Foo value);

		public class Foo {
			// Empty.
		}

		public class Bar extends Foo {
			// Empty.
		}

		public class BarFormat extends AbstractConfigurationValueProvider<Bar> {

			/**
			 * Singleton {@link TestFormat.FormatTooSpecific.BarFormat} instance.
			 */
			public static final BarFormat INSTANCE = new BarFormat();

			private BarFormat() {
				super(Bar.class);
			}

			@Override
			protected Bar getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				if ("bar".equals(propertyValue.toString())) {
					return new Bar();
				} else {
					throw new ConfigurationException("Cannot parse '" + propertyValue + "' as Bar.");
				}
			}

			@Override
			protected String getSpecificationNonNull(Bar configValue) {
				return "bar";
			}

		}

	}

	public void testDetectFormatTypeIncompatibility() throws XMLStreamException {
		FormatTooSpecific config;
		try {
			config = TypedConfiguration.newConfigItem(FormatTooSpecific.class);

			// If configuration would be instantiated, a legal configuration could not be
			// serialized:
			config.setFoo(new FormatTooSpecific.Foo());
			try (ConfigurationWriter w = new ConfigurationWriter(new StringWriter())) {
				w.write("config", FormatTooSpecific.class, config);
			}

			// The prove that the invalid format annotation can lead to problems does not work. This
			// should never be reached.
			fail("Something is really wrong, if this is reached.");
		} catch (IllegalArgumentException ex) {
			// Expected. Invalid format annotation.
			BasicTestCase.assertContains(FormatTooSpecific.BarFormat.class.getName(), ex.getMessage());
		}
	}

	public void testHasCorrectFormat() {
		FormatAnnotatedAtReturnType config = TypedConfiguration.newConfigItem(FormatAnnotatedAtReturnType.class);
		FormatAnnotatedAtReturnType.FormatAnnotatedClass annotatedClassInstance = config.getAnnotatedFormat();
		assertNotNull(annotatedClassInstance);
		assertEquals(FormatAnnotatedAtReturnType.ANNOTATED_CLASS_DEFAULT_, annotatedClassInstance.getSpecification());
	}

}
