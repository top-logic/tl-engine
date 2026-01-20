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
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContent;

/**
 * {@link AbstractConfigurationWriterTest} testing {@link JsonConfigurationWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestJsonConfigurationWithSchema extends AbstractConfigurationWriterTest {

	public interface A extends ConfigurationItem {
		B getB();

		void setB(B value);
	}

	public interface B extends ConfigurationItem {
		int getX();

		void setX(int value);
	}

	public interface C extends B {
		int getY();

		void setY(int value);
	}

	public void testPolymorphicBaseValue() throws Exception {
		A a = TypedConfiguration.newConfigItem(A.class);
		B b = TypedConfiguration.newConfigItem(B.class);
		b.setX(42);
		a.setB(b);

		doReadWrite("a", TypedConfiguration.getConfigurationDescriptor(A.class), a);
	}

	public void testPolymorphicExtensionValue() throws Exception {
		A a = TypedConfiguration.newConfigItem(A.class);
		C c = TypedConfiguration.newConfigItem(C.class);
		c.setX(42);
		c.setY(13);
		a.setB(c);

		doReadWrite("a", TypedConfiguration.getConfigurationDescriptor(A.class), a);
	}

	@Override
	protected ConfigurationItem readConfigItem(String localName, ConfigurationDescriptor expectedType,
			CharacterContent content) throws ConfigurationException {
		ConfigurationItem result = new JsonConfigurationReader(context, expectedType)
			.schemaAware()
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
			.schemaAware()
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
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonConfigurationWithSchema.class));
	}

}

