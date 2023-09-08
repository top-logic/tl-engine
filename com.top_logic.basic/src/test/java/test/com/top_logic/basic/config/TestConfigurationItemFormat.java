/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.equal.ConfigEquality;

/**
 * Test case for serialization of {@link ConfigurationItem}-valued properties that carry a
 * {@link Format} annotation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigurationItemFormat extends AbstractTypedConfigurationTestCase {

	public interface A extends ConfigurationItem {

		B getP();

		void setP(B value);

		B getQ();

		void setQ(B value);

		B getR();

		void setR(B value);

		/**
		 * List-valued property with a short-cut format.
		 * 
		 * <p>
		 * The format is used for simple cases, where the list of {@link Item}s can be easily
		 * encoded into a single string value stored in an XML attribute. In the general case, the
		 * full syntax with nested XML elements is used.
		 * </p>
		 */
		@Format(Item.ShortCutFormat.class)
		List<Item> getItems();

		/**
		 * Map-valued property with a short-cut format.
		 * 
		 * <p>
		 * The format is used for simple cases, where the list of {@link Item}s can be easily
		 * encoded into a single string value stored in an XML attribute. In the general case, the
		 * full syntax with nested XML elements is used.
		 * </p>
		 */
		@Format(Item.ShortCutFormatMap.class)
		@Key(Item.VALUE)
		@EntryTag("item")
		Map<String, Item> getItemByValue();

		/**
		 * Array-valued property with a short-cut format.
		 * 
		 * <p>
		 * The format is used for simple cases, where the list of {@link Item}s can be easily
		 * encoded into a single string value stored in an XML attribute. In the general case, the
		 * full syntax with nested XML elements is used.
		 * </p>
		 */
		@Format(Item.ShortCutFormatArray.class)
		@EntryTag("item")
		Item[] getItemArray();

		/**
		 * @see #getItemArray()
		 */
		void setItemArray(Item... items);
	}

	public interface Item extends ConfigurationItem {
		/**
		 * @see #getValue()
		 */
		String VALUE = "value";

		/**
		 * Some arbitrary string.
		 */
		@Name(VALUE)
		String getValue();

		void setValue(String v);

		boolean isEnabled();

		void setEnabled(boolean enabled);

		static Item item(String v) {
			Item result = TypedConfiguration.newConfigItem(Item.class);
			result.setValue(v);
			return result;
		}

		static Item itemEnabled(String v) {
			Item result = item(v);
			result.setEnabled(true);
			return result;
		}

		/**
		 * {@link ConfigurationValueProvider} that encodes lists of {@link Item} that are not
		 * {@link Item#isEnabled() enabled} into a comma separated list of {@link Item#getValue()
		 * values}.
		 */
		class ShortCutFormat extends AbstractConfigurationValueProvider<List<Item>> {

			public ShortCutFormat() {
				super(List.class);
			}

			@Override
			public List<Item> defaultValue() {
				return new ArrayList<>();
			}

			@Override
			protected List<Item> getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				return Stream.of(propertyValue.toString().split("\\s*,\\s*")).map(Item::item)
					.collect(Collectors.toList());
			}

			@Override
			public boolean isLegalValue(Object value) {
				return value instanceof List && onlyDisabled((List<?>) value);
			}

			private boolean onlyDisabled(List<?> value) {
				return !value.stream().filter(o -> o instanceof Item && ((Item) o).isEnabled()).findAny().isPresent();
			}

			@Override
			protected String getSpecificationNonNull(List<Item> configValue) {
				return configValue.stream().map(i -> i.getValue()).collect(Collectors.joining(", "));
			}
		}

		/**
		 * {@link ConfigurationValueProvider} that encodes lists of {@link Item} that are not
		 * {@link Item#isEnabled() enabled} into a comma separated list of {@link Item#getValue()
		 * values}.
		 */
		class ShortCutFormatMap extends AbstractConfigurationValueProvider<Map<String, Item>> {

			public ShortCutFormatMap() {
				super(Map.class);
			}

			@Override
			public Map<String, Item> defaultValue() {
				return new LinkedHashMap<>();
			}

			@Override
			protected Map<String,Item> getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				return Stream.of(propertyValue.toString().split("\\s*,\\s*")).map(Item::item)
					.collect(
						Collectors.groupingBy(Item::getValue, Collectors.reducing(null, (x, y) -> x == null ? y : x)));
			}

			@Override
			public boolean isLegalValue(Object value) {
				return value instanceof Map && onlyDisabled((Map<?, ?>) value);
			}

			private boolean onlyDisabled(Map<?, ?> value) {
				return !value.values().stream().filter(o -> o instanceof Item && ((Item) o).isEnabled()).findAny()
					.isPresent();
			}

			@Override
			protected String getSpecificationNonNull(Map<String, Item> configValue) {
				return configValue.values().stream().map(i -> i.getValue()).collect(Collectors.joining(", "));
			}
		}

		/**
		 * {@link ConfigurationValueProvider} that encodes lists of {@link Item} that are not
		 * {@link Item#isEnabled() enabled} into a comma separated list of {@link Item#getValue()
		 * values}.
		 */
		class ShortCutFormatArray extends AbstractConfigurationValueProvider<Item[]> {

			public ShortCutFormatArray() {
				super(Item[].class);
			}

			@Override
			public Item[] defaultValue() {
				return new Item[0];
			}

			@Override
			protected Item[] getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				return Stream.of(propertyValue.toString().split("\\s*,\\s*")).map(Item::item)
					.collect(Collectors.toList())
					.toArray(new Item[0]);
			}

			@Override
			public boolean isLegalValue(Object value) {
				return value instanceof Item[] && onlyDisabled((Item[]) value);
			}

			private boolean onlyDisabled(Item[] value) {
				return !Arrays.stream(value).filter(o -> o.isEnabled()).findAny().isPresent();
			}

			@Override
			protected String getSpecificationNonNull(Item[] configValue) {
				return Arrays.stream(configValue).map(i -> i.getValue()).collect(Collectors.joining(", "));
			}
		}
	}

	@Abstract
	@Format(BFormat.class)
	public interface B extends ConfigurationItem {
		// Pure base class.
	}

	public interface C extends B {
		int getX();

		void setX(int parseInt);
	}

	public interface D extends B {
		String getS();

		void setS(String substring);
	}

	public static class BFormat extends AbstractConfigurationValueProvider<B> {

		/**
		 * Creates a {@link TestConfigurationItemFormat.BFormat}.
		 */
		public BFormat() {
			super(B.class);
		}

		@Override
		protected String getSpecificationNonNull(B configValue) {
			if (configValue instanceof C) {
				return "C:" + ((C) configValue).getX();
			}
			else if (configValue instanceof D) {
				return "D:" + ((D) configValue).getS();
			} else {
				throw new IllegalArgumentException(
					"Unsupported: " + configValue.descriptor().getConfigurationInterface());
			}
		}

		@Override
		protected B getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
			String string = propertyValue.toString();
			if (string.startsWith("C:")) {
				C result = TypedConfiguration.newConfigItem(C.class);
				result.setX(Integer.parseInt(string.substring(2)));
				return result;
			} else if (string.startsWith("D:")) {
				D result = TypedConfiguration.newConfigItem(D.class);
				result.setS(string.substring(2));
				return result;
			}
			throw new IllegalArgumentException("Unsupported: " + propertyValue);
		}

	}

	public void testReadWriteSimple() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		D p = TypedConfiguration.newConfigItem(D.class);
		p.setS("Hello world");
		a.setP(p);

		a.setQ(TypedConfiguration.newConfigItem(C.class));
		a.setR(null);

		String xml = a.toString();
		ConfigurationItem copy = read(xml);
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, copy));
	}

	public void testReadWriteMultiline() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		D p = TypedConfiguration.newConfigItem(D.class);
		// This value is not serialized as attribute, but as sub-tag. This must not infer with the
		// serialization of the explicit null value in property r.
		p.setS("Hello\nworld\n");
		a.setP(p);

		a.setQ(TypedConfiguration.newConfigItem(C.class));
		a.setR(null);

		String xml = a.toString();
		ConfigurationItem copy = read(xml);
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, copy));
	}

	public void testReadWriteShortcutList() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		a.getItems().add(Item.item("foo"));
		a.getItems().add(Item.item("bar"));

		String xml = a.toString();
		BasicTestCase.assertContains("foo, bar", xml);
		ConfigurationItem copy = read(xml);

		ConfigurationItem option1 =
			read("<config config:interface=\"test.com.top_logic.basic.config.TestConfigurationItemFormat$A\""
				+ " xmlns:config=\"http://www.top-logic.com/ns/config/6.0\""
				+ "><items>foo, <!-- ignored --> bar</items></config>");
		ConfigurationItem option2 =
			read("<config config:interface=\"test.com.top_logic.basic.config.TestConfigurationItemFormat$A\""
				+ " xmlns:config=\"http://www.top-logic.com/ns/config/6.0\""
				+ "><items><item value=\"foo\"/><item value=\"bar\"/></items></config>");

		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, copy));
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, option1));
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, option2));

		a.getItems().get(1).setEnabled(true);
		String xmlWithoutShortcut = a.toString();
		assertFalse("Format does not accept enabled items.", xmlWithoutShortcut.contains("foo, bar"));
		assertTrue(xmlWithoutShortcut, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, read(xmlWithoutShortcut)));
	}

	public void testReadWriteShortcutMap() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		a.getItemByValue().put("foo", Item.item("foo"));
		a.getItemByValue().put("bar", Item.item("bar"));

		String xml = a.toString();
		BasicTestCase.assertContains("foo, bar", xml);
		ConfigurationItem copy = read(xml);

		ConfigurationItem option1 =
			read("<config config:interface=\"test.com.top_logic.basic.config.TestConfigurationItemFormat$A\""
				+ " xmlns:config=\"http://www.top-logic.com/ns/config/6.0\""
				+ "><item-by-value>foo, <!-- ignored --> bar</item-by-value></config>");
		ConfigurationItem option2 =
			read("<config config:interface=\"test.com.top_logic.basic.config.TestConfigurationItemFormat$A\""
				+ " xmlns:config=\"http://www.top-logic.com/ns/config/6.0\""
				+ "><item-by-value><item value=\"foo\"/><item value=\"bar\"/></item-by-value></config>");

		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, copy));
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, option1));
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, option2));

		a.getItemByValue().get("bar").setEnabled(true);
		String xmlWithoutShortcut = a.toString();
		assertFalse("Format does not accept enabled items.", xmlWithoutShortcut.contains("foo, bar"));
		assertTrue(xmlWithoutShortcut, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, read(xmlWithoutShortcut)));
	}

	public void testReadWriteShortcutArray() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		a.setItemArray(Item.item("foo"), Item.item("bar"));

		String xml = a.toString();
		BasicTestCase.assertContains("foo, bar", xml);
		ConfigurationItem copy = read(xml);

		ConfigurationItem option1 =
			read("<config config:interface=\"test.com.top_logic.basic.config.TestConfigurationItemFormat$A\""
				+ " xmlns:config=\"http://www.top-logic.com/ns/config/6.0\""
				+ "><item-array>foo, <!-- ignored --> bar</item-array></config>");
		ConfigurationItem option2 =
			read("<config config:interface=\"test.com.top_logic.basic.config.TestConfigurationItemFormat$A\""
				+ " xmlns:config=\"http://www.top-logic.com/ns/config/6.0\""
				+ "><item-array><item value=\"foo\"/><item value=\"bar\"/></item-array></config>");

		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, copy));
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, option1));
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, option2));

		a.getItemArray()[1].setEnabled(true);
		String xmlWithoutShortcut = a.toString();
		assertFalse("Format does not accept enabled items.", xmlWithoutShortcut.contains("foo, bar"));
		assertTrue(xmlWithoutShortcut, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, read(xmlWithoutShortcut)));
	}

	public void testReadNoMixedContent1() {
		initFailureTest();
		try {
			read("<config config:interface=\"test.com.top_logic.basic.config.TestConfigurationItemFormat$A\""
				+ " xmlns:config=\"http://www.top-logic.com/ns/config/6.0\""
				+ "><items><item value=\"foo\"/>   <!-- ignored --> bar</items></config>");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("Must not mix", ex.getMessage());
		}
	}

	public void testReadNoMixedContent2() {
		initFailureTest();
		try {
			read("<config config:interface=\"test.com.top_logic.basic.config.TestConfigurationItemFormat$A\""
				+ " xmlns:config=\"http://www.top-logic.com/ns/config/6.0\""
				+ "><items>bar, <!-- ignored -->   <item value=\"foo\"/></items></config>");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("Must not mix", ex.getMessage());
		}
	}

	public void testReadWriteFullList() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);

		a.getItems().add(Item.item("foo"));
		a.getItems().add(Item.itemEnabled("bar"));

		String xml = a.toString();
		ConfigurationItem copy = read(xml);
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(a, copy));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(A.class));
	}

	public static Test suite() {
		return suite(TestConfigurationItemFormat.class);
	}
}
