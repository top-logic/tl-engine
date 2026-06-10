/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;

/**
 * Test for {@link JsonConfigSchemaBuilder} handling of non-nullable item properties with
 * {@link ItemDefault item-defaults}.
 *
 * <p>
 * Regression test for ticket #29325: A non-nullable property whose item-default is an incomplete
 * configuration (one with unset {@link Mandatory mandatory} nested properties) must not be presented
 * as optional. Otherwise a schema-driven client omits the property and silently produces an
 * un-instantiable default value.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestJsonConfigSchemaBuilderNonNullable extends AbstractJsonConfigurationWriterTest {

	/**
	 * Polymorphic base type (mirrors {@code TitleProvider}).
	 */
	@Abstract
	public interface Greeting extends ConfigurationItem {
		// Pure marker.
	}

	/**
	 * Concrete default whose value is incomplete: {@link #getText()} is mandatory but has no default
	 * (mirrors {@code ConstantTitle.Config.getTitle()}).
	 */
	public interface ConstantGreeting extends Greeting {
		@Name("text")
		@Mandatory
		String getText();
	}

	/**
	 * Concrete type whose default value is complete (no mandatory properties).
	 */
	public interface PlainGreeting extends Greeting {
		@Name("text")
		String getText();
	}

	/**
	 * Holder with a non-nullable item property whose default ({@link ConstantGreeting}) is
	 * incomplete (mirrors the dialog template {@code title} property).
	 */
	public interface IncompleteHolder extends ConfigurationItem {
		@Name("greeting")
		@ItemDefault(ConstantGreeting.class)
		@NonNullable
		Greeting getGreeting();
	}

	/**
	 * Holder with a non-nullable item property whose default ({@link PlainGreeting}) is complete and
	 * may therefore safely be omitted.
	 */
	public interface CompleteHolder extends ConfigurationItem {
		@Name("greeting")
		@ItemDefault(PlainGreeting.class)
		@NonNullable
		Greeting getGreeting();
	}

	private ObjectSchema rootSchema(Class<? extends ConfigurationItem> configType) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configType);
		Schema schema = new JsonConfigSchemaBuilder().setInline(true).buildConfigSchema(descriptor);
		assertTrue("Expected an object schema, but got: " + schema, schema instanceof ObjectSchema);
		return (ObjectSchema) schema;
	}

	private String json(Class<? extends ConfigurationItem> configType) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configType);
		Schema schema = new JsonConfigSchemaBuilder().setInline(true).buildConfigSchema(descriptor);
		return JsonSchemaWriter.toJson(schema, true);
	}

	/**
	 * A non-nullable property must never be presented as nullable (no {@code "type": "null"}).
	 */
	public void testNonNullableHasNoNullOption() {
		assertFalse("Non-nullable property must not offer a null value.\n" + json(IncompleteHolder.class),
			json(IncompleteHolder.class).contains("\"null\""));
	}

	/**
	 * A non-nullable property whose item-default is incomplete (unset mandatory nested property) must
	 * be marked required, so a schema-driven client cannot omit it and produce an un-instantiable
	 * default.
	 */
	public void testIncompleteItemDefaultIsRequired() {
		ObjectSchema root = rootSchema(IncompleteHolder.class);
		assertTrue(
			"A non-nullable property with an incomplete item-default must be required. Required: "
				+ root.getRequired(),
			root.getRequired().contains("greeting"));
	}

	/**
	 * A non-nullable property whose item-default is complete may safely be omitted and must therefore
	 * not be marked required.
	 */
	public void testCompleteItemDefaultIsOptional() {
		ObjectSchema root = rootSchema(CompleteHolder.class);
		assertFalse(
			"A non-nullable property with a complete item-default need not be required. Required: "
				+ root.getRequired(),
			root.getRequired().contains("greeting"));
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonConfigSchemaBuilderNonNullable.class));
	}
}
