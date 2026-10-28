/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.misc.DescendingConfigurationItemVisitor;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.ResKey;

/**
 * All secrets the configuration of the application refers to, the {@link SecretReference}s.
 *
 * <p>
 * The configuration of each running service is searched for {@link SecretSource}s, through all
 * nested configurations, lists and maps, e.g. the credentials of an LLM provider in the
 * configuration of the service holding the providers. Each found source is checked once, so that
 * the administrator sees which secrets the installation must provide and which of them are
 * missing. No secret is kept, reported or put into a message.
 * </p>
 */
public final class SecretReferences {

	/**
	 * Separator of the property names in the {@link SecretReference#getPath() path} of a secret.
	 */
	public static final String PATH_SEPARATOR = " / ";

	/**
	 * Separator of the non-secret properties in the description of a secret store.
	 */
	private static final String DETAIL_SEPARATOR = ", ";

	private static final char KEY_START = '[';

	private static final char KEY_END = ']';

	private static final char ASSIGNMENT = '=';

	private SecretReferences() {
		// Utility class.
	}

	/**
	 * The secrets in the configurations of all running services, each checked now.
	 *
	 * <p>
	 * A service is searched in the configuration it was started with.
	 * </p>
	 *
	 * @return The references, by service and by their position in the service configuration.
	 */
	public static List<SecretReference> ofServices() {
		List<BasicRuntimeModule<?>> modules = new ArrayList<>(ModuleUtil.getAllModules());
		modules.sort((m1, m2) -> m1.getImplementation().getName().compareTo(m2.getImplementation().getName()));

		List<SecretReference> result = new ArrayList<>();
		for (BasicRuntimeModule<?> module : modules) {
			if (!module.isActive()) {
				continue;
			}
			ConfigurationItem config = startedConfiguration(module);
			if (config != null) {
				result.addAll(of(module, config));
			}
		}
		return result;
	}

	/**
	 * The configuration the given running service was started with.
	 */
	private static ConfigurationItem startedConfiguration(BasicRuntimeModule<?> module) {
		Object service = module.getImplementationInstance();
		if (service instanceof ConfiguredManagedClass<?> configured) {
			return configured.getConfig();
		}
		try {
			return ApplicationConfig.getInstance().getServiceConfiguration(module.getImplementation());
		} catch (ConfigurationException ex) {
			return null;
		}
	}

	/**
	 * The secrets in the given configuration, each checked now.
	 *
	 * @param service
	 *        The service the configuration belongs to, <code>null</code> if it belongs to no
	 *        service.
	 * @param config
	 *        The configuration to search.
	 * @return The references in the order of their properties in the configuration.
	 */
	public static List<SecretReference> of(BasicRuntimeModule<?> service, ConfigurationItem config) {
		Collector collector = new Collector(service);
		collector.collect(config);
		return collector.getResult();
	}

	/**
	 * The label of the given implementation class of a secret source or store.
	 */
	public static ResKey kindLabel(Class<?> kind) {
		return ResKey.fallback(ResKey.forClass(kind), ResKey.text(kind.getSimpleName()));
	}

	/**
	 * Creates the reference for the secret source configured at the given path, and checks it.
	 */
	static SecretReference reference(BasicRuntimeModule<?> service, String path,
			PolymorphicConfiguration<? extends SecretSource> config) {
		Class<?> kind = config.getImplementationClass();
		if (kind == null) {
			kind = SecretSource.class;
		}

		SecretSource source;
		try {
			source = TypedConfigUtil.createInstance(config);
		} catch (RuntimeException ex) {
			// The message of the problem is not reported, it could quote the configuration.
			return new SecretReference(service, path, kind, null, null, SecretReference.Status.ERROR,
				I18NConstants.ERROR_SECRET_SOURCE_INVALID);
		}

		ResKey resolvedBy = config instanceof NamedSecretSource.Config named ? resolvedBy(named.getName()) : null;

		try {
			if (source.lookup() != null) {
				return available(service, path, kind, source, resolvedBy);
			}
			try {
				if (source.getSecret() != null) {
					// Provided in the meantime.
					return available(service, path, kind, source, resolvedBy);
				}
				return new SecretReference(service, path, kind, source.reference(), resolvedBy,
					SecretReference.Status.MISSING, null);
			} catch (I18NRuntimeException ex) {
				return new SecretReference(service, path, kind, source.reference(), resolvedBy,
					SecretReference.Status.MISSING, ex.getErrorKey());
			}
		} catch (I18NRuntimeException ex) {
			return new SecretReference(service, path, kind, source.reference(), resolvedBy,
				SecretReference.Status.ERROR, ex.getErrorKey());
		} catch (RuntimeException ex) {
			// The message of the problem is not reported, it could quote the secret.
			return new SecretReference(service, path, kind, source.reference(), resolvedBy,
				SecretReference.Status.ERROR,
				I18NConstants.ERROR_SECRET_CHECK_FAILED__PROBLEM.fill(ex.getClass().getName()));
		}
	}

	private static SecretReference available(BasicRuntimeModule<?> service, String path, Class<?> kind,
			SecretSource source, ResKey resolvedBy) {
		return new SecretReference(service, path, kind, source.reference(), resolvedBy,
			SecretReference.Status.AVAILABLE, null);
	}

	/**
	 * What delivers the secret with the given name in the running {@link SecretStoreService}.
	 */
	private static ResKey resolvedBy(String name) {
		if (!SecretStoreService.Module.INSTANCE.isActive()) {
			return null;
		}
		SecretStoreService storeService = SecretStoreService.Module.INSTANCE.getImplementationInstance();
		SecretSource override = storeService.getOverride(name);
		if (override != null) {
			return I18NConstants.SECRET_RESOLVED_BY_OVERRIDE__SOURCE
				.fill(describe(override.getClass(), override.reference()));
		}
		PolymorphicConfiguration<? extends SecretStore> storeConfig = storeService.getConfig().getStore();
		Class<?> storeKind = storeConfig.getImplementationClass();
		if (storeKind == null) {
			storeKind = storeService.getStore().getClass();
		}
		String details = plainDetails(storeConfig);
		return describe(storeKind, details.isEmpty() ? null : ResKey.text(details));
	}

	private static ResKey describe(Class<?> kind, ResKey detail) {
		ResKey label = kindLabel(kind);
		return detail == null ? label : I18NConstants.SECRET_DESCRIPTION__KIND_DETAIL.fill(label, detail);
	}

	/**
	 * The explicitly set, non-encrypted plain properties of the given configuration, e.g. the
	 * prefix of a store or its directory.
	 */
	private static String plainDetails(ConfigurationItem config) {
		StringBuilder result = new StringBuilder();
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (property.kind() != PropertyKind.PLAIN || property.isEncrypted()
				|| PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME.equals(property.getPropertyName())
				|| !config.valueSet(property)) {
				continue;
			}
			Object value = config.value(property);
			if (value == null) {
				continue;
			}
			ConfigurationValueProvider<?> format = property.getValueProvider();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			String text = format == null ? String.valueOf(value) : ((ConfigurationValueProvider) format).getSpecification(value);
			if (result.length() > 0) {
				result.append(DETAIL_SEPARATOR);
			}
			result.append(property.getPropertyName()).append(ASSIGNMENT).append(text);
		}
		return result.toString();
	}

	/**
	 * Walks a configuration and creates a {@link SecretReference} for each secret source in it.
	 */
	private static final class Collector extends DescendingConfigurationItemVisitor {

		private final BasicRuntimeModule<?> _service;

		private final Deque<String> _path = new ArrayDeque<>();

		private final List<SecretReference> _result = new ArrayList<>();

		Collector(BasicRuntimeModule<?> service) {
			_service = service;
		}

		void collect(ConfigurationItem config) {
			handleProperties(config);
		}

		List<SecretReference> getResult() {
			return _result;
		}

		@Override
		protected void handleItemProperty(ConfigurationItem config, PropertyDescriptor property,
				ConfigurationItem itemValue) {
			if (!owned(property)) {
				return;
			}
			visit(property.getPropertyName(), itemValue);
		}

		@Override
		protected void handleListProperty(ConfigurationItem config, PropertyDescriptor property,
				List<? extends ConfigurationItem> listValue) {
			if (!owned(property)) {
				return;
			}
			visitEntries(property, listValue);
		}

		@Override
		protected void handleArrayProperty(ConfigurationItem config, PropertyDescriptor property,
				Object[] arrayValue) {
			if (!owned(property)) {
				return;
			}
			visitEntries(property, Arrays.asList(arrayValue));
		}

		@Override
		protected void handleMapProperty(ConfigurationItem config, PropertyDescriptor property,
				Map<Object, ConfigurationItem> mapValue) {
			if (!owned(property)) {
				return;
			}
			for (Map.Entry<Object, ?> entry : mapValue.entrySet()) {
				visit(entryName(property, keyText(entry.getKey())), entry.getValue());
			}
		}

		@Override
		protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
			// A plain value holds no secret source.
		}

		/**
		 * Whether the value of the given property is part of the configuration that declares it,
		 * not a reference to its container or a value computed from other properties.
		 */
		private static boolean owned(PropertyDescriptor property) {
			return !property.hasContainerAnnotation() && !property.isDerived();
		}

		private void visitEntries(PropertyDescriptor property, List<?> entries) {
			PropertyDescriptor keyProperty = property.getKeyProperty();
			int index = 0;
			for (Object entry : entries) {
				String key;
				if (keyProperty != null && entry instanceof ConfigurationItem item) {
					key = keyText(item.value(keyProperty));
				} else {
					key = Integer.toString(index);
				}
				visit(entryName(property, key), entry);
				index++;
			}
		}

		private void visit(String name, Object value) {
			if (!(value instanceof ConfigurationItem item)) {
				return;
			}
			_path.addLast(name);
			try {
				if (isSecretSource(item)) {
					@SuppressWarnings("unchecked")
					PolymorphicConfiguration<? extends SecretSource> source =
						(PolymorphicConfiguration<? extends SecretSource>) item;
					_result.add(reference(_service, String.join(PATH_SEPARATOR, _path), source));
				}
				handleProperties(item);
			} finally {
				_path.removeLast();
			}
		}

		private static boolean isSecretSource(ConfigurationItem item) {
			if (!(item instanceof PolymorphicConfiguration<?> config)) {
				return false;
			}
			Class<?> implementation = config.getImplementationClass();
			return implementation != null && SecretSource.class.isAssignableFrom(implementation);
		}

		private static String entryName(PropertyDescriptor property, String key) {
			return property.getPropertyName() + KEY_START + key + KEY_END;
		}

		private static String keyText(Object key) {
			if (key instanceof Class<?> type) {
				return type.getName();
			}
			return String.valueOf(key);
		}

	}

}
