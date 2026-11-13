/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.CommaSeparatedListValueProvider;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonSchemaProvider;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.ArraySchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.json.schema.model.StringSchema;

/**
 * Test for lists in a format of separated elements ({@link CommaSeparatedListValueProvider}) in
 * {@link JsonConfigurationReader} and {@link JsonConfigSchemaBuilder}: read from a JSON array
 * element by element, described as array of the element form.
 */
@SuppressWarnings("javadoc")
public class TestJsonFormattedList extends BasicTestCase {

	/** Description of the element form of {@link Code}. */
	static final String CODE_DESCRIPTION = "An upper case code.";

	/**
	 * Format of a single code, upper case.
	 */
	public static class Code extends AbstractConfigurationValueProvider<String> implements JsonSchemaProvider {

		public static final Code INSTANCE = new Code();

		private Code() {
			super(String.class);
		}

		@Override
		protected String getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			return propertyValue.toString().trim().toUpperCase(Locale.ROOT);
		}

		@Override
		protected String getSpecificationNonNull(String configValue) {
			return configValue;
		}

		@Override
		public Schema buildSchema(PropertyDescriptor property) {
			return StringSchema.create().setDescription(CODE_DESCRIPTION);
		}
	}

	/**
	 * A list of {@link Code}s.
	 */
	public static class Codes extends CommaSeparatedListValueProvider<String> {

		public static final Codes INSTANCE = new Codes();

		private Codes() {
			super();
		}

		@Override
		protected String parseSingleValue(String propertyName, CharSequence propertyValue, String singlePropertyValue)
				throws ConfigurationException {
			return Code.INSTANCE.getValue(propertyName, singlePropertyValue);
		}

		@Override
		protected String formatSingleValue(String singleConfigValue) {
			return Code.INSTANCE.getSpecification(singleConfigValue);
		}

		@Override
		public ConfigurationValueProvider<String> getElementFormat() {
			return Code.INSTANCE;
		}
	}

	public interface Lists extends ConfigurationItem {

		String NAMES = "names";

		String CODES = "codes";

		@Name(NAMES)
		@Format(CommaSeparatedStrings.class)
		List<String> getNames();

		@Name(CODES)
		@Format(Codes.class)
		List<String> getCodes();
	}

	/** A JSON array is read element by element with the element format. */
	public void testReadsArray() throws ConfigurationException {
		Lists lists = read("{\"names\": [\"a\", \"b c\"], \"codes\": [\"x\", \"y\"]}");

		assertEquals(List.of("a", "b c"), lists.getNames());
		assertEquals(List.of("X", "Y"), lists.getCodes());
	}

	/** The separated form is still accepted. */
	public void testReadsSeparatedString() throws ConfigurationException {
		Lists lists = read("{\"names\": \"a,b\", \"codes\": \"x,y\"}");

		assertEquals(List.of("a", "b"), lists.getNames());
		assertEquals(List.of("X", "Y"), lists.getCodes());
	}

	/** An element that is no plain value is refused. */
	public void testRefusesNestedArray() {
		try {
			read("{\"names\": [[\"a\"]]}");
			fail("A nested array is no element of a list of texts.");
		} catch (RuntimeException | ConfigurationException ex) {
			// Expected.
		}
	}

	/** The schema is an array of the element form, as the element format describes it. */
	public void testSchemaIsArrayOfElementForm() {
		Schema schema = new JsonConfigSchemaBuilder().setInline(true)
			.buildConfigSchema(TypedConfiguration.getConfigurationDescriptor(Lists.class));
		assertTrue(schema instanceof ObjectSchema);
		ObjectSchema object = (ObjectSchema) schema;

		Schema names = object.getProperties().get(Lists.NAMES);
		assertTrue(JsonSchemaWriter.toJson(names, true), names instanceof ArraySchema);
		assertTrue(((ArraySchema) names).getItems() instanceof StringSchema);

		Schema codes = object.getProperties().get(Lists.CODES);
		assertTrue(JsonSchemaWriter.toJson(codes, true), codes instanceof ArraySchema);
		assertEquals(CODE_DESCRIPTION, ((ArraySchema) codes).getItems().getDescription());
	}

	private static Lists read(String json) throws ConfigurationException {
		return (Lists) new JsonConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			TypedConfiguration.getConfigurationDescriptor(Lists.class))
				.schemaAware()
				.setSource(CharacterContents.newContent(json))
				.read();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonFormattedList.class));
	}
}
