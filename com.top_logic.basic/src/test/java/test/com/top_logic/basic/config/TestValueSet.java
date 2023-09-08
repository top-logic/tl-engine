/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.ScenarioContainerValueSet.AllPrimitives;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ArrayKindConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ContainerKindConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.DerivedKindConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ExampleConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ExampleEnum;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ItemKindConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ListOfKindComplexConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ListOfKindListConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.MapOfKindComplexConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.MapOfKindMapConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.PropertyWithDefaultConfig;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ReferenceKindConfig;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;

/**
 * Tests for {@link ConfigurationItem#valueSet(PropertyDescriptor)}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestValueSet extends AbstractTypedConfigurationTestCase {

	public void testNullAsProperty() {
		ConfigurationItem item = create(ConfigurationItem.class);
		try {
			item.valueSet(null);
			fail("Calling valueSet(null) has to throw a NullPointerException but returns without exception.");
		} catch (NullPointerException exception) {
			// Expected
			return;
		}
	}

	public void testNoneExistingProperty() {
		ConfigurationItem item = create(ConfigurationItem.class);
		String propertyName = ExampleConfig.PROPERTY_NAME_EXAMPLE;
		Class<? extends ConfigurationItem> arbitraryConfigItem = ExampleConfig.class;
		PropertyDescriptor nonExistingProperty = getProperty(create(arbitraryConfigItem), propertyName);
		assert nonExistingProperty != null;
		assertValueSet(false, item, nonExistingProperty);
	}

	public void testInstantiationWithoutValue() {
		ConfigurationItem item = create(ExampleConfig.class);
		assertValueSet(false, item, ExampleConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testReadWithoutValue() throws ConfigurationException {
		ConfigurationItem item = read(ScenarioContainerValueSet.class, "<ExampleConfig />");
		assertValueSet(false, item, ExampleConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testInstantiationWithValue() throws ConfigurationException {
		Map<String, String> initialValues = singletonMap(ExampleConfig.PROPERTY_NAME_EXAMPLE, "5");
		ConfigurationItem item = newConfigItem(ExampleConfig.class, initialValues.entrySet());
		assertValueSet(true, item, ExampleConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testReadWithValue() throws ConfigurationException {
		String configAsString = "<ExampleConfig example='5'/>";
		ConfigurationItem item = read(ScenarioContainerValueSet.class, configAsString);
		assertValueSet(true, item, ExampleConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testInstantiationWithValueEqualToDefault() throws ConfigurationException {
		Map<String, String> initialValues = singletonMap(ExampleConfig.PROPERTY_NAME_EXAMPLE, "0");
		ConfigurationItem item = newConfigItem(ExampleConfig.class, initialValues.entrySet());
		assertValueSet(true, item, ExampleConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testReadWithValueEqualToDefault() throws ConfigurationException {
		String configAsString = "<ExampleConfig example='0'/>";
		ConfigurationItem item = read(ScenarioContainerValueSet.class, configAsString);
		assertValueSet(true, item, ExampleConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testSetWithSetter() {
		ExampleConfig item = create(ExampleConfig.class);
		item.setExample(5);
		assertValueSet(true, item, ExampleConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testSetWithReflection() {
		ConfigurationItem item = create(ExampleConfig.class);
		setValue(item, ExampleConfig.PROPERTY_NAME_EXAMPLE, 5);
		assertValueSet(true, item, ExampleConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testExplicitDefaultInstantiationWithoutValue() {
		ConfigurationItem item = create(PropertyWithDefaultConfig.class);
		assertValueSet(false, item, PropertyWithDefaultConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testExplicitDefaultInstantiationWithValue() throws ConfigurationException {
		Map<String, String> initialValues = singletonMap(PropertyWithDefaultConfig.PROPERTY_NAME_EXAMPLE, "5");
		ConfigurationItem item = newConfigItem(PropertyWithDefaultConfig.class, initialValues.entrySet());
		assertValueSet(true, item, PropertyWithDefaultConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testExplicitDefaultInstantiationWithValueEqualToDefault() throws ConfigurationException {
		Map<String, String> initialValues = singletonMap(PropertyWithDefaultConfig.PROPERTY_NAME_EXAMPLE, "3");
		ConfigurationItem item = newConfigItem(PropertyWithDefaultConfig.class, initialValues.entrySet());
		assertValueSet(true, item, PropertyWithDefaultConfig.PROPERTY_NAME_EXAMPLE);
	}

	public void testPrimitiveBoolean() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_PRIMITIVE_BOOLEAN, false);
	}

	public void testPrimitiveChar() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_PRIMITIVE_CHAR, (char) 0);
	}

	public void testPrimitiveByte() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_PRIMITIVE_BYTE, (byte) 0);
	}

	public void testPrimitiveShort() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_PRIMITIVE_SHORT, (short) 0);
	}

	public void testPrimitiveInt() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_PRIMITIVE_INT, 0);
	}

	public void testPrimitiveLong() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_PRIMITIVE_LONG, 0L);
	}

	public void testPrimitiveFloat() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_PRIMITIVE_FLOAT, 0f);
	}

	public void testPrimitiveDouble() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_PRIMITIVE_DOUBLE, 0d);
	}

	public void testObjectBoolean() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_OBJECT_BOOLEAN, Boolean.FALSE);
	}

	public void testObjectCharacter() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_OBJECT_CHARACTER, Character.valueOf((char) 0));
	}

	public void testObjectByte() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_OBJECT_BYTE, Byte.valueOf((byte) 0));
	}

	public void testObjectShort() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_OBJECT_SHORT, Short.valueOf((short) 0));
	}

	public void testObjectInteger() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_OBJECT_INTEGER, Integer.valueOf(0));
	}

	public void testObjectLong() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_OBJECT_LONG, Long.valueOf(0));
	}

	public void testObjectFloat() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_OBJECT_FLOAT, Float.valueOf(0f));
	}

	public void testObjectDouble() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_OBJECT_DOUBLE, Double.valueOf(0d));
	}

	public void testDate() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_DATE, new Date(0));
	}

	public void testString() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_STRING, "");
	}

	public void testEnum() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_ENUM, ExampleEnum.FIRST);
	}

	public void testClass() {
		assertValueSet(AllPrimitives.class, AllPrimitives.PROPERTY_NAME_CLASS, null);
	}

	public void testConfigItem() {
		Class<? extends ConfigurationItem> configClass = ItemKindConfig.class;
		String propertyName = ItemKindConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(configClass, propertyName).kind() == PropertyKind.ITEM;
		assertValueSet(configClass, propertyName, null);
	}

	public void testReferenceItem() {
		Class<? extends ConfigurationItem> configClass = ReferenceKindConfig.class;
		String propertyName = ReferenceKindConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(configClass, propertyName).kind() == PropertyKind.REF;
		assertValueSet(configClass, propertyName, null);
	}

	public void testContainerItem() {
		Class<? extends ConfigurationItem> configClass = ContainerKindConfig.class;
		String propertyName = ContainerKindConfig.PROPERTY_NAME_CONTAINER;
		assert getProperty(configClass, propertyName).hasContainerAnnotation();

		ContainerKindConfig item = (ContainerKindConfig) create(configClass);
		assertValueSet(false, item, propertyName);
		ConfigurationItem initialContainer = item.getContainer();
		assert initialContainer == null;

		ItemKindConfig container = create(ItemKindConfig.class);
		setValue(container, ItemKindConfig.PROPERTY_NAME_EXAMPLE, item);
		assertValueSet(false, item, propertyName);
		assertEquals(container, item.getContainer());
	}

	public void testDerived() {
		Class<? extends ConfigurationItem> configClass = DerivedKindConfig.class;
		String propertyName = DerivedKindConfig.PROPERTY_NAME_DERIVED;
		assert getProperty(configClass, propertyName).kind() == PropertyKind.DERIVED;

		DerivedKindConfig item = (DerivedKindConfig) create(configClass);
		assertValueSet(false, item, propertyName);

		int newValue = 1;
		setValue(item, DerivedKindConfig.PROPERTY_NAME_SOURCE, newValue);
		assertEquals(newValue, item.getDerived());
		assertValueSet(false, item, propertyName);
	}

	public void testListOfKindList_SetList() {
		Class<ListOfKindListConfig> configClass = ListOfKindListConfig.class;
		String propertyName = ListOfKindListConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(configClass, propertyName).kind() == PropertyKind.LIST;
		List<ExampleConfig> newValue = new ArrayList<>();
		assertValueSet(configClass, propertyName, newValue);
	}

	public void testArrayKind() {
		ArrayKindConfig item = create(ArrayKindConfig.class);
		String propertyName = ArrayKindConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(item, propertyName).kind() == PropertyKind.ARRAY;
		assertValueSet(false, item, propertyName);
		item.setExample(new ExampleConfig[0]);
		assertValueSet(true, item, propertyName);
	}

	public void testListOfKindList_ChangeList() {
		ListOfKindListConfig item = create(ListOfKindListConfig.class);
		String propertyName = ListOfKindListConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(item, propertyName).kind() == PropertyKind.LIST;
		assertValueSet(false, item, propertyName);
		item.getExample().add(create(ExampleConfig.class));
		assertValueSet(true, item, propertyName);
	}

	public void testListOfKindComplex_SetList() {
		Class<ListOfKindComplexConfig> configClass = ListOfKindComplexConfig.class;
		String propertyName = ListOfKindComplexConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(configClass, propertyName).kind() == PropertyKind.COMPLEX;
		List<String> newValue = new ArrayList<>();
		assertValueSet(configClass, propertyName, newValue);
	}

	public void testListOfKindComplex_ChangeList() {
		ListOfKindComplexConfig item = create(ListOfKindComplexConfig.class);
		String propertyName = ListOfKindComplexConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(item, propertyName).kind() == PropertyKind.COMPLEX;
		assertValueSet(false, item, propertyName);
		item.setExample(Arrays.asList("test value"));
		assertValueSet(true, item, propertyName);
	}

	public void testMapOfKindMap_SetMap() {
		Class<MapOfKindMapConfig> configClass = MapOfKindMapConfig.class;
		String propertyName = MapOfKindMapConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(configClass, propertyName).kind() == PropertyKind.MAP;
		Map<Integer, ExampleConfig> newValue = new HashMap<>();
		assertValueSet(configClass, propertyName, newValue);
	}

	public void testMapOfKindMap_ChangeMap() {
		MapOfKindMapConfig item = create(MapOfKindMapConfig.class);
		String propertyName = MapOfKindMapConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(item, propertyName).kind() == PropertyKind.MAP;
		assertValueSet(false, item, propertyName);
		item.getExampleMap().put(0, create(ExampleConfig.class));
		assertValueSet(true, item, propertyName);
	}

	public void testMapOfKindComplex_SetMap() {
		Class<MapOfKindComplexConfig> configClass = MapOfKindComplexConfig.class;
		String propertyName = MapOfKindComplexConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(configClass, propertyName).kind() == PropertyKind.COMPLEX;
		Map<String, String> newValue = new HashMap<>();
		assertValueSet(configClass, propertyName, newValue);
	}

	public void testMapOfKindComplex_ChangeMap() {
		MapOfKindComplexConfig item = create(MapOfKindComplexConfig.class);
		String propertyName = MapOfKindComplexConfig.PROPERTY_NAME_EXAMPLE;
		assert getProperty(item, propertyName).kind() == PropertyKind.COMPLEX;
		assertValueSet(false, item, propertyName);
		item.setExampleMap(Collections.singletonMap("test key", "test value"));
		assertValueSet(true, item, propertyName);
	}

	private void assertValueSet(
			Class<? extends ConfigurationItem> configItemClass, String propertyName, Object newValue) {
		ConfigurationItem item = create(configItemClass);
		assertValueSet(false, item, propertyName);
		setValue(item, propertyName, newValue);
		assertValueSet(true, item, propertyName);
	}

	private void assertValueSet(boolean expected, ConfigurationItem item, String propertyName) {
		PropertyDescriptor property = getProperty(item, propertyName);
		assertValueSet(expected, item, property);
	}

	private void assertValueSet(boolean expected, ConfigurationItem item, PropertyDescriptor property) {
		boolean actual = item.valueSet(property);
		assertEquals(expected, actual);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestValueSet.class);
	}

}
