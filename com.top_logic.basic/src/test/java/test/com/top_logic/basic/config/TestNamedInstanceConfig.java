/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedInstanceConfig;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;

/**
 * Test case for {@link NamedInstanceConfig} and {@link NamedPolymorphicConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestNamedInstanceConfig extends AbstractTypedConfigurationTestCase {

	/**
	 * The implementation interface.
	 */
	public interface Foo {
		// Marker only.
	}

	/**
	 * A singleton implementation without configuration.
	 */
	public static class SingletonFoo implements Foo {
		/**
		 * Singleton {@link TestNamedInstanceConfig.SingletonFoo} instance.
		 */
		public static final SingletonFoo INSTANCE = new SingletonFoo();

		private SingletonFoo() {
			// Singleton constructor.
		}
	}

	/**
	 * A {@link ConfiguredInstance} without any further configuration options.
	 */
	public static class ConfiguredFoo implements Foo, ConfiguredInstance<PolymorphicConfiguration<Foo>> {

		private final PolymorphicConfiguration<Foo> _config;

		/**
		 * Creates a {@link TestNamedInstanceConfig.ConfiguredFoo} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public ConfiguredFoo(InstantiationContext context, PolymorphicConfiguration<Foo> config) {
			_config = config;
		}

		@Override
		public PolymorphicConfiguration<Foo> getConfig() {
			return _config;
		}
	}

	/**
	 * A configured instance with custom configuration options.
	 */
	public static class FooWithOnlyOptions extends ConfiguredFoo {

		public static interface Config extends PolymorphicConfiguration<Foo> {
			String getSomeOption();
		}

		/**
		 * Creates a {@link TestNamedInstanceConfig.ConfiguredFoo} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public FooWithOnlyOptions(InstantiationContext context, Config config) {
			super(context, config);
		}

	}

	/**
	 * A configured instance with custom configuration options including
	 * {@link NamedPolymorphicConfiguration} options.
	 */
	public static class FooWithOptionsAndName extends FooWithOnlyOptions {

		public static interface Config extends FooWithOnlyOptions.Config, NamedPolymorphicConfiguration<Foo> {
			// Pure sum interface.
		}

		/**
		 * Creates a {@link TestNamedInstanceConfig.ConfiguredFoo} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public FooWithOptionsAndName(InstantiationContext context, Config config) {
			super(context, config);
		}

	}

	/**
	 * The root-configuration interface for parser testing.
	 */
	public interface Config extends ConfigurationItem {
		@Key(NamedInstanceConfig.NAME_ATTRIBUTE)
		List<NamedInstanceConfig<Foo>> getListInstances();

		@Key(NamedInstanceConfig.NAME_ATTRIBUTE)
		Collection<NamedInstanceConfig<Foo>> getCollectionInstances();

		@Key(NamedInstanceConfig.NAME_ATTRIBUTE)
		Map<String, NamedInstanceConfig<Foo>> getMapInstances();

		@Key(NamedPolymorphicConfiguration.NAME_ATTRIBUTE)
		List<NamedPolymorphicConfiguration<Foo>> getListGenerics();

		@Key(NamedPolymorphicConfiguration.NAME_ATTRIBUTE)
		Collection<NamedPolymorphicConfiguration<Foo>> getCollectionGenerics();

		@Key(NamedPolymorphicConfiguration.NAME_ATTRIBUTE)
		Map<String, NamedPolymorphicConfiguration<Foo>> getMapGenerics();
	}

	public void testListSingletonInstances() throws ConfigurationException {
		Config result =
			read("<config><list-instances><list-instance name='foo'><impl class='" + SingletonFoo.class.getName()
				+ "'/></list-instance></list-instances></config>");
		assertEquals(1, result.getListInstances().size());
		assertEquals(SingletonFoo.class,
			getInstanceListNamed(context, result.getListInstances()).get(0).getClass());
	}

	public void testListConfiguredInstances() throws ConfigurationException {
		Config result =
			read("<config><list-instances><list-instance name='foo'><impl class='" + ConfiguredFoo.class.getName()
				+ "'/></list-instance></list-instances></config>");
		assertEquals(1, result.getListInstances().size());
		assertEquals(ConfiguredFoo.class,
			getInstanceListNamed(context, result.getListInstances()).get(0).getClass());
	}

	public void testListOptionInstances() throws ConfigurationException {
		Config result =
			read("<config><list-instances><list-instance name='foo'><impl class='" + FooWithOnlyOptions.class.getName()
				+ "'/></list-instance></list-instances></config>");
		assertEquals(1, result.getListInstances().size());
		assertEquals(FooWithOnlyOptions.class,
			getInstanceListNamed(context, result.getListInstances()).get(0).getClass());
	}

	public void testListGenericsSingleton() throws ConfigurationException {
		Config result =
			read("<config><list-generics><list-generic name='foo' class='" + SingletonFoo.class.getName()
				+ "'/></list-generics></config>");
		assertEquals(1, result.getListGenerics().size());
		assertEquals(SingletonFoo.class,
			getInstanceList(context, result.getListGenerics()).get(0).getClass());
	}

	public void testListGenericsConfigured() throws ConfigurationException {
		Config result =
			read("<config><list-generics><list-generic name='foo' class='" + ConfiguredFoo.class.getName()
				+ "'/></list-generics></config>");
		assertEquals(1, result.getListGenerics().size());
		assertEquals(ConfiguredFoo.class,
			getInstanceList(context, result.getListGenerics()).get(0).getClass());
	}

	public void testListGenericsWithOnlyOptions() {
		initFailureTest();
		try {
			read("<config><list-generics><list-generic name='foo' class='" + FooWithOnlyOptions.class.getName()
				+ "'/></list-generics></config>");
			fail("Expected failure: Config interface is required to extends NamedPolymorphicConfiguration.");
		} catch (ConfigurationException ex) {
			BufferingProtocol bufferingProtocol = (BufferingProtocol) protocol;
			BasicTestCase.assertContains("has no property 'name'", bufferingProtocol.getError());
			BasicTestCase.assertContains(
				"expected to be of type 'com.top_logic.basic.config.NamedPolymorphicConfiguration'",
				bufferingProtocol.getError());
		}
	}

	public void testListGenericsWithOptionsAndName() throws ConfigurationException {
		Config result =
			read("<config><list-generics><list-generic name='foo' class='" + FooWithOptionsAndName.class.getName()
				+ "'/></list-generics></config>");
		assertEquals(1, result.getListGenerics().size());
		assertEquals(FooWithOptionsAndName.class,
			getInstanceList(context, result.getListGenerics()).get(0).getClass());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestNamedInstanceConfig.class);
	}

}
