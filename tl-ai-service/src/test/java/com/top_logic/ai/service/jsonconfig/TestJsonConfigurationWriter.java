/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.jsonconfig;

import java.io.StringWriter;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.config.AbstractConfigurationWriterTest;

import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;

import com.top_logic.ai.service.jsonschema.JsonSchemaWriter;
import com.top_logic.ai.service.jsonschema.model.Schema;
import com.top_logic.ai.service.tools.config.ConfigSchemaBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
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
		Schema schemaDoc = new ConfigSchemaBuilder().build(staticType);
		String schemaData = JsonSchemaWriter.toJson(schemaDoc);

		StringWriter buffer = new StringWriter();
		new JsonConfigurationWriter(buffer)
			.prettyPrint()
			.write(staticType, item);
		String configJson = buffer.toString();

		SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder().build();

		SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
			builder -> builder.schemaRegistryConfig(schemaRegistryConfig));

		com.networknt.schema.Schema schema = schemaRegistry.getSchema(schemaData, InputFormat.JSON);

		List<com.networknt.schema.Error> errors = schema.validate(configJson, InputFormat.JSON, executionContext -> {
			/* By default since Draft 2019-09 the format keyword only generates annotations and not
			 * assertions. */
			executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
		});

		assertTrue("Errors: " + errors, errors.isEmpty());

		return configJson;
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonConfigurationWriter.class));
	}

}

