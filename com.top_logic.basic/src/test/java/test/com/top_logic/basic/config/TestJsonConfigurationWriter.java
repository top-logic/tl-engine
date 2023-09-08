/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.io.character.CharacterContent;

/**
 * {@link AbstractConfigurationWriterTest} testing {@link JsonConfigurationWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestJsonConfigurationWriter extends AbstractConfigurationWriterTest {

	@Override
	protected ConfigurationItem readConfigItem(String localName, ConfigurationDescriptor expectedType,
			CharacterContent content) throws ConfigurationException {
		ConfigurationItem result = new JsonConfigurationReader(context, expectedType)
			.setSource(content)
			.read();
		return result;
	}

	@Override
	protected String writeConfigurationItem(String localName, ConfigurationDescriptor staticType,
			ConfigurationItem item) throws Exception {
		StringWriter buffer = new StringWriter();
		new JsonConfigurationWriter(buffer)
			.prettyPrint()
			.write(staticType, item);
		return buffer.toString();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonConfigurationWriter.class));
	}

}

