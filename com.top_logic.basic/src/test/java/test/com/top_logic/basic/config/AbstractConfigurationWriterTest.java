/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import test.com.top_logic.basic.config.Scenario2.TestItemWithFormat;
import test.com.top_logic.basic.config.TypedConfigurationSzenario.A.Config;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Abstract test case for writing {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractConfigurationWriterTest extends AbstractTypedConfigurationTestCase
		implements TypedConfigurationSzenario {

	public interface TestItemPropertyWithFormat extends ConfigurationItem{
		
		TestItemWithFormat getItemWithFormat();

		void setItemWithFormat(TestItemWithFormat value);
	}

	public interface TestA extends ConfigurationItem {

		TestA getOtherWhichIsPolymophic();

		void setOtherWhichIsPolymophic(TestA value);
	}

	public static class PolymporphicImplementingTestAImpl
			extends AbstractConfiguredInstance<PolymporphicImplementingTestAImpl.Config> {

		public interface Config extends PolymorphicConfiguration<PolymporphicImplementingTestAImpl>, TestA {
			// No additional
		}

		public PolymporphicImplementingTestAImpl(InstantiationContext context, Config config) {
			super(context, config);
		}

	}

	public interface TestCDATAInAttributeValue extends ConfigurationItem {

		String getValue();

		void setValue(String val);
	}

	@DisplayOrder({ ConfigWithDisplayAnnotation.C_NAME, ConfigWithDisplayAnnotation.A_NAME })
	public interface ConfigWithDisplayAnnotation extends ConfigurationItem {

		String A_NAME = "a";

		String B_NAME = "b";

		String C_NAME = "c";

		@Name(A_NAME)
		ConfigWithDisplayAnnotation getA();

		void setA(ConfigWithDisplayAnnotation a);

		@Name(B_NAME)
		ConfigWithDisplayAnnotation getB();

		void setB(ConfigWithDisplayAnnotation b);

		@Name(C_NAME)
		ConfigWithDisplayAnnotation getC();

		void setC(ConfigWithDisplayAnnotation c);

	}

	@DisplayOrder({ ConfigWithDisplayStrategyIgnoreAnnotation.C_NAME,
		ConfigWithDisplayStrategyIgnoreAnnotation.A_NAME })
	@DisplayInherited(DisplayStrategy.IGNORE)
	public interface ConfigWithDisplayStrategyIgnoreAnnotation extends SuperConfigOfDisplayStrategyIgnoreAnnotation {

		String A_NAME = "a";

		String B_NAME = "b";

		String C_NAME = "c";

		@Name(A_NAME)
		int getA();

		void setA(int a);

		@Name(B_NAME)
		int getB();

		void setB(int b);

		@Name(C_NAME)
		int getC();

		void setC(int c);

	}

	public interface SuperConfigOfDisplayStrategyIgnoreAnnotation extends ConfigurationItem {

		String D_NAME = "d";

		@Name(D_NAME)
		int getD();

		void setD(int d);

	}

	public interface WithImplClassDefault extends ConfigurationItem {

		@ImplementationClassDefault(C.class)
		PolymorphicConfiguration<B> getB();

		void setB(PolymorphicConfiguration<B> value);

	}

	public static interface TagNameTest {

		public interface TestConfig extends ConfigurationItem {

			String CONFIG_NAME = "config";

			String POLYMORPHIC_NAME = "polymorphic";

			String ENTRY = "entry";

			@Name(POLYMORPHIC_NAME)
			@EntryTag(ENTRY)
			List<SuperPolymorphic<TagNameTest>> getPolymorphic();

			@Name(CONFIG_NAME)
			@EntryTag(ENTRY)
			List<SuperConfiguration> getConfigs();

		}

		public interface SuperPolymorphic<T> extends PolymorphicConfiguration<T> {
			// Marker interface to avoid uniqueness problems with tag names
		}

		public interface SuperConfiguration extends ConfigurationItem {
			// Marker interface to avoid uniqueness problems with tag names
		}

		@TagName(NamedPolymorphicConfig.NAMED_POLYMORPHIC_TAG)
		public interface NamedPolymorphicConfig extends SuperPolymorphic<TagNameTest> {

			String NAMED_POLYMORPHIC_TAG = "named_polymorphic";

		}

		public interface UnnamedPolymorphicConfig extends SuperPolymorphic<TagNameTest> {
			// marker interface
		}

		@TagName(NamedConfig.NAMED_CONFIG_TAG)
		public interface NamedConfig extends SuperConfiguration {

			String NAMED_CONFIG_TAG = "named_config";

		}

		public interface UnnamedConfig extends SuperConfiguration {
			// marker interface
		}

		public static final TagNameTest INSTANCE = new TagNameTest() {
			// No properties
		};
	}

	public void testWritePropertiesDisplayStrategyIgnore() throws Exception {
		int A_VALUE = 1;
		int B_VALUE = 2;
		int C_VALUE = 3;
		int D_VALUE = 4;

		ConfigWithDisplayStrategyIgnoreAnnotation root =
			TypedConfiguration.newConfigItem(ConfigWithDisplayStrategyIgnoreAnnotation.class);
		root.setA(A_VALUE);
		root.setB(B_VALUE);
		root.setC(C_VALUE);
		root.setD(D_VALUE);

		String rootSerialized = serializeItem(root);
		ConfigWithDisplayStrategyIgnoreAnnotation rootDeserialized =
			(ConfigWithDisplayStrategyIgnoreAnnotation) readItem(rootSerialized);

		assertEquals(ConfigWithDisplayAnnotation.A_NAME + " has value " + A_VALUE + ".",
			A_VALUE, rootDeserialized.getA());
		assertEquals(ConfigWithDisplayAnnotation.B_NAME
			+ " is not contained in display order, but must be written with value " + B_VALUE + ".",
			B_VALUE, rootDeserialized.getB());
		assertEquals(ConfigWithDisplayAnnotation.C_NAME + " has value " + C_VALUE + ".",
			C_VALUE, rootDeserialized.getC());
		assertEquals(SuperConfigOfDisplayStrategyIgnoreAnnotation.D_NAME
			+ " is not contained in display order and is ignored at GUI, but must be written with value " + D_VALUE
			+ ".", D_VALUE, rootDeserialized.getD());
	}

	public void testSetEmptyForListPropertyWithDefault() throws Exception {
		checkSettingValue(FConfig.class, FConfig.LIST_PROPERTY_NAME, Collections.emptyList());
	}
	
	public void testListDefault() throws Exception {
		FConfig item = TypedConfiguration.newConfigItem(FConfig.class);
		assertEquals(Collections.singletonList(FConfig.LIST_DEFAULT_VALUE), item.getList());
		checkSettingValue(FConfig.class, FConfig.LIST_PROPERTY_NAME, Collections.emptyList());
	}
	
	public void testSetNullForIndextedProperty() throws Exception {
		// null will be normalized to an empty map
		checkSettingValue(A.Config.class, A.Config.INDEXED_NAME, null);
	}
	
	public void testSetNullForListProperty() throws Exception {
		// null will be normalized to an empty list
		checkSettingValue(FConfig.class, FConfig.LIST_PROPERTY_NAME, null);
	}

	public void testSetNullForClassPropertyWithDefault() throws Exception {
		checkSettingValue(EConfig.class, EConfig.CLAZZ_NAME, null);
	}
	
	public void testSetNullForStringPropertyWithDefault() throws Exception {
		// null will be normalized to an empty string
		checkSettingValue(D.Config.class, D.Config.Z_NAME, null);
	}
	
	public void testSetEmptyForStringPropertyWithDefault() throws Exception {
		checkSettingValue(D.Config.class, D.Config.Z_NAME, "");
	}

	private void checkSettingValue(Class<?> configurationInterface, String propertyName, Object value)
			throws Exception {
		final ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configurationInterface);
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(descriptor);
		builder.initValue(descriptor.getProperty(propertyName), value);
		doReadWrite("test", descriptor, createConfig(builder));
	}
	
	public void testSetDefaultForPropertyWithDefault() throws Exception {
		final ConfigurationDescriptor dDescriptor = TypedConfiguration.getConfigurationDescriptor(D.Config.class);
		ConfigBuilder dbuilder = TypedConfiguration.createConfigBuilder(dDescriptor);
		final PropertyDescriptor property = dDescriptor.getProperty(D.Config.Z_NAME);
		dbuilder.initValue(property, property.getDefaultValue());
		
		final ConfigurationItem config = createConfig(dbuilder);
		doReadWrite("d", dDescriptor, config);
	}

	public void testWrite() throws Exception {
		ConfigurationDescriptor aDescr = TypedConfiguration.getConfigurationDescriptor(A.Config.class);
		ConfigurationDescriptor bDescr = TypedConfiguration.getConfigurationDescriptor(B.Config.class);
		
		PropertyDescriptor pProperty = aDescr.getProperty(A.Config.P_NAME);
		PropertyDescriptor bProperty = aDescr.getProperty(A.Config.B_CONFIG_NAME);
		PropertyDescriptor indexedProperty = aDescr.getProperty(A.Config.INDEXED_NAME);
		
		ConfigBuilder bBuilder = TypedConfiguration.createConfigBuilder(bDescr);
		bBuilder.initValue(bDescr.getProperty(B.Config.X_NAME), Integer.valueOf(13));
		
		ConfigBuilder aBuilder = TypedConfiguration.createConfigBuilder(aDescr);
		aBuilder.initValue(pProperty, Integer.valueOf(3));
		aBuilder.initValue(bProperty, createConfig(bBuilder));
		{
			// TODO: This should be automatically initialized.
			aBuilder.initValue(indexedProperty, new HashMap());
			
			ConfigBuilder a1Builder = TypedConfiguration.createConfigBuilder(aDescr);
			a1Builder.initValue(pProperty, 13);
			// TODO: This should be simplified with .add(a1Builder.createConfig());
			((Map) aBuilder.value(indexedProperty)).put(13, createConfig(a1Builder));
			
			ConfigBuilder a2Builder = TypedConfiguration.createConfigBuilder(aDescr);
			a2Builder.initValue(pProperty, 42);
			// TODO: This should be simplified with .add(a1Builder.createConfig());
			((Map) aBuilder.value(indexedProperty)).put(42, createConfig(a2Builder));
		}
		
		A.Config a = (Config) createConfig(aBuilder);
		assertEquals(3, a.getP());
		assertNotNull(a.getBConfig());
		assertEquals(13, a.getBConfig().getX());
		
		String serializedA = writeConfigurationItem("a", aDescr, a);
		// System.out.println(serializedA);
		
		CharacterContent content = CharacterContents.newContent(serializedA);
		A.Config aCopy = (Config) readConfigItem("a", aDescr, content);
		
		assertEquals(a, aCopy);
		String serializedCopy = writeConfigurationItem("a", aDescr, aCopy);
		// System.out.println(serializedCopy);
		
		assertEquals(serializedA, serializedCopy);
	}

	public void testPolymorphicValueForNonPolymorphicProperty() throws Exception {
		TestA newConfigItem = TypedConfiguration.newConfigItem(TestA.class);
		PolymporphicImplementingTestAImpl.Config polymorphicConfig =
			TypedConfiguration.newConfigItem(PolymporphicImplementingTestAImpl.Config.class);
		newConfigItem.setOtherWhichIsPolymophic(polymorphicConfig);

		doReadWrite("test-polymorphic", TypedConfiguration.getConfigurationDescriptor(TestA.class), newConfigItem);
	}

	public void testNewLineInStringValue() throws Exception {
		doTestStringValue("\nvalue\nwith\nnew\nline\n!\n", true);
	}

	public void testCRInStringValue() throws Exception {
		doTestStringValue("\rvalue\rwith\rnew\rline\r!\r", false);
	}

	public void testCRLFInStringValue() throws Exception {
		doTestStringValue("\r\nvalue\r\nwith\r\nnew\rline\r\n!\r\n", false);
	}

	private void doTestStringValue(String stringValue, boolean pretty)
			throws Exception {
		D.Config d = create(D.Config.class);
		d.setX(33);
		d.setZ(stringValue);
		d.setZZ("a");
		
		String serializedA = serializeItem(d);

		if (pretty) {
			serializedA = prettyPrintSerialized(serializedA);
		}

		D.Config dCopy = (D.Config) readItem(serializedA);
		
		assertEquals(d, dCopy);
	}

	/**
	 * Pretty prints the serialised item (if possible).
	 */
	protected String prettyPrintSerialized(String serializedA) {
		return serializedA;
	}

	private ConfigurationItem readItem(String serializedA) throws ConfigurationException {
		CharacterContent content = CharacterContents.newContent(serializedA);
		ConfigurationDescriptor rootType = TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class);
		return readConfigItem("item", rootType, content);
	}

	protected String serializeItem(ConfigurationItem a) throws Exception {
		return writeConfigurationItem("item", TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class), a);
	}

	/**
	 * Test case that ensures that even non-{@link PolymorphicConfiguration}
	 * items are serialized with their concrete type, if they are used
	 * polymorphically.
	 */
	public void testInterfaceAnnotation() throws Exception {
		ConfigurationDescriptor aDescr = TypedConfiguration.getConfigurationDescriptor(Scenario2.A.class);
		ConfigurationDescriptor bDescr = TypedConfiguration.getConfigurationDescriptor(Scenario2.B.class);
		ConfigurationDescriptor cDescr = TypedConfiguration.getConfigurationDescriptor(Scenario2.C.class);
		
		ConfigBuilder b1Builder = TypedConfiguration.createConfigBuilder(bDescr);
		Scenario2.B b1 = (Scenario2.B) createConfig(b1Builder);
		
		ConfigBuilder b2Builder = TypedConfiguration.createConfigBuilder(bDescr);
		b2Builder.initValue(bDescr.getProperty("parent"), b1);
		b2Builder.initValue(bDescr.getProperty("other"), b1);
		Scenario2.B b2 = (Scenario2.B) createConfig(b2Builder);
		
		doReadWrite("a", aDescr, b2);
		doReadWrite("b", bDescr, b2);
		
		ConfigBuilder c1Builder = TypedConfiguration.createConfigBuilder(cDescr);
		c1Builder.initValue(cDescr.getProperty("parent"), b1);
		c1Builder.initValue(cDescr.getProperty("other"), b1);
		Scenario2.C c1 = (Scenario2.C) createConfig(c1Builder);

		doReadWrite("a", aDescr, c1);
		doReadWrite("b", bDescr, c1);
		doReadWrite("c", cDescr, c1);
		
		ConfigBuilder c2Builder = TypedConfiguration.createConfigBuilder(cDescr);
		c2Builder.initValue(cDescr.getProperty("parent"), c1);
		c2Builder.initValue(cDescr.getProperty("other"), c1);
		c2Builder.initValue(cDescr.getProperty("next"), c1);
		Scenario2.C c2 = (Scenario2.C) createConfig(c2Builder);

		doReadWrite("a", aDescr, c2);
		doReadWrite("b", bDescr, c2);
		doReadWrite("c", cDescr, c2);
	}

	public void testCDATAInStringValue() throws Exception {
		TestCDATAInAttributeValue item = create(TestCDATAInAttributeValue.class);
		String content = "<root><!CDATA[some fancy content]]></root>";
		item.setValue(content);

		TestCDATAInAttributeValue result = (TestCDATAInAttributeValue) doReadWrite("config", item.descriptor(), item);
		assertEquals(content, result.getValue());
	}

	private ConfigurationItem createConfig(ConfigBuilder a2Builder) {
		return a2Builder.createConfig(context);
	}

	public void testItemWithFormatAnnotation() throws Exception {
		TestItemPropertyWithFormat tmp = create(TestItemPropertyWithFormat.class);
		tmp.setItemWithFormat(create(TestItemWithFormat.class));

		doReadWrite("local", tmp.descriptor(), tmp);
		tmp.getItemWithFormat().setInt(15);
		doReadWrite("local", tmp.descriptor(), tmp);
		tmp.getItemWithFormat().setBoolean(true);
		doReadWrite("local", tmp.descriptor(), tmp);
	}

	/**
	 * Serializes the given {@link ConfigurationItem} to XML, deserializes that
	 * XML and checks that the deserialized {@link ConfigurationItem} is equal
	 * to the given one.
	 * 
	 * @return the deserialized {@link ConfigurationItem}
	 */
	protected ConfigurationItem doReadWrite(String localName, ConfigurationDescriptor staticType,
			ConfigurationItem item) throws Exception {
		String serialized = writeConfigurationItem(localName, staticType, item);
		StringWriter stringWriter = new StringWriter();
		new ConfigurationWriter(stringWriter).write(localName, staticType, item);
		new ConfigurationReader(context, Collections.singletonMap(localName, staticType))
			.setSource(CharacterContents.newContent(stringWriter.toString()))
			.read();

		CharacterContent content = CharacterContents.newContent(serialized);
		ConfigurationItem copy = readConfigItem(localName, staticType, content);

		assertEquals(item, copy);
		return copy;
	}

	protected abstract String writeConfigurationItem(String localName, ConfigurationDescriptor staticType,
			ConfigurationItem item) throws Exception;

	protected abstract ConfigurationItem readConfigItem(String localName, ConfigurationDescriptor expectedType,
			CharacterContent content) throws ConfigurationException;

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return GLOBAL_CONFIGS;
	}
	
}
