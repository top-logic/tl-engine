/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * The class {@link TestFormatAnnotations} tests annotations defining the
 * formatter of a property.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestFormatAnnotations extends AbstractTypedConfigurationTestCase {
	
	public static class Foo {
		
		public static final Foo INSTANCE = new Foo();
		
		protected Foo() {
			// singleton instance
		}
	}

	public static class Foo2 extends Foo {
		
		@SuppressWarnings("hiding")
		public static final Foo2 INSTANCE = new Foo2();
		
		private Foo2() {
			// singleton instance
		}
	}

	public interface Config extends ConfigurationItem {

		@ListBinding(format = StringValueProvider.class, tag = "entry", attribute = "value")
		List<String> getAllowedStrings();

		@ListBinding()
		List<String> getStrings();
		
		@MapBinding(valueFormat = CommaSeparatedStrings.class, tag = "entry", attribute = "value", key = "key")
		Map<String, List<String>> getListByName();

		@MapBinding()
		Map<String, Boolean> getBooleans();
		
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault("s1,s2,s3")
		List<String> getCommaSeparatedString();
		
		@InstanceFormat()
		@InstanceDefault(TestFormatAnnotations.Foo.class)
		Foo getFoo();

		@InstanceFormat()
		@InstanceDefault(TestFormatAnnotations.Foo2.class)
		Foo2 getFoo2();
		
		@MapBinding()
		Map<Integer, Boolean> getBooleanByInt();
		
		@Format(WrappedName.class)
		TestValue getItemWithShortcutFormat();

		void setItemWithShortcutFormat(TestValue value);

		NameWithFormat getItemWithTypeFormat();

		@Format(WrappedNameList.class)
		List<TestValue> getListWithShortcutFormat();

		@Format(WrappedNameMap.class)
		@Key(TestValue.NAME_ATTRIBUTE)
		Map<String, TestValue> getMapWithShortcutFormat();
	}

	public interface TestValue extends NamedConfigMandatory {
		// Pure marker.
	}
	
	@Format(WrappedName.class)
	public interface NameWithFormat extends TestValue {
		// Pure marker interface.
	}

	public static class WrappedName extends AbstractConfigurationValueProvider<TestValue> {

		/**
		 * Singleton {@link TestFormatAnnotations.WrappedName} instance.
		 */
		public static final WrappedName INSTANCE = new WrappedName();

		private WrappedName() {
			super(TestValue.class);
		}

		@Override
		protected TestValue getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			return name(propertyValue);
		}

		static TestValue name(CharSequence propertyValue) {
			NameWithFormat result = TypedConfiguration.newConfigItem(NameWithFormat.class);
			result.update(result.descriptor().getProperty(NameWithFormat.NAME_ATTRIBUTE),
				propertyValue.toString());
			return result;
		}

		@Override
		protected String getSpecificationNonNull(TestValue configValue) {
			return configValue.getName();
		}

		@Override
		public boolean isLegalValue(Object value) {
			return value == null || ((value instanceof TestValue)
				&& !((TestValue) value).getName().startsWith("Special: "));
		}

	}

	public static class WrappedNameList extends AbstractConfigurationValueProvider<List<TestValue>> {

		/**
		 * Singleton {@link TestFormatAnnotations.WrappedNameList} instance.
		 */
		public static final WrappedNameList INSTANCE = new WrappedNameList();

		private WrappedNameList() {
			super(List.class);
		}

		@Override
		protected List<TestValue> getValueEmpty(String propertyName) throws ConfigurationException {
			return defaultValue();
		}

		@Override
		protected List<TestValue> getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			ArrayList<TestValue> result = new ArrayList<>();
			for (String name : propertyValue.toString().split("\\s*,\\s*")) {
				result.add(WrappedName.name(name));
			}
			return result;
		}

		@Override
		protected String getSpecificationNonNull(List<TestValue> configValue) {
			StringBuilder result = new StringBuilder();
			for (TestValue name : configValue) {
				if (result.length() > 0) {
					result.append(", ");
				}
				result.append(name.getName());
			}
			return result.toString();
		}

		@Override
		public List<TestValue> defaultValue() {
			return new ArrayList<>();
		}

	}

	public static class WrappedNameMap extends AbstractConfigurationValueProvider<Map<String, TestValue>> {

		/**
		 * Singleton {@link TestFormatAnnotations.WrappedNameMap} instance.
		 */
		public static final WrappedNameMap INSTANCE = new WrappedNameMap();

		private WrappedNameMap() {
			super(Map.class);
		}

		@Override
		protected Map<String, TestValue> getValueEmpty(String propertyName) throws ConfigurationException {
			return defaultValue();
		}

		@Override
		protected Map<String, TestValue> getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			Map<String, TestValue> result = new HashMap<>();
			for (String name : propertyValue.toString().split("\\s*,\\s*")) {
				result.put(name, WrappedName.name(name));
			}
			return result;
		}

		@Override
		protected String getSpecificationNonNull(Map<String, TestValue> configValue) {
			StringBuilder result = new StringBuilder();
			for (TestValue name : configValue.values()) {
				if (result.length() > 0) {
					result.append(", ");
				}
				result.append(name.getName());
			}
			return result.toString();
		}

		@Override
		public Map<String, TestValue> defaultValue() {
			return new HashMap<>();
		}
	}

	@NoImplementationClassGeneration
	public interface IllegalItemDefault extends ConfigurationItem {
		
		@ItemDefault
		boolean getConfig();
	}
	
	public void testIllegal() {
		try {
			TypedConfiguration.newConfigItem(IllegalItemDefault.class);
			fail("ItemDefault must only be annotated to properties of kind " + PropertyKind.ITEM);
		} catch (RuntimeException ex) {
			// expected
		}
	}

	public void testDefault() {
		final Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals(list("s1", "s2", "s3"), config.getCommaSeparatedString());
		assertEquals(list(), config.getAllowedStrings());
		assertEquals(Collections.emptyMap(), config.getListByName());
		assertSame(Foo.INSTANCE, config.getFoo());
		assertEquals(Collections.emptyMap(), config.getBooleans());
		assertSame(Foo2.INSTANCE, config.getFoo2());
		assertEquals(Collections.emptyMap(), config.getBooleanByInt());
		assertNull(config.getItemWithShortcutFormat());
		assertNull(config.getItemWithTypeFormat());
		assertEquals(Collections.emptyList(), config.getListWithShortcutFormat());
		assertEquals(Collections.emptyMap(), config.getMapWithShortcutFormat());
	}

	public void testSimple() throws IOException, ConfigurationException {
		ConfigurationItem configuration = readConfiguration(TestFormatAnnotations.class, getDescriptors(),
				"simple.xml", null);
		assertInstanceof(configuration, Config.class);
		Config config = (Config) configuration;
		
		assertEquals(list("allowed1","allowed2"), config.getAllowedStrings());
		
		assertEquals(list("c1","c2", "c3"), config.getCommaSeparatedString());
		
		Map<String, List<String>> listByName = config.getListByName();
		String key1 = "L1";
		String key2 = "L2";
		String key3 = "L3";
		assertEquals(set(key1, key2, key3), listByName.keySet());
		assertEquals(list("L11", "L12"), listByName.get(key1));
		assertEquals(list("L21", "L22", "L23"), listByName.get(key2));
		assertEquals(list(), listByName.get(key3));
		
		assertSame(Foo2.INSTANCE, config.getFoo());

		assertEquals(list("s1","s2", "s3"), config.getStrings());
		
		Map<String, Boolean> booleans = config.getBooleans();
		assertEquals(set("b1", "b2", "b3"), booleans.keySet());
		assertEquals(Boolean.TRUE, booleans.get("b1"));
		assertEquals(Boolean.TRUE, booleans.get("b2"));
		assertEquals(Boolean.FALSE, booleans.get("b3"));
		
		Map<Integer, Boolean> booleanByInt = config.getBooleanByInt();
		assertEquals(set(1, 2, 3), booleanByInt.keySet());
		assertEquals(Boolean.FALSE, booleanByInt.get(1));
		assertEquals(Boolean.TRUE, booleanByInt.get(2));
		assertEquals(Boolean.FALSE, booleanByInt.get(3));

		assertNull(config.getItemWithShortcutFormat());
		assertNull(config.getItemWithTypeFormat());
		assertEquals(Collections.emptyList(), config.getListWithShortcutFormat());
		assertEquals(Collections.emptyMap(), config.getMapWithShortcutFormat());
	}

	/**
	 * Test overloading configurations with {@link MapBinding} and {@link ListBinding} annotations.
	 */
	public void testMapAdapt() throws IOException, ConfigurationException {
		ConfigurationItem base = readConfiguration(TestFormatAnnotations.class, getDescriptors(),
			"simple.xml", null);
		Config config = (Config) readConfiguration(TestFormatAnnotations.class, getDescriptors(),
			"overlay.xml", base);

		assertEquals(list("s1", "s2", "s3", "s4", "s5"), config.getStrings());
		
		Map<String, List<String>> listByName = config.getListByName();
		String key1 = "L1";
		String key2 = "L2";
		String key3 = "L3";
		String key4 = "L4";
		assertEquals(set(key1, key2, key3, key4), listByName.keySet());
		assertEquals(list("L11", "L12"), listByName.get(key1));
		assertEquals(list("L21", "L22", "L23"), listByName.get(key2));
		assertEquals(list("L31"), listByName.get(key3));
		assertEquals(list("L41", "L42"), listByName.get(key4));
	}

	public void testFormattedItems() throws IOException, ConfigurationException {
		ConfigurationItem configuration = readConfiguration(TestFormatAnnotations.class, getDescriptors(),
			"formatted.xml", null);
		Config config = checkFormattedInstances(configuration);
		checkFormattedLists(config);
	}

	public void testFormattedItemsAsElement() throws IOException, ConfigurationException {
		ConfigurationItem configuration = readConfiguration(TestFormatAnnotations.class, getDescriptors(),
			"formattedAsElement.xml", null);
		checkFormattedInstances(configuration);
	}

	private Config checkFormattedInstances(ConfigurationItem configuration) {
		assertInstanceof(configuration, Config.class);
		Config config = (Config) configuration;

		assertNotNull(config.getItemWithShortcutFormat());
		assertEquals("foo", config.getItemWithShortcutFormat().getName());

		assertNotNull(config.getItemWithTypeFormat());
		assertEquals("foo", config.getItemWithTypeFormat().getName());
		return config;
	}

	public void testWriteItemWithShortCutFormat() throws XMLStreamException {
		checkWrite(
			"<?xml version=\"1.0\" ?><test xmlns:config=\"http://www.top-logic.com/ns/config/6.0\" item-with-shortcut-format=\"foobar\"></test>",
			"foobar");
		checkWrite(
			"<?xml version=\"1.0\" ?><test xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"><item-with-shortcut-format><![CDATA[foo\n"
				+ "bar]]></item-with-shortcut-format></test>",
			"foo\nbar");
		checkWrite(
			"<?xml version=\"1.0\" ?><test xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"><item-with-shortcut-format name=\"Special: foobar\"></item-with-shortcut-format></test>",
			"Special: foobar");
	}

	private void checkWrite(String expected, String value) throws XMLStreamException {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		TestValue item = TypedConfiguration.newConfigItem(TestValue.class);
		item.setName(value);
		config.setItemWithShortcutFormat(item);
		assertEquals(
			expected,
			toXML(config));
	}

	private void checkFormattedLists(Config config) {
		assertEquals(2, config.getListWithShortcutFormat().size());
		assertEquals("foo", config.getListWithShortcutFormat().get(0).getName());
		assertEquals("bar", config.getListWithShortcutFormat().get(1).getName());
	
		assertEquals(2, config.getMapWithShortcutFormat().size());
		assertEquals("foo", config.getMapWithShortcutFormat().get("foo").getName());
		assertEquals("bar", config.getMapWithShortcutFormat().get("bar").getName());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.singletonMap("test",
				TypedConfiguration.getConfigurationDescriptor(Config.class));
		return globalDescriptors;
	}

	public static Test suite() {
		return suite(TestFormatAnnotations.class);
	}
}
