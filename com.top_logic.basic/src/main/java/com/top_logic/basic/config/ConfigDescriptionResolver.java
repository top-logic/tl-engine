/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ApplicationConfig.Config.Defaults;
import com.top_logic.basic.config.ApplicationConfig.Config.StringDefaultSpec;
import com.top_logic.basic.config.annotation.Factory;

/**
 * The class {@link ConfigDescriptionResolver} resolves
 * {@link ConfigurationDescriptor} for unknown configuration interfaces.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class ConfigDescriptionResolver {

	private static final Map<Class<?>, ConfigurationDescriptorBuilder> newDescriptors = new LinkedHashMap<>();
	private static final Map<Class<?>, ConfigurationDescriptor> processedDescriptors = new HashMap<>();
	private static final Map<Class<?>, ConfigurationDescriptor> descriptorsByClass = new HashMap<>();

	private static final Map<Class<?>, TagNameMap> pendingTagMaps = new HashMap<>();
	private static final Map<Class<?>, TagNameMap> tagMaps = new HashMap<>();

	private static Class<?> topInterface = null;

	static synchronized ConfigurationDescriptor getDescriptor(Class<?> configurationInterface)
			throws ConfigurationException {
		final ConfigurationDescriptor knownDescriptor = getExistingDescriptor(configurationInterface);
		if (knownDescriptor != null) {
			return knownDescriptor;
		}

		Protocol protocol = new BufferingProtocol(new LogProtocol(ConfigDescriptionResolver.class));
		boolean registerNew = topInterface == null;
		if (registerNew) {
			topInterface = configurationInterface;
			protocol.info("Resolving: " + configurationInterface.getName(), Protocol.VERBOSE);
		} else {
			protocol.info(
					configurationInterface.getName() + " is resolved during resolving of " + topInterface.getName(),
					Protocol.VERBOSE);
		}
		try {
			ConfigurationDescriptor descriptor = createNewDescriptor(protocol, configurationInterface);
			if (registerNew) {
				registerNewDescriptors();
			} else {
				checkProtocol(protocol, configurationInterface);
			}
			return descriptor;
		} finally {
			if (registerNew) {
				cleanup();
			}
		}
	}

	private static void cleanup() {
		newDescriptors.clear();
		processedDescriptors.clear();
		pendingTagMaps.clear();
		topInterface = null;
	}

	static synchronized ConfigurationDescriptor getDescriptor(Protocol protocol, Class<?> configurationInterface)
			throws ConfigurationException {
		final ConfigurationDescriptor knownDescriptor = getExistingDescriptor(configurationInterface);
		if (knownDescriptor != null) {
			return knownDescriptor;
		}
		return createNewDescriptor(protocol, configurationInterface);
	}

	private static ConfigurationDescriptor createNewDescriptor(Protocol protocol, Class<?> configurationInterface)
			throws ConfigurationException {
		ConfigurationDescriptor newDescriptor = createDescriptor(protocol, configurationInterface);
		resolveNewDescriptors(protocol);
		return newDescriptor;
	}

	private static void resolveNewDescriptors(Protocol protocol) throws ConfigurationException {
		Collection<ConfigurationDescriptorBuilder> unresolvedDescriptors = newDescriptors.values();
		Object[] builderOrDescriptor = unresolvedDescriptors.toArray();

		int done = 0;
		for (int n = 0, cnt = builderOrDescriptor.length; n < cnt; n++) {
			ConfigurationDescriptorBuilder builder = (ConfigurationDescriptorBuilder) builderOrDescriptor[n];
			Class<?> configurationInterface = builder.getConfigInterface();

			// check whether the descriptor was already resolved
			// remove descriptor to ensure that the descriptor is
			if (newDescriptors.remove(configurationInterface) == null) {
				// was already removed
				continue;
			}

			// must be added before resolving as resolving could enforce other
			// ConfigurationDescriptors to access the currently resolved
			// descriptor.
			ConfigurationDescriptorImpl descriptor = builder.getDescriptor();
			processedDescriptors.put(configurationInterface, descriptor);

			descriptor.resolve(protocol);
			checkProtocol(protocol, configurationInterface);

			// Store ConfigurationDescriptor to freeze later.
			builderOrDescriptor[done++] = descriptor;
		}

		for (int n = 0; n < done; n++) {
			AbstractConfigurationDescriptor descriptor = (AbstractConfigurationDescriptor) builderOrDescriptor[n];
			descriptor.freeze(protocol);
			checkProtocol(protocol, descriptor.getConfigurationInterface());
		}

	}

	private static void checkProtocol(Protocol protocol, Class<?> configurationInterface) throws ConfigurationException {
		if (protocol instanceof BufferingProtocol) {
			if (protocol.hasErrors()) {
				final Throwable cause = protocol.getFirstProblem();
				String detail = ((BufferingProtocol) protocol).getError();
				String configurationInterfaceName = configurationInterface.getName();
				String simplifiedDetail = detail.replace(configurationInterfaceName + ".", "");
				String prefix = ConfigurationDescriptorBuilder.getErrorPrefix(configurationInterface);
				final String message;
				if (simplifiedDetail.startsWith(prefix)) {
					message = simplifiedDetail;
				} else {
					message = prefix + simplifiedDetail;
				}
				if (cause != null) {
					throw new ConfigurationException(message, cause);
				} else {
					throw new ConfigurationException(message);
				}
			}
		} else {
			try {
				protocol.checkErrors();
			} catch (RuntimeException ex) {
				throw new ConfigurationException(
					"Error when creating descriptor for " + configurationInterface.getName(), ex);
			}
		}
	}

	/**
	 * Creates the {@link ConfigurationDescriptor} for the given configuration
	 * interface (and all super interfaces) and adds them to
	 * {@link #newDescriptors}.
	 * 
	 * If some error occurred nothing remains in {@link #newDescriptors}.
	 */
	private static ConfigurationDescriptor createDescriptor(Protocol protocol, Class<?> configurationInterface)
			throws ConfigurationException {
		// Build super descriptors for super interfaces.
		Class<?>[] superInterfaces = configurationInterface.getInterfaces();

		if (!ConfigurationItem.class.isAssignableFrom(configurationInterface)) {
			// E.g. annotation items.
			Class<?>[] syntheticSuperInterfaces = new Class<?>[superInterfaces.length + 1];
			System.arraycopy(superInterfaces, 0, syntheticSuperInterfaces, 0, superInterfaces.length);
			syntheticSuperInterfaces[superInterfaces.length] = ConfigurationItem.class;
			superInterfaces = syntheticSuperInterfaces;
		}

		List<ConfigurationDescriptor> superDescriptorsList = getSuperDescriptors(protocol, superInterfaces);
		ConfigurationDescriptorImpl[] superDescriptors = new ConfigurationDescriptorImpl[superDescriptorsList.size()];
		superDescriptors = superDescriptorsList.toArray(superDescriptors);
		ConfigurationDescriptorBuilder builder =
			new ConfigurationDescriptorBuilder(protocol, configurationInterface, superDescriptors);
		builder.build();
		checkProtocol(protocol, configurationInterface);

		ConfigurationDescriptor newConfigurationDescriptor = builder.getDescriptor();
		newDescriptors.put(newConfigurationDescriptor.getConfigurationInterface(), builder);

		return newConfigurationDescriptor;
	}

	private static List<ConfigurationDescriptor> getSuperDescriptors(Protocol protocol, Class<?>[] superInterfaces)
			throws ConfigurationException {
		List<ConfigurationDescriptor> superDescriptors = new ArrayList<>(superInterfaces.length);
		for (int n = 0, cnt = superInterfaces.length; n < cnt; n++) {
			Class<?> superInterface = superInterfaces[n];
			if (superInterface.getAnnotation(Factory.class) != null) {
				continue;
			}

			ConfigurationDescriptor superDescriptor = getExistingDescriptor(superInterface);
			if (superDescriptor == null) {
				superDescriptor = createDescriptor(protocol, superInterface);
			}
			superDescriptors.add(superDescriptor);
		}
		return superDescriptors;
	}

	/**
	 * returns the already created {@link ConfigurationDescriptor} for the given
	 * configuration interface or <code>null</code>
	 */
	private static ConfigurationDescriptor getExistingDescriptor(Class<?> configurationInterface) {
		// already published
		ConfigurationDescriptor finishedConfigurationDescriptor = descriptorsByClass.get(configurationInterface);
		if (finishedConfigurationDescriptor != null) {
			return finishedConfigurationDescriptor;
		}

		// already resolved
		final ConfigurationDescriptor resolvedDescriptor = processedDescriptors.get(configurationInterface);
		if (resolvedDescriptor != null) {
			return resolvedDescriptor;
		}

		// new descriptor
		ConfigurationDescriptorBuilder builder = newDescriptors.get(configurationInterface);
		if (builder == null) {
			return null;
		}

		return builder.getDescriptor();
	}

	private static void registerNewDescriptors() {
		Set<Class<?>> interfaces = processedDescriptors.keySet();
		while (!interfaces.isEmpty()) {
			assert newDescriptors.isEmpty() : "Not all descriptors resolved";
			// copy list to avoid concurrent modification
			ArrayList<Class<?>> l = new ArrayList<>(interfaces);
			for (Class<?> configurationInterface : l) {
				ConfigurationDescriptorImpl descriptor =
					(ConfigurationDescriptorImpl) processedDescriptors.get(configurationInterface);

				final ConfigurationDescriptor clash = descriptorsByClass.put(configurationInterface, descriptor);
				if (clash != null) {
					assert false : configurationInterface + " was already processed before.";
				}
				interfaces.remove(configurationInterface);
			}
		}

		tagMaps.putAll(pendingTagMaps);
	}

	/**
	 * Reads the values that are explicit set in the given {@link ConfigurationItem items} and
	 * installs these values as default for the corresponding property.
	 * 
	 * @param defaults
	 *        Items use to determine new defaults indexed by their configuration interface.
	 */
	static synchronized void adaptConfiguredDefaults(Map<Class<?>, Defaults> defaults) {
		Collection<Defaults> sortedDefaults = topSort(defaults);

		// Load all descriptors whose defaults are configured.
		sortedDefaults.stream().map(d -> d.getInterface()).forEach(TypedConfiguration::getConfigurationDescriptor);

		// Index all loaded descriptors by super class.
		Map<ConfigurationDescriptor, List<ConfigurationDescriptor>> descriptorsBySuperDescriptor =
			getDescriptorsBySuperDescriptor();

		for (Defaults itemDefaults : sortedDefaults) {
			Class<?> configInterface = itemDefaults.getInterface();
			for (PropertyDescriptor property : TypedConfiguration.getConfigurationDescriptor(configInterface)
				.getProperties()) {
				StringDefaultSpec defaultSpec = itemDefaults.getProperties().get(property.getPropertyName());
				if (defaultSpec == null) {
					// No default configured.
					continue;
				}
				String formattedDefault = defaultSpec.getValue();

				@SuppressWarnings("rawtypes")
				ConfigurationValueProvider valueProvider = property.getValueProvider();
				if (valueProvider == null) {
					Logger.error("Default value for property '" + property.getPropertyName() + "' of '"
						+ configInterface.getName() + "' cannot be configured, because the property has no format at "
						+ itemDefaults.location() + ".",
						ConfigDescriptionResolver.class);
					continue;
				}
				try {
					Object defaultValue = valueProvider.getValue(property.getPropertyName(), formattedDefault);
					updateDefaultValue(property, defaultValue);
					handDownConfiguredDefault(property, defaultValue, descriptorsBySuperDescriptor);
				} catch (ConfigurationException ex) {
					Logger.error(
						"Invalid configured default value '" + formattedDefault + "' for property '"
							+ property.getPropertyName() + "' of '" + configInterface.getName() + "' at "
							+ itemDefaults.location() + ".",
						ex, ConfigDescriptionResolver.class);
				}
			}
		}
	}

	private static void updateDefaultValue(PropertyDescriptor property, Object defaultValue) {
		((PropertyDescriptorImpl) property).setConfiguredDefault(defaultValue, false);
	}

	private static Map<ConfigurationDescriptor, List<ConfigurationDescriptor>> getDescriptorsBySuperDescriptor() {
		Map<ConfigurationDescriptor, List<ConfigurationDescriptor>> result =
				new HashMap<>();
		
		for (ConfigurationDescriptor descriptor : descriptorsByClass.values()) {
			// add storage for current descriptor
			if (!result.containsKey(descriptor)) {
				result.put(descriptor, new ArrayList<>());
			}
			// register current descriptor to super descriptor
			for (ConfigurationDescriptor superDescriptor : descriptor.getSuperDescriptors()) {
				List<ConfigurationDescriptor> subDescriptors = result.get(superDescriptor);
				if (subDescriptors == null) {
					subDescriptors = new ArrayList<>();
					result.put(superDescriptor, subDescriptors);
				}
				subDescriptors.add(descriptor);
			}
		}
		return result;
	}
	
	/**
	 * Sets the given value as default value for the given {@link PropertyDescriptor}. Moreover for
	 * each property that inherits for the property which have no default specification annotated (
	 * {@link PropertyDescriptorImpl#hasLocalExplicitDefault()}) the value is set as default value.
	 * 
	 * @param property
	 *        The {@link PropertyDescriptor} to set default value to.
	 * @param newDefaultValue
	 *        The new default value.
	 * @param descriptorsBySuperDescriptor
	 *        A Mapping from {@link ConfigurationDescriptor} to their "sub"
	 *        {@link ConfigurationDescriptor}, i.e. to the {@link ConfigurationDescriptor}s whose
	 *        configuration interface is a sub interface of the configuration interface of the given
	 *        {@link ConfigurationDescriptor}.
	 */
	private static void handDownConfiguredDefault(PropertyDescriptor property, Object newDefaultValue,
			Map<ConfigurationDescriptor, List<ConfigurationDescriptor>> descriptorsBySuperDescriptor) {
		List<ConfigurationDescriptor> subDescriptors = descriptorsBySuperDescriptor.get(property.getDescriptor());
		for (ConfigurationDescriptor desc : subDescriptors) {
			PropertyDescriptorImpl subProperty =
				((ConfigurationDescriptorImpl) desc).getProperty(property.getPropertyName());

			if (!subProperty.hasLocalExplicitDefault() && !subProperty.hasLocalMandatoryAnnotation()) {
				// do no override annotated default of specialized configuration interface.
				subProperty.setConfiguredDefault(newDefaultValue, true);
				handDownConfiguredDefault(subProperty, newDefaultValue, descriptorsBySuperDescriptor);
			}
		}
	}

	/**
	 * Sorts the given item topologically, i.e. if a {@link ConfigurationItem} <code>c1</code> has a
	 * configuration interface <code>A</code> and {@link ConfigurationItem} <code>c2</code> has a
	 * configuration interface <code>B extends A</code>, then <code>c2</code> occurs after
	 * <code>c1</code> in the result.
	 */
	private static Collection<Defaults> topSort(Map<Class<?>, Defaults> defaults) {
		LinkedHashSet<Defaults> sorted = new LinkedHashSet<>();
		for (Defaults item : defaults.values()) {
			addItem(sorted, defaults, TypedConfiguration.getConfigurationDescriptor(item.getInterface()));
		}
		return sorted;
	}

	private static void addItem(Collection<Defaults> sorted, Map<Class<?>, Defaults> defaults,
			ConfigurationDescriptor descriptor) {
		for (ConfigurationDescriptor superDescriptor : descriptor.getSuperDescriptors()) {
			addItem(sorted, defaults, superDescriptor);
		}

		Defaults configurationItem = defaults.get(descriptor.getConfigurationInterface());
		if (configurationItem != null) {
			sorted.add(configurationItem);
		}
	}

	public static synchronized TagNameMap subtypes(Protocol log, Class<?> configType) {
		TagNameMap existingResult = lookupTagMap(configType);
		if (existingResult != null) {
			return existingResult;
		}

		TagNameMap newResult = new TagNameResolver(log).subtypes(configType);
		if (topInterface != null) {
			pendingTagMaps.put(configType, newResult);
		} else if (!log.hasErrors()) {
			tagMaps.put(configType, newResult);
		}

		return newResult;
	}

	public static synchronized TagNameMap polymorphicSubtypes(Protocol log, Class<?> instanceType) {
		TagNameMap existingResult = lookupTagMap(instanceType);
		if (existingResult != null) {
			return existingResult;
		}

		TagNameMap newResult = new TagNameResolver(log).polymorphicSubtypes(instanceType);
		if (topInterface != null) {
			pendingTagMaps.put(instanceType, newResult);
		} else if (!log.hasErrors()) {
			tagMaps.put(instanceType, newResult);
		}
		return newResult;
	}

	private static TagNameMap lookupTagMap(Class<?> instanceType) {
		TagNameMap globalResult = tagMaps.get(instanceType);
		if (globalResult != null) {
			return globalResult;
		}
		TagNameMap pendingResult = pendingTagMaps.get(instanceType);
		if (pendingResult != null) {
			return pendingResult;
		}
		return null;
	}

}
