/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.format.StringConstantReference;

/**
 * Test case for {@link StringConstantReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestStringConstantReference extends AbstractTypedConfigurationTestCase {

	public interface Config extends ConfigurationItem {

		@Format(StringConstantReference.class)
		String getConst();

	}

	public static final String CONST = "Hello world!";

	public void testConstant() throws ConfigurationException {
		Config item = read("<config const='" + TestStringConstantReference.class.getName() + "#" + "CONST" + "'/>");
		assertEquals(CONST, item.getConst());
	}

	public void testLiteral() throws ConfigurationException {
		Config item = read("<config const='" + CONST + "'/>");
		assertEquals(CONST, item.getConst());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

}
