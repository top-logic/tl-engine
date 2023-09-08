/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.FirstCharacterMapping;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ByteDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.ShortDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;

/**
 * Test for {@link TestDeclarativeConfigDescriptor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDeclarativeConfigDescriptor extends AbstractTypedConfigurationTestCase {

	public interface ExtendedConfig extends ConfigurationItem {

		String PROGRAMMATIC_STRING = "programmatic-string";

		@StringDefault("someDefault")
		@Name(PROGRAMMATIC_STRING)
		String getProgrammaticString();
	}

	public interface LeftExtendedConfig extends ExtendedConfig {

		@Override
		@StringDefault("otherDefault")
		String getProgrammaticString();
	}

	public interface RightExtendedConfig extends ExtendedConfig {

		@Override
		@Hidden
		String getProgrammaticString();
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("declarative-config-descriptor",
			TypedConfiguration.getConfigurationDescriptor(ConfigurationDescriptorConfig.class));
	}

	public void testConfigInterfaceExtension() throws Throwable {
		ConfigurationDescriptor descriptor = readDescriptor("simpleExtension.xml");
		
		ConfigurationDescriptor[] superDesc = descriptor.getSuperDescriptors();
		assertEquals(1, superDesc.length);
		ConfigurationDescriptor inheritedConfigDescr =
			TypedConfiguration.getConfigurationDescriptor(ExtendedConfig.class);
		assertEquals(inheritedConfigDescr, superDesc[0]);

		PropertyDescriptor stringProp = descriptor.getProperty(ExtendedConfig.PROGRAMMATIC_STRING);
		assertNotNull(stringProp);
		PropertyDescriptor[] superProperties = stringProp.getSuperProperties();
		assertEquals(1, superProperties.length);
		PropertyDescriptor inheritedStringProp = inheritedConfigDescr.getProperty(ExtendedConfig.PROGRAMMATIC_STRING);
		assertEquals(inheritedStringProp, superProperties[0]);

		ConfigurationItem newItem = descriptor.factory().createNew();
		assertEquals(descriptor, newItem.descriptor());
		assertEquals("someDefault", newItem.value(stringProp));
		assertTrue(newItem instanceof ExtendedConfig);
		assertEquals("Access via reflection method.", "someDefault",
			((ExtendedConfig) newItem).getProgrammaticString());

		ResKey label = stringProp.labelKey(null);
		ResKey inheritedLabel = inheritedStringProp.labelKey(null);
		assertEquals(inheritedLabel, label.fallback());
	}

	public void testOverrideInherited() throws Throwable {
		ConfigurationDescriptor descriptor = readDescriptor("overrideInherited.xml");

		PropertyDescriptor overriddenProperty = descriptor.getProperty(ExtendedConfig.PROGRAMMATIC_STRING);
		assertNotNull(overriddenProperty);

		ConfigurationItem newItem = descriptor.factory().createNew();
		assertEquals(descriptor, newItem.descriptor());
		assertEquals("otherDefault", newItem.value(overriddenProperty));
	}

	public void testMultipleExtension() throws Throwable {
		ConfigurationDescriptor descriptor = readDescriptor("multipleExtension.xml");

		PropertyDescriptor inheritedProperty = descriptor.getProperty(ExtendedConfig.PROGRAMMATIC_STRING);
		assertNotNull(inheritedProperty);
		PropertyDescriptor[] superProperties = inheritedProperty.getSuperProperties();
		assertEquals(2, superProperties.length);
		assertEquals(TypedConfiguration.getConfigurationDescriptor(LeftExtendedConfig.class)
			.getProperty(ExtendedConfig.PROGRAMMATIC_STRING), superProperties[0]);
		assertEquals(TypedConfiguration.getConfigurationDescriptor(RightExtendedConfig.class)
			.getProperty(ExtendedConfig.PROGRAMMATIC_STRING), superProperties[1]);

		ConfigurationItem newItem = descriptor.factory().createNew();
		assertEquals(descriptor, newItem.descriptor());
		assertEquals("otherDefault", newItem.value(inheritedProperty));
		assertNotNull(inheritedProperty.getAnnotation(Hidden.class));
		assertTrue(newItem instanceof LeftExtendedConfig);
		assertTrue(newItem instanceof RightExtendedConfig);
		assertEquals("Access using java method failed.", "otherDefault",
			((LeftExtendedConfig) newItem).getProgrammaticString());
		assertEquals("Access using java method failed.", "otherDefault",
			((RightExtendedConfig) newItem).getProgrammaticString());
	}

	public void testSimpleConfigurationDescriptor() throws Throwable {
		ConfigurationDescriptor descriptor = readDescriptor("simpleConfigurationDescriptor.xml");

		PropertyDescriptor stringProp = descriptor.getProperty("string-property");
		assertNotNull(stringProp);
		assertEquals(PropertyKind.PLAIN, stringProp.kind());
		assertEquals(String.class, stringProp.getType());
		assertEquals("Hello", stringProp.getDefaultValue());
		assertTrue(ResKey.encode(descriptor.getPropertyLabel(stringProp.getPropertyName(), null)).startsWith("a.b.c."));
		assertTrue(
			ResKey.encode(descriptor.getPropertyLabel(stringProp.getPropertyName(), "blaFasel")).endsWith("blaFasel"));

		PropertyDescriptor intProp = descriptor.getProperty("int-property");
		assertNotNull(intProp);
		assertEquals(PropertyKind.PLAIN, intProp.kind());
		assertEquals(Integer.class, intProp.getType());
		assertEquals(true, intProp.isMandatory());
		assertNotNull(intProp.getAnnotation(Hidden.class));

		PropertyDescriptor configProp = descriptor.getProperty("config-property");
		assertNotNull(configProp);
		assertEquals(PropertyKind.ITEM, configProp.kind());
		assertEquals(TestValue.class, configProp.getType());

		PropertyDescriptor mapProp = descriptor.getProperty("map-property");
		assertNotNull(mapProp);
		assertEquals(PropertyKind.MAP, mapProp.kind());
		assertEquals(Map.class, mapProp.getType());
		assertEquals(TestValue.class, mapProp.getElementType());
		assertEquals(TypedConfiguration.getConfigurationDescriptor(TestValue.class)
			.getProperty(TestValue.NAME_ATTRIBUTE), mapProp.getKeyProperty());

		PropertyDescriptor listProp = descriptor.getProperty("list-property");
		assertNotNull(listProp);
		assertEquals(PropertyKind.LIST, listProp.kind());
		assertEquals(List.class, listProp.getType());
		assertEquals(TestValue.class, listProp.getElementType());

		PropertyDescriptor listPropWithKey = descriptor.getProperty("list-property-with-key");
		assertNotNull(listPropWithKey);
		assertEquals(PropertyKind.LIST, listPropWithKey.kind());
		assertEquals(List.class, listPropWithKey.getType());
		assertEquals(TestValue.class, listPropWithKey.getElementType());
		assertEquals(TypedConfiguration.getConfigurationDescriptor(TestValue.class)
			.getProperty(TestValue.NAME_ATTRIBUTE), listPropWithKey.getKeyProperty());

		PropertyDescriptor plainlistProp = descriptor.getProperty("plain-list");
		assertNotNull(plainlistProp);
		assertEquals(List.class, plainlistProp.getType());
		assertNotNull(plainlistProp.getValueProvider());
		assertEquals(CommaSeparatedStrings.INSTANCE, plainlistProp.getValueProvider());

		PropertyDescriptor instanceTypeProp = descriptor.getProperty("instance-type");
		assertNotNull(instanceTypeProp);
		assertEquals(Mapping.class, instanceTypeProp.getType());
		assertEquals(FirstCharacterMapping.INSTANCE, instanceTypeProp.getDefaultValue());

		PropertyDescriptor resKeyProp = descriptor.getProperty("res-key");
		assertNotNull(resKeyProp);
		assertEquals(ResKey.class, resKeyProp.getType());

		ConfigurationItem newItem = descriptor.factory().createNew();
		assertEquals(descriptor, newItem.descriptor());
		try {
			newItem.check(new AssertProtocol());
			fail("Property int-property not set.");
		} catch (AssertionFailedError ex) {
			// expected
		}
		newItem.update(intProp, 15);
		newItem.check(protocol);
		try {
			newItem.update(intProp, "not an int");
			fail("Property value is not an int.");
		} catch (IllegalArgumentException ex) {
			// expected
		}
		assertEquals("Hello", newItem.value(stringProp));
		assertEquals(FirstCharacterMapping.INSTANCE, newItem.value(instanceTypeProp));
		assertEquals(ResKey.text("some constant text"), newItem.value(resKeyProp));
	}

	public void testAbstract() throws Throwable {
		ConfigurationDescriptor explicitAbstractDesc = readDescriptor("explicitAbstractConfigurationDescriptor.xml");
		assertTrue(explicitAbstractDesc.isAbstract());
		assertFalse(explicitAbstractDesc.getProperty("string-property").isAbstract());

		ConfigurationDescriptorConfig descriptorConfig =
			(ConfigurationDescriptorConfig) readConfiguration("errorConfigurationDescriptorWithAbstractProperty.xml");
		assertFalse(descriptorConfig.isAbstract());
		ConfigurationDescriptor implicitAbstractDesc;
		try {
			implicitAbstractDesc = TypedConfiguration.getConfigurationDescriptor(protocol, descriptorConfig);
			protocol.checkErrors();
		} catch (AssertionFailedError ex) {
			if (ex.getMessage().contains("must be declared abstract, since it has an abstract property")) {
				// expected
				implicitAbstractDesc = null;
			} else {
				throw ex;
			}
		}
		if (implicitAbstractDesc != null) {
			assertTrue("Test expects an abstract property",
				implicitAbstractDesc.getProperty("string-property").isAbstract());
			fail("Non abstract configuration descriptor with abstract property.");
		}
	}

	public interface TestDeclarativeConfigDescriptor_Shortcut extends ConfigurationItem {

		@StringDefault("hallo")
		String getStringProperty();

		@LongDefault(15)
		@Name("Long-property")
		Long getLongProperty();

		@Name("long-property")
		long getlongProperty();

		@IntDefault(15)
		Integer getIntProperty();

		@ShortDefault(15)
		Short getShortProperty();

		@ByteDefault(15)
		Byte getByteProperty();

		@BooleanDefault(true)
		Boolean getBooleanProperty();

		@ClassDefault(TestValue.class)
		Class<?> getClassProperty();

		@FormattedDefault("2019-11-08T08:51:00.003Z")
		Date getDateProperty();

		PolymorphicConfiguration<Mapping<?, ?>> getConfigProperty();

		@Key(TestValue.NAME_ATTRIBUTE)
		@Mandatory
		Map<String, TestValue> getMapProperty();

		@FormattedDefault("a, b,c")
		@Format(CommaSeparatedStrings.class)
		List<String> getPlainListProperty();

		@InstanceFormat
		@InstanceDefault(FirstCharacterMapping.class)
		Mapping<?, ?> getInstanceTypeProperty();

		List<PolymorphicConfiguration<Mapping<?, ?>>> getPolymorphicListProperty();

	}

	public interface TestValue extends NamedConfigMandatory {
		// Pure marker.
	}

	private void testPropertyEquality(ConfigurationDescriptor expected, ConfigurationDescriptor actual) {
		Map<String, PropertyDescriptor> actualIndex = actual.getProperties().stream()
			.collect(Collectors.toMap(PropertyDescriptor::getPropertyName, Function.identity()));
		actualIndex.remove(ConfigurationItem.CONFIGURATION_INTERFACE_NAME);
		actualIndex.remove(ConfigurationItem.ANNOTATION_TYPE);
		Map<String, PropertyDescriptor> expectedIndex = expected.getProperties().stream()
			.collect(Collectors.toMap(PropertyDescriptor::getPropertyName, Function.identity()));
		expectedIndex.remove(ConfigurationItem.CONFIGURATION_INTERFACE_NAME);
		expectedIndex.remove(ConfigurationItem.ANNOTATION_TYPE);
		HashSet<String> actualKeys = new HashSet<>(actualIndex.keySet());
		actualKeys.removeAll(expectedIndex.keySet());
		assertTrue("Too many properties " + actualKeys, actualKeys.isEmpty());
		HashSet<String> expectedKeys = new HashSet<>(expectedIndex.keySet());
		expectedKeys.removeAll(actualIndex.keySet());
		assertTrue("Missing properties " + expectedKeys, expectedKeys.isEmpty());

		for (String propertyName : actualIndex.keySet()) {
			PropertyDescriptor actualProperty = actualIndex.get(propertyName);
			PropertyDescriptor expectedProperty = expectedIndex.get(propertyName);

			assertEquals("Different type in property " + propertyName, expectedProperty.getType(),
				actualProperty.getType());
			assertEquals("Different default value in property " + propertyName, expectedProperty.getDefaultValue(),
				actualProperty.getDefaultValue());
			assertEquals("Different mandatory in property " + propertyName, expectedProperty.isMandatory(),
				actualProperty.isMandatory());
			assertEquals("Different abstract in property " + propertyName, expectedProperty.isAbstract(),
				actualProperty.isAbstract());
			assertEquals("Different instance valued in property " + propertyName, expectedProperty.isInstanceValued(),
				actualProperty.isInstanceValued());
			assertEquals("Different instance type in property " + propertyName, expectedProperty.getInstanceType(),
				actualProperty.getInstanceType());
			assertEquals("Different kind in property " + propertyName, expectedProperty.kind(),
				actualProperty.kind());
			switch (actualProperty.kind()) {
				case COMPLEX:
					assertEquals("Different value provider in property " + propertyName,
						expectedProperty.getValueBinding(),
						actualProperty.getValueBinding());
					break;
				case DERIVED:
					assertEquals("Different algorithm in property " + propertyName,
						expectedProperty.getAlgorithm(),
						actualProperty.getAlgorithm());
					break;
				case ITEM:
					assertEquals("Different element type in property " + propertyName,
						expectedProperty.getElementType(),
						actualProperty.getElementType());
					break;
				case ARRAY:
				case LIST:
				case MAP:
					assertEquals("Different element type in property " + propertyName,
						expectedProperty.getElementType(),
						actualProperty.getElementType());
					assertEquals("Different key property in property " + propertyName,
						expectedProperty.getKeyProperty(),
						actualProperty.getKeyProperty());
					break;
				case PLAIN:
					assertEquals("Different value provider in property " + propertyName,
						expectedProperty.getValueProvider(),
						actualProperty.getValueProvider());
					break;
				default:
					break;
			}
		}
	}

	public void testPropertyShortcut() throws Throwable {
		ConfigurationDescriptor desc = readDescriptor("shortcut.xml");
		PropertyDescriptor prop;

		prop = desc.getProperty("string-property");
		assertNotNull(prop);
		assertEquals(String.class, prop.getType());
		assertEquals("hallo", prop.getDefaultValue());

		prop = desc.getProperty("Long-property");
		assertNotNull(prop);
		assertEquals(Long.class, prop.getType());
		assertEquals((long) 15, prop.getDefaultValue());

		prop = desc.getProperty("long-property");
		assertNotNull(prop);
		assertEquals(long.class, prop.getType());
		assertEquals((long) 0, prop.getDefaultValue());

		prop = desc.getProperty("int-property");
		assertNotNull(prop);
		assertEquals(Integer.class, prop.getType());
		assertEquals(15, prop.getDefaultValue());

		prop = desc.getProperty("short-property");
		assertNotNull(prop);
		assertEquals(Short.class, prop.getType());
		assertEquals((short) 15, prop.getDefaultValue());

		prop = desc.getProperty("byte-property");
		assertNotNull(prop);
		assertEquals(Byte.class, prop.getType());
		assertEquals((byte) 15, prop.getDefaultValue());

		prop = desc.getProperty("boolean-property");
		assertNotNull(prop);
		assertEquals(Boolean.class, prop.getType());
		assertEquals(true, prop.getDefaultValue());

		prop = desc.getProperty("class-property");
		assertNotNull(prop);
		assertEquals(Class.class, prop.getType());
		assertEquals(TestValue.class, prop.getDefaultValue());

		prop = desc.getProperty("date-property");
		assertNotNull(prop);
		assertEquals(Date.class, prop.getType());
		assertEquals(XmlDateTimeFormat.INSTANCE.parseObject("2019-11-08T08:51:00.003Z"), prop.getDefaultValue());

		prop = desc.getProperty("config-property");
		assertNotNull(prop);
		assertEquals(PolymorphicConfiguration.class, prop.getType());
		assertEquals(Mapping.class, prop.getInstanceType());

		prop = desc.getProperty("map-property");
		assertNotNull(prop);
		assertEquals(Map.class, prop.getType());
		assertEquals(TestValue.class, prop.getElementType());
		assertEquals(TypedConfiguration.getConfigurationDescriptor(TestValue.class)
			.getProperty(TestValue.NAME_ATTRIBUTE), prop.getKeyProperty());

		prop = desc.getProperty("plain-list-property");
		assertNotNull(prop);
		assertEquals(List.class, prop.getType());
		assertEquals(CommaSeparatedStrings.INSTANCE, prop.getValueProvider());
		assertEquals(BasicTestCase.list("a", "b", "c"), prop.getDefaultValue());

		prop = desc.getProperty("instance-type-property");
		assertNotNull(prop);
		assertEquals(Mapping.class, prop.getType());
		assertEquals(true, prop.isInstanceValued());
		assertEquals(FirstCharacterMapping.INSTANCE, prop.getDefaultValue());

		prop = desc.getProperty("polymorphic-list-property");
		assertNotNull(prop);
		assertEquals(List.class, prop.getType());
		assertEquals(PolymorphicConfiguration.class, prop.getElementType());
		assertEquals(Mapping.class, prop.getInstanceType());

		testPropertyEquality(
			TypedConfiguration.getConfigurationDescriptor(TestDeclarativeConfigDescriptor_Shortcut.class), desc);

	}

	private ConfigurationDescriptor readDescriptor(String fileSuffix) throws Throwable {
		ConfigurationDescriptorConfig descriptorConfig =
			(ConfigurationDescriptorConfig) readConfiguration(fileSuffix);

		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(descriptorConfig);
		return descriptor;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDeclarativeConfigDescriptor}.
	 */
	public static Test suite() {
		return suite(TestDeclarativeConfigDescriptor.class);
	}
}
