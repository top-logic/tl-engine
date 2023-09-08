/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * The class {@link TestConfigurationFallback} tests fallback to other
 * configurations.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigurationFallback extends AbstractTypedConfigurationTestCase {

	private static Map<String, ConfigurationDescriptor> base() {
		return Collections.singletonMap("base", TypedConfiguration.getConfigurationDescriptor(Base.class));
	}

	public interface A extends ConfigurationItem {


		String A_STRING_VALUE_DEFAULT_VALUE = "aDefault";

		String STRING_VALUE_PROPERTY_NAME = "stringValue";

		@StringDefault(A_STRING_VALUE_DEFAULT_VALUE)
		@Name(STRING_VALUE_PROPERTY_NAME)
		String getStringValue();

		void setStringValue(String stringValue);

		int A_INT_VALUE_DEFAULT_VALUE = 13;
		String INT_VALUE_PROPERTY_NAME = "intValue";

		@IntDefault(A_INT_VALUE_DEFAULT_VALUE)
		@Name(INT_VALUE_PROPERTY_NAME)
		int getIntValue();

		void setIntValue(int intValue);

		B getBConfig();

		@EntryTag("aConfig")
		@Key(STRING_VALUE_PROPERTY_NAME)
		@Subtypes({
			@Subtype(tag = "bConfig", type = B.class),
			@Subtype(tag = "cConfig", type = C.class),
		})
		Map<String, A> getAMapConfigs();

		@EntryTag("aConfig")
		@Subtypes({
			@Subtype(tag = "bConfig", type = B.class),
			@Subtype(tag = "cConfig", type = C.class),
		})
		@Key(STRING_VALUE_PROPERTY_NAME)
		List<A> getAListConfigs();
		
		@Subtypes({})
		PolymorphicConfiguration<? extends Object> getImpl();
	}

	public interface B extends A {
		String B_STRING_VALUE_DEFAULT_VALUE = "bDefault";
		@Override
		@StringDefault(B_STRING_VALUE_DEFAULT_VALUE)
		String getStringValue();

		int B_INT_VALUE_DEFAULT_VALUE = 15;
		@Override
		@IntDefault(B_INT_VALUE_DEFAULT_VALUE)
		int getIntValue();
	}

	/**
	 * Extension of {@link A} which has a {@link Map} of {@link ConfigurationItem}s identified by
	 * the {@link ConfigurationItem#getConfigurationInterface()}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface C extends A {
		
		/**
		 * A {@link Map} of {@link ConfigurationItem}s identified by the
		 * {@link ConfigurationItem#getConfigurationInterface()}
		 */
		@Key(CONFIGURATION_INTERFACE_NAME)
		@Subtypes({})
		Map<Class<?>, ConfigurationItem> getConfigItems();

		/**
		 * Simple property not existing in {@link A}
		 */
		Integer getNotInA();
	}
	
	public interface D extends PolymorphicConfiguration<Object> {

		@ClassDefault(Dimpl.class)
		@Override
		public Class<? extends Object> getImplementationClass();
	}

	public static class Dimpl {

	}

	public static class DSpecial extends Dimpl {

	}

	public static class DImplSub {

		public interface Config extends D {
			String getString();
		}

		public DImplSub(InstantiationContext context, Config config) {
		}

	}

	public void testFallback1() throws ConfigurationException {
		final A aConfig = TypedConfiguration.newConfigItem(A.class);
		final B bConfig = newBConfig(aConfig);
		assertEquals(B.B_STRING_VALUE_DEFAULT_VALUE, bConfig.getStringValue());

		String newBValue = "bValue";
		bConfig.setStringValue(newBValue);
		assertEquals(newBValue, bConfig.getStringValue());
	}

	private PropertyDescriptor bProperty(String propertyName) {
		final ConfigurationDescriptor bDescr = TypedConfiguration.getConfigurationDescriptor(B.class);
		final PropertyDescriptor property = bDescr.getProperty(propertyName);
		return property;
	}

	private B newBConfig(final ConfigurationItem fallback) throws ConfigurationException {
		final ConfigBuilder bConfigbuilder = TypedConfiguration.createConfigBuilder(B.class);
		TypedConfiguration.applyFallback(fallback, bConfigbuilder, context);
		B result = (B) bConfigbuilder.createConfig(context);
		// Report errors asap, so they are not hidden by failing assertions.
		context.checkErrors();
		return result;
	}

	public void testFallback2() throws ConfigurationException {
		final A aConfig = TypedConfiguration.newConfigItem(A.class);
		String stringValue = "aValue";
		aConfig.setStringValue(stringValue);
		assertEquals(stringValue, aConfig.getStringValue());

		final B bConfig = newBConfig(aConfig);
		assertEquals(stringValue, bConfig.getStringValue());

		String newBValue = "bValue";
		bConfig.setStringValue(newBValue);
		assertEquals(newBValue, bConfig.getStringValue());

	}

	public void testFallbackPrimitive1() throws ConfigurationException {
		final ConfigurationItem aConfig = TypedConfiguration.newConfigItem(A.class);
		final B bConfig = newBConfig(aConfig);
		assertEquals(B.B_INT_VALUE_DEFAULT_VALUE, bConfig.getIntValue());

		int newBValue = 34;
		bConfig.setIntValue(newBValue);
		assertEquals(newBValue, bConfig.getIntValue());
	}

	public void testFallbackPrimitive2() throws ConfigurationException {
		final A aConfig = TypedConfiguration.newConfigItem(A.class);
		int intValue = 34;
		aConfig.setIntValue(intValue);
		assertEquals(intValue, aConfig.getIntValue());

		final B bConfig = newBConfig(aConfig);
		assertEquals(intValue, bConfig.getIntValue());

		int newBValue = 245;
		bConfig.setIntValue(newBValue);
		assertEquals(newBValue, bConfig.getIntValue());
	}

	public void testInterfaceSpecializationNewPropertyDefaultValue() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);
		ConfigBuilder cBuilder = TypedConfiguration.createConfigBuilder(C.class);
		TypedConfiguration.applyFallback(a, cBuilder, context);

		ConfigurationItem config = cBuilder.createConfig(context);
		// Report errors asap, so they are not hidden by failing assertions.
		context.checkErrors();
		assertEquals(C.class, config.getConfigurationInterface());
		C c = (C) config;
		// Access default value of primitive property which does not exist in fallback item
		assertNull(c.getNotInA());
	}

	public void testInterfaceSpecializationRedeclaredPropertyDefaultValue() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);
		ConfigBuilder bBuilder = TypedConfiguration.createConfigBuilder(B.class);
		TypedConfiguration.applyFallback(a, bBuilder, context);
		ConfigurationItem config = bBuilder.createConfig(context);
		// Report errors asap, so they are not hidden by failing assertions.
		context.checkErrors();
		assertEquals(B.class, config.getConfigurationInterface());
		B b = (B) config;
		// Access default value of primitive property which exist in fallback item but is redeclared
		// in concrete item, i.e. access occurs with a different PropertyDescriptor.
		assertEquals(B.B_STRING_VALUE_DEFAULT_VALUE, b.getStringValue());
	}

	public void testInterfaceSpecializationRedeclaredPropertySetValue() throws ConfigurationException {
		A a = TypedConfiguration.newConfigItem(A.class);
		int intValue = 17;
		assertNotSame(intValue, A.A_INT_VALUE_DEFAULT_VALUE);
		assertNotSame(intValue, B.B_INT_VALUE_DEFAULT_VALUE);
		a.setIntValue(intValue);
		ConfigBuilder bBuilder = TypedConfiguration.createConfigBuilder(B.class);
		TypedConfiguration.applyFallback(a, bBuilder, context);
		ConfigurationItem config = bBuilder.createConfig(context);
		// Report errors asap, so they are not hidden by failing assertions.
		context.checkErrors();
		assertEquals(B.class, config.getConfigurationInterface());
		B b = (B) config;
		assertEquals(intValue, b.getIntValue());
		PropertyDescriptor intProperty = b.descriptor().getProperty(A.INT_VALUE_PROPERTY_NAME);
		assertEquals(intValue, b.value(intProperty));
	}

	public void testListMoveUpdate() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();
		
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "listUpdate.move.expected.xml", "listUpdate.move.xml", "listUpdate.move.increment.xml");
	}

	public void testListMoveStartEndUpdate() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();

		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName,
			"listUpdate.moveStartEnd.expected.xml",
			"listUpdate.moveStartEnd.xml",
			"listUpdate.moveStartEnd.increment.xml");
	}

	public void testListUpdate() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();
		
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "listUpdate.expected.xml", "listUpdate.xml", "listUpdate.increment.xml");
	}
	
	public void testListAddOrUpdate() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();
		
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "listAddOrUpdate.expected.xml", "listAddOrUpdate.xml", "listAddOrUpdate.increment.xml");
	}
	
	public void testSpecialisationListAddOrUpdate() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();
		
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName,
			"listSpecialisationAddOrUpdate.expected.xml",
			"listSpecialisationAddOrUpdate.xml",
			"listSpecialisationAddOrUpdate.increment.xml");
	}
	
	public void testMapSpecialisationAddOrUpdate() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();

		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName,
			"mapSpecialisationAddOrUpdate.expected.xml",
			"mapSpecialisationAddOrUpdate.xml",
			"mapSpecialisationAddOrUpdate.increment.xml");
	}

	public void testImplUpdate() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();

		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "implUpdate.increment.expected.xml",
			"implUpdate.xml", "implUpdate.increment.xml");
	}

	public void testSimpleItemUpdate() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();
		
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "itemUpdate.increment.expected.xml", "itemUpdate.xml", "itemUpdate.increment.xml");
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "itemUpdate.override.expected.xml", "itemUpdate.xml", "itemUpdate.override.xml");
	}
	
	public void testImplementationClass() throws ConfigurationException, IOException {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();

		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "implClass.increment.expected.xml",
			"implClass.xml", "implClass.increment.xml");
	}

	public void testMapUpdate() throws ConfigurationException, IOException {
		HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();

		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "mapUpdate.increment.expected.xml", "mapUpdate.xml", "mapUpdate.increment.xml");
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "mapUpdate.override.expected.xml", "mapUpdate.xml", "mapUpdate.override.xml");
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "mapAddOrUpdate.expected.xml", "mapUpdate.xml", "mapAddOrUpdate.increment.xml");
	}

	public interface Base extends ConfigurationItem {
		@Key(Value.KEY)
		@EntryTag(Value.TAG_NAME)
		Collection<Value> getCollection();

		@Key(Value.KEY)
		@EntryTag(Value.TAG_NAME)
		Map<String, Value> getMap();

		@Key(Value.KEY)
		@EntryTag(Value.TAG_NAME)
		List<Value> getList();
	}
	
	public interface Value extends ConfigurationItem {
		String TAG_NAME = "value";

		String KEY = "key";

		@Name(KEY)
		@StringDefault("foobar")
		String getKey();

		int getX();
	}
	
	public void testDefaultMapKey() throws Exception {
		assertEquals(TestConfigurationFallback.class, base(),
			"defaultMapKey.expected.xml", "defaultMapKey.xml", "defaultMapKey.increment.xml");
	}
	
	public void testDefaultListKey() throws Exception {
		assertEquals(TestConfigurationFallback.class, base(),
			"defaultListKey.expected.xml", "defaultListKey.xml", "defaultListKey.increment.xml");
	}

	public void testDefaultCollectionKey() throws Exception {
		assertEquals(TestConfigurationFallback.class, base(),
			"defaultCollectionKey.expected.xml", "defaultCollectionKey.xml", "defaultCollectionKey.increment.xml");
	}

	/**
	 * Tests that the {@link ConfigurationItem#getConfigurationInterface()} can be used as key in a
	 * map valued {@link PropertyDescriptor}.
	 */
	public void testMapByConfigInterface() throws ConfigurationException, IOException {
		HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();
		
		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "mapByInterface.expected.xml",
			"mapByInterface.xml", "mapByInterface.increment.xml");
	}
	
	/**
	 * Tests that in an indexed list the last element can be removed when updating configuration.
	 */
	public void testRemoveLastListElement() throws ConfigurationException, IOException {
		Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName = getDescriptors();

		assertEquals(TestConfigurationFallback.class, globalDescriptorsByLocalName, "listRemoveLast.expected.xml",
			"listRemoveLast.xml", "listRemoveLast.increment.xml", "listRemoveLast.increment2.xml");
	}

	public interface ScenarioTypeItemOwner extends ConfigurationItem {

		ScenarioTypeItem getItem();

	}

	public interface ScenarioTypeItem extends ConfigurationItem {

		String getAlpha();

		String getBeta();
	}

	@SuppressWarnings("unchecked")
	public void testOverrideTrueOnItemOwner() throws ConfigurationException {
		String baseXml = XML_DECLARATION
			+ "<ScenarioTypeItemOwner " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <item alpha='a' beta='b' />"
			+ "</ScenarioTypeItemOwner>";
		String incrementXml = XML_DECLARATION
			+ "<ScenarioTypeItemOwner config:override='true' " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <item alpha='x' />"
			+ "</ScenarioTypeItemOwner>";
		ScenarioTypeItemOwner actualItem =
			read(createDescriptorMap(ScenarioTypeItemOwner.class), baseXml, incrementXml);
		assertNotNull(actualItem.getItem());
		assertEquals("x", actualItem.getItem().getAlpha());
		assertEquals("", actualItem.getItem().getBeta());
	}

	@SuppressWarnings("unchecked")
	public void testOverrideTrueOnItemEntry() throws ConfigurationException {
		String baseXml = XML_DECLARATION
			+ "<ScenarioTypeItemOwner " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <item alpha='a' beta='b' />"
			+ "</ScenarioTypeItemOwner>";
		String incrementXml = XML_DECLARATION
			+ "<ScenarioTypeItemOwner " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <item config:override='true' alpha='x' />"
			+ "</ScenarioTypeItemOwner>";
		ScenarioTypeItemOwner actualItem =
			read(createDescriptorMap(ScenarioTypeItemOwner.class), baseXml, incrementXml);
		assertNotNull(actualItem.getItem());
		assertEquals("x", actualItem.getItem().getAlpha());
		assertEquals("", actualItem.getItem().getBeta());
	}

	public interface ScenarioTypeMapOwner extends ConfigurationItem {

		@Key(Value.KEY)
		@EntryTag(Value.TAG_NAME)
		Map<String, Value> getMap();

	}

	@SuppressWarnings("unchecked")
	public void testOverrideTrueOnMapOwner() throws ConfigurationException {
		String baseXml = XML_DECLARATION
			+ "<ScenarioTypeMapOwner " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <map>"
			+ "    <value key='first' x='1' />"
			+ "    <value key='second' x='2' />"
			+ "  </map>"
			+ "</ScenarioTypeMapOwner>";
		String incrementXml = XML_DECLARATION
			+ "<ScenarioTypeMapOwner config:override='true' " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <map>"
			+ "    <value key='first' />"
			+ "  </map>"
			+ "</ScenarioTypeMapOwner>";
		ScenarioTypeMapOwner actualItem = read(createDescriptorMap(ScenarioTypeMapOwner.class), baseXml, incrementXml);
		assertEquals(1, actualItem.getMap().size());
		assertEquals(0, actualItem.getMap().get("first").getX());
	}

	@SuppressWarnings("unchecked")
	public void testOverrideTrueOnMapTag() throws ConfigurationException {
		String baseXml = XML_DECLARATION
			+ "<ScenarioTypeMapOwner " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <map>"
			+ "    <value key='first' x='1' />"
			+ "    <value key='second' x='2' />"
			+ "  </map>"
			+ "</ScenarioTypeMapOwner>";
		String incrementXml = XML_DECLARATION
			+ "<ScenarioTypeMapOwner " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <map config:override='true'>"
			+ "    <value key='first' />"
			+ "  </map>"
			+ "</ScenarioTypeMapOwner>";
		ScenarioTypeMapOwner actualItem = read(createDescriptorMap(ScenarioTypeMapOwner.class), baseXml, incrementXml);
		assertEquals(1, actualItem.getMap().size());
		assertEquals(0, actualItem.getMap().get("first").getX());
	}

	@SuppressWarnings("unchecked")
	public void testOverrideTrueOnMapEntry() throws ConfigurationException {
		String baseXml = XML_DECLARATION
			+ "<ScenarioTypeMapOwner " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <map>"
			+ "    <value key='first' x='1' />"
			+ "    <value key='second' x='2' />"
			+ "  </map>"
			+ "</ScenarioTypeMapOwner>";
		String incrementXml = XML_DECLARATION
			+ "<ScenarioTypeMapOwner " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <map>"
			+ "    <value config:override='true' key='first' />"
			+ "  </map>"
			+ "</ScenarioTypeMapOwner>";
		ScenarioTypeMapOwner actualItem = read(createDescriptorMap(ScenarioTypeMapOwner.class), baseXml, incrementXml);
		assertEquals(2, actualItem.getMap().size());
		assertEquals(0, actualItem.getMap().get("first").getX());
		assertEquals(2, actualItem.getMap().get("second").getX());
	}

	public void testMapOverrideKeepsConfigInterface() throws ConfigurationException {
		String baseXml = XML_DECLARATION
			+ "<A " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <a-map-configs>"
			+ "    <aConfig config:interface='" + C.class.getName() + "' stringValue='first' />"
			+ "  </a-map-configs>"
			+ "</A>";
		Map<String, ConfigurationDescriptor> descriptorMap = createDescriptorMap(A.class);
		A baseItem = read(descriptorMap, baseXml);
		A firstC = baseItem.getAMapConfigs().get("first");
		assertNotNull(firstC);
		assertTrue(firstC instanceof C);

		String increment = XML_DECLARATION
				+ "<A " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
				+ "  <a-map-configs>"
				+ "    <aConfig stringValue='first' not-in-a='43' />"
				+ "  </a-map-configs>"
				+ "</A>";
		ConfigurationReader reader = new ConfigurationReader(context, descriptorMap);
		reader.setBaseConfig(baseItem);
		reader.setSource(CharacterContents.newContent(increment));
		ConfigurationItem incrementItem;
		try {
			incrementItem = reader.read();
		} catch (AssertionFailedError ex) {
			throw BasicTestCase.fail(
				"Ticket #19377: Configuration interface of fallback item (C.class) not inherited?", ex);
		}

		assertTrue(incrementItem instanceof A);
		A incrementC = ((A) incrementItem).getAMapConfigs().get("first");
		assertNotNull(incrementC);
		assertTrue(incrementC instanceof C);
		assertSame(43, ((C) incrementC).getNotInA());
	}

	@Override
	protected HashMap<String, ConfigurationDescriptor> getDescriptors() {
		final HashMap<String, ConfigurationDescriptor> globalDescriptorsByLocalName =
			new HashMap<>();
		globalDescriptorsByLocalName.put("fallback-test", TypedConfiguration.getConfigurationDescriptor(A.class));
		globalDescriptorsByLocalName.put("fallback-test-c", TypedConfiguration.getConfigurationDescriptor(C.class));
		globalDescriptorsByLocalName.put("fallback-test-d", TypedConfiguration.getConfigurationDescriptor(D.class));
		return globalDescriptorsByLocalName;
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestConfigurationFallback.class));
	}

}

