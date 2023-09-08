/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ByteDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.ShortDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.NewLineStyle;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;

/**
 * Factory for configuration item objects that implement user-defined configuration interface types.
 * 
 * <p>
 * This factory produces generic Java bean implementations that implement given bean interfaces.
 * </p>
 * 
 * @see <a href="http://tl/trac/wiki/TypedConfiguration">http://tl/trac/wiki/TypedConfiguration</a>
 * @see ConfigurationItem
 * @see TypedConfigUtil
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypedConfiguration {

	private static final String DEFAULT_ROOT_TAG = "config";
	private static final String TO_STRING_ENCODING = "utf-8";
	private static final Config DEFAULT_PRINTER_CONFIG = printerConfig();

	/**
	 * Creates an instance of the given configuration interface.
	 *
	 * @param <T>
	 *        The concrete type of the configuration.
	 * @param configurationInterface
	 *        The configuration interface.
	 * @return A new instance of the given interface with all values set to their corresponding
	 *         defaults (if there are some).
	 */
	public static <T extends ConfigurationItem> T newConfigItem(Class<? extends T> configurationInterface) {
		return newInstance(getConfigurationDescriptor(configurationInterface));
	}

	/**
	 * Instantiates the given {@link Annotation}.
	 * 
	 * @param <T>
	 *        The concrete annotation type.
	 * @param annotationInterface
	 *        The annotation type to instantiate.
	 * @return The annotation item also implementing {@link ConfigurationItem}.
	 */
	public static <T extends Annotation> T newAnnotationItem(Class<T> annotationInterface) {
		return newInstance(getConfigurationDescriptor(annotationInterface));
	}

	private static <T> T newInstance(ConfigurationDescriptor configurationDescriptor) {
		@SuppressWarnings("unchecked")
		T result = (T) configurationDescriptor.factory().createNew();
		return result;
	}

	/**
	 * Resolves the {@link ConfigurationDescriptor} for the given {@link ConfigurationItem} or
	 * {@link Annotation} interface class.
	 *
	 * @param configurationInterface
	 *        The configuration interface (must be either {@link ConfigurationItem} or
	 *        {@link Annotation} subclass).
	 * @return The {@link ConfigurationDescriptor} describing the properties of the given interface.
	 */
	public static ConfigurationDescriptor getConfigurationDescriptor(Class<?> configurationInterface) {
		try {
			return internalGetConfigurationDescriptor(configurationInterface);
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private static ConfigurationDescriptor internalGetConfigurationDescriptor(Class<?> configurationInterface)
			throws ConfigurationException {
		return ConfigDescriptionResolver.getDescriptor(configurationInterface);
	}

	/**
	 * Creates a {@link ConfigBuilder} that accepts properties of the given
	 * {@link ConfigurationDescriptor}.
	 *
	 * @param configurationDescriptor
	 *        The type of configuration to build.
	 * @return A builder for the given configuration type.
	 */
	public static ConfigBuilder createConfigBuilder(ConfigurationDescriptor configurationDescriptor) {
		if (configurationDescriptor instanceof AbstractConfigurationDescriptor) {
			if (!((AbstractConfigurationDescriptor) configurationDescriptor).isFrozen()) {
				throw new IllegalStateException("Creating a builder for "
					+ configurationDescriptor.getConfigurationInterface().getName() + " is not possible. Its "
					+ ConfigurationDescriptor.class.getSimpleName() + " is not finished, yet.");
			}
		}
		return new ConfigBuilderImpl(configurationDescriptor);
	}

	/**
	 * Creates a {@link ConfigBuilder} that creates a {@link PolymorphicConfiguration} to
	 * instantiate the given implementation class.
	 */
	public static ConfigBuilder createConfigBuilderForImplementationClass(Class<?> implementationClass,
			ConfigurationDescriptor expectedDescriptor) throws ConfigurationException {
		ConfigurationDescriptor configDescriptor =
			getDescriptorForImplementationClassConfig(implementationClass, expectedDescriptor);
		ConfigBuilder configBuilder = TypedConfiguration.createConfigBuilder(configDescriptor);
		fillImplementationClass(implementationClass, configDescriptor, configBuilder);
		return configBuilder;
	}

	/**
	 * Creates an instance for the implementation class having a config constructor.
	 * 
	 * @param implementationClass
	 *        The class to get an instance for.
	 * @return An instance of the given implementation class.
	 * @throws ConfigurationException
	 *         When creating configuration item for implementation class failed.
	 */
	public static <T> T newConfiguredInstance(InstantiationContext context, Class<T> implementationClass)
			throws ConfigurationException {
		Object instance = context.getInstance(createConfigItemForImplementationClass(implementationClass));
		return implementationClass.cast(instance);
	}

	/**
	 * Creates a {@link PolymorphicConfiguration} with the given implementation class.
	 */
	public static <T> PolymorphicConfiguration<T> createConfigItemForImplementationClass(
			Class<T> implementationClass) throws ConfigurationException {
		ConfigurationDescriptor desc = getConfigurationDescriptor(PolymorphicConfiguration.class);
		return createConfigItemForImplementationClass(implementationClass, desc);
	}

	/**
	 * Creates a {@link PolymorphicConfiguration} for the given {@link ConfigurationDescriptor} with
	 * the given implementation class.
	 */
	public static <T> PolymorphicConfiguration<T> createConfigItemForImplementationClass(Class<T> implementationClass,
			ConfigurationDescriptor expectedDescriptor) throws ConfigurationException {
		ConfigurationDescriptor configDescriptor =
			getDescriptorForImplementationClassConfig(implementationClass, expectedDescriptor);
		PolymorphicConfiguration<T> item = newInstance(configDescriptor);
		fillImplementationClass(implementationClass, configDescriptor, item);
		return item;
	}

	private static ConfigurationDescriptor getDescriptorForImplementationClassConfig(Class<?> implementationClass,
			ConfigurationDescriptor expectedDescriptor) throws ConfigurationException {
		Factory factory = DefaultConfigConstructorScheme.getFactory(implementationClass);
		Class<?> configurationInterface = factory.getConfigurationInterface();
		if (!PolymorphicConfiguration.class.isAssignableFrom(configurationInterface)) {
			throw new ConfigurationException(implementationClass.getName() + " has not a polymorphic interface.");
		}
		if ((expectedDescriptor != null)
			&& configurationInterface.isAssignableFrom(expectedDescriptor.getConfigurationInterface())) {
			return expectedDescriptor;
		}
		return internalGetConfigurationDescriptor(configurationInterface);
	}

	private static void fillImplementationClass(Class<?> implementationClass, ConfigurationDescriptor configDescriptor,
			ConfigurationItem item) {
		PropertyDescriptor classProperty =
			configDescriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		if (implementationClass != classProperty.getDefaultValue()) {
			item.update(classProperty, implementationClass);
		}
	}

	/**
	 * calls {@link #createConfigBuilder(ConfigurationDescriptor)} with the
	 * result of {@link #getConfigurationDescriptor(Class)} for the given class.
	 * 
	 * @see #getConfigurationDescriptor(Class)
	 * @see #createConfigBuilder(Class)
	 */
	public static ConfigBuilder createConfigBuilder(Class<?> configurationInterface) {
		return createConfigBuilder(getConfigurationDescriptor(configurationInterface));
	}
	
	/**
	 * Fills the given values into the given configuration
	 * 
	 * @param config
	 *        the configuration to update
	 * @param values
	 *        a sequence of pairs of a property name and its serialized value
	 * 
	 * @throws ConfigurationException
	 *         if the value could not be deserialized or some property does not exist
	 */
	public static void fillValues(ConfigurationItem config, Iterable<? extends Entry<String, String>> values) throws ConfigurationException {
		ConfigurationDescriptor descriptor = config.descriptor();
		for (Entry<String, String> entry : values) {
			String propertyName = entry.getKey();
			PropertyDescriptor property = descriptor.getProperty(propertyName);
			if (property == null) {
				throw new ConfigurationException("Configuration descriptor '" + descriptor + "' has no property '" + propertyName + "'.");
			}

			Object value;
			String rawValue = entry.getValue();
			final ConfigurationValueProvider<?> valueProvider = property.getValueProvider();
			if (valueProvider == null) {
				if (!property.isInstanceValued()) {
					throw new ConfigurationException("Can not interpret '" + rawValue + "' as value for property '"
						+ property + "', because the property has no @" + Format.class.getSimpleName() + " annotation.");
				}

				value = ConfigUtil.getInstanceWithInstanceDefault(Object.class, propertyName, rawValue, null);
			} else {
				value = valueProvider.getValue(propertyName, rawValue);
			}
			config.update(property, value);
		}
	}

	/**
	 * Creates a new configuration of the given type and fills it with the given
	 * values
	 * 
	 * @param configurationInterface
	 *        the type of configuration to instantiate
	 * @param initialValues
	 *        a sequence of pairs of property names and their serialized values
	 * 
	 * @see #newConfigItem(Class)
	 * @see #fillValues(ConfigurationItem, Iterable)
	 */
	public static <T extends ConfigurationItem> T newConfigItem(Class<T> configurationInterface,
			Iterable<? extends Entry<String, String>> initialValues) throws ConfigurationException {
		T configItem = newConfigItem(configurationInterface);
		fillValues(configItem, initialValues);
		return configItem;
	}
	
	/**
	 * Creates a view to the given {@link ConfigurationItem} of the same type
	 * 
	 * @param <T>
	 *        the type of the {@link ConfigurationItem}
	 * @param configItem
	 *        the item to create a view for
	 * 
	 * @return a view to the given configItem. Each modification on the given
	 *         configuration item reflects to the resulting view. Each
	 *         modification on the given view results in an {@link Exception}.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ConfigurationItem> T createView(T configItem) {
		ConfigurationDescriptorImpl descriptor = (ConfigurationDescriptorImpl) configItem.descriptor();
		Class<?> configurationInterface = descriptor.getConfigurationInterface();
		if (!configurationInterface.isInstance(configItem)) {
			throw new IllegalArgumentException(configItem + " must be of type " + configurationInterface.getName());
		}

		return (T) descriptor.createView(configItem);
	}

	/**
	 * Instantiates a map of configurations. The result map is a mapping with the same keys as the
	 * configurations map whereas the value is the instantiated configuration.
	 * 
	 * <p>
	 * If a configuration value can not be cast to the given type, an error is logged.
	 * </p>
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate single configurations.
	 * @param configurations
	 *        The mapping to instantiate.
	 * @return A modifiable {@link Map} of instances.
	 * @since 5.8.0
	 */
	public static <K, V> Map<K, V> getInstanceMap(InstantiationContext context,
			Map<K, ? extends PolymorphicConfiguration<? extends V>> configurations) {
		if (configurations == null) {
			return new LinkedHashMap<>();
		}
		return instanceMap(context, configurations);
	}

	/**
	 * Optimized variant of {@link #getInstanceMap(InstantiationContext, Map)} creating a
	 * potentially unmodifiable map.
	 */
	public static <K, V> Map<K, V> getInstanceMapReadOnly(InstantiationContext context,
			Map<K, ? extends PolymorphicConfiguration<? extends V>> configurations) {
		if (configurations == null) {
			return Collections.emptyMap();
		}
		switch (configurations.size()) {
			case 0: {
				return Collections.emptyMap();
			}
			case 1: {
				Entry<K, ? extends PolymorphicConfiguration<? extends V>> entry =
					configurations.entrySet().iterator().next();
				V configuredObject = context.getInstance(entry.getValue());
				if (configuredObject == null) {
					return Collections.emptyMap();
				} else {
					return Collections.singletonMap(entry.getKey(), configuredObject);
				}
			}
			default: {
				return instanceMap(context, configurations);
			}
		}
	}

	private static <K, V> HashMap<K, V> instanceMap(InstantiationContext context,
			Map<K, ? extends PolymorphicConfiguration<? extends V>> configurations) {
		HashMap<K, V> result = MapUtil.newLinkedMap(configurations.size());
		for (Entry<K, ? extends PolymorphicConfiguration<? extends V>> entry : configurations.entrySet()) {
			V configuredObject = context.getInstance(entry.getValue());
			if (configuredObject != null) {
				result.put(entry.getKey(), configuredObject);
			}
		}
		return result;
	}

	/**
	 * Instantiates a list of configurations. The result list has the same order as the
	 * configurations list.
	 * 
	 * <p>
	 * If a configuration value can not be cast to the given type, an error is logged.
	 * </p>
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate single configurations.
	 * @param configurations
	 *        The mapping to instantiate.
	 * @return A modifiable {@link List} of instances.
	 * @since 5.8.0
	 * 
	 * @see #getInstanceListReadOnly(InstantiationContext, List)
	 */
	public static <T> List<T> getInstanceList(InstantiationContext context, List<? extends PolymorphicConfiguration<? extends T>> configurations) {
		if (configurations == null || configurations.isEmpty()) {
			 return new ArrayList<>();
		}
		return instanceList(context, configurations);
	}

	/**
	 * Optimized variant of {@link #getInstanceList(InstantiationContext, List)}.
	 * 
	 * <p>
	 * The returned list may or may not be unmodifiable. It is not recommend to modify the returned
	 * list.
	 * </p>
	 * 
	 * @see #getInstanceList(InstantiationContext, List)
	 */
	public static <T> List<T> getInstanceListReadOnly(InstantiationContext context,
			List<? extends PolymorphicConfiguration<? extends T>> configurations) {
		if (configurations == null) {
			return Collections.emptyList();
		}
		switch (configurations.size()) {
			case 0: {
				return Collections.emptyList();
			}
			case 1: {
				T configuredObject = context.getInstance(configurations.get(0));
				if (configuredObject != null) {
					return Collections.singletonList(configuredObject);
				} else {
					return Collections.emptyList();
				}
			}
			default: {
				List<T> result = instanceList(context, configurations);
				if (result.isEmpty()) {
					return Collections.emptyList();
				}
				return result;
			}
		}
	}

	private static <T> List<T> instanceList(InstantiationContext context,
			List<? extends PolymorphicConfiguration<? extends T>> configurations) {
		List<T> result = new ArrayList<>(configurations.size());
		for (PolymorphicConfiguration<? extends T> config : configurations) {
			T configuredObject = context.getInstance(config);
			if (configuredObject != null) {
				result.add(configuredObject);
			}
		}
		return result;
	}

	/**
	 * Instantiates a {@link List} of {@link NamedInstanceConfig}s.
	 * 
	 * @see #getInstanceList(InstantiationContext, List)
	 */
	public static <T> List<T> getInstanceListNamed(InstantiationContext context,
			List<? extends NamedInstanceConfig<? extends T>> configurations) {
		if (configurations == null) {
			 return new ArrayList<>();
		}
		List<T> result = new ArrayList<>(configurations.size());
		for (NamedInstanceConfig<? extends T> config : configurations) {
			T configuredObject = context.getInstance(config.getImpl());
			if (configuredObject != null) {
				result.add(configuredObject);
			}
		}
		return result;
	}

	/**
	 * Creates a String serialisation of the given {@link ConfigurationItem} which can be parsed
	 * later via {@link #fromString(String)}.
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} to serialise. May be <code>null</code>.
	 * 
	 * @return A string representation of the given {@link ConfigurationItem}, or <code>null</code>
	 *         if configuration is <code>null</code>.
	 * 
	 * @see TypedConfiguration#fromString(String)
	 * 
	 * @since 5.7.3
	 */
	public static String toString(ConfigurationItem config) {
		return toString(DEFAULT_ROOT_TAG, config);
	}

	/**
	 * Creates a String serialisation using the custom root tag name.
	 * 
	 * @see #toString(ConfigurationItem)
	 */
	public static String toString(String rootTagName, ConfigurationItem config) {
		return toString(rootTagName, config, DEFAULT_PRINTER_CONFIG);
	}

	/**
	 * Creates a String serialisation using the custom root tag name and the given printer
	 * configuration.
	 * 
	 * @see #toString(ConfigurationItem)
	 */
	public static String toString(String rootTagName, ConfigurationItem config, Config prettyPrinterConfig) {
		if (config == null) {
			return null;
		}

		return prettyPrint(toStringRaw(rootTagName, config), prettyPrinterConfig);
	}

	/**
	 * Pretty prints a raw configuration string using the given printer configuration.
	 */
	public static String prettyPrint(String rawConfig, Config prettyPrinterConfig) {
		if (StringServices.isEmpty(rawConfig)) {
			return rawConfig;
		}

		return prettyPrint(DOMUtil.parse(rawConfig), prettyPrinterConfig);
	}

	/**
	 * The given configuration in raw serialized form without pretty printing.
	 */
	public static String toStringRaw(ConfigurationItem config) {
		return toStringRaw(DEFAULT_ROOT_TAG, config);
	}

	/**
	 * The given configuration in raw serialized form without pretty printing.
	 */
	public static String toStringRaw(String rootTagName, ConfigurationItem config) {
		if (config == null) {
			return null;
		}

		StringWriter out = new StringWriter();
		try {
			serialize(rootTagName, config, new ConfigurationWriter(out));
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
		return out.getBuffer().toString();
	}

	private static String prettyPrint(Document document, Config prettyPrinterConfig) throws UnreachableAssertion {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			try (XMLPrettyPrinter printer = new XMLPrettyPrinter(buffer, prettyPrinterConfig)) {
				printer.write(document);
			}
			return new String(buffer.toByteArray(), TO_STRING_ENCODING);
		} catch (IOException ex) {
			throw new UnreachableAssertion("Writing to buffer must not fail.", ex);
		}
	}

	private static Config printerConfig() {
		Config printerConfig = XMLPrettyPrinter.newConfiguration();
		printerConfig.setXMLHeader(false);
		printerConfig.setIndentChar(' ');
		printerConfig.setIndentStep(2);
		printerConfig.setMaxIndent(80);
		printerConfig.setNewLineStyle(NewLineStyle.LF);
		printerConfig.setEncoding(TO_STRING_ENCODING);
		return printerConfig;
	}

	/**
	 * Serialisation the given {@link ConfigurationItem} to the given {@link Writer}.
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} to serialise.
	 * 
	 * @see TypedConfiguration#parse(Content)
	 * 
	 * @since 5.7.3
	 */
	public static void serialize(ConfigurationItem config, Writer out) {
		try {
			serialize(DEFAULT_ROOT_TAG, config, new ConfigurationWriter(out));
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Serialize the given {@link ConfigurationItem} using the given {@link Writer} with a custom
	 * root tag name.
	 * 
	 * @see #serialize(ConfigurationItem, Writer)
	 */
	public static void serialize(String rootTagName, ConfigurationItem config, ConfigurationWriter out) {
		try {
			out.write(rootTagName, configItemDescriptor(), config);
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static ConfigurationDescriptor configItemDescriptor() {
		return getConfigurationDescriptor(ConfigurationItem.class);
	}

	/**
	 * Parses a string serialisation of a {@link ConfigurationItem}, formerly produced by
	 * {@link #toString(ConfigurationItem)}
	 * 
	 * @param config
	 *        The serialised {@link ConfigurationItem}. May be <code>null</code>.
	 * 
	 * @return The {@link ConfigurationItem} parsed from the given string, or <code>null</code> if
	 *         configuration is <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if the given String could not be parsed to a valid {@link ConfigurationItem}.
	 * 
	 * @see TypedConfiguration#toString(ConfigurationItem)
	 * 
	 * @since 5.7.3
	 */
	public static ConfigurationItem fromString(String config) throws ConfigurationException {
		if (config == null) {
			return null;
		}
		return parse(CharacterContents.newContent(config));
	}

	/**
	 * Parses a {@link ConfigurationItem}, from the given {@link CharacterContent}.
	 * 
	 * @param source
	 *        The serialised {@link ConfigurationItem}.
	 * 
	 * @return The {@link ConfigurationItem} parsed from the given {@link CharacterContent}. Never
	 *         <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if the given {@link CharacterContent} could not be parsed to a valid
	 *         {@link ConfigurationItem}.
	 * 
	 * @see TypedConfiguration#serialize(ConfigurationItem, Writer)
	 * 
	 * @since 5.7.3
	 */
	public static ConfigurationItem parse(Content source) throws ConfigurationException {
		return parse("config", ConfigurationItem.class, source);
	}

	/**
	 * Parses a {@link ConfigurationItem}, from the given {@link CharacterContent}.
	 * 
	 * @param rootTag
	 *        The root tag to expect in the XML configuration.
	 * @param rootType
	 *        The configuration type to expect to be serialized in the given content.
	 * @param source
	 *        The serialized {@link ConfigurationItem}.
	 * 
	 * @return The {@link ConfigurationItem} parsed from the given {@link CharacterContent}. Never
	 *         <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if the given {@link CharacterContent} could not be parsed to a valid
	 *         {@link ConfigurationItem}.
	 * 
	 * @see TypedConfiguration#serialize(ConfigurationItem, Writer)
	 */
	public static <T> T parse(String rootTag, Class<T> rootType, Content source) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		return readConfig(log, reader(log, rootTag, rootType, source));
	}

	/**
	 * Parses a {@link ConfigurationItem}, from the given {@link CharacterContent}.
	 * 
	 * @param rootTag
	 *        The root tag to expect in the XML configuration.
	 * @param rootType
	 *        The configuration type to expect to be serialized in the given content.
	 * @param source
	 *        The serialized {@link ConfigurationItem}.
	 * 
	 * @return The {@link ConfigurationItem} parsed from the given {@link CharacterContent}. Never
	 *         <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if the given {@link CharacterContent} could not be parsed to a valid
	 *         {@link ConfigurationItem}.
	 * 
	 * @see TypedConfiguration#serialize(ConfigurationItem, Writer)
	 */
	public static Object parse(String rootTag, ConfigurationDescriptor rootType, Content source)
			throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		return readConfig(log, reader(log, rootTag, rootType, source));
	}

	/**
	 * Reads the configuration from the given reader and aborting if some error was logged during
	 * reading.
	 *
	 * @param <T>
	 *        The expected configuration type.
	 * @param log
	 *        The log to report errors to.
	 * @param reader
	 *        The reader to read the configuration from.
	 * @return The read configuration item.
	 * @throws ConfigurationException
	 *         If the reader encountered some errors.
	 */
	public static <T> T readConfig(BufferingProtocol log, ConfigurationReader reader) throws ConfigurationException {
		@SuppressWarnings("unchecked")
		T parsedConfig = (T) reader.read();
		if (log.hasErrors()) {
			Throwable parseProblem = log.getFirstProblem();
			ConfigurationException ex = new ConfigurationException(ResKey.text(log.getError()));
			if (parseProblem != null) {
				ex.initCause(parseProblem);
			}
			throw ex;
		}

		return parsedConfig;
	}

	/**
	 * Builds a {@link ConfigurationReader} for the given source expecting the given root tag
	 * corresponding to the given configuration type.
	 *
	 * @param <T>
	 *        The expected type of the resulting configuration.
	 * @param log
	 *        The {@link Log} to report errors to.
	 * @param rootTag
	 *        The expected root tag.
	 * @param rootType
	 *        The expected configuration type.
	 * @param source
	 *        The XML source to read.
	 * @return A {@link ConfigurationReader} reading the given source.
	 */
	public static <T> ConfigurationReader reader(Log log, String rootTag, Class<T> rootType,
			Content source) {
		return reader(log, rootTag, getConfigurationDescriptor(rootType), source);
	}

	/**
	 * Builds a {@link ConfigurationReader} for the given source expecting the given root tag
	 * corresponding to the given configuration type.
	 *
	 * @param log
	 *        The {@link Log} to report errors to.
	 * @param rootTag
	 *        The expected root tag.
	 * @param rootDescriptor
	 *        The expected configuration type.
	 * @param source
	 *        The XML source to read.
	 * @return A {@link ConfigurationReader} reading the given source.
	 */
	public static ConfigurationReader reader(Log log, String rootTag, ConfigurationDescriptor rootDescriptor,
			Content source) {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(rootTag, rootDescriptor);
		ConfigurationReader reader = new ConfigurationReader(new DefaultInstantiationContext(log), descriptors);
		reader.setSource(source);
		return reader;
	}

	/**
	 * Copies the values from the fallback to the target.
	 * <p>
	 * <b>Errors are reported to the {@link InstantiationContext}, which has to be checked for
	 * errors by the caller.</b>
	 * </p>
	 * <p>
	 * Copies only properties for which {@link ConfigurationItem#valueSet(PropertyDescriptor)}
	 * returns true in the fallback, but false in the target. I.e. it does not override values in
	 * the target that have already been explicitly set. And it does not copy default values from
	 * the fallback, as the target has its own default values.
	 * </p>
	 * 
	 * @param fallback
	 *        Is not allowed to be null.
	 * @param target
	 *        Is not allowed to be null.
	 * @param context
	 *        For copying {@link ConfigurationItem}s and error reporting.
	 */
	public static void applyFallback(ConfigurationItem fallback, ConfigBuilder target, InstantiationContext context) {
		ConfigCopier.fillDeepCopy(fallback, target, context);
	}

	/**
	 * Deep-copies the given {@link ConfigurationItem}.
	 * <p>
	 * See {@link ConfigCopier} for the details and further copy methods.
	 * </p>
	 * 
	 * @param original
	 *        Is allowed to be null.
	 * @return null, if and only if the given item is null.
	 */
	public static <T extends ConfigurationItem> T copy(T original) {
		if (original == null) {
			return null;
		}
		return ConfigCopier.copy(original);
	}

	/**
	 * Is the {@link ConfigurationItem} at the given {@link PropertyDescriptor property} empty?
	 * <p>
	 * Empty means:
	 * <ul>
	 * <li><code>null</code></li>
	 * <li><code>CharSequence.isEmpty()</code></li>
	 * <li><code>Collection.isEmpty()</code></li>
	 * <li><code>Map.isEmpty()</code></li>
	 * </ul>
	 * </p>
	 * <p>
	 * Must not be called when the property is mandatory but not set.
	 * </p>
	 * 
	 * @param item
	 *        Is not allowed to be <code>null</code>.
	 * @param property
	 *        Is not allowed to be <code>null</code>.
	 */
	public static boolean isEmpty(ConfigurationItem item, PropertyDescriptor property) {
		Object propertyValue = item.value(property);
		if (propertyValue == null) {
			return true;
		}
		if ((propertyValue instanceof CharSequence) && ((CharSequence) propertyValue).length() == 0) {
			return true;
		}
		// Ignore the PropertyKind,
		// as for example some lists are stored in a property of kind PLAIN and not LIST.
		if ((propertyValue instanceof Collection) && ((Collection<?>) propertyValue).isEmpty()) {
			return true;
		}
		if ((propertyValue instanceof Map) && ((Map<?, ?>) propertyValue).isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Resets all properties that have a value equal to their default value.
	 * 
	 * @param config
	 *        The configuration to be minimized.
	 */
	public static void minimize(ConfigurationItem config) {
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (property.isMandatory() || property.getAnnotation(ValueInitializer.class) != null) {
				continue;
			}

			Object value = config.value(property);
			if (config.valueSet(property)) {
				if (Utils.equals(value, property.getDefaultValue())) {
					config.reset(property);
					continue;
				}
			}

			if (value == null) {
				continue;
			}

			switch (property.kind()) {
				case ITEM: {
					minimize(property.getConfigurationAccess().getConfig(value));
					break;
				}
				case LIST: {
					Collection<?> list = (Collection<?>) value;
					for (Object innerValue : list) {
						minimize(property.getConfigurationAccess().getConfig(innerValue));
					}
					break;
				}
				case ARRAY: {
					for (int n = 0, cnt = Array.getLength(value); n < cnt; n++) {
						minimize(property.getConfigurationAccess().getConfig(Array.get(value, n)));
					}
					break;
				}
				case MAP: {
					Map<?, ?> map = (Map<?, ?>) value;
					for (Object innerValue : map.values()) {
						minimize(property.getConfigurationAccess().getConfig(innerValue));
					}
					break;
				}
				default:
					// Ignore non-item properties.
					break;
			}
		}
	}

	/**
	 * Creates a {@link ConfigurationDescriptor} from the given
	 * {@link ConfigurationDescriptorConfig} and logs errors into a {@link LogProtocol}.
	 * 
	 * @see #getConfigurationDescriptor(Protocol, ConfigurationDescriptorConfig)
	 */
	public static ConfigurationDescriptor getConfigurationDescriptor(ConfigurationDescriptorConfig config) {
		LogProtocol protocol = new LogProtocol(TypedConfiguration.class);
		ConfigurationDescriptor result = getConfigurationDescriptor(protocol, config);
		protocol.checkErrors();
		return result;

	}
	
	/**
	 * Creates a {@link ConfigurationDescriptor} from the given
	 * {@link ConfigurationDescriptorConfig}.
	 * 
	 * @param protocol
	 *        Protocol to log errors to.
	 * @param config
	 *        The configuration to instantiate.
	 */
	public static ConfigurationDescriptor getConfigurationDescriptor(Protocol protocol,
			ConfigurationDescriptorConfig config) {
		ConfigurationDescriptor[] superDescriptors = resolveDescriptors(config.getExtensions());
		DeclarativeConfigDescriptor configDescriptor =
			new DeclarativeConfigDescriptor(config.getAnnotations(), superDescriptors, config.location());
		if (config.isAbstract()) {
			configDescriptor.setAbstract();
		}
		String resPrefix = config.getResPrefix();
		if (resPrefix != null) {
			if (resPrefix.charAt(resPrefix.length() - 1) != '.') {
				resPrefix = resPrefix + '.';
			}
			configDescriptor.setResPrefix(resPrefix);
		}
		addProperties(protocol, configDescriptor, config.getProperties());
	
		for (PropertyDescriptorImpl property : configDescriptor.getProperties()) {
			property.resolveLocalProperties(protocol);
		}
		configDescriptor.resolve(protocol);
		configDescriptor.freeze(protocol);
	
		return configDescriptor;
	}

	private static void addProperties(Protocol protocol, DeclarativeConfigDescriptor configDescriptor,
			Map<String, PropertyDescriptorConfig> properties) {
		Map<String, PropertyDescriptorImpl[]> superPropsByName =
			propertyDescriptorsByName(configDescriptor.getSuperDescriptors());
		for (PropertyDescriptorConfig declaredProperties : properties.values()) {
			String propertyName = declaredProperties.getName();
			PropertyDescriptorImpl[] superProps;
			if (superPropsByName.containsKey(propertyName)) {
				superProps = superPropsByName.remove(propertyName);
			} else {
				superProps = PropertyDescriptorImpl.NO_PROPERTY_DESCRIPTORS;
			}
			addProperty(protocol, configDescriptor, declaredProperties, superProps);
		}
		for (Entry<String, PropertyDescriptorImpl[]> inheritedProperties : superPropsByName.entrySet()) {
			addProperty(protocol, configDescriptor, inheritedProperties.getKey(), inheritedProperties.getValue());
		}
	}

	private static Map<String, PropertyDescriptorImpl[]> propertyDescriptorsByName(
			ConfigurationDescriptor[] superDescriptors) {
		switch (superDescriptors.length) {
			case 0:
				return Collections.emptyMap();
			default:
				Map<String, PropertyDescriptorImpl[]> result = new LinkedHashMap<>();
				for (ConfigurationDescriptor descriptor : superDescriptors) {
					for (PropertyDescriptor property : descriptor.getProperties()) {
						PropertyDescriptorImpl propImpl = (PropertyDescriptorImpl) property;
						String propertyName = property.getPropertyName();
						PropertyDescriptorImpl[] clash =
							result.put(propertyName, new PropertyDescriptorImpl[] { propImpl });
						if (clash != null) {
							result.put(propertyName, ArrayUtil.addElement(clash, propImpl));
						}
					}
				}
				return result;

		}
	}

	/**
	 * The {@link ConfigurationDescriptor}s defined by the given configuration interfaces.
	 * 
	 * @param extensions
	 *        {@link ConfigurationItem}s to get {@link ConfigurationDescriptor}s for.
	 * @return An array of the same length as the input. The order of the
	 *         {@link ConfigurationDescriptor} is according to the order of configuration
	 *         interfaces.
	 */
	private static ConfigurationDescriptor[] resolveDescriptors(Class<? extends ConfigurationItem>[] extensions) {
		int numberSuperDescr = extensions.length;
		if (numberSuperDescr == 0) {
			return ConfigurationDescriptor.NO_CONFIGURATION_DESCRIPTORS;
		}
		ConfigurationDescriptor[] descriptors = new ConfigurationDescriptor[numberSuperDescr];
		for (int i = 0; i < numberSuperDescr; i++) {
			descriptors[i] = TypedConfiguration.getConfigurationDescriptor(extensions[i]);
		}
		return descriptors;
	}

	private static void addProperty(Protocol protocol, DeclarativeConfigDescriptor configDescriptor,
			String propertyName, PropertyDescriptorImpl[] superProperties) {
		DeclarativePropertyDescriptor propertyDescriptor =
			new DeclarativePropertyDescriptor(protocol, configDescriptor, propertyName, superProperties);

		propertyDescriptor.setType(propertyDescriptor.firstSuperProperty().getType());
		configDescriptor.addPropertyDescriptor(protocol, propertyDescriptor);
	}

	private static void addProperty(Protocol protocol, DeclarativeConfigDescriptor configDescriptor,
			PropertyDescriptorConfig propertyConfig, PropertyDescriptorImpl[] superProperties) {
		DeclarativePropertyDescriptor propertyDescriptor =
			new DeclarativePropertyDescriptor(protocol, configDescriptor, propertyConfig.getName(), superProperties);
		
		Class<?> returnType = propertyConfig.getType();
		if (returnType == null) {
			if (superProperties.length == 0) {
				protocol.error(
					addConfigurationItemInfo("No type set for property " + propertyConfig.getName() + ".",
						propertyConfig));
				return;
			} else {
				returnType = propertyDescriptor.firstSuperProperty().getType();
			}
		}
		propertyDescriptor.setType(returnType);
		propertyDescriptor.setDeclaredInstanceType(propertyConfig.getInstanceType());
	
		if (propertyConfig.getElementType() != null) {
			propertyDescriptor.setElementType(propertyConfig.getElementType());
		}
	
		if (propertyConfig.getKeyAttribute() != null) {
			propertyDescriptor.setKeyPropertyConfigurationName(propertyConfig.getKeyAttribute());
		}
	
		Map<Class<? extends Annotation>, Annotation> annotations = propertyConfig.getAnnotations();
		String annotatedDefault = propertyConfig.getDefault();
		if (annotatedDefault != null) {
			try {
				Annotation defaultAnnotation = getDefaultAnnotation(annotations, returnType, annotatedDefault);
				annotations = putIfAbsent(protocol, propertyConfig, annotations, defaultAnnotation);
			} catch (ConfigurationException ex) {
				protocol.error(addConfigurationItemInfo(
					"Unable to determine default annotation from value '" + annotatedDefault + "'.",
					propertyConfig), ex);
			}
		}
	
		SimpleAnnotatedElement annotationsElement = new SimpleAnnotatedElement(annotations);
		propertyDescriptor.setAnnotations(annotationsElement);
	
		configDescriptor.addPropertyDescriptor(protocol, propertyDescriptor);
		configDescriptor.initPropertyAnnotations(protocol, propertyDescriptor, returnType, annotationsElement);
	}

	private static Map<Class<? extends Annotation>, Annotation> putIfAbsent(Protocol log,
			PropertyDescriptorConfig propertyConfig, Map<Class<? extends Annotation>, Annotation> annotations,
			Annotation annotation) {
		Class<? extends Annotation> key = annotation.annotationType();
		if (annotations.containsKey(key)) {
			log.info(addConfigurationItemInfo("Configuration contains duplicate annotation '" + key.getName()
				+ "'. An explicit one and a short cut variant.", propertyConfig),
				Protocol.WARN);
		} else {
			annotations = lazyCopy(propertyConfig, annotations);
			annotations.put(key, annotation);
		}
		return annotations;
	}

	private static String addConfigurationItemInfo(String msg, PropertyDescriptorConfig config) {
		return msg + " Location: " + config.location();
	}

	private static Annotation getDefaultAnnotation(Map<Class<? extends Annotation>, Annotation> annotations,
			Class<?> propertyType, String defaultValue) throws ConfigurationException {
		if (propertyType == String.class) {
			return updateFormattedValue(newAnnotationItem(StringDefault.class), defaultValue);
		}
		if (propertyType == Class.class) {
			return updateFormattedValue(newAnnotationItem(ClassDefault.class), defaultValue);
		}
		if (propertyType == Long.class || propertyType == long.class) {
			return updateFormattedValue(newAnnotationItem(LongDefault.class), defaultValue);
		}
		if (propertyType == Integer.class || propertyType == int.class) {
			return updateFormattedValue(newAnnotationItem(IntDefault.class), defaultValue);
		}
		if (propertyType == Short.class || propertyType == short.class) {
			return updateFormattedValue(newAnnotationItem(ShortDefault.class), defaultValue);
		}
		if (propertyType == Byte.class || propertyType == byte.class) {
			return updateFormattedValue(newAnnotationItem(ByteDefault.class), defaultValue);
		}
		if (propertyType == Double.class || propertyType == double.class) {
			return updateFormattedValue(newAnnotationItem(DoubleDefault.class), defaultValue);
		}
		if (propertyType == Float.class || propertyType == float.class) {
			return updateFormattedValue(newAnnotationItem(FloatDefault.class), defaultValue);
		}
		if (propertyType == Boolean.class || propertyType == boolean.class) {
			return updateFormattedValue(newAnnotationItem(BooleanDefault.class), defaultValue);
		}
		if (propertyType == Date.class) {
			// Date type finally leads to a format.
			return updateFormattedValue(newAnnotationItem(FormattedDefault.class), defaultValue);
		}
		if (ConfigurationItem.class.isAssignableFrom(propertyType)) {
			return updateFormattedValue(newAnnotationItem(ItemDefault.class), defaultValue);
		}
		if (annotations.containsKey(InstanceFormat.class)) {
			return updateFormattedValue(newAnnotationItem(InstanceDefault.class), defaultValue);
		}
		if (annotations.containsKey(Format.class)) {
			return updateFormattedValue(newAnnotationItem(FormattedDefault.class), defaultValue);
		}
		if (propertyType == List.class) {
			return updateFormattedValue(newAnnotationItem(ListDefault.class), defaultValue);
		}
		ResKey key =
			I18NConstants.ERROR_INVALID_DEFAULT_VALUE_FOR_TYPE__TYPE___VALUE.fill(propertyType.getName(), defaultValue);
		throw new ConfigurationException(key, PropertyDescriptorConfig.DEFAULT, defaultValue);
	}

	private static Annotation updateFormattedValue(Annotation annotation, String value)
			throws ConfigurationException {
		return updateFormattedValue(annotation, "value", value);
	}

	private static Annotation updateFormattedValue(Annotation annotation, String propertyName, String value)
			throws ConfigurationException {
		ConfigurationItem annotationAsItem = (ConfigurationItem) annotation;
		PropertyDescriptor property = annotationAsItem.descriptor().getProperty(propertyName);
		Object parsedValue = property.getValueProvider().getValue(propertyName, value);
		annotationAsItem.update(property, parsedValue);
		return annotation;
	}

	/**
	 * Updates an arbitrary value for a given {@link Annotation}.
	 */
	public static Annotation updateValue(Annotation annotation, String propertyName, Object value) {
		ConfigurationItem annotationAsItem = (ConfigurationItem) annotation;
		PropertyDescriptor property = annotationAsItem.descriptor().getProperty(propertyName);
		annotationAsItem.update(property, value);
		return annotation;
	}

	private static Map<Class<? extends Annotation>, Annotation> lazyCopy(PropertyDescriptorConfig propertyConfig,
			Map<Class<? extends Annotation>, Annotation> annotations) {
		if (annotations == propertyConfig.getAnnotations()) {
			annotations = new LinkedHashMap<>(annotations);
		}
		return annotations;
	}

}
