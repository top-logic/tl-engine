/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static java.util.Collections.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ScenarioTypeModifiedItemDefaultInstance;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ScenarioTypeModifiedListDefault;
import test.com.top_logic.basic.config.ScenarioContainerValueSet.ScenarioTypeModifiedListDefaultInstances;
import test.com.top_logic.basic.config.ScenarioDefaultValues.Example;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfiguredDefault;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ByteDefault;
import com.top_logic.basic.config.annotation.defaults.CharDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.ShortDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.xml.DOMUtil;

/**
 * Test case for default values in {@link TypedConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultValues extends AbstractTypedConfigurationTestCase {

	private static final String FOURTY_TWO = "fourtytwo";
	
	static int next = 42;
	
	public interface ASharedDefault extends ConfigurationItem {
		
		/* Large int to ensure new Integer instances are build. */
		String INTEGER_DEFAULT = "1545784";

		String INT = "int";

		@FormattedDefault("2000-12-31T23:00:00.000Z")
		Date getDate();
		
		@StringDefault("defaultString")
		String getString();
		
		@FormattedDefault(INTEGER_DEFAULT)
		Integer getInteger();

		@Name(INT)
		@FormattedDefault(INTEGER_DEFAULT)
		int getInt();

		void setInt(int value);

		@Format(IntFormatWithDefault.class)
		int getIntWithDefaultFromFormat();

		@IntDefault(1545784)
		@Format(IntFormatWithDefault.class)
		int getIntWithFormatAndDefault();

		@FormattedDefault(INTEGER_DEFAULT)
		@Format(IntFormatWithDefault.class)
		int getIntWithFormatAndFormattedDefault();

		class IntFormatWithDefault extends AbstractConfigurationValueProvider<Integer> {

			public static Integer DEFAULT_VALUE = Integer.valueOf(42);

			/**
			 * Singleton {@link TestDefaultValues.ASharedDefault.IntFormatWithDefault} instance.
			 */
			public static final ASharedDefault.IntFormatWithDefault INSTANCE =
				new ASharedDefault.IntFormatWithDefault();

			private IntFormatWithDefault() {
				super(Integer.class);
			}

			@Override
			protected Integer getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				return Integer.valueOf(Integer.parseInt(propertyValue.toString()));
			}

			@Override
			protected String getSpecificationNonNull(Integer configValue) {
				return configValue.toString();
			}

			@Override
			public Integer defaultValue() {
				return DEFAULT_VALUE;
			}

		}
	}

	public interface BSharedDefault extends ASharedDefault {
		// Extended to check inheritance of "shared default" in case getter are not redeclared.
	}

	public interface A extends ConfigurationItem {
		String POLY_CONFIG_ITEM = "poly-config-item";

		String CONFIG_ITEM = "config-item";

		String C_TYPED_CONFIG_INSTANCE = "c-typed-config-instance";

		String COMPLEX_FORMATTED = "complex-formatted";

		String PLAIN_ANNOTATED_NAME = "plain-annotated";

		String PLAIN_FORMATTED = "plain-formatted";

		@Name(PLAIN_ANNOTATED_NAME)
		@IntDefault(42)
		int getPlainAnnotated();
		
		@Format(WrittenFourtyTwo.class)
		@FormattedDefault(FOURTY_TWO)
		@Name(PLAIN_FORMATTED)
		int getPlainFormatted();

		void setPlainFormatted(int value);
		
		@Name("plainConfigured")
		@ComplexDefault(ConfiguredDefault.class)
		int getPlainConfigured();
		
		@InstanceFormat
		C getComplexInstance();
		
		@Name(COMPLEX_FORMATTED)
		@InstanceFormat
		@InstanceDefault(TestDefaultValues.E.class)
		C getComplexFormatted();
		
		@Name(C_TYPED_CONFIG_INSTANCE)
		@InstanceFormat
		@InstanceDefault(TestDefaultValues.F.class)
		C getCTypedConfigInstance();

		@Name(CONFIG_ITEM)
		@ItemDefault(ScenarioTypeEntry.class)
		ScenarioTypeEntry getConfigItem();

		void setConfigItem(ScenarioTypeEntry value);
		
		@Name(POLY_CONFIG_ITEM)
		@ItemDefault(PolyItemDefault.class)
		PolyItemDefault getPolyConfigItem();

		@ListDefault({ ScenarioTypeEntry.class, TestDefaultValues.class })
		@Subtypes({})
		List<ConfigurationItem> getList();

		@ListDefault({ C.class, F.class })
		@Subtypes({})
		@InstanceFormat
		List<C> getInstanceList();

		@Key(ScenarioTypeEntry.PROPERTY_NAME_KEY)
		Map<Integer, ScenarioTypeEntry> getMap();

		@ImplementationClassDefault(F.class)
		PolymorphicConfiguration<C> getCConfig();

		@ComplexDefault(CreateArrayValue.class)
		TestValue[] getArray();

		class CreateArrayValue extends DefaultValueProvider {

			static final TestValue[] DEFAULT_VALUE = new TestValue[] {
				TypedConfiguration.newConfigItem(TestValue.class),
				TypedConfiguration.newConfigItem(TestValue.class)
			};

			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return DEFAULT_VALUE;
			}

		}
	}
	
	public interface WithItemOptions extends ConfigurationItem {
		AbstractConf getConfig();

		@ImplementationClassDefault(OptionText.class)
		AbstractConf getConfigText();

		@ImplementationClassDefault(OptionValue.class)
		AbstractConf getConfigValue();
	}

	@Abstract
	public interface AbstractConf extends ConfigurationItem {
		// Pure marker interface.
	}

	public interface OptionText extends AbstractConf {
		String getText();
	}

	public interface OptionValue extends AbstractConf {
		int getValue();
	}

	public interface B extends A {

		@IntDefault(500)
		@Override
		int getPlainAnnotated();

		@Override
		@ListDefault({})
		List<ConfigurationItem> getList();

		@Override
		@ListDefault({})
		List<C> getInstanceList();

	}

	public interface ScenarioTypeEntry extends ConfigurationItem {

		String PROPERTY_NAME_KEY = "value";

		String PROPERTY_NAME_VALUE = "key";

		@Name(PROPERTY_NAME_KEY)
		int getKey();

		void setKey(int newKey);

		@Name(PROPERTY_NAME_VALUE)
		int getValue();

		void setValue(int newValue);

	}
	
	public interface PolyItemDefault extends PolymorphicConfiguration<TestDefaultValues> {
		// No special here
	}

	public interface H extends ConfigurationItem {
		boolean getBooleanPrimitive();
		Boolean getBoolean();
		int getIntegerPrimitive();
		Integer getInteger();
		long getLongPrimitive();
		Long getLong();
		byte getBytePrimitive();
		Byte getByte();
		short getShortPrimitive();
		Short getShort();
		float getFloatPrimitive();
		Float getFloat();
		double getDoublePrimitive();
		Double getDouble();
		char getCharacterPrimitive();
		Character getCharacter();
		Class<?> getClazz();
		String getString();
		List<H> getList();

		@Key(TestValue.NAME_ATTRIBUTE)
		Map<String, TestValue> getMap();
		H[] getArray();
	}

	public void testPropertyTypeDefaults() throws ConfigurationException {
		H h = TypedConfiguration.newConfigItem(H.class);
		assertEquals(false, h.getBooleanPrimitive());
		assertNull(h.getBoolean());
		assertEquals(0, h.getIntegerPrimitive());
		assertNull(h.getInteger());
		assertEquals(0L, h.getLongPrimitive());
		assertNull(h.getLong());
		assertEquals((byte) 0, h.getBytePrimitive());
		assertNull(h.getByte());
		assertEquals((short) 0, h.getShortPrimitive());
		assertNull(h.getShort());
		assertEquals(0.0F, h.getFloatPrimitive());
		assertNull(h.getFloat());
		assertEquals(0.0D, h.getDoublePrimitive());
		assertNull(h.getDouble());
		assertEquals((char) 0, h.getCharacterPrimitive());
		assertNull(h.getCharacter());
		assertNull(h.getClazz());
		assertNotNull(h.getList());
		assertTrue(h.getList().isEmpty());
		assertNotNull(h.getMap());
		assertTrue(h.getMap().isEmpty());
		assertNotNull(h.getArray());
		assertTrue(h.getArray().length == 0);
		ConstraintChecker constraintChecker = new ConstraintChecker();
		constraintChecker.check(h);
		assertTrue(constraintChecker.getFailures().isEmpty());
	}

	public void testDefaults() {
		A a = TypedConfiguration.newConfigItem(A.class);
		checkDefaultA(a);
	}

	public void testDefaultRead() throws ConfigurationException {
		A a = (A) read("<a/>", "<a/>");
		checkDefaultA(a);
	}

	private void checkDefaultA(A a) {
		assertEquals(42, a.getPlainAnnotated());
		assertEquals(42, a.getPlainFormatted());
		assertEquals(88, a.getPlainConfigured());
		assertEquals(null, a.getComplexInstance());
		assertEquals(13, a.getComplexFormatted().getValue());
		assertEquals(E.class, a.getComplexFormatted().getClass());
		C cTypedConfigInstance = a.getCTypedConfigInstance();
		assertEquals(F.class, cTypedConfigInstance.getClass());
		assertEquals(15, cTypedConfigInstance.getValue());
		List<ConfigurationItem> listDefault = a.getList();
		assertEquals(2, listDefault.size());
		assertTrue(listDefault.get(0) instanceof ScenarioTypeEntry);
		assertTrue(listDefault.get(1) instanceof PolymorphicConfiguration);
		assertSame(TestDefaultValues.class, ((PolymorphicConfiguration<?>) listDefault.get(1)).getImplementationClass());
		List<?> instanceListDefault = a.getInstanceList();
		assertEquals(2, instanceListDefault.size());
		assertTrue(instanceListDefault.get(0) instanceof C);
		assertTrue(instanceListDefault.get(1) instanceof F);
		assertEquals(F.class, ((F) instanceListDefault.get(1)).getConfig().getImplementationClass());
		assertNotNull(a.getPolyConfigItem());
		assertTrue(Arrays.equals(a.getArray(), A.CreateArrayValue.DEFAULT_VALUE));
	}
	
	public void testDefaultInheritance() {
		B b = TypedConfiguration.newConfigItem(B.class);
		assertEquals(500, b.getPlainAnnotated());
		assertEquals(42, b.getPlainFormatted());
		assertEquals(99, b.getPlainConfigured());
		assertEquals(null, b.getComplexInstance());
		assertEquals(13, b.getComplexFormatted().getValue());
		assertEquals(E.class, b.getComplexFormatted().getClass());
		assertEquals(emptyList(), b.getList());
		assertEquals(emptyList(), b.getInstanceList());
	}
	
	public void testGenericValueAccessWithSubProperties() {
		ConfigurationDescriptor bDescr = TypedConfiguration.getConfigurationDescriptor(B.class);
		ConfigurationDescriptor aDescr = TypedConfiguration.getConfigurationDescriptor(A.class);
		ConfigurationItem aConfig = TypedConfiguration.createConfigBuilder(aDescr).createConfig(context);
		ConfigurationItem bConfig = TypedConfiguration.createConfigBuilder(bDescr).createConfig(context);
		
		assertEquals(42, aConfig.value(aDescr.getProperty(A.PLAIN_ANNOTATED_NAME)));
		assertEquals(42, aConfig.value(bDescr.getProperty(A.PLAIN_ANNOTATED_NAME)));
		assertEquals(500, bConfig.value(aDescr.getProperty(A.PLAIN_ANNOTATED_NAME)));
		assertEquals(500, bConfig.value(bDescr.getProperty(A.PLAIN_ANNOTATED_NAME)));
	}

	public void testSameDefaultValueOnEachCall() {
		testSameDefaultValueOnEachCall(TypedConfiguration.newConfigItem(A.class));
	}

	public void testSameDefaultValueOnEachCallOfSubInterface() {
		testSameDefaultValueOnEachCall(TypedConfiguration.newConfigItem(B.class));
	}

	private void testSameDefaultValueOnEachCall(A a) {
		String message = "Ticket #12664: Default value is recomputed on every access.";
		assertSame(message, a.getComplexFormatted(), a.getComplexFormatted());
		assertSame(message, a.getCTypedConfigInstance(), a.getCTypedConfigInstance());
		assertSame(message, a.getConfigItem(), a.getConfigItem());
		assertSame(message, a.getPolyConfigItem(), a.getPolyConfigItem());
		assertSame(message, a.getList(), a.getList());
		assertSame(message, a.getMap(), a.getMap());
	}

	public void testDistinctDefaultValuesOnDistinctConfigItems() {
		A a1 = TypedConfiguration.newConfigItem(A.class);
		A a2 = TypedConfiguration.newConfigItem(A.class);
		testDistinctDefaultValuesOnDistinctConfigItems(a1, a2);
	}

	public void testDistinctDefaultValuesOnDistinctConfigItemsOfSubInterface() {
		B b1 = TypedConfiguration.newConfigItem(B.class);
		B b2 = TypedConfiguration.newConfigItem(B.class);
		testDistinctDefaultValuesOnDistinctConfigItems(b1, b2);
	}

	private void testDistinctDefaultValuesOnDistinctConfigItems(A a1, A a2) {
		String message = "Ticket #12664: Default value is illegally shared.";
		assertNotSame(message, a1.getComplexFormatted(), a2.getComplexFormatted());
		assertNotSame(message, a1.getCTypedConfigInstance(), a2.getCTypedConfigInstance());
		assertNotSame(message, a1.getConfigItem(), a2.getConfigItem());
		assertNotSame(message, a1.getPolyConfigItem(), a2.getPolyConfigItem());
		assertNotSame(message, a1.getList(), a2.getList());
		assertNotSame(message, a1.getMap(), a2.getMap());
	}

	public void testOverrideDefaultInstanceWithNull() throws ConfigurationException {
		A a1 = TypedConfiguration.newConfigItem(A.class);
		assertNotNull(a1.getCTypedConfigInstance());

		A a2 = read("<a/>");
		assertNotNull(a2.getCTypedConfigInstance());

		A a3 = read("<a c-typed-config-instance=''/>");
		assertNull(a3.getCTypedConfigInstance());

		A a4 = read(TypedConfiguration.toString(read("<a/>")));
		assertNotNull(a4.getCTypedConfigInstance());

		A a5 = read(TypedConfiguration.toString(read("<a c-typed-config-instance=''/>")));
		assertNull(a5.getCTypedConfigInstance());

		A a6 = TypedConfiguration.copy(a5);
		assertNull(a6.getCTypedConfigInstance());
	}

	public void testOverrideDefaultItemWithNull() throws ConfigurationException {
		A a1 = TypedConfiguration.newConfigItem(A.class);
		assertNotNull(a1.getConfigItem());

		A a2 = read("<a/>");
		assertNotNull(a2.getConfigItem());

		A a4 = read(TypedConfiguration.toString(read("<a/>")));
		assertNotNull(a4.getConfigItem());

		A a5s = TypedConfiguration.newConfigItem(A.class);
		a5s.setConfigItem(null);
		A a5 = read(TypedConfiguration.toString(a5s));
		assertNull(a5.getConfigItem());

		A a6 = TypedConfiguration.copy(a5s);
		assertNull(a6.getConfigItem());

		A a3 = read(
			"<a xmlns:config='" + ConfigurationSchemaConstants.CONFIG_NS + "'><config-item config:interface=''/></a>");
		assertNull(a3.getConfigItem());

		assertNull(read("<a xmlns:config='" + ConfigurationSchemaConstants.CONFIG_NS + "' config:interface=''/>"));
	}

	public void testSameDefaultValuesOnDistinctConfigItems() {
		ASharedDefault a1 = TypedConfiguration.newConfigItem(ASharedDefault.class);
		ASharedDefault a2 = TypedConfiguration.newConfigItem(ASharedDefault.class);
		testSameDefaultValuesOnDistinctConfigItems(a1, a2);
	}

	public void testSameDefaultValuesOnDistinctConfigItemsOfSubInterface() {
		BSharedDefault b1 = TypedConfiguration.newConfigItem(BSharedDefault.class);
		BSharedDefault b2 = TypedConfiguration.newConfigItem(BSharedDefault.class);
		testSameDefaultValuesOnDistinctConfigItems(b1, b2);
	}

	private void testSameDefaultValuesOnDistinctConfigItems(ASharedDefault a1, ASharedDefault a2) {
		String message = "Ticket #12664: Default value is not shared.";
		assertSame(message, a1.getString(), a2.getString());
		assertNotSame("Test is useless when Integer parses Strings to same object.",
			Integer.parseInt(ASharedDefault.INTEGER_DEFAULT), Integer.parseInt(ASharedDefault.INTEGER_DEFAULT));
		assertSame(message, a1.getInteger(), a2.getInteger());
		assertSame(message, a1.getDate(), a2.getDate());
	}
	
	public interface HWithDefaults extends H {

		@BooleanDefault(true)
		@Override
		boolean getBooleanPrimitive();

		@FormattedDefault("true")
		@Override
		Boolean getBoolean();

		@IntDefault(1)
		@Override
		int getIntegerPrimitive();

		@FormattedDefault("1")
		@Override
		Integer getInteger();

		@LongDefault(1)
		@Override
		long getLongPrimitive();

		@FormattedDefault("1")
		@Override
		Long getLong();

		@ByteDefault(1)
		@Override
		byte getBytePrimitive();

		@FormattedDefault("1")
		@Override
		Byte getByte();

		@ShortDefault(1)
		@Override
		short getShortPrimitive();

		@FormattedDefault("1")
		@Override
		Short getShort();

		@FloatDefault(1)
		@Override
		float getFloatPrimitive();

		@FormattedDefault("1")
		@Override
		Float getFloat();

		@DoubleDefault(1)
		@Override
		double getDoublePrimitive();

		@FormattedDefault("1")
		@Override
		Double getDouble();

		@CharDefault('c')
		@Override
		char getCharacterPrimitive();

		@FormattedDefault("c")
		@Override
		Character getCharacter();

		@ClassDefault(TestDefaultValues.class)
		@Override
		Class<?> getClazz();

		@StringDefault("string")
		@Override
		String getString();
	}

	public interface HExt extends H {
		// Some extension of H

	}

	public interface HAndHWithDefaultsExt extends HExt, HWithDefaults {
		// Sum interface
	}

	public void testDefaultValueInSecondarySuperInterface() {
		HWithDefaults hWithDefaults = TypedConfiguration.newConfigItem(HWithDefaults.class);
		assertEquals(true, hWithDefaults.getBooleanPrimitive());
		assertEquals(Boolean.TRUE, hWithDefaults.getBoolean());
		assertEquals(1, hWithDefaults.getIntegerPrimitive());
		assertEquals(Integer.valueOf(1), hWithDefaults.getInteger());
		assertEquals(1L, hWithDefaults.getLongPrimitive());
		assertEquals(Long.valueOf(1), hWithDefaults.getLong());
		assertEquals((byte) 1, hWithDefaults.getBytePrimitive());
		assertEquals(Byte.valueOf((byte) 1), hWithDefaults.getByte());
		assertEquals((short) 1, hWithDefaults.getShortPrimitive());
		assertEquals(Short.valueOf((short) 1), hWithDefaults.getShort());
		assertEquals(1D, hWithDefaults.getDoublePrimitive());
		assertEquals(Double.valueOf(1), hWithDefaults.getDouble());
		assertEquals(1F, hWithDefaults.getFloatPrimitive());
		assertEquals(Float.valueOf(1), hWithDefaults.getFloat());
		assertEquals('c', hWithDefaults.getCharacterPrimitive());
		assertEquals(Character.valueOf('c'), hWithDefaults.getCharacter());
		assertEquals(TestDefaultValues.class, hWithDefaults.getClazz());
		assertEquals("string", hWithDefaults.getString());

		HAndHWithDefaultsExt hAndHWithDefaultsExt = TypedConfiguration.newConfigItem(HAndHWithDefaultsExt.class);
		assertEquals(true, hAndHWithDefaultsExt.getBooleanPrimitive());
		assertEquals(Boolean.TRUE, hAndHWithDefaultsExt.getBoolean());
		assertEquals(1, hAndHWithDefaultsExt.getIntegerPrimitive());
		assertEquals(Integer.valueOf(1), hAndHWithDefaultsExt.getInteger());
		assertEquals(1L, hAndHWithDefaultsExt.getLongPrimitive());
		assertEquals(Long.valueOf(1), hAndHWithDefaultsExt.getLong());
		assertEquals((byte) 1, hAndHWithDefaultsExt.getBytePrimitive());
		assertEquals(Byte.valueOf((byte) 1), hAndHWithDefaultsExt.getByte());
		assertEquals((short) 1, hAndHWithDefaultsExt.getShortPrimitive());
		assertEquals(Short.valueOf((short) 1), hAndHWithDefaultsExt.getShort());
		assertEquals(1D, hAndHWithDefaultsExt.getDoublePrimitive());
		assertEquals(Double.valueOf(1), hAndHWithDefaultsExt.getDouble());
		assertEquals(1F, hAndHWithDefaultsExt.getFloatPrimitive());
		assertEquals(Float.valueOf(1), hAndHWithDefaultsExt.getFloat());
		assertEquals('c', hAndHWithDefaultsExt.getCharacterPrimitive());
		assertEquals(Character.valueOf('c'), hAndHWithDefaultsExt.getCharacter());
		assertEquals(TestDefaultValues.class, hAndHWithDefaultsExt.getClazz());
		assertEquals("string", hAndHWithDefaultsExt.getString());
	}

	public void testDefaultValuesAreMutable() {
		A a = TypedConfiguration.newConfigItem(A.class);
		try {
			a.getConfigItem().setKey(2);
			assertEquals(2, a.getConfigItem().getKey());
			ScenarioTypeEntry exampleValue = create(ScenarioTypeEntry.class);
			a.getList().add(exampleValue);
			assertTrue(a.getList().contains(exampleValue));
			assertEquals("Default values are not contained.", 3, a.getList().size());
			a.getMap().put(exampleValue.getKey(), exampleValue);
			assertEquals(exampleValue, a.getMap().get(exampleValue.getKey()));
		} catch (UnsupportedOperationException ex) {
			BasicTestCase.fail("Default values are illegally immutable.", ex);
		}

		A a2 = TypedConfiguration.newConfigItem(A.class);
		assertEquals(0, a2.getConfigItem().getKey());
	}

	public void testConfigurationSuperseedsImplementationClassDefault() throws ConfigurationException {
		A a1 = read("<a></a>");
		assertNull(a1.getCConfig());

		A a2 = read("<a><c-config/></a>");
		assertNotNull(a2.getCConfig());
		assertEquals(F.class, a2.getCConfig().getImplementationClass());
		assertEquals(F.Config.class, a2.getCConfig().descriptor().getConfigurationInterface());

		A a3 = read("<a><c-config class='" + G.class.getName() + "'/></a>");
		assertNotNull(a3.getCConfig());
		assertEquals(G.class, a3.getCConfig().getImplementationClass());
		assertEquals(G.Config.class, a3.getCConfig().descriptor().getConfigurationInterface());

		A a4 = read("<a><c-config class='" + C.class.getName() + "'/></a>");
		assertNotNull(a4.getCConfig());
		assertEquals(C.class, a4.getCConfig().getImplementationClass());
		// The configuration interface of the concrete configured class is more general than the
		// configuration interface of the default implementation class. Since a concrete class is
		// configured, the configuration type must not be too specific.
		assertEquals(PolymorphicConfiguration.class, a4.getCConfig().descriptor().getConfigurationInterface());
	}

	public void testConfigBuilderDefault() {
		ConfigBuilder bBuilder = TypedConfiguration.createConfigBuilder(B.class);
		PropertyDescriptor property = bBuilder.descriptor().getProperty(A.PLAIN_ANNOTATED_NAME);
		assertEquals(500, bBuilder.value(property));
		assertEquals("ConfigBuilder must deliver default value of concrete property if called with super property.",
			500, bBuilder.value(property.getSuperProperties()[0]));
		PropertyDescriptor foreignProperty =
			TypedConfiguration.getConfigurationDescriptor(ASharedDefault.class).getProperty(
				ASharedDefault.INTEGER_DEFAULT);
		try {
			bBuilder.value(foreignProperty);
			fail("Must not fetch value for unknown property.");
		} catch (Exception ex) {
			// expected
		}
	}

	public void testRead() throws ConfigurationException {
		ASharedDefault a = read("<a-shared></a-shared>");
		int expectedInt = Integer.parseInt(ASharedDefault.INTEGER_DEFAULT);
		Integer expectedInteger = Integer.valueOf(expectedInt);
		assertEquals("Ticket #16769:", expectedInteger, a.getInteger());
		assertEquals("Ticket #16769:", expectedInt, a.getInt());
		assertEquals("Ticket #16769:", ASharedDefault.IntFormatWithDefault.DEFAULT_VALUE.intValue(),
			a.getIntWithDefaultFromFormat());
		assertEquals("Ticket #16769:", expectedInt, a.getIntWithFormatAndDefault());
		assertEquals("Ticket #16769:", expectedInt, a.getIntWithFormatAndFormattedDefault());
	}

	public void testReadEmptyInteger() throws ConfigurationException {
		ASharedDefault a =
			read("<a-shared integer=''/>");
		assertEquals("Ticket #16769:", null, a.getInteger());
	}

	public void testReadEmptyInt() {
		String xml = "<ASharedDefault int=''/>";
		Pattern errorPattern = Pattern.compile(".*must not be empty.*");
		assertIllegalXml("Setting a primitive property to null has to fail.", xml, errorPattern, ASharedDefault.class);
	}

	public void testReadEmptyIntWithDefaultFromFormat() {
		String xml = "<ASharedDefault int-with-default-from-format=''/>";
		Pattern errorPattern = Pattern.compile(".*A primitive property cannot be null\\..*");
		assertIllegalXml("Setting a primitive property to null has to fail.", xml, errorPattern, ASharedDefault.class);
	}

	public void testReadEmptyIntWithFormatAndDefault() {
		String xml = "<ASharedDefault int-with-format-and-default=''/>";
		Pattern errorPattern = Pattern.compile(".*A primitive property cannot be null\\..*");
		assertIllegalXml("Setting a primitive property to null has to fail.", xml, errorPattern, ASharedDefault.class);
	}

	public void testReadEmptyIntWithFormatAndFormattedDefault() {
		String xml = "<ASharedDefault int-with-format-and-formatted-default=''/>";
		Pattern errorPattern = Pattern.compile(".*A primitive property cannot be null\\..*");
		assertIllegalXml("Setting a primitive property to null has to fail.", xml, errorPattern, ASharedDefault.class);
	}

	public static final class WrittenFourtyTwo extends AbstractConfigurationValueProvider<Integer> {
		
		/**
		 * Singleton {@link TestDefaultValues.WrittenFourtyTwo} instance.
		 */
		public static final WrittenFourtyTwo INSTANCE = new WrittenFourtyTwo();

		private WrittenFourtyTwo() {
			super(Integer.class);
		}
		
		@Override
		public Integer defaultValue() {
			return 0;
		}
	
		@Override
		public Integer getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
			return FOURTY_TWO.equals(propertyValue.toString()) ? 42 : 0;
		}
	
		@Override
		public String getSpecificationNonNull(Integer configValue) {
			return configValue == 42 ? FOURTY_TWO : "";
		}
	}

	public static class DynamicFourtyTwo extends DefaultValueProvider {
		private int _value = 42;

		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return _value++;
		}
	}

	/** 
	 * Custom value class.
	 */
	public static class C {
		int getValue() {
			return 42;
		}

		@Override
		public int hashCode() {
			return getClass().hashCode();
		}

		@Override
		public final boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			return equalsContents(obj);
		}

		protected boolean equalsContents(Object obj) {
			return getClass() == obj.getClass();
		}

	}
	
	public static class E extends C {
		@Override
		int getValue() {
			return 13;
		}
	}

	public static class G extends C {

		/**
		 * Configuration options for {@link TestDefaultValues.X}.
		 */
		public interface Config<I extends TestDefaultValues.G> extends PolymorphicConfiguration<I> {
			// Marker.
		}

		/**
		 * Creates a {@link TestDefaultValues.X} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public G(InstantiationContext context, Config<?> config) {
			// Noop.
		}

	}

	public static class F extends C implements ConfiguredInstance<F.Config> {

		private final Config _config;

		public interface Config extends PolymorphicConfiguration<C> {

			@IntDefault(15)
			int getValue();
		}

		/**
		 * Called by the typed configuration via reflection.
		 * 
		 * @param context
		 *        Required by the typed configuration.
		 * @param config
		 *        Required by the typed configuration.
		 */
		@CalledByReflection
		public F(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Config getConfig() {
			return _config;
		}

		@Override
		int getValue() {
			return _config.getValue();
		}

		@Override
		public int hashCode() {
			return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(_config);
		}

		@Override
		protected boolean equalsContents(Object obj) {
			return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(_config, ((F) obj).getConfig());
		}
	}

	public interface X extends ConfigurationItem {

		@Format(InitializedFormat.class)
		TestValue getN();

		class InitializedFormat extends AbstractConfigurationValueProvider<TestValue> {

			/**
			 * Singleton {@link TestDefaultValues.X.InitializedFormat} instance.
			 */
			public static final InitializedFormat INSTANCE = new InitializedFormat();

			private InitializedFormat() {
				super(TestValue.class);
			}

			@Override
			protected TestValue getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				if (propertyValue.toString().equals("uninitialized!")) {
					return name();
				} else {
					return name(propertyValue.toString());
				}
			}

			private TestValue name(String name) {
				TestValue result = name();
				result.update(result.descriptor().getProperty(TestValue.NAME_ATTRIBUTE), name);
				return result;
			}

			private TestValue name() {
				return TypedConfiguration.newConfigItem(TestValue.class);
			}

			@Override
			protected String getSpecificationNonNull(TestValue configValue) {
				return configValue.getName();
			}

			@Override
			public TestValue defaultValue() {
				return name("no-name");
			}

		}
	}
	
	public interface TestValue extends NamedConfigMandatory {
		// Pure marker.
	}

	public void testDefaultWithMandatoryProperties() {
		X item = TypedConfiguration.newConfigItem(X.class);
		assertNotNull(item.getN());
		assertEquals("no-name", item.getN().getName());
	}

	public interface Y extends ConfigurationItem {

		@FormattedDefault("some-name")
		@Format(X.InitializedFormat.class)
		TestValue getN();

	}

	public void testExplicitDefaultWithMandatoryProperties() {
		Y item = TypedConfiguration.newConfigItem(Y.class);
		assertEquals("some-name", item.getN().getName());
	}

	public interface Z extends ConfigurationItem {

		@ItemDefault
		TestValue getN();

	}

	public void testDefaultWithUninitializedMandatoryProperty() {
		Z z = TypedConfiguration.newConfigItem(Z.class);
		try {
			ExpectedFailureProtocol log = new ExpectedFailureProtocol();
			z.check(log);
			log.checkErrors();
			fail("Ticket #21085: Unset mandatory properties must be detected in check().");
		} catch (ExpectedFailure ex) {
			BasicTestCase.assertContains("Property is mandatory but not set", ex.getMessage());
		}
	}

	public interface Z1 extends ConfigurationItem {

		@FormattedDefault("uninitialized!")
		@Format(X.InitializedFormat.class)
		TestValue getN();

	}

	public void testDefaultWithDynamicallyUninitializedMandatoryProperty() {
		Z1 z1 = TypedConfiguration.newConfigItem(Z1.class);
		try {
			ExpectedFailureProtocol log = new ExpectedFailureProtocol();
			z1.check(log);
			log.checkErrors();
			fail("Ticket #21085: Unset mandatory properties must be detected in check().");
		} catch (ExpectedFailure ex) {
			BasicTestCase.assertContains("Property is mandatory but not set", ex.getMessage());
		}
	}

	public void testReadEmptyPrimitive() {
		String xml = "<Example int='' />";
		Pattern errorPattern = Pattern.compile(".*must not be empty.*");
		assertIllegalXml("Setting a primitive property to null has to fail.", xml, errorPattern, Example.class);
	}

	public void testReadEmptyPrimitiveWrapper() throws ConfigurationException {
		Example item = read("<Example integer='' />");
		assertEquals("Ticket #16769:", null, item.getInteger());
	}

	public void testReadEmptyNullableString() throws ConfigurationException {
		Example item = read("<Example string-nullable='' />");
		assertEquals("Ticket #16769:", null, item.getStringNullable());
	}

	public void testReadEmptyNonNullableString() throws ConfigurationException {
		Example item = read("<Example string-non-nullable='' />");
		assertEquals("Ticket #16769:", "", item.getStringNonNullable());
	}

	public void testReadEmptyNullableClass() throws ConfigurationException {
		Example item = read("<Example class-nullable='' />");
		assertEquals("Ticket #16769:", null, item.getClassNullable());
	}

	public void testReadEmptyNonNullableClass() {
		String xml = "<Example class-non-nullable='' />";
		Pattern errorPattern = Pattern.compile(".*Property is non-nullable, therefore 'null' is not allowed.*");
		assertIllegalXml("Setting a non-nullable property to null has to fail.", xml, errorPattern, Example.class);
	}

	public void testWriteWithOutUnconfiguredDefaultChange() throws XMLStreamException, ConfigurationException {
		A a = create(A.class);

		C instanceValuedDefault = a.getComplexFormatted();
		assertNotNull("Tests needs a default value", instanceValuedDefault);
		assertFalse("Test needs a default value without configuration.",
			instanceValuedDefault instanceof ConfiguredInstance<?>);
		String xml = toXML(a);
		Document dom = DOMUtil.parse(xml);
		NodeList children = dom.getDocumentElement().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			assertFalse(
				A.COMPLEX_FORMATTED
					+ " is an unset property with default value which is not configured. It must not be serialized",
				A.COMPLEX_FORMATTED.equals(children.item(i).getLocalName()));
		}

		ConfigurationItem storedA = fromXML(xml);
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(storedA, a));
	}

	public void testWriteModifiedItemDefault() throws XMLStreamException, ConfigurationException {
		A a = create(A.class);
		a.getConfigItem().setKey(42);

		assertFalse(valueSet(a, A.C_TYPED_CONFIG_INSTANCE));
		assertFalse(valueSet(a, A.COMPLEX_FORMATTED));
		assertFalse(valueSet(a, A.CONFIG_ITEM));

		String xml = toXML(a);

		ConfigurationItem storedA = fromXML(xml);
		assertTrue(xml, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(storedA, a));
	}

	public void testWriteModifiedItemDefaultInstance() throws ConfigurationException {
		ScenarioTypeModifiedItemDefaultInstance outer = create(ScenarioTypeModifiedItemDefaultInstance.class);
		outer.getExample().getConfig().setExample(1);
		PropertyDescriptor property = outer.descriptor().getProperty(ScenarioTypeModifiedItemDefaultInstance.EXAMPLE);
		assertFalse(outer.valueSet(property));
		ConfigurationItem throughXML = fromString(TypedConfiguration.toString(outer));
		String knownBugMarker = "Ticket #24062: ConfiguredInstances are not compared deep.";
		assertTrue(knownBugMarker, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(throughXML, outer));
	}

	public void testWriteModifiedListDefault() throws ConfigurationException {
		ScenarioTypeModifiedListDefault outer = create(ScenarioTypeModifiedListDefault.class);
		outer.getExampleList().get(0).setExample(1);
		PropertyDescriptor property = outer.descriptor().getProperty(ScenarioTypeModifiedListDefault.EXAMPLE);
		assertFalse(outer.valueSet(property));
		ConfigurationItem throughXML = fromString(TypedConfiguration.toString(outer));
		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(throughXML, outer));
	}

	public void testWriteModifiedListDefaultInstance() throws ConfigurationException {
		ScenarioTypeModifiedListDefaultInstances outer = create(ScenarioTypeModifiedListDefaultInstances.class);
		outer.getExampleList().get(0).getConfig().setExample(1);
		PropertyDescriptor property = outer.descriptor().getProperty(ScenarioTypeModifiedListDefaultInstances.EXAMPLE);
		assertFalse(outer.valueSet(property));
		ConfigurationItem throughXML = fromString(TypedConfiguration.toString(outer));
		String knownBugMarker = "Ticket #24062: ConfiguredInstances are not compared deep.";
		assertTrue(knownBugMarker, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(throughXML, outer));
	}

	private boolean valueSet(ConfigurationItem a, String property) {
		return a.valueSet(property(a, property));
	}

	private PropertyDescriptor property(ConfigurationItem a, String property) {
		return a.descriptor().getProperty(property);
	}

	public void testMinimize() throws XMLStreamException {
		A a = create(A.class);
		int plainDefault = a.getPlainFormatted();
		int innerDefault = a.getConfigItem().getValue();

		String xml1 = toXML(a);
		assertFalse(xml1, xml1.contains(A.PLAIN_FORMATTED));
		assertFalse(xml1, xml1.contains(ScenarioTypeEntry.PROPERTY_NAME_VALUE));

		a.setPlainFormatted(plainDefault);
		a.getConfigItem().setValue(innerDefault);

		String xml2 = toXML(a);
		assertTrue(xml2, xml2.contains(A.PLAIN_FORMATTED));
		assertTrue(xml2, xml2.contains(ScenarioTypeEntry.PROPERTY_NAME_VALUE));

		TypedConfiguration.minimize(a);

		String xml3 = toXML(a);
		assertFalse(xml3, xml3.contains(A.PLAIN_FORMATTED));
		assertFalse(xml1, xml1.contains(ScenarioTypeEntry.PROPERTY_NAME_VALUE));
	}

	public void testItemOptionsNull() {
		WithItemOptions item = TypedConfiguration.newConfigItem(WithItemOptions.class);
		assertNull(item.getConfig());
		assertNull(item.getConfigText());
		assertNull(item.getConfigValue());
	}

	public void testItemOptionsReadNull() throws ConfigurationException {
		WithItemOptions item = read("<with-item-options></with-item-options>");
		assertNull(item.getConfig());
		assertNull(item.getConfigText());
		assertNull(item.getConfigValue());
	}

	public void testItemOptionsReadDefault() throws ConfigurationException {
		WithItemOptions item =
			read("<with-item-options><config-text text='foobar'/><config-value value='42'/></with-item-options>");
		assertNull(item.getConfig());
		assertNotNull(item.getConfigText());
		assertNotNull(item.getConfigValue());
		assertEquals("foobar", ((OptionText) item.getConfigText()).getText());
		assertEquals(42, ((OptionValue) item.getConfigValue()).getValue());
	}

	public void testItemOptionsReadExplicit() throws ConfigurationException {
		WithItemOptions item =
			read(
				"<with-item-options xmlns:config='" + ConfigurationSchemaConstants.CONFIG_NS + "'>" +
					"<config config:interface='" + OptionValue.class.getName() + "' value='1'/>" +
					"<config-text config:interface='" + OptionValue.class.getName() + "' value='2'/>" +
					"<config-value config:interface='" + OptionValue.class.getName() + "' value='3'/>" +
				"</with-item-options>");
		assertNotNull(item.getConfig());
		assertNotNull(item.getConfigText());
		assertNotNull(item.getConfigValue());
		assertEquals(1, ((OptionValue) item.getConfig()).getValue());
		assertEquals(2, ((OptionValue) item.getConfigText()).getValue());
		assertEquals(3, ((OptionValue) item.getConfigValue()).getValue());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return new MapBuilder<String, ConfigurationDescriptor>()
			.put("a", TypedConfiguration.getConfigurationDescriptor(A.class))
			.put("a-shared", TypedConfiguration.getConfigurationDescriptor(ASharedDefault.class))
			.put("Example", TypedConfiguration.getConfigurationDescriptor(Example.class))
			.put("with-item-options", TypedConfiguration.getConfigurationDescriptor(WithItemOptions.class))
			.toMap();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(AbstractTypedConfigurationTestCase.suite(TestDefaultValues.class));
	}

}
