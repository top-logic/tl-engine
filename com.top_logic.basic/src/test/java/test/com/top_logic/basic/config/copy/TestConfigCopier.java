/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.copy;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeArray;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeConfigPart;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeConfiguredInstance;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeConfiguredInstanceConfig;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeConfiguredInstanceItem;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeDate;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeDefaultArrayValue;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeDefaultItemValue;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeDefaultListValue;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeDefaultValue;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeDifferentDefaultValue;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeEntry;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeItem;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeJavaPrimitive;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeList;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeMap;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeMapEntry;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeReference;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeReferenceFromArray;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeReferenceFromList;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeReferenceFromMap;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeReferencePropertyOrder;
import test.com.top_logic.basic.config.copy.ScenarioConfigCopier.ScenarioTypeSubclass;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.config.equal.ConfigEquality;

/**
 * {@link TestCase} for {@link ConfigCopier}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigCopier extends AbstractTypedConfigurationTestCase {

	public void testNull() {
		try {
			ConfigCopier.copy(null);
		} catch (NullPointerException ex) {
			return;
		}
		fail("Copying null is not possible.");
	}

	public void testEmpty() {
		assertCopy(create(ConfigurationItem.class));
	}

	public void testSubclass() {
		assertCopy(create(ScenarioTypeSubclass.class));
	}

	public void testJavaPrimitive() {
		ScenarioTypeJavaPrimitive original = create(ScenarioTypeJavaPrimitive.class);
		original.setExample(3);
		assertCopy(original);
	}

	public void testDate() {
		ScenarioTypeDate original = create(ScenarioTypeDate.class);
		original.setExample(new Date());
		assertCopy(original);
	}

	public void testItem() {
		ScenarioTypeItem outer = create(ScenarioTypeItem.class);
		ScenarioTypeItem inner = create(ScenarioTypeItem.class);
		outer.setExample(inner);
		assertCopy(outer);
	}

	public void testArray() {
		ScenarioTypeArray outer = create(ScenarioTypeArray.class);
		List<ScenarioTypeEntry> list = new ArrayList<>();
		ScenarioTypeEntry entry1 = create(ScenarioTypeEntry.class);
		entry1.setExample(1);
		list.add(entry1);
		ScenarioTypeEntry entry2 = create(ScenarioTypeEntry.class);
		entry2.setExample(2);
		list.add(entry2);
		ScenarioTypeEntry entry3 = create(ScenarioTypeEntry.class);
		entry3.setExample(3);
		list.add(entry3);
		outer.setExample(list.toArray(new ScenarioTypeEntry[list.size()]));
		assertCopy(outer);
	}

	public void testList() {
		ScenarioTypeList outer = create(ScenarioTypeList.class);
		List<ConfigurationItem> list = new ArrayList<>();
		ScenarioTypeEntry entry1 = create(ScenarioTypeEntry.class);
		entry1.setExample(1);
		list.add(entry1);
		ScenarioTypeEntry entry2 = create(ScenarioTypeEntry.class);
		entry2.setExample(2);
		list.add(entry2);
		ScenarioTypeEntry entry3 = create(ScenarioTypeEntry.class);
		entry3.setExample(3);
		list.add(entry3);
		outer.setExample(list);
		assertCopy(outer);
	}

	public void testMap() {
		ScenarioTypeMap outer = create(ScenarioTypeMap.class);
		Map<Integer, ConfigurationItem> map = new HashMap<>();
		ScenarioTypeEntry entry1 = create(ScenarioTypeEntry.class);
		entry1.setExample(1);
		map.put(entry1.getExample(), entry1);
		ScenarioTypeEntry entry2 = create(ScenarioTypeEntry.class);
		entry2.setExample(2);
		map.put(entry2.getExample(), entry2);
		ScenarioTypeEntry entry3 = create(ScenarioTypeEntry.class);
		entry3.setExample(3);
		map.put(entry3.getExample(), entry3);
		outer.setExample(map);
		assertCopy(outer);
	}

	public void testMapOrder() {
		ScenarioTypeMap original = create(ScenarioTypeMap.class);
		List<Integer> order = new ArrayList<>();
		long seed = "testMapOrder".hashCode();
		Random random = new Random(seed);
		int testSize = 100; // Use a lot of object to be make the test as stable as possible.
		for (int i = 0; i < testSize; i++) {
			ScenarioTypeEntry entry = create(ScenarioTypeEntry.class);
			int key = random.nextInt();
			entry.setExample(key);
			order.add(key);
			original.getExample().put(entry.getExample(), entry);
		}
		ScenarioTypeMap copy = ConfigCopier.copy(original);
		/* Don't try to compare the map "values", as they are ConfigItems which are compared by
		 * identity, not by value. */
		assertEquals(original.getExample().keySet(), copy.getExample().keySet());
		int i = 0;
		for (Entry<Integer, ScenarioTypeEntry> entry : copy.getExample().entrySet()) {
			assertEquals(order.get(i), entry.getKey());
			i += 1;
		}
		assertEquals(i, order.size());
	}

	public void testConfiguredInstance() {
		ScenarioTypeConfiguredInstanceItem outer = create(ScenarioTypeConfiguredInstanceItem.class);
		ScenarioTypeConfiguredInstanceConfig innerConfig = create(ScenarioTypeConfiguredInstanceConfig.class);
		innerConfig.setInt(1);
		ScenarioTypeConfiguredInstance inner = instantiate(innerConfig);
		outer.setExample(inner);
		assertCopy(outer);
	}

	public void testDefaultValue() {
		ScenarioTypeDefaultValue original = create(ScenarioTypeDefaultValue.class);
		assertCopy(original);
		ScenarioTypeDefaultValue copy = ConfigCopier.copy(original);
		PropertyDescriptor defaultValueProperty = getProperty(copy, ScenarioTypeDefaultValue.PROPERTY_NAME_EXAMPLE);
		assertFalse(copy.valueSet(defaultValueProperty));
	}

	public void testDefaultArrayValue() {
		ScenarioTypeDefaultArrayValue original = create(ScenarioTypeDefaultArrayValue.class);
		PropertyDescriptor property = getProperty(original, ScenarioTypeDefaultArrayValue.VALUES);
		assertEquals(2, original.getValues().length);
		original.getValues()[0].setName("foo");
		original.getValues()[1].setName("bar");
		assertFalse(original.valueSet(property));
		ScenarioTypeDefaultArrayValue copy = ConfigCopier.copy(original);
		assertEquals(2, copy.getValues().length);
		assertEquals("foo", copy.getValues()[0].getName());
		assertEquals("bar", copy.getValues()[1].getName());
		assertFalse(copy.valueSet(property));
		assertCopy(original, copy);
	}

	public void testDefaultListValue() {
		ScenarioTypeDefaultListValue original = create(ScenarioTypeDefaultListValue.class);
		PropertyDescriptor property = getProperty(original, ScenarioTypeDefaultListValue.VALUES);
		assertEquals(2, original.getValues().size());
		original.getValues().get(0).setName("foo");
		original.getValues().get(1).setName("bar");
		assertFalse(original.valueSet(property));
		ScenarioTypeDefaultListValue copy = ConfigCopier.copy(original);
		assertEquals(2, copy.getValues().size());
		assertEquals("foo", copy.getValues().get(0).getName());
		assertEquals("bar", copy.getValues().get(1).getName());
		assertFalse(copy.valueSet(property));
		assertCopy(original, copy);
	}

	public void testDefaultItemValue() {
		ScenarioTypeDefaultItemValue original = create(ScenarioTypeDefaultItemValue.class);
		PropertyDescriptor property = getProperty(original, ScenarioTypeDefaultItemValue.VALUE);
		assertNotNull(original.getValue());
		original.getValue().setName("foo");
		assertFalse(original.valueSet(property));
		ScenarioTypeDefaultItemValue copy = ConfigCopier.copy(original);
		assertNotNull(copy.getValue());
		assertEquals("foo", copy.getValue().getName());
		assertFalse(copy.valueSet(property));
		assertCopy(original, copy);
	}

	public void testDefaultValueExplicitSet() {
		ScenarioTypeDefaultValue original = create(ScenarioTypeDefaultValue.class);
		PropertyDescriptor defaultValueProperty = getProperty(original, ScenarioTypeDefaultValue.PROPERTY_NAME_EXAMPLE);
		original.setExample((Integer) defaultValueProperty.getDefaultValue());
		assertCopy(original);
		ScenarioTypeDefaultValue copy = ConfigCopier.copy(original);
		assertTrue(copy.valueSet(defaultValueProperty));
	}

	public void testFillAndDefaultValue() {
		ScenarioTypeDefaultValue source = create(ScenarioTypeDefaultValue.class);
		ConfigBuilder sink = TypedConfiguration.createConfigBuilder(ScenarioTypeDifferentDefaultValue.class);
		ConfigCopier.fillDeepCopy(source, sink, getInstantiationContext());
		PropertyDescriptor sinkProperty = getProperty(sink, ScenarioTypeDifferentDefaultValue.PROPERTY_NAME_EXAMPLE);
		assertFalse(sink.valueSet(sinkProperty));
		assertFalse(CollectionUtil.equals(source.getExample(), sink.value(sinkProperty)));
	}

	public void testFillAndDefaultValueAsExplicitValue() {
		ScenarioTypeDefaultValue source = create(ScenarioTypeDefaultValue.class);
		ConfigBuilder sink = TypedConfiguration.createConfigBuilder(ScenarioTypeDifferentDefaultValue.class);
		PropertyDescriptor sinkProperty = getProperty(sink, ScenarioTypeDifferentDefaultValue.PROPERTY_NAME_EXAMPLE);
		// This is not the default value of "source", only of "sink":
		source.setExample((Integer) sinkProperty.getDefaultValue());
		ConfigCopier.fillDeepCopy(source, sink, getInstantiationContext());
		assertTrue(sink.valueSet(sinkProperty));
		assertTrue(CollectionUtil.equals(source.getExample(), sink.value(sinkProperty)));
	}

	public void testOverrideNoExplicitValue() {
		ScenarioTypeDefaultValue source = create(ScenarioTypeDefaultValue.class);
		PropertyDescriptor sourceProperty = getProperty(source, ScenarioTypeDefaultValue.PROPERTY_NAME_EXAMPLE);
		Integer explicitSourceValue = 10;
		source.update(sourceProperty, explicitSourceValue);

		ConfigBuilder sink = TypedConfiguration.createConfigBuilder(ScenarioTypeDifferentDefaultValue.class);
		PropertyDescriptor sinkProperty = getProperty(sink, ScenarioTypeDifferentDefaultValue.PROPERTY_NAME_EXAMPLE);
		Integer explicitSinkValue = 5;
		sink.update(sinkProperty, explicitSinkValue);

		ConfigCopier.fillDeepCopy(source, sink, getInstantiationContext());
		assertEquals(explicitSinkValue, sink.value(sinkProperty));
	}

	public void testLocation() {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(ScenarioTypeJavaPrimitive.class);
		builder.initLocation("my-file", 47, 11);
		builder.update(builder.descriptor().getProperty(ScenarioTypeJavaPrimitive.EXAMPLE), 13);
		ScenarioTypeJavaPrimitive item =
			(ScenarioTypeJavaPrimitive) builder.createConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);

		assertEquals(13, item.getExample());
		assertEquals("my-file", item.location().getResource());
		assertEquals(47, item.location().getLine());
		assertEquals(11, item.location().getColumn());

		ScenarioTypeJavaPrimitive copy = TypedConfiguration.copy(item);
		
		assertEquals(13, copy.getExample());
		assertEquals("my-file", copy.location().getResource());
		assertEquals(47, copy.location().getLine());
		assertEquals(11, copy.location().getColumn());
	}

	public void testCopyBuilder() {
		ScenarioTypeItem outer = create(ScenarioTypeItem.class);
		ScenarioTypeItem inner = create(ScenarioTypeItem.class);
		outer.setExample(inner);
		ConfigBuilder copy = ConfigCopier.copyBuilder(outer, getInstantiationContext());
		PropertyDescriptor propertyCopy = getProperty(copy, ScenarioTypeItem.PROPERTY_NAME_EXAMPLE);
		assertTrue(copy.value(propertyCopy) instanceof ConfigBuilder);
		ConfigurationItem copiedItem = copy.createConfig(getInstantiationContext());
		ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(outer, copiedItem);
	}

	public void testReference() {
		ScenarioTypeReference originalOuter = create(ScenarioTypeReference.class);
		ScenarioTypeMapEntry originalInner = createEntry("arbitrary key", "arbitrary value");
		originalOuter.setSource(originalInner);
		originalOuter.setReference(originalInner);
		ScenarioTypeReference copyOuter = ConfigCopier.copy(originalOuter);
		assertCopy(originalOuter, copyOuter);
		assertCopy(originalOuter.getSource(), copyOuter.getSource());
		assertCopy(originalOuter.getReference(), copyOuter.getReference());
		assertSame(copyOuter.getSource(), copyOuter.getReference());
	}

	public void testReferenceFromArray() {
		ScenarioTypeReferenceFromArray originalOuter = create(ScenarioTypeReferenceFromArray.class);
		ScenarioTypeMapEntry inner0 = createEntry("arbitrary key 1", "arbitrary value 1");
		ScenarioTypeMapEntry inner1 = createEntry("arbitrary key 2", "arbitrary value 2");
		ScenarioTypeMapEntry inner2 = createEntry("arbitrary key 3", "arbitrary value 3");
		originalOuter.setSource(inner0, inner1, inner2);
		originalOuter.setReference(inner1);
		ScenarioTypeReferenceFromArray copyOuter = ConfigCopier.copy(originalOuter);
		assertCopy(originalOuter, copyOuter);
		assertCopy(originalOuter.getSource()[0], copyOuter.getSource()[0]);
		assertCopy(originalOuter.getSource()[1], copyOuter.getSource()[1]);
		assertCopy(originalOuter.getSource()[2], copyOuter.getSource()[2]);
		assertCopy(originalOuter.getReference(), copyOuter.getReference());
		assertSame(copyOuter.getSource()[1], copyOuter.getReference());
	}

	public void testReferenceFromList() {
		ScenarioTypeReferenceFromList originalOuter = create(ScenarioTypeReferenceFromList.class);
		ScenarioTypeMapEntry inner0 = createEntry("arbitrary key 1", "arbitrary value 1");
		ScenarioTypeMapEntry inner1 = createEntry("arbitrary key 2", "arbitrary value 2");
		ScenarioTypeMapEntry inner2 = createEntry("arbitrary key 3", "arbitrary value 3");
		originalOuter.setSource(list(inner0, inner1, inner2));
		originalOuter.setReference(inner1);
		ScenarioTypeReferenceFromList copyOuter = ConfigCopier.copy(originalOuter);
		assertCopy(originalOuter, copyOuter);
		assertCopy(originalOuter.getSource().get(0), copyOuter.getSource().get(0));
		assertCopy(originalOuter.getSource().get(1), copyOuter.getSource().get(1));
		assertCopy(originalOuter.getSource().get(2), copyOuter.getSource().get(2));
		assertCopy(originalOuter.getReference(), copyOuter.getReference());
		assertSame(copyOuter.getSource().get(1), copyOuter.getReference());
	}

	public void testReferenceFromMap() {
		ScenarioTypeReferenceFromMap originalOuter = create(ScenarioTypeReferenceFromMap.class);
		ScenarioTypeMapEntry inner0 = createEntry("arbitrary key 1", "arbitrary value 1");
		ScenarioTypeMapEntry inner1 = createEntry("arbitrary key 2", "arbitrary value 2");
		ScenarioTypeMapEntry inner2 = createEntry("arbitrary key 3", "arbitrary value 3");
		Map<String, ScenarioTypeMapEntry> map = map();
		map.put(inner0.getKey(), inner0);
		map.put(inner1.getKey(), inner1);
		map.put(inner2.getKey(), inner2);
		originalOuter.setSource(map);
		originalOuter.setReference(inner1);
		ScenarioTypeReferenceFromMap copyOuter = ConfigCopier.copy(originalOuter);
		assertCopy(originalOuter, copyOuter);
		Map<String, ScenarioTypeMapEntry> originalSource = originalOuter.getSource();
		Map<String, ScenarioTypeMapEntry> copySource = copyOuter.getSource();
		assertCopy(originalSource.get(inner0.getKey()), copySource.get(inner0.getKey()));
		assertCopy(originalSource.get(inner1.getKey()), copySource.get(inner1.getKey()));
		assertCopy(originalSource.get(inner2.getKey()), copySource.get(inner2.getKey()));

		assertCopy(originalOuter.getReference(), copyOuter.getReference());
		assertSame(copySource.get(inner1.getKey()), copyOuter.getReference());
	}

	private ScenarioTypeMapEntry createEntry(String key, String value) {
		ScenarioTypeMapEntry item = create(ScenarioTypeMapEntry.class);
		item.setKey(key);
		item.setValue(value);
		return item;
	}

	/** @see ScenarioTypeReferencePropertyOrder */
	public void testReferencePropertyOrder() {
		ScenarioTypeReferencePropertyOrder originalOuter = createReferencePropertyOrder();
		ScenarioTypeReferencePropertyOrder copyOuter = ConfigCopier.copy(originalOuter);
		assertReferencePropertyOrder(originalOuter, copyOuter);
	}

	private ScenarioTypeReferencePropertyOrder createReferencePropertyOrder() {
		ScenarioTypeReferencePropertyOrder originalOuter = create(ScenarioTypeReferencePropertyOrder.class);
		ScenarioTypeMapEntry originalInner = createEntry("arbitrary key", "arbitrary value");

		originalOuter.setProperty07Source(originalInner);
		originalOuter.setProperty01Reference(originalInner);
		originalOuter.setProperty02Reference(originalInner);
		originalOuter.setProperty03Reference(originalInner);
		originalOuter.setProperty04Reference(originalInner);
		originalOuter.setProperty05Reference(originalInner);
		originalOuter.setProperty06Reference(originalInner);
		originalOuter.setProperty08Reference(originalInner);
		originalOuter.setProperty09Reference(originalInner);
		originalOuter.setProperty10Reference(originalInner);
		originalOuter.setProperty11Reference(originalInner);
		originalOuter.setProperty12Reference(originalInner);
		originalOuter.setProperty13Reference(originalInner);
		return originalOuter;
	}

	private void assertReferencePropertyOrder(
			ScenarioTypeReferencePropertyOrder originalOuter, ScenarioTypeReferencePropertyOrder copyOuter) {
		assertCopy(originalOuter, copyOuter);
		assertCopy(originalOuter.getProperty07Source(), copyOuter.getProperty07Source());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty01Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty02Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty03Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty04Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty05Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty06Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty08Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty09Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty10Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty11Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty12Reference());
		assertSame(copyOuter.getProperty07Source(), copyOuter.getProperty13Reference());
	}

	public void testCopyContent() {
		ScenarioTypeItem source = create(ScenarioTypeItem.class);
		source.setExample(create(ScenarioTypeConfigPart.class));

		ScenarioTypeItem destination = create(ScenarioTypeItem.class);
		ConfigCopier.copyContent(getInstantiationContext(), source, destination);
		assertNotNull(destination.getExample());
		assertEquals(destination, ((ScenarioTypeConfigPart) destination.getExample()).getContainer());
		assertEquals(source, ((ScenarioTypeConfigPart) source.getExample()).getContainer());

	}

	private static <T extends ConfiguredInstance<?>> T instantiate(PolymorphicConfiguration<T> config) {
		return getInstantiationContext().getInstance(config);
	}

	private static InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	private void assertCopy(ConfigurationItem original) {
		ConfigurationItem copy = ConfigCopier.copy(original);
		assertCopy(original, copy);
	}

	private void assertCopy(ConfigurationItem original, ConfigurationItem copy) {
		assertNotSame(original, copy);
		/* Don't use AbstractTypedConfigurationTestCase.assertNotEquals, as that uses
		 * ConfigEquality.INSTANCE_ALL_BUT_DERIVED. Here, the equals implementation of the
		 * ConfigItem itself should be used. */
		assertFalse(original.equals(copy));
		assertEquals(original.getClass(), copy.getClass());
		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(original, copy));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestConfigCopier.class);
	}

}
