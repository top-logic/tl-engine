/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.AllowedValues;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.Schema;

/**
 * Test for {@link AllowedValues} at properties declared in Java and in XML.
 */
@SuppressWarnings("javadoc")
public class TestAllowedValues extends BasicTestCase {

	public enum Color {
		RED, GREEN, BLUE;
	}

	/**
	 * Properties declared in Java.
	 */
	public interface JavaConfig extends ConfigurationItem {

		String STATE = "state";

		String TAGS = "tags";

		String CODES = "codes";

		String NOTE = "note";

		@Name(STATE)
		@AllowedValues({ "open", "closed" })
		String getState();

		void setState(String value);

		@Name(TAGS)
		@Format(CommaSeparatedStrings.class)
		@AllowedValues({ "a", "b" })
		List<String> getTags();

		void setTags(List<String> value);

		@Name(CODES)
		@ListBinding(tag = "code", attribute = "value")
		@AllowedValues({ "x", "y" })
		List<String> getCodes();

		@Name(NOTE)
		@Nullable
		@AllowedValues({ "one", "two" })
		String getNote();

	}

	private static final String DECLARATION =
		"<properties>"
			+ "<property name='state' type='String'><allowed-values value='open, closed'/></property>"
			+ "<property name='level' type='int'><allowed-values value='1, 2, 3'/></property>"
			+ "<property name='color' type='" + Color.class.getName() + "'>"
			+ "<allowed-values value='GREEN, BLUE'/></property>"
			+ "</properties>";

	/**
	 * (a) A declared text property accepts allowed values and rejects others, naming the allowed
	 * ones; its default is the first allowed value.
	 */
	public void testDeclaredString() throws ConfigurationException {
		ConfigurationDescriptor descriptor = descriptor(DECLARATION);
		PropertyDescriptor state = descriptor.getProperty("state");
		assertEquals(Arrays.asList("open", "closed"), state.getAllowedValues());

		ConfigurationItem item = read(descriptor, "<config state='closed'/>");
		assertEquals("closed", item.value(state));
		assertEquals("open", descriptor.factory().createNew().value(state));

		assertErrors(descriptor, "<config state='pending'/>", "pending", "open, closed");
		assertIllegalUpdate(item, state, "pending");
	}

	/**
	 * (b) A declared number property and an enumeration property restricted to some constants.
	 */
	public void testDeclaredIntAndEnum() throws ConfigurationException {
		ConfigurationDescriptor descriptor = descriptor(DECLARATION);
		PropertyDescriptor level = descriptor.getProperty("level");
		PropertyDescriptor color = descriptor.getProperty("color");
		assertEquals(Arrays.asList(1, 2, 3), level.getAllowedValues());
		assertEquals(Arrays.asList(Color.GREEN, Color.BLUE), color.getAllowedValues());

		ConfigurationItem defaults = descriptor.factory().createNew();
		assertEquals(1, defaults.value(level));
		assertEquals(Color.GREEN, defaults.value(color));

		ConfigurationItem item = read(descriptor, "<config level='3' color='BLUE'/>");
		assertEquals(3, item.value(level));
		assertEquals(Color.BLUE, item.value(color));

		assertErrors(descriptor, "<config level='4'/>", "'4'", "1, 2, 3");
		assertErrors(descriptor, "<config color='RED'/>", "RED", "GREEN, BLUE");
		assertIllegalUpdate(item, level, 0);
		assertIllegalUpdate(item, color, Color.RED);
	}

	/**
	 * (c) A property declared in Java.
	 */
	public void testJavaProperty() throws ConfigurationException {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(JavaConfig.class);
		JavaConfig config = TypedConfiguration.newConfigItem(JavaConfig.class);
		assertEquals("open", config.getState());
		assertNull("A nullable property keeps null as default.", config.getNote());

		config.setState("closed");
		assertEquals("closed", config.getState());
		try {
			config.setState("pending");
			fail("Value is not allowed.");
		} catch (IllegalArgumentException ex) {
			assertContains("open, closed", fullMessage(ex));
		}

		JavaConfig read = (JavaConfig) read(descriptor, "<config state='closed' note='two'/>");
		assertEquals("closed", read.getState());
		assertEquals("two", read.getNote());
		assertErrors(descriptor, "<config state='pending'/>", "pending", "open, closed");
		assertErrors(descriptor, "<config note='three'/>", "three", "one, two");
	}

	/**
	 * (d) Each element of a list is checked, for a list in a text format and for a list with an
	 * element per entry.
	 */
	public void testListElements() throws ConfigurationException {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(JavaConfig.class);
		assertEquals(Arrays.asList("a", "b"), descriptor.getProperty(JavaConfig.TAGS).getAllowedValues());
		assertEquals(Arrays.asList("x", "y"), descriptor.getProperty(JavaConfig.CODES).getAllowedValues());

		JavaConfig read = (JavaConfig) read(descriptor,
			"<config tags='b, a'><codes><code value='y'/><code value='x'/></codes></config>");
		assertEquals(Arrays.asList("b", "a"), read.getTags());
		assertEquals(Arrays.asList("y", "x"), read.getCodes());

		assertErrors(descriptor, "<config tags='a, c'/>", "'c'", "a, b");
		assertErrors(descriptor, "<config><codes><code value='z'/></codes></config>", "'z'", "x, y");

		JavaConfig config = TypedConfiguration.newConfigItem(JavaConfig.class);
		config.setTags(Arrays.asList("a"));
		try {
			config.setTags(Arrays.asList("a", "c"));
			fail("Element is not allowed.");
		} catch (IllegalArgumentException ex) {
			assertContains("a, b", fullMessage(ex));
		}
	}

	/**
	 * (e) An allowed value that cannot be read in the format of the property is an error of the
	 * declaration.
	 */
	public void testUnreadableAllowedValue() throws ConfigurationException {
		String errors = descriptorErrors("<properties>"
			+ "<property name='level' type='int'><allowed-values value='1, two'/></property>"
			+ "</properties>");
		assertContains("two", errors);
	}

	/**
	 * (f) Allowed values at a property without text format are an error of the declaration.
	 */
	public void testPropertyWithoutTextFormat() throws ConfigurationException {
		String errors = descriptorErrors("<properties>"
			+ "<property name='item' type='" + ConfigurationItem.class.getName() + "'>"
			+ "<allowed-values value='x'/></property>"
			+ "</properties>");
		assertContains("text format", errors);
	}

	/**
	 * An explicit default outside the allowed values is an error of the declaration.
	 */
	public void testDefaultNotAllowed() throws ConfigurationException {
		String errors = descriptorErrors("<properties>"
			+ "<property name='state' type='String' default='pending'><allowed-values value='open, closed'/>"
			+ "</property>"
			+ "</properties>");
		assertContains("pending", errors);
	}

	/**
	 * (g) The JSON schema lists the allowed values in the JSON type of the property.
	 */
	public void testSchema() throws ConfigurationException {
		String declared = schema(descriptor(DECLARATION));
		assertContains("\"state\":{\"enum\":[\"open\",\"closed\"]", declared);
		assertContains("\"level\":{\"enum\":[1,2,3]", declared);
		assertContains("\"color\":{\"enum\":[\"GREEN\",\"BLUE\"]", declared);

		String java = schema(TypedConfiguration.getConfigurationDescriptor(JavaConfig.class));
		assertContains("\"tags\":{\"type\":\"array\",\"items\":{\"enum\":[\"a\",\"b\"]}", java);
	}

	private static void assertIllegalUpdate(ConfigurationItem item, PropertyDescriptor property, Object value) {
		try {
			item.update(property, value);
			fail("Value '" + value + "' is not allowed.");
		} catch (IllegalArgumentException ex) {
			assertContains("allowed values", fullMessage(ex));
		}
	}

	private static String fullMessage(Throwable ex) {
		StringBuilder result = new StringBuilder();
		for (Throwable current = ex; current != null; current = current.getCause()) {
			result.append(current.getMessage()).append('\n');
		}
		return result.toString();
	}

	private static void assertErrors(ConfigurationDescriptor descriptor, String xml, String... parts) {
		BufferingProtocol protocol = new BufferingProtocol();
		try {
			reader(new SimpleInstantiationContext(protocol), descriptor, xml).read();
		} catch (ConfigurationException | RuntimeException ex) {
			protocol.error("Reading failed.", ex);
		}
		String errors = protocol.getError();
		assertFalse("Expected errors reading: " + xml, errors.isEmpty());
		for (String part : parts) {
			assertContains(part, errors);
		}
	}

	private static ConfigurationItem read(ConfigurationDescriptor descriptor, String xml)
			throws ConfigurationException {
		return reader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptor, xml).read();
	}

	private static ConfigurationReader reader(com.top_logic.basic.config.InstantiationContext context,
			ConfigurationDescriptor descriptor, String xml) {
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap("config", descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		return reader;
	}

	private static String schema(ConfigurationDescriptor descriptor) {
		Schema schema = new JsonConfigSchemaBuilder().setInline(true).buildConfigSchema(descriptor);
		return JsonSchemaWriter.toJson(schema, false);
	}

	private static ConfigurationDescriptor descriptor(String declaration) throws ConfigurationException {
		return TypedConfiguration.getConfigurationDescriptor(declaration(declaration));
	}

	private static String descriptorErrors(String declaration) throws ConfigurationException {
		BufferingProtocol protocol = new BufferingProtocol();
		try {
			TypedConfiguration.getConfigurationDescriptor(protocol, declaration(declaration));
		} catch (RuntimeException ex) {
			protocol.error("Creating the descriptor failed.", ex);
		}
		String errors = protocol.getError();
		assertFalse("Expected errors for: " + declaration, errors.isEmpty());
		return errors;
	}

	private static ConfigurationDescriptorConfig declaration(String declaration) throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("properties",
				TypedConfiguration.getConfigurationDescriptor(ConfigurationDescriptorConfig.class)));
		reader.setSource(CharacterContents.newContent(declaration));
		return (ConfigurationDescriptorConfig) reader.read();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestAllowedValues.class));
	}
}
