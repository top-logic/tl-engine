/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.annotation;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static java.util.Collections.*;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeEnum;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeEnumProperty;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeEnumPropertyNullDefault;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeEnumPropertyNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeEnumPropertyNullableNullDefault;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeJavaPrimitiveProperty;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeJavaPrimitivePropertyNonNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeJavaPrimitivePropertyNullDefault;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeJavaPrimitivePropertyNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeLocalNonNullableInheritedNullableLocalSetter;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeLocalNonNullableInheritedNullableNoSetter;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeLocalNothingInheritedNonNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeLocalNothingInheritedNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeLocalNullDefaultInheritedNonNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeLocalNullableInheritedNonNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindDerived;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindDerivedNullDefault;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindDerivedNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindItem;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindItemNonNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindItemNullDefault;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindItemNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindList;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindListNonNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindListNullDefault;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindListNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindMap;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindMapNonNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindMapNullDefault;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypePropertyKindMapNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeStringProperty;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeStringPropertyNullDefault;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeStringPropertyNullable;
import test.com.top_logic.basic.config.annotation.ScenarioNullable.ScenarioTypeStringPropertyNullableNullDefault;

import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Tests for {@link Nullable}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestNullable extends AbstractTypedConfigurationTestCase {

	private static final String PROPERTY_NAME = ScenarioNullable.EXAMPLE_PROPERTY_NAME;

	public void testIsNonNullable() {
		String message = "A property without @Nullable annotation has to be non-nullable.";
		assertNonNullable(message, ScenarioTypeStringProperty.class, PROPERTY_NAME);
	}

	public void testIsNullable_Nullable() {
		String message = "A property without @Nullable annotation has to be non-nullable.";
		assertNullable(message, ScenarioTypeStringPropertyNullable.class, PROPERTY_NAME);
	}

	public void testIsNullable_NullDefault() {
		String message = "A property without @Nullable annotation has to be non-nullable.";
		assertNullable(message, ScenarioTypeStringPropertyNullDefault.class, PROPERTY_NAME);
	}

	public void testIsNullable_NullableNullDefault() {
		String message = "A property without @Nullable annotation has to be non-nullable.";
		assertNullable(message, ScenarioTypeStringPropertyNullableNullDefault.class, PROPERTY_NAME);
	}

	public void testSetToNull_String() {
		ScenarioTypeStringProperty item = create(ScenarioTypeStringProperty.class);
		// Ensure it is not null, when it is later set to null.
		assertNotNull(item.getExample());
		item.setExample(null);
		assertEquals("", item.getExample());
	}

	public void testSetToNull_Enum() {
		ScenarioTypeEnumProperty item = create(ScenarioTypeEnumProperty.class);
		// Ensure it is not null, when it is later set to null.
		item.setExample(ScenarioTypeEnum.A);
		assert item.getExample() != null;

		try {
			item.setExample(null);
		} catch (Exception ex) {
			// Expected
			return;
		}
		fail("Setting an enum property to null has to fail.");
	}

	public void testSetToNull_Nullable_String() {
		ScenarioTypeStringPropertyNullable item = create(ScenarioTypeStringPropertyNullable.class);
		// Ensure it is not null, when it is later set to null.
		item.setExample("non null");
		assert item.getExample() != null;

		item.setExample(null);
		assertNull(item.getExample());
	}

	public void testSetToEmptyString_Nullable_String() {
		ScenarioTypeStringPropertyNullable item = create(ScenarioTypeStringPropertyNullable.class);
		// Ensure it is not "", when it is later set to "".
		item.setExample("non null");
		item.setExample("");
		assertNull(item.getExample());
	}

	public void testSetToNull_Nullable_Enum() {
		ScenarioTypeEnumPropertyNullable item = create(ScenarioTypeEnumPropertyNullable.class);
		// Ensure it is not null, when it is later set to null.
		item.setExample(ScenarioTypeEnum.A);
		assert item.getExample() != null;

		item.setExample(null);
		assertNull(item.getExample());
	}

	public void testSetToNull_NullDefault_String() {
		ScenarioTypeStringPropertyNullDefault item = create(ScenarioTypeStringPropertyNullDefault.class);
		// Ensure it is not null, when it is later set to null.
		item.setExample("non null");
		assert item.getExample() != null;

		item.setExample(null);
		assertNull(item.getExample());
	}

	public void testSetToNull_NullDefault_Enum() {
		ScenarioTypeEnumPropertyNullDefault item = create(ScenarioTypeEnumPropertyNullDefault.class);
		// Ensure it is not null, when it is later set to null.
		item.setExample(ScenarioTypeEnum.A);
		assert item.getExample() != null;

		item.setExample(null);
		assertNull(item.getExample());
	}

	public void testSetToNull_NullableNullDefault_String() {
		ScenarioTypeStringPropertyNullableNullDefault item =
			create(ScenarioTypeStringPropertyNullableNullDefault.class);
		// Ensure it is not null, when it is later set to null.
		item.setExample("non null");
		assert item.getExample() != null;

		item.setExample(null);
		assertNull(item.getExample());
	}

	public void testSetToNull_NullableNullDefault_Enum() {
		ScenarioTypeEnumPropertyNullableNullDefault item = create(ScenarioTypeEnumPropertyNullableNullDefault.class);
		// Ensure it is not null, when it is later set to null.
		item.setExample(ScenarioTypeEnum.A);
		assert item.getExample() != null;

		item.setExample(null);
		assertNull(item.getExample());
	}

	public void testDefault_String() {
		ScenarioTypeStringProperty item = create(ScenarioTypeStringProperty.class);
		assertEquals("", item.getExample());
	}

	public void testDefault_Enum() {
		ScenarioTypeEnumProperty item = create(ScenarioTypeEnumProperty.class);
		assertEquals(ScenarioTypeEnum.A, item.getExample());
	}

	public void testDefault_Nullable_String() {
		ScenarioTypeStringPropertyNullable item = create(ScenarioTypeStringPropertyNullable.class);
		assertEquals(null, item.getExample());
	}

	public void testDefault_Nullable_Enum() {
		ScenarioTypeEnumPropertyNullable item = create(ScenarioTypeEnumPropertyNullable.class);
		assertEquals(ScenarioTypeEnum.A, item.getExample());
	}

	public void testDefault_NullDefault_String() {
		ScenarioTypeStringPropertyNullDefault item = create(ScenarioTypeStringPropertyNullDefault.class);
		assertEquals(null, item.getExample());
	}

	public void testDefault_NullDefaultEnum() {
		ScenarioTypeEnumPropertyNullDefault item = create(ScenarioTypeEnumPropertyNullDefault.class);
		assertEquals(null, item.getExample());
	}

	public void testDefault_NullableNullDefault_String() {
		ScenarioTypeStringPropertyNullableNullDefault item =
			create(ScenarioTypeStringPropertyNullableNullDefault.class);
		assertEquals(null, item.getExample());
	}

	public void testDefault_NullableNullDefault_Enum() {
		ScenarioTypeEnumPropertyNullableNullDefault item = create(ScenarioTypeEnumPropertyNullableNullDefault.class);
		assertEquals(null, item.getExample());
	}

	public void testLocalNothingInheritedNullable() {
		String message = "Nullable has to be inherited but is not.";
		assertNullable(message, ScenarioTypeLocalNothingInheritedNullable.class, PROPERTY_NAME);
	}

	public void testLocalNonNullableInheritedNullable_NoSetter() {
		String message = "Changing a property from nullable to non-nullable when there is no setter does not work.";
		assertNonNullable(message, ScenarioTypeLocalNonNullableInheritedNullableNoSetter.class, PROPERTY_NAME);
	}

	public void testLocalNonNullableInheritedNullable_LocalSetter() {
		String message = "Changing a property from nullable to non-nullable"
			+ " when there is a local but no inherited setter does not work.";
		assertNonNullable(message, ScenarioTypeLocalNonNullableInheritedNullableLocalSetter.class, PROPERTY_NAME);
	}

	public void testIsNullable_LocalNothingInheritedNullDefault() {
		String message = "A property has to be nullable, if the super-properties are nullable."
			+ " Otherwise that property would break the contract about what values is it allowed to accept.";
		assertNullable(message, ScenarioTypeLocalNothingInheritedNullable.class, PROPERTY_NAME);
	}

	public void testLocalNothingInheritedNonNullable() {
		String message = "Non-nullable has to be inherited but is not.";
		assertNonNullable(message, ScenarioTypeLocalNothingInheritedNonNullable.class, PROPERTY_NAME);
	}

	public void testLocalNullableInheritedNonNullable() {
		String message = "Making a non-nullable property nullable must not be allowed,"
			+ " as it would break the contract about the values the property is allowed to return.";
		String errorPart = "A property cannot be made nullable, if it has an non-nullable parent.";
		assertIllegal(message, errorPart, ScenarioTypeLocalNullableInheritedNonNullable.class);
	}

	public void testLocalNullDefaultInheritedNonNullable() {
		String message = "Making a non-nullable property nullable must not be allowed,"
			+ " as it would break the contract about the values the property is allowed to return.";
		String errorPart = "A property cannot be made nullable, if it has an non-nullable parent.";
		assertIllegal(message, errorPart, ScenarioTypeLocalNullDefaultInheritedNonNullable.class);
	}

	public void testJavaPrimitiveProperty() throws ConfigurationException {
		Iterable<? extends Entry<String, String>> values = singletonMap(PROPERTY_NAME, "1").entrySet();
		ConfigurationItem item = newConfigItem(ScenarioTypeJavaPrimitiveProperty.class, values);
		String message = "A property storing a Java primitive has to be non-nullable.";
		assertNonNullable(message, item, PROPERTY_NAME);
		assertNullIsRejected(message, item, PROPERTY_NAME);
	}

	public void testJavaPrimitiveProperty_NonNullable() throws ConfigurationException {
		Iterable<? extends Entry<String, String>> values = singletonMap(PROPERTY_NAME, "1").entrySet();
		ConfigurationItem item = newConfigItem(ScenarioTypeJavaPrimitivePropertyNonNullable.class, values);
		String message = "A property storing a Java primitive and annotated as non-nullable has to be non-nullable.";
		assertNonNullable(message, item, PROPERTY_NAME);
		assertNullIsRejected(message, item, PROPERTY_NAME);
	}

	public void testJavaPrimitiveProperty_Nullable() {
		String message = "A property storing a Java primitive cannot be annotated as nullable.";
		String errorPart = "A property storing a Java primitive cannot be declared as nullable.";
		assertIllegal(message, errorPart, ScenarioTypeJavaPrimitivePropertyNullable.class);
	}

	public void testJavaPrimitiveProperty_NullDefault() {
		String message = "A property storing a Java primitive cannot be annotated with @NullDefault.";
		String errorPart = "A property storing a Java primitive cannot be declared as nullable.";
		assertIllegal(message, errorPart, ScenarioTypeJavaPrimitivePropertyNullDefault.class);
	}

	public void testPropertyKindList() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindList.class);
		String message = "A property of kind 'List' has to be non-nullable.";
		assertNonNullable(message, item, PROPERTY_NAME);

		assertNotNull(item.value(getProperty(item, PROPERTY_NAME)));
		setValue(item, PROPERTY_NAME, null);
		assertEquals(emptyList(), item.value(getProperty(item, PROPERTY_NAME)));
	}

	public void testPropertyKindList_NonNullable() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindListNonNullable.class);
		String message = "A property of kind 'List' annotated as non-nullable has to be non-nullable.";
		assertNonNullable(message, item, PROPERTY_NAME);

		assertNotNull(item.value(getProperty(item, PROPERTY_NAME)));
		setValue(item, PROPERTY_NAME, null);
		assertEquals(emptyList(), item.value(getProperty(item, PROPERTY_NAME)));
	}

	public void testPropertyKindList_Nullable() {
		String message = "A property of kind 'List' cannot be annotated as nullable.";
		String errorPart = "Properties of kind 'LIST', 'MAP', and 'ARRAY' cannot be nullable.";
		assertIllegal(message, errorPart, ScenarioTypePropertyKindListNullable.class);
	}

	public void testPropertyKindList_NullDefault() {
		String message = "A property of kind 'List' cannot be annotated with @NullDefault.";
		String errorPart = "Properties of kind 'LIST', 'MAP', and 'ARRAY' cannot be nullable.";
		assertIllegal(message, errorPart, ScenarioTypePropertyKindListNullDefault.class);
	}

	public void testPropertyKindMap() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindMap.class);
		String message = "A property of kind 'Map' has to be non-nullable.";
		assertNonNullable(message, item, PROPERTY_NAME);

		assertNotNull(item.value(getProperty(item, PROPERTY_NAME)));
		setValue(item, PROPERTY_NAME, null);
		assertEquals(emptyMap(), item.value(getProperty(item, PROPERTY_NAME)));
	}

	public void testPropertyKindMap_NonNullable() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindMapNonNullable.class);
		String message = "A property of kind 'Map' annotated as non-nullable has to be non-nullable.";
		assertNonNullable(message, item, PROPERTY_NAME);

		assertNotNull(item.value(getProperty(item, PROPERTY_NAME)));
		setValue(item, PROPERTY_NAME, null);
		assertEquals(emptyMap(), item.value(getProperty(item, PROPERTY_NAME)));
	}

	public void testPropertyKindMap_Nullable() {
		String message = "A property of kind 'Map' cannot be annotated as nullable.";
		String errorPart = "Properties of kind 'LIST', 'MAP', and 'ARRAY' cannot be nullable.";
		assertIllegal(message, errorPart, ScenarioTypePropertyKindMapNullable.class);
	}

	public void testPropertyKindMap_NullDefault() {
		String message = "A property of kind 'Map' cannot be annotated with @NullDefault.";
		String errorPart = "Properties of kind 'LIST', 'MAP', and 'ARRAY' cannot be nullable.";
		assertIllegal(message, errorPart, ScenarioTypePropertyKindMapNullDefault.class);
	}

	public void testPropertyKindItem() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindItem.class);
		String message = "A property of kind 'List' has to be non-nullable.";
		assertNullable(message, item, PROPERTY_NAME);
		assertNullIsAccepted(message, item, PROPERTY_NAME);
	}

	public void testPropertyKindItem_NonNullable() {
		Class<? extends ConfigurationItem> type = ScenarioTypePropertyKindItemNonNullable.class;
		ConfigBuilder builder = createConfigBuilder(type);
		builder.update(getProperty(builder, PROPERTY_NAME), newConfigItem(ConfigurationItem.class));
		ConfigurationItem item = builder.createConfig(context);

		String message = "A property of kind 'Item' annotated as non-nullable has to be non-nullable.";
		assertNonNullable(message, item, PROPERTY_NAME);
		assertNullIsRejected(message, item, PROPERTY_NAME);
		assertInstantiationFails(message, type);
	}

	public void testPropertyKindItem_Nullable() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindItemNullable.class);
		String message = "A property of kind 'Item' annotated as nullable has to be nullable.";
		assertNullable(message, item, PROPERTY_NAME);
		assertNullIsAccepted(message, item, PROPERTY_NAME);
	}

	public void testPropertyKindItem_NullDefault() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindItemNullDefault.class);
		String message = "A property of kind 'Item' annotated with @NullDefault has to be nullable.";
		assertNullable(message, item, PROPERTY_NAME);

		// Change the value to non null for the check whether it can be reset to null.
		setValue(item, PROPERTY_NAME, create(ConfigurationItem.class));
		assertNullIsAccepted(message, item, PROPERTY_NAME);
	}

	public void testPropertyKindDerived() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindDerived.class);
		String message = "A property of kind 'Derived' have to be nullable.";
		assertNullable(message, item, PROPERTY_NAME);
		assertNullIsAccepted(message, item, "source");
	}

	public void testPropertyKindDerived_Nullable() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindDerivedNullable.class);
		String message = "A property of kind 'Derived' annotated as nullable has to be nullable.";
		assertNullable(message, item, PROPERTY_NAME);
		assertNullIsAccepted(message, item, "source");
	}

	public void testPropertyKindDerived_NullDefault() {
		ConfigurationItem item = newConfigItem(ScenarioTypePropertyKindDerivedNullDefault.class);
		String message = "A property of kind 'Derived' annotated with @NullDefault has to be nullable.";
		assertNullable(message, item, PROPERTY_NAME);
		assertNullIsAccepted(message, item, "source");
	}

	private void assertInstantiationFails(String messagePrefix, Class<? extends ConfigurationItem> type) {
		try {
			create(type);
		} catch (Exception ex) {
			// Good
			return;
		}
		fail(messagePrefix + " Instantiation is expected to fail, but succeeded.");
	}

	private void assertNullIsAccepted(String messagePrefix, ConfigurationItem item, String propertyName) {
		try {
			setValue(item, propertyName, null);
		} catch (RuntimeException ex) {
			BasicTestCase.fail(messagePrefix + " Setting a nullable property to null failed.", ex);
		}
	}

	private void assertNullIsRejected(String messagePrefix, ConfigurationItem item, String propertyName) {
		Object oldValue = item.value(getProperty(item, propertyName));
		assertNotNull(messagePrefix + " Non-nullable property is null.", oldValue);
		boolean success = trySetNull(item, propertyName);
		if (success) {
			fail(messagePrefix + " Setting a non-nullable property to null does not fail.");
		}
		Object newValue = item.value(getProperty(item, propertyName));
		String valueChangeMessage = " Setting a non-nullable property to null failed as expected,"
			+ " but also cause the property to change its value, which is not correct.";
		assertEquals(messagePrefix + valueChangeMessage, oldValue, newValue);
	}

	private boolean trySetNull(ConfigurationItem item, String propertyName) {
		try {
			setValue(item, propertyName, null);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private void assertNullable(String message, Class<? extends ConfigurationItem> type, String propertyName) {
		PropertyDescriptor property = getProperty(type, propertyName);
		assertTrue(message, property.isNullable());
	}

	private void assertNullable(String message, ConfigurationItem item, String propertyName) {
		PropertyDescriptor property = getProperty(item, propertyName);
		assertTrue(message, property.isNullable());
	}

	private void assertNonNullable(String message, ConfigurationItem item, String propertyName) {
		PropertyDescriptor property = getProperty(item, propertyName);
		assertFalse(message, property.isNullable());
	}

	private void assertNonNullable(String message, Class<? extends ConfigurationItem> type, String propertyName) {
		PropertyDescriptor property = getProperty(type, propertyName);
		assertFalse(message, property.isNullable());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestNullable.class);
	}

}
