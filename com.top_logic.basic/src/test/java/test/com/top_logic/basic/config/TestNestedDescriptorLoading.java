/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;

/**
 * Regression test for {@link ConfigurationDescriptor} loading.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestNestedDescriptorLoading extends TestCase {

	public interface A extends ConfigurationItem {

		/**
		 * Looks like an item property, but is a plain formatted value, with implementations that
		 * are {@link ConfigurationItem}s by accident.
		 */
		@Format(B.Format.class)
		B getB();

	}

	public interface B extends ConfigurationItem {

		int getValue();

		void setValue(int value);

		public class Format extends AbstractConfigurationValueProvider<B> {

			/**
			 * Workaround for not being able to instantiate configuration items with unfinished
			 * descriptors.
			 */
			static class Default {

				/**
				 * The shared default value for all {@link B} values.
				 */
				static final B NULL;

				static {
					NULL = TypedConfiguration.newConfigItem(B.class);
					NULL.setValue(42);
				}

			}

			/**
			 * Singleton {@link TestNestedDescriptorLoading.B.Format} instance.
			 */
			public static final Format INSTANCE = new Format();

			private Format() {
				super(B.class);
			}

			@Override
			protected B getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
				B result = TypedConfiguration.newConfigItem(B.class);
				result.setValue(Integer.parseInt(propertyValue.toString()));
				return result;
			}

			@Override
			protected String getSpecificationNonNull(B configValue) {
				return Integer.toString(configValue.getValue());
			}

			@Override
			public B defaultValue() {
				return Default.NULL;
			}
		}
	}

	public void testLoading() {
		try {
			assertNotNull(TypedConfiguration.getConfigurationDescriptor(A.class));
		} catch (ExceptionInInitializerError ex) {
			BasicTestCase.fail("Creating of shared instance in format constructor faild.", ex);
		}
	}

	public void testDefault() {
		A a = TypedConfiguration.newConfigItem(A.class);
		assertNotNull(a.getB());
		assertEquals(42, a.getB().getValue());
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestNestedDescriptorLoading.class);
	}
}
