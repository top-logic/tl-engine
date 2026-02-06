/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import java.io.StringWriter;
import java.util.List;

import test.com.top_logic.basic.config.AbstractConfigurationWriterTest;

import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.Schema;

/**
 * Base class for JSON configuration tests with schema validation.
 *
 * <p>
 * Provides common functionality for writing configurations to JSON and validating them against
 * generated JSON schemas.
 * </p>
 */
public abstract class AbstractJsonConfigurationWriterTest extends AbstractConfigurationWriterTest {

	@Override
	protected ConfigurationItem readConfigItem(String localName, ConfigurationDescriptor expectedType,
			CharacterContent content) throws ConfigurationException {
		return new JsonConfigurationReader(context, expectedType)
			.schemaAware()
			.setSource(content)
			.read();
	}

	@Override
	protected String writeConfigurationItem(String localName, ConfigurationDescriptor staticType,
			ConfigurationItem item) throws Exception {
		String configJson = writeJson(staticType, item);
		validateAgainstSchema(staticType, configJson);
		return configJson;
	}

	/**
	 * Writes the given configuration item to JSON.
	 *
	 * @param staticType
	 *        The static type descriptor.
	 * @param item
	 *        The configuration item to write.
	 * @return The JSON string representation.
	 */
	protected String writeJson(ConfigurationDescriptor staticType, ConfigurationItem item) throws Exception {
		StringWriter buffer = new StringWriter();
		new JsonConfigurationWriter(buffer)
			.schemaAware()
			.prettyPrint()
			.write(staticType, item);
		return buffer.toString();
	}

	/**
	 * Builds a JSON schema for the given configuration descriptor.
	 *
	 * @param descriptor
	 *        The configuration descriptor.
	 * @return The JSON schema as a string.
	 */
	protected String buildSchemaJson(ConfigurationDescriptor descriptor) {
		Schema schemaDoc = new JsonConfigSchemaBuilder().setInline(true).buildConfigSchema(descriptor);
		return JsonSchemaWriter.toJson(schemaDoc);
	}

	/**
	 * Validates the given JSON configuration against the schema for the given descriptor.
	 *
	 * @param descriptor
	 *        The configuration descriptor to build the schema from.
	 * @param configJson
	 *        The JSON configuration to validate.
	 */
	protected void validateAgainstSchema(ConfigurationDescriptor descriptor, String configJson) {
		String schemaJson = buildSchemaJson(descriptor);
		validateAgainstSchema(schemaJson, configJson);
	}

	/**
	 * Validates the given JSON configuration against the given schema.
	 *
	 * @param schemaJson
	 *        The JSON schema.
	 * @param configJson
	 *        The JSON configuration to validate.
	 */
	protected void validateAgainstSchema(String schemaJson, String configJson) {
		SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder().build();

		SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
			builder -> builder.schemaRegistryConfig(schemaRegistryConfig));

		com.networknt.schema.Schema schema = schemaRegistry.getSchema(schemaJson, InputFormat.JSON);

		List<com.networknt.schema.Error> errors = schema.validate(configJson, InputFormat.JSON, executionContext -> {
			/* By default since Draft 2019-09 the format keyword only generates annotations and not
			 * assertions. */
			executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
		});

		assertTrue("Validation errors: " + errors, errors.isEmpty());
	}

}
