/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.autoconf;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.gui.ThemeUtil;

/**
 * The service configurations an application stores in the autoconf folder of its top level module.
 *
 * <p>
 * Each service keeps its configuration in a file of its own, named after the service class. The
 * content of such a file is an application configuration holding a single service entry. An entry
 * written as an override replaces the whole service configuration of the underlying configuration
 * layers, an entry written without the override marker is layered onto them: a keyed list is then
 * merged by the key of its entries.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class InAppServiceConfigStore {

	/** Root tag of an application configuration. */
	public static final String APPLICATION_TAG = "application";

	/** Suffix of the file holding the configuration of a single service. */
	public static final String FILE_SUFFIX = ".config.xml";

	/**
	 * The configuration types marked with the override attribute when a service entry replaces the
	 * configuration of the underlying layers.
	 */
	public static final Set<Class<?>> OVERRIDE_SERVICE = Collections.singleton(ModuleConfiguration.class);

	/** No configuration type is marked with the override attribute. */
	public static final Set<Class<?>> LAYER_ONTO_BASE = Collections.emptySet();

	private InAppServiceConfigStore() {
		// Only static operations.
	}

	/**
	 * The file holding the stored configuration of the service of the given module.
	 *
	 * @param module
	 *        The module of the service.
	 * @return The file, existing or not.
	 */
	public static File fileFor(BasicRuntimeModule<?> module) {
		return new File(XMLProperties.Setting.resolveAutoconfFolder(), fileName(module));
	}

	/**
	 * The name of the file holding the stored configuration of the service of the given module.
	 *
	 * @param module
	 *        The module of the service.
	 * @return The file name, see {@link #FILE_SUFFIX}.
	 */
	public static String fileName(BasicRuntimeModule<?> module) {
		return module.getImplementation().getName() + FILE_SUFFIX;
	}

	/**
	 * Reads the application configuration from the given file.
	 *
	 * @param file
	 *        The file to read.
	 * @return The stored configuration, an empty one when the file does not exist.
	 * @throws ConfigurationException
	 *         When the file cannot be parsed.
	 */
	public static ApplicationConfig.Config read(File file) throws ConfigurationException {
		if (!file.exists()) {
			return TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
		}
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(APPLICATION_TAG,
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(BinaryDataFactory.createBinaryData(file));
		return (ApplicationConfig.Config) reader.read();
	}

	/**
	 * Serializes the given configuration.
	 *
	 * @param rootTag
	 *        The tag name of the root element.
	 * @param config
	 *        The configuration to serialize.
	 * @param overrideTypes
	 *        The configuration types to mark with the override attribute, see
	 *        {@link #OVERRIDE_SERVICE} and {@link #LAYER_ONTO_BASE}.
	 * @return The pretty printed XML.
	 */
	public static String toXml(String rootTag, ConfigurationItem config, Set<Class<?>> overrideTypes) {
		String raw;
		try (StringWriter buffer = new StringWriter()) {
			try (OverrideConfigurationWriter writer = new OverrideConfigurationWriter(buffer, overrideTypes)) {
				TypedConfiguration.serialize(rootTag, config, writer);
			}
			raw = buffer.toString();
		} catch (IOException | XMLStreamException ex) {
			throw new RuntimeException("Cannot serialize configuration.", ex);
		}
		return TypedConfiguration.prettyPrint(raw, printerConfig());
	}

	/**
	 * Writes the given application configuration to the given file, creating the folder of the file
	 * when it does not exist.
	 *
	 * @param file
	 *        The file to write.
	 * @param config
	 *        The configuration to store.
	 * @param overrideTypes
	 *        The configuration types to mark with the override attribute, see
	 *        {@link #OVERRIDE_SERVICE} and {@link #LAYER_ONTO_BASE}.
	 * @throws IOException
	 *         When the file cannot be written.
	 */
	public static void write(File file, ApplicationConfig.Config config, Set<Class<?>> overrideTypes)
			throws IOException {
		FileUtilities.enforceDirectory(file.getParentFile());
		FileUtilities.writeStringToFile(toXml(APPLICATION_TAG, config, overrideTypes), file, StringServices.UTF8);
	}

	/**
	 * The configuration types to mark with the override attribute for the given mode.
	 *
	 * @param override
	 *        Whether the stored entry replaces the configuration of the underlying layers.
	 * @return {@link #OVERRIDE_SERVICE}, or {@link #LAYER_ONTO_BASE} when the entry is layered onto
	 *         the underlying configuration.
	 */
	public static Set<Class<?>> overrideTypes(boolean override) {
		return override ? OVERRIDE_SERVICE : LAYER_ONTO_BASE;
	}

	/**
	 * Creates the service entry of an application configuration.
	 *
	 * @param serviceClass
	 *        The service the entry configures.
	 * @param instance
	 *        The configuration of the service.
	 * @return The entry, not yet part of an application configuration.
	 *
	 * @see ApplicationConfig.Config#getServices()
	 */
	public static ModuleConfiguration newServiceEntry(Class<? extends ManagedClass> serviceClass,
			ServiceConfiguration<?> instance) {
		ModuleConfiguration result = TypedConfiguration.newConfigItem(ModuleConfiguration.class);
		result.setServiceClass(serviceClass);
		result.setInstance(instance);
		return result;
	}

	/**
	 * Stores the given configuration of the service of the given module, dropping a configuration
	 * stored before.
	 *
	 * @param module
	 *        The module of the service.
	 * @param instance
	 *        The configuration to store.
	 * @param override
	 *        Whether the stored configuration replaces the configuration of the underlying layers
	 *        instead of being layered onto them.
	 * @return The written file.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When a configuration stored before cannot be parsed.
	 */
	public static File store(BasicRuntimeModule<?> module, ServiceConfiguration<?> instance, boolean override)
			throws IOException, ConfigurationException {
		File file = fileFor(module);
		Class<? extends ManagedClass> serviceClass = module.getImplementation();
		ApplicationConfig.Config config = read(file);
		config.getServices().put(serviceClass, newServiceEntry(serviceClass, instance));
		write(file, config, overrideTypes(override));
		return file;
	}

	private static XMLPrettyPrinter.Config printerConfig() {
		return ThemeUtil.THEME_PRINTER_CONFIG;
	}

}
