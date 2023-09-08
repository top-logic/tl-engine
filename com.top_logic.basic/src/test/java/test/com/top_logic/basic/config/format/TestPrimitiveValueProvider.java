/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.format;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.format.BuiltInFormats;
import com.top_logic.basic.config.format.PrimitiveValueProvider;

/**
 * Test for {@link PrimitiveValueProvider}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPrimitiveValueProvider extends AbstractTypedConfigurationTestCase {

	public static enum TestEnum1 {
		ENUM1,
		ENUM2,

		;
	}

	public static enum TestEnum2 {
		ENUM1 {
			@Override
			int foo() {
				return 0;
			}
		},
		ENUM2 {

			@Override
			int foo() {
				return 1;
			}

		},

		;

		abstract int foo();
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public void testEnumValueProvider() {
		ConfigurationValueProvider valueProvider = BuiltInFormats.getPrimitiveValueProvider(Enum.class);
		checkWriteRead(valueProvider, TestEnum1.ENUM1);
		checkWriteRead(valueProvider, TestEnum2.ENUM1);
	}

	private void checkWriteRead(ConfigurationValueProvider valueProvider, Enum<?> enumValue) {
		String specification = valueProvider.getSpecification(enumValue);
		Object deserialised;
		try {
			deserialised = valueProvider.getValue("ignored", specification);
		} catch (ConfigurationException ex) {
			throw BasicTestCase.fail("Serialised value '" + specification + "' must be deserialisable.", ex);
		}
		assertEquals("Serialising and deserialising must result in equal object.", enumValue, deserialised);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPrimitiveValueProvider}.
	 */
	public static Test suite() {
		return suite(TestPrimitiveValueProvider.class);
	}

}

