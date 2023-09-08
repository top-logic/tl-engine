/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.filtergen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.util.ElementTestSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.EmptyGenerator;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory.Config;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory.Config.GeneratorConfig;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory.Config.GeneratorSpiConfig;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorSpi;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;
import com.top_logic.layout.form.model.utility.OptionModel;

/**
 * {@link TestGeneratorFactory} tests the {@link GeneratorFactory}
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestGeneratorFactory extends BasicTestCase {
	
	private static final InstantiationContext FAIL_IMMEDIATELY_CONTEXT =
		SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;

	private static class TestingGeneratorFactory extends GeneratorFactory {
		
		/**
		 * Creates a new {@link TestGeneratorFactory.TestingGeneratorFactory} from the given
		 * configuration.
		 * 
		 * @param context
		 *        {@link InstantiationContext} to instantiate sub configurations.
		 * @param config
		 *        Configuration for this {@link TestGeneratorFactory.TestingGeneratorFactory}.
		 * 
		 * @throws ConfigurationException
		 *         iff configuration is invalid.
		 */
		public TestingGeneratorFactory(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}

		@Override
		protected String internalGetGeneratorName(Generator generator) {
			return super.internalGetGeneratorName(generator);
		}
		
		@Override
		protected Generator internalGetGenerator(String generatorName) throws ConfigurationException {
			return super.internalGetGenerator(generatorName);
		}
	}
	
	public static class Spi1 implements GeneratorSpi {

		@Override
		public Generator bind(final String... config) {
			return new ListGeneratorAdaptor() {
				
				@Override
				public List<?> generateList(EditContext editContext) {
					return Arrays.asList(config);
				}
			};
		}
		
	}

	public void testGeneratorRegistration() throws ConfigurationException {
		String key = "gen1";
		Class<? extends Generator> generatorClass = EmptyGenerator.class;
		Map<String, Class<? extends Generator>> generators =
			Collections.<String, Class<? extends Generator>> singletonMap(key, generatorClass);
		GeneratorFactory.Config config = newConfig(generators, Collections.<String, Class<? extends GeneratorSpi>>emptyMap());
		TestingGeneratorFactory factory = new TestingGeneratorFactory(FAIL_IMMEDIATELY_CONTEXT, config);
		Generator generator = factory.internalGetGenerator(key);
		assertNotNull(generator);
		assertInstanceof(generator, generatorClass);
	}

	private GeneratorFactory.Config newConfig(Map<String, Class<? extends Generator>> generators,
			Map<String, Class<? extends GeneratorSpi>> spis) {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(GeneratorFactory.Config.class);
		initSpis(builder, spis);
		initGenerators(builder, generators);
		return (Config) builder.createConfig(FAIL_IMMEDIATELY_CONTEXT);
	}

	private void initSpis(ConfigBuilder builder, Map<String, Class<? extends GeneratorSpi>> spis) {
		Map<String, GeneratorSpiConfig> spiConfigs = MapUtil.newMap(spis.size());
		for (Entry<String, Class<? extends GeneratorSpi>> spi : spis.entrySet()) {
			GeneratorSpiConfig conf = createGeneratorSpiConfig(spi.getKey(), spi.getValue());
			spiConfigs.put(conf.getName(), conf);
		}
		builder.initValue(builder.descriptor().getProperty(GeneratorFactory.Config.GENERATOR_SPIS), spiConfigs);
	}
	
	private void initGenerators(ConfigBuilder builder, Map<String, Class<? extends Generator>> generators) {
		Map<String, GeneratorConfig> generatorConfigs = MapUtil.newMap(generators.size());
		for (Entry<String, Class<? extends Generator>> generator : generators.entrySet()) {
			GeneratorConfig conf = createGeneratorConfig(generator.getKey(), generator.getValue());
			generatorConfigs.put(conf.getName(), conf);
		}
		builder.initValue(builder.descriptor().getProperty(GeneratorFactory.Config.GENERATORS), generatorConfigs);
	}

	private GeneratorSpiConfig createGeneratorSpiConfig(String name, Class<? extends GeneratorSpi> spi) {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(GeneratorSpiConfig.class);
		builder.initValue(builder.descriptor().getProperty(GeneratorSpiConfig.NAME_ATTRIBUTE), name);
		builder.initValue(builder.descriptor().getProperty(GeneratorSpiConfig.IMPL), newPolymorphicConfig(spi));
		return (GeneratorSpiConfig) builder.createConfig(FAIL_IMMEDIATELY_CONTEXT);
	}

	private GeneratorConfig createGeneratorConfig(String name, Class<? extends Generator> spi) {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(GeneratorConfig.class);
		builder.initValue(builder.descriptor().getProperty(GeneratorConfig.NAME_ATTRIBUTE), name);
		builder.initValue(builder.descriptor().getProperty(GeneratorConfig.IMPL), newPolymorphicConfig(spi));
		return (GeneratorConfig) builder.createConfig(FAIL_IMMEDIATELY_CONTEXT);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> PolymorphicConfiguration<T> newPolymorphicConfig(Class<T> implClass) {
		PolymorphicConfiguration result = TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		result.setImplementationClass(implClass);
		return result;
	}

	public void testSpiRegistration() throws ConfigurationException {
		String key = "spi";
		Map<String, Class<? extends GeneratorSpi>> spis =
			Collections.<String, Class<? extends GeneratorSpi>> singletonMap(key, Spi1.class);
		Config config = newConfig(Collections.<String, Class<? extends Generator>> emptyMap(), spis);
		TestingGeneratorFactory generatorFactory =
			new TestingGeneratorFactory(FAIL_IMMEDIATELY_CONTEXT, config);
		try {
			generatorFactory.internalGetGenerator(key);
			fail("No such generator:" + key);
		} catch (Exception ex) {
			// expected
		}
		
		Generator generator = generatorFactory.internalGetGenerator("spi(a,b,c)");
		assertNotNull(generator);
		OptionModel<?> options = generator.generate(null);
		assertEquals(list("a", "b", "c"), CollectionUtil.toList(options.iterator()));
		String generatorName = generatorFactory.internalGetGeneratorName(generator);
		assertEquals("spi(a,b,c)", generatorName);
	}
	
	public static Test suite() {
		return ElementTestSetup.createElementTestSetup(TestGeneratorFactory.class);
	}
}

