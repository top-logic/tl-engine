/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.SharedInstance;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Test case for using (configured) instances as part of a configuration.
 * 
 * @see ConfiguredInstance
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestConfiguredInstance extends AbstractTypedConfigurationTestCase {

	public interface Config extends ConfigurationItem {
		
		String CONFIGURED_INSTANCE_EXT_LIST = "configured-instance-ext-list";

		String CONFIGURED_INSTANCE_EXT_MAP = "configured-instance-ext-map";

		@ImplementationClassDefault(A1.class)
		A.AConfig getAConfig();

		@ImplementationClassDefault(A1.class)
		A getA();
		
		@ImplementationClassDefault(B1.class)
		PolymorphicConfiguration<B> getBConfig();

		@InstanceFormat
		@InstanceDefault(B1.class)
		@ImplementationClassDefault(B3.class)
		B getB();

		@InstanceFormat
		@Format(BFormat.class)
		B getBShortcut();

		@ImplementationClassDefault(A1.class)
		@Key(A.AConfig.X_PROPERTY)
		List<A.AConfig> getAConfigList();

		@ImplementationClassDefault(A1.class)
		@Key(A.AConfig.X_PROPERTY)
		List<A> getAList();

		@ImplementationClassDefault(B1.class)
		@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
		List<PolymorphicConfiguration<B>> getBConfigList();

		@InstanceFormat
		@ImplementationClassDefault(B1.class)
		@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
		List<B> getBList();

		@ImplementationClassDefault(A1.class)
		@Key(A.AConfig.X_PROPERTY)
		Map<Integer, A.AConfig> getAConfigMap();

		@ImplementationClassDefault(A1.class)
		@Key(A.AConfig.X_PROPERTY)
		Map<Integer, A> getAMap();

		@ImplementationClassDefault(B1.class)
		@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
		Map<Class<?>, PolymorphicConfiguration<B>> getBConfigMap();

		@InstanceFormat
		@ImplementationClassDefault(B1.class)
		@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
		Map<Class<?>, B> getBMap();

		/**
		 * Check that it is possible that an implementation class does not has any useful
		 * constructor if it is abstract. It must just be possible to instantiate concrete subtypes.
		 */
		@InstanceFormat
		AbstractClass getAbstractClass();

		@InstanceFormat
		B getOtherB();

		@Name(CONFIGURED_INSTANCE_EXT_LIST)
		@EntryTag("entry")
		List<ConfiguredInstanceExt> getConfiguredInstanceExtList();

		@Key(A.AConfig.X_PROPERTY)
		@Name(CONFIGURED_INSTANCE_EXT_MAP)
		@EntryTag("entry")
		Map<String, ConfiguredInstanceExt> getConfiguredInstanceExtMap();

		@Key(NamedPolymorphicConfiguration.NAME_ATTRIBUTE)
		@Subtypes({})
		Map<String, NamedPolymorphicConfiguration<?>> getMapOfPolymorphicConfigs();

	}

	public interface SubConfig extends Config {
		@Override
		public AbstractB getOtherB();
	}

	public abstract class AbstractClass {
		// just an abstract super class

		protected AbstractClass() {
			// no public constructor
		}
	}
	
	/**
	 * Configurable abstract base class.
	 */
	public abstract static class A implements ConfiguredInstance<A.AConfig> {
		private final AConfig _config;

		public interface AConfig extends PolymorphicConfiguration<A> {
			String X_PROPERTY = "x";

			@Name(X_PROPERTY)
			String getX();
		}
		
		/**
		 * Creates a {@link TestConfiguredInstance.A} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public A(InstantiationContext context, AConfig config) {
			_config = config;
		}

		@Override
		public AConfig getConfig() {
			return _config;
		}

		public abstract int getZ();

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_config == null) ? 0 :
				ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(_config));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			A other = (A) obj;
			if (_config == null) {
				if (other._config != null)
					return false;
			} else if (!ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(_config, other._config))
				return false;
			return true;
		}
	}
	
	/**
	 * Interface extending {@link ConfiguredInstance}.
	 * 
	 * @see "Ticket #24162"
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ConfiguredInstanceExt extends ConfiguredInstance<A.AConfig> {
		// Test interface
	}

	/**
	 * Subclass of {@link A} without additional configuration options.
	 */
	@SharedInstance
	public static class A1 extends A {

		public A1(InstantiationContext context, AConfig config) {
			super(context, config);
		}

		@Override
		public int getZ() {
			return 13;
		}
	}

	/**
	 * Subclass of {@link A} with additional configuration options.
	 */
	@SharedInstance
	public static class A2 extends A {

		public interface A1Config extends AConfig {
			int getZ();
		}

		public A2(InstantiationContext context, A1Config config) {
			super(context, config);
		}

		public A1Config config() {
			return (A1Config) super.getConfig();
		}

		@Override
		public int getZ() {
			return config().getZ();
		}

	}

	/**
	 * Instance interface that has configured and non-configured implementations.
	 */
	public interface B {
		public abstract int getZ();
	}

	public static class BFormat extends AbstractConfigurationValueProvider<B> {

		/**
		 * Singleton {@link TestConfiguredInstance.BFormat} instance.
		 */
		public static final BFormat INSTANCE = new BFormat();

		private BFormat() {
			super(B.class);
		}

		@Override
		protected B getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
			if ("B1".equals(propertyValue.toString())) {
				return new B1();
			}
			if ("B2".equals(propertyValue.toString())) {
				return B2.INSTANCE;
			}
			throw new ConfigurationException("Invalid configuration value: " + propertyValue);
		}

		@Override
		protected String getSpecificationNonNull(B configValue) {
			if (configValue instanceof B1) {
				return "B1";
			}
			if (configValue instanceof B2) {
				return "B2";
			}
			throw new UnsupportedOperationException();
		}

	}

	public abstract static class AbstractB implements B {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getZ();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AbstractB other = (AbstractB) obj;
			if (getZ() != other.getZ())
				return false;
			return true;
		}

	}

	/**
	 * Implementation of {@link B} without configuration options.
	 */
	@SharedInstance
	public static class B1 extends AbstractB {

		public B1() {
			super();
		}

		@Override
		public int getZ() {
			return 13;
		}
	}

	/**
	 * Singleton implementation of {@link B}.
	 */
	public static class B2 extends AbstractB {
		/**
		 * Singleton {@link TestConfiguredInstance.B2} instance.
		 */
		public static final TestConfiguredInstance.B2 INSTANCE = new TestConfiguredInstance.B2();

		private B2() {
			// Singleton constructor.
		}

		@Override
		public int getZ() {
			return 13;
		}
	}

	@SharedInstance
	public static class B3 extends AbstractB implements ConfiguredInstance<PolymorphicConfiguration<?>> {

		private final B3Config _config;

		public interface B3Config extends PolymorphicConfiguration<B> {
			int getZ();

			long getY();
		}

		/**
		 * Creates a {@link TestConfiguredInstance.B3} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public B3(InstantiationContext context, B3Config config) {
			_config = config;
		}

		@Override
		public int getZ() {
			return _config.getZ();
		}

		public long getMYFunnyValue() {
			return _config.getY();
		}

		@Override
		public PolymorphicConfiguration<?> getConfig() {
			return _config;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((_config == null) ? 0 :
				ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(_config));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			B3 other = (B3) obj;
			if (_config == null) {
				if (other._config != null)
					return false;
			} else if (!ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(_config, other._config))
				return false;
			return true;
		}

	}
	
	public void testInstanceList() throws Throwable {
		doTestReadWrite("testInstanceList.xml");
	}

	public void testDefaultClass() throws Throwable {
		doTestReadWrite("testDefaultClass.xml");
	}

	public void testConfiguredClass() throws Throwable {
		doTestReadWrite("testConfiguredClass.xml");
	}

	public void testClassAttribute() throws Throwable {
		doTestReadWrite("testClassAttribute.xml");
	}

	public void testSpecializedInstanceFormat() throws Throwable {
		doTestReadWrite("testSpecializedInstanceFormat.xml");
	}

	public void testInstanceShortcutFormat() throws Throwable {
		Config item1 = read("<config b-shortcut='B1'/>");
		assertTrue(item1.getBShortcut() instanceof B1);

		Config item2 = read("<config b-shortcut='B2'/>");
		assertTrue(item2.getBShortcut() instanceof B2);
	}

	public void testConfigurationItemInline() throws Throwable {
		Config config = (Config) readConfiguration("testConfigItemInline.xml");
		PolymorphicConfiguration<B> bConfig = config.getBConfig();
		assertEquals(B1.class, bConfig.getImplementationClass());
	}

	public interface ScenarioTypeConfiguredInstanceNull extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		A getExample();

	}

	public void testConfiguredInstanceNull() {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(ScenarioTypeConfiguredInstanceNull.class);
		setValue(builder, ScenarioTypeConfiguredInstanceNull.PROPERTY_NAME_EXAMPLE, null);
		builder.createConfig(context);
	}

	public void testImplementationClassNull() {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(PolymorphicConfiguration.class);
		setValue(builder, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, null);
		builder.createConfig(context);
	}

	public interface ScenarioTypeExampleInterface {
		// Nothing needed.
	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeImplClassDefaultIsInterface extends ConfigurationItem {

		// The implementation class default is an interface but not a concrete class.
		@ImplementationClassDefault(ScenarioTypeExampleInterface.class)
		ConfiguredInstance<?> getExample();

	}

	public void testImplementationClassDefaultIsInterface() {
		String message = "Interface in @ImplementationClassDefault";
		String errorPart = "Abstract type cannot be used as implementation class";
		assertIllegal(message, errorPart, ScenarioTypeImplClassDefaultIsInterface.class);
	}

	public interface ScenarioTypePolymorphicConfigurationA extends
			PolymorphicConfiguration<ScenarioTypeConfiguredInstanceA> {
		// Nothing needed
	}

	public interface ScenarioTypePolymorphicConfigurationA2 extends ScenarioTypePolymorphicConfigurationA {
		// Nothing needed
	}

	public static class ScenarioTypeConfiguredInstanceA implements
			ConfiguredInstance<ScenarioTypePolymorphicConfigurationA> {

		private ScenarioTypePolymorphicConfigurationA _config;

		@SuppressWarnings("unused")
		public ScenarioTypeConfiguredInstanceA(InstantiationContext context,
				ScenarioTypePolymorphicConfigurationA config) {
			_config = config;
		}

		@Override
		public ScenarioTypePolymorphicConfigurationA getConfig() {
			return _config;
		}

	}

	public interface ScenarioTypeRootA extends ConfigurationItem {

		ScenarioTypeConfiguredInstanceA getExample();

	}

	@SuppressWarnings("unchecked")
	public void testConfiguredInstanceAndSpecialConfigViaBaseXml() throws ConfigurationException {
		String base = XML_DECLARATION
			+ "<ScenarioTypeRootA " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <example config:interface='" + ScenarioTypePolymorphicConfigurationA2.class.getName() + "' />"
			+ "</ScenarioTypeRootA>";
		String increment = XML_DECLARATION
			+ "<ScenarioTypeRootA>"
			+ "  <example />"
			+ "</ScenarioTypeRootA>";
		ScenarioTypeRootA item = read(createDescriptorMap(ScenarioTypeRootA.class), base, increment);
		Class<?> configInterface = item.getExample().getConfig().getConfigurationInterface();
		assertEquals(ScenarioTypePolymorphicConfigurationA2.class, configInterface);
	}

	public interface ScenarioTypePolymorphicConfigurationB extends
			PolymorphicConfiguration<ScenarioTypeConfiguredInstanceB> {
		// Nothing needed
	}

	public static class ScenarioTypeConfiguredInstanceB implements
			ConfiguredInstance<ScenarioTypePolymorphicConfigurationB> {

		private ScenarioTypePolymorphicConfigurationB _config;

		@SuppressWarnings("unused")
		public ScenarioTypeConfiguredInstanceB(InstantiationContext context,
				ScenarioTypePolymorphicConfigurationB config) {
			_config = config;
		}

		@Override
		public ScenarioTypePolymorphicConfigurationB getConfig() {
			return _config;
		}

	}

	public static class ScenarioTypeConfiguredInstanceB2 extends ScenarioTypeConfiguredInstanceB {

		public ScenarioTypeConfiguredInstanceB2(InstantiationContext context,
				ScenarioTypePolymorphicConfigurationB config) {
			super(context, config);
		}

	}

	public interface ScenarioTypeRootB extends ConfigurationItem {

		@InstanceDefault(ScenarioTypeConfiguredInstanceB2.class)
		ScenarioTypeConfiguredInstanceB getExample();

	}

	@SuppressWarnings("unchecked")
	public void testInstanceDefaultOnConfiguredInstance() throws ConfigurationException {
		String xml = XML_DECLARATION
			+ "<ScenarioTypeRootB " + XML_CONFIG_NAMESPACE_DECLARATION + " />";
		ScenarioTypeRootB item = read(createDescriptorMap(ScenarioTypeRootB.class), xml);
		assertTrue(item.getExample() instanceof ScenarioTypeConfiguredInstanceB2);
	}

	public interface ScenarioTypePolymorphicConfigurationC1 extends
			PolymorphicConfiguration<ScenarioTypeConfiguredInstanceC> {

		String getKey();

	}

	public interface ScenarioTypePolymorphicConfigurationC2 extends
			ScenarioTypePolymorphicConfigurationC1 {
		// Nothing needed
	}

	public static class ScenarioTypeConfiguredInstanceC implements
			ConfiguredInstance<ScenarioTypePolymorphicConfigurationC1> {

		private ScenarioTypePolymorphicConfigurationC1 _config;

		@SuppressWarnings("unused")
		public ScenarioTypeConfiguredInstanceC(InstantiationContext context,
				ScenarioTypePolymorphicConfigurationC1 config) {
			_config = config;
		}

		@Override
		public ScenarioTypePolymorphicConfigurationC1 getConfig() {
			return _config;
		}

	}

	public interface ScenarioTypeRootC extends ConfigurationItem {

		@Key("key")
		@Subtypes(@Subtype(tag = "special", type = ScenarioTypePolymorphicConfigurationC2.class))
		Map<String, ScenarioTypeConfiguredInstanceC> getExample();

	}

	@SuppressWarnings("unchecked")
	public void testConfiguredInstanceAndSpecialConfigViaTag() throws ConfigurationException {
		String xml = XML_DECLARATION
			+ "<ScenarioTypeRootC " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
			+ "  <example>"
			+ "    <entry key='a' />"
			+ "    <special key='b' />"
			+ "  </example>"
			+ "</ScenarioTypeRootC>";
		ScenarioTypeRootC item = read(createDescriptorMap(ScenarioTypeRootC.class), xml);
		assertFalse(item.getExample().get("a").getConfig() instanceof ScenarioTypePolymorphicConfigurationC2);
		assertTrue(item.getExample().get("b").getConfig() instanceof ScenarioTypePolymorphicConfigurationC2);
	}

	public void testElementDescriptorOfConfiguredInstanceExtension() {
		ConfigurationDescriptor configDescriptor = TypedConfiguration.getConfigurationDescriptor(Config.class);
		PropertyDescriptor listProperty = configDescriptor.getProperty(Config.CONFIGURED_INSTANCE_EXT_LIST);
		assertEquals("element type is a " + ConfiguredInstance.class.getSimpleName() + " of " + A.AConfig.class,
			TypedConfiguration.getConfigurationDescriptor(A.AConfig.class),
			listProperty.getElementDescriptor("entry"));
		PropertyDescriptor mapProperty = configDescriptor.getProperty(Config.CONFIGURED_INSTANCE_EXT_MAP);
		assertEquals("element type is a " + ConfiguredInstance.class.getSimpleName() + " of " + A.AConfig.class,
			TypedConfiguration.getConfigurationDescriptor(A.AConfig.class),
			mapProperty.getElementDescriptor("entry"));
		assertEquals(TypedConfiguration.getConfigurationDescriptor(A.AConfig.class).getProperty(A.AConfig.X_PROPERTY),
			mapProperty.getKeyProperty());
	}

	public static class ScenarioTypeNamedConfiguredInstance
			extends AbstractConfiguredInstance<NamedPolymorphicConfiguration<ScenarioTypeNamedConfiguredInstance>> {

		public ScenarioTypeNamedConfiguredInstance(InstantiationContext context,
				NamedPolymorphicConfiguration<ScenarioTypeNamedConfiguredInstance> config) {
			super(context, config);
		}

	}

	public void testPolymorphicConfigMapOrder() {
		Config config = newConfigItem(Config.class);
		/** Count between 10 and 99 to get only two digit numbers. */
		for (int i = 10; i < 100; i++) {
			createEntry(config, "Entry " + i);
		}
		Map<String, Object> instances = getInstanceMap(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config.getMapOfPolymorphicConfigs());
		List<String> originalList = list(instances.keySet());
		List<String> sortedList = list(originalList);
		sortedList.sort(Comparator.naturalOrder());
		assertTrue("Order was lost.", sortedList.equals(originalList));
	}

	private void createEntry(Config enclosingConfig, String name) {
		@SuppressWarnings("unchecked")
		NamedPolymorphicConfiguration<ScenarioTypeNamedConfiguredInstance> newConfig =
			newConfigItem(NamedPolymorphicConfiguration.class);
		newConfig.setName(name);
		newConfig.setImplementationClass(ScenarioTypeNamedConfiguredInstance.class);
		enclosingConfig.getMapOfPolymorphicConfigs().put(name, newConfig);
	}

	@Override
	protected AssertProtocol createProtocol() {
		return new AssertProtocol("Ticket #5785");
	}

	private void doTestReadWrite(String testFixture) throws Throwable, XMLStreamException {
		ConfigurationItem config = readConfiguration(testFixture);
		ConfigurationItem newConfig = throughXML(config);
		assertEquals(config, newConfig);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestConfiguredInstance.class);
	}
}
