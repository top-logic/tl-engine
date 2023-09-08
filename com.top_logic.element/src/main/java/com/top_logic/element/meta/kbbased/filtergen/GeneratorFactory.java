/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory.Config.GeneratorConfig;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory.Config.GeneratorSpiConfig;

/**
 * Get the Generator for a config entry
 * 
 * @author <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class GeneratorFactory extends ConfiguredManagedClass<GeneratorFactory.Config> {

	/**
	 * Configuration of the {@link GeneratorFactory}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<GeneratorFactory> {

		/**
		 * @see #getGeneratorSpis()
		 */
		String GENERATOR_SPIS = "generator-spis";

		/**
		 * @see #getGenerators()
		 */
		String GENERATORS = "generators";

		/**
		 * Configuration of all {@link GeneratorSpi}s known by the factory.
		 */
		@Name(GENERATOR_SPIS)
		@Key(GeneratorSpiConfig.NAME_ATTRIBUTE)
		Map<String, GeneratorSpiConfig> getGeneratorSpis();

		/**
		 * Configuration of all {@link Generator}s known by the factory.
		 */
		@Name(GENERATORS)
		@Key(GeneratorConfig.NAME_ATTRIBUTE)
		Map<String, GeneratorConfig> getGenerators();

		/**
		 * Configuration of an {@link GeneratorSpi} within a {@link GeneratorFactory}. Each
		 * {@link GeneratorSpi} is known under a unique name.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface GeneratorSpiConfig extends NamedConfigMandatory {

			/**
			 * @see #getImpl()
			 */
			String IMPL = "impl";

			/**
			 * Configuration of the {@link GeneratorSpi}.
			 */
			@Mandatory
			@Name(IMPL)
			@DefaultContainer
			PolymorphicConfiguration<GeneratorSpi> getImpl();

		}

		/**
		 * Configuration of an {@link Generator} within a {@link GeneratorFactory}.Each
		 * {@link Generator} is known under a unique name.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface GeneratorConfig extends NamedConfigMandatory {

			/**
			 * @see #getImpl()
			 */
			String IMPL = "impl";

			/**
			 * Configuration of the {@link Generator}.
			 */
			@Mandatory
			@Name(IMPL)
			@DefaultContainer
			PolymorphicConfiguration<Generator> getImpl();

		}

	}

	private final Map<String, GeneratorSpi> generatorSPIByName = new HashMap<>();
	private final ConcurrentMap<String, Generator> generatorsByName = new ConcurrentHashMap<>();
	private final ConcurrentMap<Generator, String> nameByGenerator = new ConcurrentHashMap<>();

	/**
	 * Creates a new {@link GeneratorFactory} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link GeneratorFactory}
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid
	 */
	public GeneratorFactory(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		for (GeneratorSpiConfig spi : config.getGeneratorSpis().values()) {
			generatorSPIByName.put(spi.getName(), context.getInstance(spi.getImpl()));
		}
	}

	/**
	 * Names of all registered {@link Generator}s.
	 */
	public Collection<String> getGeneratorNames() {
		return Collections.unmodifiableCollection(
			CollectionUtil.union2(getConfig().getGenerators().keySet(), getConfig().getGeneratorSpis().keySet()));
	}

	/**
	 * Get the Generator for a configuration entry
	 * 
	 * @param generatorName
	 *        the configuration entry
	 * @return the Generator or <code>null</code> if none is configured or if
	 *         creation fails
	 */
	public static Generator getGenerator(String generatorName) throws ConfigurationException {
		return getInstance().internalGetGenerator(generatorName);
	}

	protected Generator internalGetGenerator(String generatorName) throws ConfigurationException {
		Generator result = generatorsByName.get(generatorName);
		if (result == null) {
			result = addNewGenerator(generatorName);
		}
		return result;
	}

	private Generator addNewGenerator(String generatorName) throws ConfigurationException {
		Generator newGenerator = createGenerator(generatorName);
		Generator knownGenerator = generatorsByName.putIfAbsent(generatorName, newGenerator);
		if (knownGenerator == null) {
			String clash = nameByGenerator.put(newGenerator, generatorName);
			assert clash == null;
			return newGenerator;
		} else {
			return knownGenerator;
		}
	}

	private Generator createGenerator(String generatorName) throws ConfigurationException {
		int startSeparator = generatorName.indexOf('(');
		if (startSeparator == -1) {
			return createSimpleGenerator(generatorName);
		}
		String generatorSpiName = generatorName.substring(0, startSeparator);
		GeneratorSpi generatorSpi = generatorSPIByName.get(generatorSpiName);
		if (generatorSpi == null) {
			throw new ConfigurationException("No " + GeneratorSpi.class.getName() + " with name '" + generatorSpiName + "' configured");
		}
		int endSeparator = generatorName.indexOf(')', startSeparator);
		if (endSeparator == -1) {
			throw new IllegalArgumentException("GeneratorSpi '" + generatorName + "' must be of the form 'spiName(arg1,arg2,...,argN)'");
		}
		String[] configuration = generatorName.substring(startSeparator + 1, endSeparator).split(",");
		return generatorSpi.bind(configuration);
	}

	private Generator createSimpleGenerator(String generatorName) throws ConfigurationException {
		GeneratorConfig generatorConfig = getConfig().getGenerators().get(generatorName);
		if (generatorConfig == null) {
			throw new ConfigurationException("No Generator configured for " + generatorName);
		}

		// Note: Generators must be instantiated lazily, since they may depend on the application
		// model, which is not available, when the GeneratorFactory starts.
		Generator generator =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(generatorConfig.getImpl());
		if (generator != null) {
			return generator;
		}
		throw new ConfigurationException("Generator could not be instantiated: " + generatorConfig);
	}

	/**
	 * The configuration name of a {@link Generator} retrieved from
	 * {@link #getGenerator(String)}.
	 */
	public static String getGeneratorName(Generator generator) {
		return getInstance().internalGetGeneratorName(generator);
	}
	
	protected String internalGetGeneratorName(Generator generator) {
		String result = nameByGenerator.get(generator);
		if (result == null) {
			throw new IllegalArgumentException("Unknown Generator '" + generator + "'.");
		}
		return result;
	}
	
	private static GeneratorFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link BasicRuntimeModule} for {@link GeneratorFactory}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Module extends TypedRuntimeModule<GeneratorFactory> {

		/**
		 * Singleton {@link GeneratorFactory.Module} instance.
		 */
		public static final GeneratorFactory.Module INSTANCE = new GeneratorFactory.Module();
		
		@Override
		public Class<GeneratorFactory> getImplementation() {
			return GeneratorFactory.class;
		}
		
	}
}