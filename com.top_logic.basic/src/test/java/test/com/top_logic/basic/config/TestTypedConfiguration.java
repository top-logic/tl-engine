/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.config.TypedConfigurationSzenario.A;
import test.com.top_logic.basic.config.TypedConfigurationSzenario.A.Config;
import test.com.top_logic.basic.config.TypedConfigurationSzenario.EConfig;
import test.com.top_logic.basic.config.TypedConfigurationSzenario.TestEnum;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.EnumFormat;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Test case for {@link TypedConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfiguration extends AbstractTypedConfigurationTestCase {
	
	public interface TestConfig extends ConfigurationItem {
		String INSTANCE_DEFAULT = "instance-default";

		String INDEXED = "indexed";

		String INT_PROPERTY_NAME = "int_property";

		String ARRAY = "array";

		// Property "int".
		@Name(INT_PROPERTY_NAME)
		int getInt();
		void setInt(int value);
		
		// Property "propertyWithDefault".
		String PROPERTY_WITH_DEFAULT_DEFAULT_VALUE = "Hello World!";
		@StringDefault(PROPERTY_WITH_DEFAULT_DEFAULT_VALUE)
		String getPropertyWithDefault();
		void setPropertyWithDefault(String value);
		
		// Property "primitiveWithDefault".
		@BooleanDefault(true)
		boolean getPrimitiveWithDefault();
		void setPrimitiveWithDefault(boolean value);
		
		@Name(INDEXED)
		@EntryTag("value")
		public double getIndexed(int index);
		public void setIndexed(int index, double value);
		
		String STRING_WITHOUT_DEFAULT_PROPERTY_NAME = "stringWithoutDefault";
		@Name(STRING_WITHOUT_DEFAULT_PROPERTY_NAME)
		String getStringWithoutDefault();
		
		@EntryTag("config")
		List<TestConfig> getListWithoutDefault();
		
		void setListWithoutDefault(List<TestConfig> value);

		@Name(ARRAY)
		@EntryTag("config")
		TestConfig[] getArray();

		void setArray(TestConfig[] value);

		@EntryTag("config")
		@Key(STRING_WITHOUT_DEFAULT_PROPERTY_NAME)
		Map<String, TestConfig> getMapWithoutDefault();
		
		String COMMA_SEPARATED_LIST_PROPERTY_NAME = "commaSeparatedList";
		@Format(CommaSeparatedStrings.class)
		@Name(COMMA_SEPARATED_LIST_PROPERTY_NAME)
		List<String> getCommaSeparatedList();
		
		String COMMA_SEPARATED_ARRAY_PROPERTY_NAME = "commaSeparatedArray";

		@Name(COMMA_SEPARATED_ARRAY_PROPERTY_NAME)
		@FormattedDefault("s1,s2,s3")
		String[] getCommaSeparatedArray();

		@Name(INSTANCE_DEFAULT)
		@InstanceDefault(DefaultFoo.class)
		@InstanceFormat
		Foo getInstanceDefault();

		void setInstanceDefault(Foo value);
		
		@DerivedRef(INSTANCE_DEFAULT)
		Object getDerived();
	}

	public interface Foo {
		int foo();
	}

	public static class DefaultFoo implements Foo {

		// Singleton instance, as this class has no state,
		// and all instances have to be equal to each other,
		// as otherwise 'assertEquals(ConfigItem, ConfigItem)' fails.
		public static final DefaultFoo INSTANCE = new DefaultFoo();

		private DefaultFoo() {
			// Reduce visibility
		}

		@Override
		public int foo() {
			return 13;
		}
	}

	public static class SpecialFoo implements Foo {

		// Singleton instance, as this class has no state,
		// and all instances have to be equal to each other,
		// as otherwise 'assertEquals(ConfigItem, ConfigItem)' fails.
		public static final SpecialFoo INSTANCE = new SpecialFoo();

		private SpecialFoo() {
			// Reduce visibility
		}

		@Override
		public int foo() {
			return 42;
		}
	}
	
	public static interface EncryptedConfig extends TestConfig {

		String ENCRYPTED_STRING_NAME = "encrypted-string";

		@Name(ENCRYPTED_STRING_NAME)
		@Encrypted
		String getEncryptedString();

		void setEncryptedString(String s);

		@Encrypted
		@Override
		String getStringWithoutDefault();

		@Encrypted
		@Override
		List<String> getCommaSeparatedList();
	}
	
	public interface InheritedEncryptedConfig extends EncryptedConfig {

		// inherits encrypted attributes.

		/* Redeclare encrypted attribute */
		@Override
		String getStringWithoutDefault();

		/* Do not redeclare encrypted attribute */
//		@Override
//		String getEncryptedString();

	}

	TestConfig configItem;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.configItem = newConfigItem(TestConfig.class);
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.configItem = null;
		
		super.tearDown();
	}
	
	public void testDecodeEncodedString() throws ConfigurationException {
		testDecodeEncodedString("encrypted");
	}

	public void testDecodeInheritedEncodedString() throws ConfigurationException {
		testDecodeEncodedString("inherited-encrypted");
	}

	private void testDecodeEncodedString(String rootTag)
			throws ConfigurationException {
		String plaintext = "Some test text";
		String encrypted = ConfigurationEncryption.encrypt(plaintext);
		testDecodeString(rootTag, encrypted, plaintext);
		testDecodeString(rootTag, "", "");
		initFailureTest();
		try {
			testDecodeString(rootTag, "UNENCRYPTED VALUE", "");
			fail("Must not be able to decode unencoded values.");
		} catch (ConfigurationException ex) {
			// expected.
		} catch (ExpectedFailure ex) {
			// expected.
		} finally {
			initDefaultTest();
		}
		testDecodeString(rootTag, "unencrypted: Bla blub ", " Bla blub ");
	}

	private void testDecodeString(String rootTag, String encrypted, String decrypted) throws ConfigurationException {
		EncryptedConfig readItem =
			read("<" + rootTag +
				" " + EncryptedConfig.ENCRYPTED_STRING_NAME + "='" + encrypted + "'" +
				" " + EncryptedConfig.STRING_WITHOUT_DEFAULT_PROPERTY_NAME + "='" + encrypted + "'" +
				"/>");
		assertEquals(decrypted, readItem.getEncryptedString());
		assertEquals(decrypted, readItem.getStringWithoutDefault());
	}

	public void testEncryptedString() throws XMLStreamException, ConfigurationException {
		testEncryptedString(EncryptedConfig.class);
	}

	public void testInheritedEncryptedString() throws XMLStreamException, ConfigurationException {
		testEncryptedString(InheritedEncryptedConfig.class);
	}

	private void testEncryptedString(Class<? extends EncryptedConfig> configInterface)
			throws XMLStreamException, ConfigurationException {
		testEncodedString(configInterface, EncryptedConfig.ENCRYPTED_STRING_NAME, "someString", "someString", "Some other String",
			"Some other String");
	}

	public void testEncrypedNonString() throws XMLStreamException, ConfigurationException {
		testEncryptedNonString(EncryptedConfig.class);
	}

	public void testInheritedEncrypedNonString() throws XMLStreamException, ConfigurationException {
		testEncryptedNonString(InheritedEncryptedConfig.class);
	}

	private void testEncryptedNonString(Class<? extends EncryptedConfig> configInterface)
			throws XMLStreamException, ConfigurationException {
		List<String> value = new ListBuilder<String>().add("list1").add("list2").add("list3").toList();
		String serializedString = CommaSeparatedStrings.INSTANCE.getSpecification(value);
		List<String> otherValue = new ListBuilder<String>().add("first").add("second").add("third").toList();
		String otherSerializedString = CommaSeparatedStrings.INSTANCE.getSpecification(otherValue);
		testEncodedString(configInterface, TestConfig.COMMA_SEPARATED_LIST_PROPERTY_NAME, value, serializedString,
			otherValue, otherSerializedString);
	}

	public void testOverrideEncrypted() throws XMLStreamException, ConfigurationException {
		testOverrideEncrypted(EncryptedConfig.class);
	}

	public void testOverrideInheritedEncrypted() throws XMLStreamException, ConfigurationException {
		testOverrideEncrypted(InheritedEncryptedConfig.class);
	}

	private void testOverrideEncrypted(Class<? extends EncryptedConfig> configInterface)
			throws XMLStreamException, ConfigurationException {
		testEncodedString(configInterface, TestConfig.STRING_WITHOUT_DEFAULT_PROPERTY_NAME, "someString", "someString", "Some other String",
			"Some other String");
	}

	private void testEncodedString(Class<? extends EncryptedConfig> configInterface, String propertyName, Object value,
			String serializedValue, Object otherValue, String otherSerializedValue)
			throws XMLStreamException, ConfigurationException {
		PropertyDescriptor property =
			TypedConfiguration.getConfigurationDescriptor(configInterface).getProperty(propertyName);
		EncryptedConfig config = newConfigItem(EncryptedConfig.class);
		config.update(property, value);
		assertEquals("Setting programmatically occurs unencrypted.", value, config.value(property));

		ConfigurationItem configRestored = throughXML(config);
		assertTrue(configRestored instanceof EncryptedConfig);
		assertEquals("WritingReading must not modify encryption.", value, configRestored.value(property));

		String serializedXML = toXML(config);
		assertFalse("Serializing does not encrypt value", serializedXML.contains(serializedValue));

		String newEncrypted = ConfigurationEncryption.encrypt(otherSerializedValue);
		Pattern findEncryptedValue = Pattern.compile(propertyName + "\\s*=\\s*\"[^\"]*\"");
		Matcher matcher = findEncryptedValue.matcher(serializedXML);
		assertTrue("No value for property encryptedString found.", matcher.find());

		String modifiedSerialization = matcher.replaceAll(propertyName + "=\"" + newEncrypted + '"');
		ConfigurationItem modifiedConfig = fromXML(modifiedSerialization);
		assertTrue(modifiedConfig instanceof EncryptedConfig);
		assertEquals("Encrypted values must be decrypted", otherValue, modifiedConfig.value(property));
	}

	public void testFillConfiguration() throws ConfigurationException {
		Map<String, String> values = new HashMap<>();
		values.put(TestConfig.STRING_WITHOUT_DEFAULT_PROPERTY_NAME, "value1");
		values.put(TestConfig.INT_PROPERTY_NAME, "156");
		values.put(TestConfig.INSTANCE_DEFAULT, SpecialFoo.class.getName());
		values.put(TestConfig.COMMA_SEPARATED_LIST_PROPERTY_NAME, "v1,v2,v3");
		values.put(TestConfig.COMMA_SEPARATED_ARRAY_PROPERTY_NAME, "v1, v2, v3");
		TypedConfiguration.fillValues(configItem, values.entrySet());

		assertEquals("value1", configItem.getStringWithoutDefault());
		assertEquals(156, configItem.getInt());
		assertTrue(configItem.getInstanceDefault() instanceof SpecialFoo);
		assertEquals(BasicTestCase.list("v1", "v2", "v3"), configItem.getCommaSeparatedList());
		BasicTestCase.assertEquals(new String[] { "v1", "v2", "v3" }, configItem.getCommaSeparatedArray());
	}

	public void testFillNoConfiguration() throws ConfigurationException {
		Map<String, String> values = new HashMap<>();
		// No values set
		TypedConfiguration.fillValues(configItem, values.entrySet());

		assertEquals("", configItem.getStringWithoutDefault());
		assertEquals(0, configItem.getInt());
		assertTrue(configItem.getInstanceDefault() instanceof DefaultFoo);
		assertEquals(BasicTestCase.list(), configItem.getCommaSeparatedList());
		BasicTestCase.assertEquals(new String[] { "s1", "s2", "s3" }, configItem.getCommaSeparatedArray());
	}

	public void testFillEmptyConfiguration() throws ConfigurationException {
		Map<String, String> values = new HashMap<>();
		values.put(TestConfig.INSTANCE_DEFAULT, "");
		values.put(TestConfig.COMMA_SEPARATED_ARRAY_PROPERTY_NAME, "");
		TypedConfiguration.fillValues(configItem, values.entrySet());

		assertNull(configItem.getInstanceDefault());
		BasicTestCase.assertEquals(new String[] {}, configItem.getCommaSeparatedArray());
	}

	public void testFillConfigurationIllegalValues() {
		String nonExistingPropertyName = "does Not Exist";
		Map<String, String> notExistingProperty = Collections.singletonMap(nonExistingPropertyName, "ff");
		try {
			TypedConfiguration.fillValues(configItem, notExistingProperty.entrySet());
			fail("There is no property " + nonExistingPropertyName);
		} catch (ConfigurationException ex) {
			// expected
		}
		
		String illegalValue = "ff";
		Map<String, String> illegalValues = Collections.singletonMap(TestConfig.INT_PROPERTY_NAME, illegalValue);
		try {
			TypedConfiguration.fillValues(configItem, illegalValues.entrySet());
			fail(TestConfig.INT_PROPERTY_NAME + " has int-Type. Must not be able to set " + illegalValue);
		} catch (ConfigurationException ex) {
			// expected
		}
	}
	
	/**
	 * Tests access of {@link ConfigurationItem#getConfigurationInterface()}
	 */
	public void testGetConfigurationInterface() {
		assertSame(TestConfig.class, configItem.getConfigurationInterface());
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(TestConfig.class);
		// check access via generic API
		PropertyDescriptor property = descriptor.getProperty(ConfigurationItem.CONFIGURATION_INTERFACE_NAME);
		assertSame(TestConfig.class, configItem.value(property));
		
		try {
			configItem.update(property, ConfigurationItem.class);
			fail("Property " + property + " is immutable.");
		} catch (IllegalArgumentException ex) {
			// expected
		}

		assertEquals(false, property.canHaveSetter());
	}

	public void testView() {
		TestConfig view = TypedConfiguration.createView(configItem);
		assertEquals(configItem, view);
		configItem.setPropertyWithDefault("new value");
		assertEquals(configItem, view);
		try {
			view.setPropertyWithDefault("illegalSet");
			fail("It must not be possible to set values in a view");
		} catch (RuntimeException ex) {
			// expected
		}
	}
	
	public void testArray() {
		assertEquals(0, configItem.getArray().length);
		TestConfig[] value = {
			TypedConfiguration.newConfigItem(TestConfig.class),
			TypedConfiguration.newConfigItem(TestConfig.class),
		};
		configItem.setArray(value);
		assertEquals(2, configItem.getArray().length);

		value[0] = null;
		assertNotNull("Array value must not be shared.", configItem.getArray()[0]);

		configItem.getArray()[0] = null;
		assertNotNull("Array value must not be shared.", configItem.getArray()[0]);
	}

	public void testNullArray() {
		configItem.setArray(null);
		assertNotNull(configItem.getArray());
		assertEquals(0, configItem.getArray().length);
	}

	public void testParseArray() throws ConfigurationException {
		TestConfig item =
			read("<config><array><config int_property='42'/><config int_property='13'/></array></config>");
		checkArray(item);
	}

	public void testParseArrayUpdate() throws ConfigurationException {
		TestConfig item = read("<config><array><config int_property='42'/></array></config>",
			"<config><array><config int_property='13'/></array></config>");
		checkArray(item);
	}

	private void checkArray(TestConfig item) {
		assertEquals(2, item.getArray().length);
		assertEquals(42, item.getArray()[0].getInt());
		assertEquals(13, item.getArray()[1].getInt());
	}

	public void testArrayEvent() {
		PropertyDescriptor arrayProperty = configItem.descriptor().getProperty(TestConfig.ARRAY);
		final TestConfig element1 = newConfigItem(TestConfig.class);
		TestConfig[] newValueSet = new TestConfig[] { element1 };

		ConfigurationListener l1 = new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				TestConfig[] newValueReceived = (TestConfig[]) change.getNewValue();
				assertSame(element1, newValueReceived[0]);

				TestConfig[] oldValueReceived = (TestConfig[]) change.getOldValue();
				assertTrue(Arrays.equals(new TestConfig[0], oldValueReceived));
			}
		};
		configItem.addConfigurationListener(arrayProperty, l1);
		configItem.setArray(newValueSet);
	}

	public void testNotNullValues() {
		final List<TestConfig> listWithoutDefault = configItem.getListWithoutDefault();
		assertNotNull(listWithoutDefault);
		assertTrue(listWithoutDefault.isEmpty());

		final Map<String, TestConfig> mapWithoutDefault = configItem.getMapWithoutDefault();
		assertNotNull(mapWithoutDefault);
		assertTrue(mapWithoutDefault.isEmpty());

		final String stringWithoutDefault = configItem.getStringWithoutDefault();
		assertNotNull(stringWithoutDefault);
		assertEquals(0, stringWithoutDefault.length());
	}
	
	public void testParseList() throws ConfigurationException {
		TestConfig item = read(
			"<config><list-without-default><config int_property='42'/><config int_property='13'/></list-without-default></config>");
		checkList(item);
	}

	public void testParseListUpdate() throws ConfigurationException {
		TestConfig item = read(
			"<config><list-without-default><config int_property='42'/></list-without-default></config>",
			"<config><list-without-default><config int_property='13'/></list-without-default></config>");
		checkList(item);
	}

	private void checkList(TestConfig item) {
		assertEquals(2, item.getListWithoutDefault().size());
		assertEquals(42, item.getListWithoutDefault().get(0).getInt());
		assertEquals(13, item.getListWithoutDefault().get(1).getInt());
	}

	public void testSimpleProperty() {
		assertEquals(0, configItem.getInt());
		configItem.setInt(42);
		assertEquals(42, configItem.getInt());
	}
	
	public void testPropertyWithDefault() {
		assertEquals(TestConfig.PROPERTY_WITH_DEFAULT_DEFAULT_VALUE, configItem.getPropertyWithDefault());
		configItem.setPropertyWithDefault("foo");
		assertEquals("foo", configItem.getPropertyWithDefault());
	}
	
	public void testGenericEnum() {
		EConfig eConfig = TypedConfiguration.newConfigItem(EConfig.class);

		assertEquals(TestEnum.value2, eConfig.getEnum());
	}

	public void testRegularEnum() {
		EConfig eConfig = TypedConfiguration.newConfigItem(EConfig.class);

		assertEquals(TestEnum.value1, eConfig.getRegularEnum());
		eConfig.setRegularEnum(TestEnum.value2);
		assertEquals(TestEnum.value2, eConfig.getRegularEnum());
		try {
			eConfig.setRegularEnum(null);
			fail("Null not allowed.");
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains("Property is non-nullable", ex.getMessage());
		}

		assertEquals(TestEnum.value2, eConfig.getEnumFormattedDefault());
	}

	public void testLoadEnum() throws ConfigurationException {
		EConfig eConfig = (EConfig) TypedConfiguration.fromString("<config xmlns:config='"
			+ ConfigurationSchemaConstants.CONFIG_NS + "' config:interface='" + EConfig.class.getName() + "'"
			+ " regular-enum='" + TestEnum.value2.name() + "'/>");
		assertEquals(TestEnum.value2, eConfig.getRegularEnum());
	}

	public void testLoadEnumQualified() throws ConfigurationException {
		EConfig eConfig = (EConfig) TypedConfiguration.fromString("<config xmlns:config='"
			+ ConfigurationSchemaConstants.CONFIG_NS + "' config:interface='" + EConfig.class.getName() + "'"
			+ " regular-enum='" + TestEnum.class.getName() + EnumFormat.SEPARATOR_CHAR + TestEnum.value2.name()
			+ "'/>");
		assertEquals(TestEnum.value2, eConfig.getRegularEnum());
	}

	public void testNullableEnum() throws ConfigurationException, XMLStreamException {
		EConfig eConfig = TypedConfiguration.newConfigItem(EConfig.class);

		assertEquals(TestEnum.value1, eConfig.getNullableEnum());
		assertEquals(TestEnum.value1, dumpLoad(eConfig).getNullableEnum());

		eConfig.setNullableEnum(TestEnum.value2);
		assertEquals(TestEnum.value2, eConfig.getNullableEnum());
		assertEquals(TestEnum.value2, dumpLoad(eConfig).getNullableEnum());

		eConfig.setNullableEnum(null);
		assertNull(eConfig.getNullableEnum());
		assertNull(dumpLoad(eConfig).getNullableEnum());
	}

	public void testNullDefaultEnum() throws ConfigurationException, XMLStreamException {
		EConfig eConfig = TypedConfiguration.newConfigItem(EConfig.class);

		assertNull(eConfig.getNullDefaultEnum());
		assertNull(dumpLoad(eConfig).getNullDefaultEnum());

		eConfig.setNullDefaultEnum(TestEnum.value2);
		assertEquals(TestEnum.value2, eConfig.getNullDefaultEnum());
		assertEquals(TestEnum.value2, dumpLoad(eConfig).getNullDefaultEnum());

		eConfig.setNullDefaultEnum(null);
		assertNull(eConfig.getNullDefaultEnum());
		assertNull(dumpLoad(eConfig).getNullDefaultEnum());
	}

	private <T extends ConfigurationItem> T dumpLoad(T eConfig) throws ConfigurationException, XMLStreamException {
		return read(write(eConfig));
	}

	private String write(ConfigurationItem config) throws XMLStreamException {
		StringWriter buffer = new StringWriter();
		new ConfigurationWriter(buffer).write("config",
			TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class), config);
		return buffer.toString();
	}

	public void testInstanceDefault() {
		assertEquals(DefaultFoo.class, configItem.getInstanceDefault().getClass());
		configItem.setInstanceDefault(new SpecialFoo());
		assertEquals(SpecialFoo.class, configItem.getInstanceDefault().getClass());
	}

	public void testSettingDefaultProperty() {
		configItem.setPropertyWithDefault(configItem.getPropertyWithDefault());
		assertEquals("Setting of default values must be a noop", newConfigItem(TestConfig.class), configItem);
	}
	
	public void testPrimitivePropertyWithDefault() {
		assertTrue(configItem.getPrimitiveWithDefault());
		configItem.setPrimitiveWithDefault(false);
		assertFalse(configItem.getPrimitiveWithDefault());
	}
	
	public void testIndexed() {
		configItem.setIndexed(0, 1.0);
		configItem.setIndexed(1, 1.1);
		configItem.setIndexed(2, 1.2);
		configItem.setIndexed(5, 1.5);
		
		assertEquals(1.0, configItem.getIndexed(0), 0.0);
		assertEquals(1.1, configItem.getIndexed(1), 0.0);
		assertEquals(1.2, configItem.getIndexed(2), 0.0);
		assertEquals(0.0, configItem.getIndexed(3), 0.0);
		assertEquals(0.0, configItem.getIndexed(4), 0.0);
		assertEquals(1.5, configItem.getIndexed(5), 0.0);
	}
	
	public void testDescriptor() {
		ConfigurationDescriptor descriptor = configItem.descriptor();
		
		assertEquals(TestConfig.class, descriptor.getConfigurationInterface());
		
		String defaultConfigurationName = "property-with-default";
		PropertyDescriptor property = descriptor.getProperty(defaultConfigurationName);
		assertEquals(defaultConfigurationName, property.getPropertyName());
		assertEquals(String.class, property.getType());
		assertEquals(TestConfig.PROPERTY_WITH_DEFAULT_DEFAULT_VALUE, property.getDefaultValue());
	}

	public void testUnimplementable() {
		Object unimplementable = configItem.unimplementable();
		assertNull(unimplementable);
	}

	/**
	 * Test for
	 * {@link TypedConfiguration#getInstanceList(com.top_logic.basic.config.InstantiationContext, List)}
	 */
	public void testGetInstanceList() {
		A.Config p0;
		{
			ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(A.Config.class);
			initValue(aBuilder, A.Config.P_NAME, 0);
			initValue(aBuilder, A.Config.IMPLEMENTATION_CLASS_NAME, A.class);
			p0 = (Config) createConfig(aBuilder);
		}
		A.Config p1;
		{
			ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(A.Config.class);
			initValue(aBuilder, A.Config.P_NAME, 1);
			initValue(aBuilder, A.Config.IMPLEMENTATION_CLASS_NAME, A.class);
			p1 = (Config) createConfig(aBuilder);
		}
		List<A> as =
			TypedConfiguration.getInstanceList(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, Arrays.asList(p0, p1));
		assertNotNull(as);
		assertSame(2, as.size());
		assertNotNull(as.get(0));
		assertEquals(0, as.get(0).getP());
		assertNotNull(as.get(1));
		assertEquals(1, as.get(1).getP());

		List<A> empty =
			TypedConfiguration.getInstanceList(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				Collections.<Config> emptyList());
		assertNotNull(empty);
		assertSame(0, empty.size());
	}

	public void testGetInstanceListReadOnly() {
		A.Config p0;
		{
			ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(A.Config.class);
			initValue(aBuilder, A.Config.P_NAME, 0);
			initValue(aBuilder, A.Config.IMPLEMENTATION_CLASS_NAME, A.class);
			p0 = (Config) createConfig(aBuilder);
		}
		A.Config p1;
		{
			ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(A.Config.class);
			initValue(aBuilder, A.Config.P_NAME, 1);
			initValue(aBuilder, A.Config.IMPLEMENTATION_CLASS_NAME, A.class);
			p1 = (Config) createConfig(aBuilder);
		}
		List<A> asEmpty =
			TypedConfiguration.getInstanceList(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				Collections.<A.Config> emptyList());
		assertNotNull(asEmpty);
		assertSame(0, asEmpty.size());

		List<A> asSingleton =
			TypedConfiguration.getInstanceList(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				Collections.singletonList(p0));
		assertNotNull(asSingleton);
		assertSame(1, asSingleton.size());
		assertNotNull(asSingleton.get(0));
		assertEquals(0, asSingleton.get(0).getP());

		List<A> as =
			TypedConfiguration.getInstanceList(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				Arrays.asList(p0, p1));
		assertNotNull(as);
		assertSame(2, as.size());
		assertNotNull(as.get(0));
		assertEquals(0, as.get(0).getP());
		assertNotNull(as.get(1));
		assertEquals(1, as.get(1).getP());

		List<A> empty =
			TypedConfiguration.getInstanceList(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				Collections.<Config> emptyList());
		assertNotNull(empty);
		assertSame(0, empty.size());
	}

	/**
	 * Test for
	 * {@link TypedConfiguration#getInstanceMap(com.top_logic.basic.config.InstantiationContext, Map)}
	 */
	public void testGetInstanceMap() {
		A.Config p0;
		{
			ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(A.Config.class);
			initValue(aBuilder, A.Config.P_NAME, 0);
			initValue(aBuilder, A.Config.IMPLEMENTATION_CLASS_NAME, A.class);
			p0 = (Config) createConfig(aBuilder);
		}
		A.Config p1;
		{
			ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(A.Config.class);
			initValue(aBuilder, A.Config.P_NAME, 1);
			initValue(aBuilder, A.Config.IMPLEMENTATION_CLASS_NAME, A.class);
			p1 = (Config) createConfig(aBuilder);
		}
		Map<Integer, Config> map = new MapBuilder<Integer, A.Config>().put(0, p0).put(1, p1).toMap();
		Map<Integer, A> as =
			TypedConfiguration.getInstanceMap(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, map);
		assertNotNull(as);
		assertSame(2, as.size());
		assertNotNull(as.get(0));
		assertEquals(0, as.get(0).getP());
		assertNotNull(as.get(1));
		assertEquals(1, as.get(1).getP());

		Map<String, A> empty =
			TypedConfiguration.getInstanceMap(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				Collections.<String, PolymorphicConfiguration<A>> emptyMap());
		assertNotNull(empty);
		assertSame(0, empty.size());
	}
	
	public void testGetInstanceMapReadOnly() {
		A.Config p0;
		{
			ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(A.Config.class);
			initValue(aBuilder, A.Config.P_NAME, 0);
			initValue(aBuilder, A.Config.IMPLEMENTATION_CLASS_NAME, A.class);
			p0 = (Config) createConfig(aBuilder);
		}
		A.Config p1;
		{
			ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(A.Config.class);
			initValue(aBuilder, A.Config.P_NAME, 1);
			initValue(aBuilder, A.Config.IMPLEMENTATION_CLASS_NAME, A.class);
			p1 = (Config) createConfig(aBuilder);
		}
		Map<Integer, Config> map = new HashMap<>();
		Map<Integer, A> asEmpty =
			TypedConfiguration.getInstanceMap(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, map);
		assertNotNull(asEmpty);
		assertSame(0, asEmpty.size());

		map.put(0, p0);
		Map<Integer, A> asSingleton =
			TypedConfiguration.getInstanceMap(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, map);
		assertNotNull(asSingleton);
		assertSame(1, asSingleton.size());
		assertNotNull(asSingleton.get(0));
		assertEquals(0, asSingleton.get(0).getP());

		map.put(1, p1);
		Map<Integer, A> as =
			TypedConfiguration.getInstanceMap(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, map);
		assertNotNull(as);
		assertSame(2, as.size());
		assertNotNull(as.get(0));
		assertEquals(0, as.get(0).getP());
		assertNotNull(as.get(1));
		assertEquals(1, as.get(1).getP());
	}

	public interface CollectionConfig extends ConfigurationItem {
		
		@Key(X.Config.KEY)
		Map<String, X.Config> getMap();
		
		@Key(X.Config.KEY)
		List<X.Config> getList();

		@Key(X.Config.KEY)
		Map<String, X> getInstanceMap();

		@Key(X.Config.KEY)
		List<X> getInstanceList();

		public static class X implements ConfiguredInstance<X.Config> {

			private final Config _config;

			public interface Config extends PolymorphicConfiguration<X> {
				String KEY = "key";

				@Name(KEY)
				String getKey();
			}

			/**
			 * Creates a {@link TestTypedConfiguration.CollectionConfig.X} from configuration.
			 * 
			 * @param context
			 *        The context for instantiating sub configurations.
			 * @param config
			 *        The configuration.
			 */
			@CalledByReflection
			public X(InstantiationContext context, Config config) {
				_config = config;
			}

			@Override
			public Config getConfig() {
				return _config;
			}
		}

		public class Y extends X {

			public Y(InstantiationContext context, Config config) {
				super(context, config);

				throw new RuntimeException("Simulated instantiation failure.");
			}

		}
	}
	
	public void testGetInstanceListFailure() throws ConfigurationException {
		CollectionConfig config = read("<collection>" +
			"<list>" +
			"<entry key='1' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
			"<entry key='2' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
			"<entry key='3' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
			"<entry key='4' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
			"</list>" +
			"</collection>");

		assertEquals(4, config.getList().size());

		InstantiationContext failingContext = new DefaultInstantiationContext(TestTypedConfiguration.class);

		List<CollectionConfig.X> instanceList = TypedConfiguration.getInstanceList(failingContext, config.getList());
		assertTrue(failingContext.hasErrors());
		assertEquals("Ticket #12773: Failed instantiations must not be reported.", 2, instanceList.size());
	}

	public void testReadInstanceListFailure() throws ConfigurationException {
		try {
			read(
				"<collection>" +
					"<instance-list>" +
					"<entry key='1' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
					"<entry key='3' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
					"</instance-list>" +
					"</collection>",
				"<collection xmlns:x='http://www.top-logic.com/ns/config/6.0'>" +
					"<instance-list>" +
					"<entry key='1' x:operation='update' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='2a' x:operation='add' x:position='begin' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='2b' x:operation='add' x:position='end' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='2c' x:operation='add' x:position='before' x:reference='3' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='2d' x:operation='add' x:position='after' x:reference='3' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='3' x:operation='update' x:position='begin' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='3' x:operation='update' x:position='end' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='3' x:operation='update' x:position='before' x:reference='1' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='3' x:operation='update' x:position='after' x:reference='1' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='4' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='5' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
					"</instance-list>" +
					"</collection>"
				);
			context.checkErrors();
		} catch (RuntimeException ex) {
			// Good
			return;
		} catch (Error ex) {
			// Good
			return;
		}
		fail("Reading invalid configuration did not fail.");
	}

	public void testGetInstanceMapFailure() throws ConfigurationException {
		CollectionConfig config = read("<collection>" +
			"<map>" +
			"<entry key='1' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
			"<entry key='2' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
			"<entry key='3' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
			"<entry key='4' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
			"</map>" +
			"</collection>");

		assertEquals(4, config.getMap().size());

		InstantiationContext failingContext = new DefaultInstantiationContext(TestTypedConfiguration.class);

		Map<String, CollectionConfig.X> instanceMap = TypedConfiguration.getInstanceMap(failingContext, config.getMap());
		assertTrue(failingContext.hasErrors());
		assertEquals("Ticket #12773: Failed instantiations must not be reported.", 2, instanceMap.size());
	}

	public void testReadInstanceMapFailure() throws ConfigurationException {
		try {
			read(
				"<collection>" +
					"<instance-map>" +
					"<entry key='1' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
					"</instance-map>" +
					"</collection>",
				"<collection xmlns:x='http://www.top-logic.com/ns/config/6.0'>" +
					"<instance-map>" +
					"<entry key='1' x:operation='update' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='2' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"<entry key='3' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$X' />" +
					"<entry key='4' class='test.com.top_logic.basic.config.TestTypedConfiguration$CollectionConfig$Y' />" +
					"</instance-map>" +
					"</collection>"
				);
			context.checkErrors();
		} catch (RuntimeException ex) {
			// Good
			return;
		} catch (Error ex) {
			// Good
			return;
		}
		fail("Reading invalid configuration did not fail.");
	}

	public void testConfigToString() throws ConfigurationException {
		Map<String, String> values = new HashMap<>();
		values.put(TestConfig.STRING_WITHOUT_DEFAULT_PROPERTY_NAME, "value1");
		values.put(TestConfig.INT_PROPERTY_NAME, "156");
		values.put(TestConfig.COMMA_SEPARATED_LIST_PROPERTY_NAME, "v1,v2,v3");
		TypedConfiguration.fillValues(configItem, values.entrySet());

		String serializedConfig = TypedConfiguration.toString(configItem);
		ConfigurationItem parsedConfig = TypedConfiguration.fromString(serializedConfig);
		assertEquals(configItem, parsedConfig);
	}

	public void testNullConfigToString() throws ConfigurationException {
		String serializedConfig = TypedConfiguration.toString(null);
		assertNull(serializedConfig);
		ConfigurationItem parsedConfig = TypedConfiguration.fromString(serializedConfig);
		assertNull(parsedConfig);
	}

	public void testPolymorphicConfigSubClassToString() throws ConfigurationException {
		TypedConfigurationSzenario.B.Config configItem =
			newConfigItem(TypedConfigurationSzenario.B.Config.class);
		configItem.setImplementationClass(TypedConfigurationSzenario.C.class);

		String serializedConfig = TypedConfiguration.toString(configItem);
		ConfigurationItem parsedConfig = TypedConfiguration.fromString(serializedConfig);
		assertEquals(configItem, parsedConfig);
	}

	public void testPolymorphicConfigConcreteClassSetToString() throws ConfigurationException {
		TypedConfigurationSzenario.B.Config bConfig =
			newConfigItem(TypedConfigurationSzenario.B.Config.class);
		bConfig.setImplementationClass(TypedConfigurationSzenario.B.class);

		String serializedConfig = TypedConfiguration.toString(bConfig);
		ConfigurationItem parsedConfig = TypedConfiguration.fromString(serializedConfig);
		assertEquals(bConfig, parsedConfig);
	}

	public void testPropertyToString() {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(TestConfig.class);
		assertEquals(TestConfig.class.getName() + ".getInt()", descriptor.getProperty(TestConfig.INT_PROPERTY_NAME)
			.toString());
		assertEquals(TestConfig.class.getName() + ".getIndexed(int)", descriptor.getProperty(TestConfig.INDEXED).toString());
	}

	public void testFailInvalidRootTag() {
		try {
			TypedConfiguration.parse(CharacterContents.newContent("<group/>"));
			fail("No descriptor for tag 'group' known.");
		} catch (ConfigurationException ex) {
			// expected
		} catch (ConfigurationError err) {
			throw BasicTestCase.fail("Ticket #22933: ", err);
		}
	}

	public void testCustomRootTag() throws ConfigurationException {
		TestConfig c = TypedConfiguration.parse("group", TestConfig.class,
			CharacterContents.newContent("<group int_property='42'/>"));
		assertNotNull(c);
		assertEquals(42, c.getInt());
	}

	public void testCustomRootTagWithDescriptor() throws ConfigurationException {
		ConfigurationDescriptor rootType = TypedConfiguration.getConfigurationDescriptor(TestConfig.class);
		TestConfig c = (TestConfig) TypedConfiguration.parse("group", rootType,
			CharacterContents.newContent("<group int_property='42'/>"));
		assertNotNull(c);
		assertEquals(42, c.getInt());
	}

	protected <T extends ConfigurationItem> T newConfigItem(Class<T> type) {
		return TypedConfiguration.newConfigItem(type);
	}

	public interface ScenarioTypeItemNull extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		@Subtypes({})
		ConfigurationItem getExample();

	}

	public void testItemNull() {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(ScenarioTypeItemNull.class);
		setValue(builder, ScenarioTypeItemNull.PROPERTY_NAME_EXAMPLE, null);
		builder.createConfig(context);
	}

	public void testToStringEmpty() {
		A.Config config = newConfigItem(Config.class);
		String NL = "\n";
		assertEquals(
			"<config config:interface=\"test.com.top_logic.basic.config.TypedConfigurationSzenario$A$Config\"" + NL +
				"  xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"" + NL +
				"  class=\"test.com.top_logic.basic.config.TypedConfigurationSzenario$A\"" + NL +
				"/>",
			config.toString());
	}

	public void testIteratorSet() {
		TestConfig config = newConfigItem(TestConfig.class);
		List<TestConfig> listWithoutDefault = config.getListWithoutDefault();
		TestConfig config1 = newConfigItem(TestConfig.class);
		TestConfig config2 = newConfigItem(TestConfig.class);
		TestConfig config3 = newConfigItem(TestConfig.class);
		listWithoutDefault.add(config1);
		listWithoutDefault.add(config2);
		ListIterator<TestConfig> it = listWithoutDefault.listIterator();
		assertTrue(it.hasNext());
		assertEquals(config1, it.next());
		it.set(config3);
		assertEquals(BasicTestCase.list(config3, config2), listWithoutDefault);
		assertTrue(it.hasNext());
		try {
			assertEquals(config2, it.next());
		} catch (ConcurrentModificationException ex) {
			BasicTestCase.fail("Ticket #23654: List was modified using iterator.", ex);
		}
	}

	public void testToStringInner() {
		A.Config config = newConfigItem(Config.class);
		config.getEConfigs().add(newConfigItem(EConfig.class));
		String NL = "\n";
		assertEquals(
			"<config config:interface=\"test.com.top_logic.basic.config.TypedConfigurationSzenario$A$Config\"" + NL +
				"  xmlns:config=\"http://www.top-logic.com/ns/config/6.0\"" + NL +
				"  class=\"test.com.top_logic.basic.config.TypedConfigurationSzenario$A\"" + NL +
				">" + NL +
				"  <e-configs>" + NL +
				"    <e-config/>" + NL +
				"  </e-configs>" + NL +
				"</config>",
			config.toString());
	}

	private ConfigurationItem createConfig(ConfigBuilder aBuilder) {
		return aBuilder.createConfig(context);
	}

	private void initValue(ConfigBuilder builder, String name, Object value) {
		builder.initValue(builder.descriptor().getProperty(name), value);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		MapBuilder<String, ConfigurationDescriptor> descriptors = new MapBuilder<>();
		descriptors.put("config", TypedConfiguration.getConfigurationDescriptor(TestConfig.class));
		descriptors.put("collection", TypedConfiguration.getConfigurationDescriptor(CollectionConfig.class));
		descriptors.put("encrypted", TypedConfiguration.getConfigurationDescriptor(EncryptedConfig.class));
		descriptors.put("inherited-encrypted",
			TypedConfiguration.getConfigurationDescriptor(InheritedEncryptedConfig.class));
		return descriptors.toMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfiguration.class);
	}

}
