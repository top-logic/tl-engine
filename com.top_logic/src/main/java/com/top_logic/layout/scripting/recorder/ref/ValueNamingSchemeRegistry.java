/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.HashMap;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Registry of configured {@link ValueNamingScheme}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ValueNamingSchemeRegistry extends ConfiguredManagedClass<ValueNamingSchemeRegistry.Config> {

	/**
	 * Configuration of the {@link ValueNamingSchemeRegistry}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ValueNamingSchemeRegistry> {
		/** Getter for the {@link Scheme}s. */
		@Mandatory
		List<Scheme> getSchemes();
	}

	/**
	 * Configuration of a {@link ValueNamingScheme} for a type.
	 */
	public interface Scheme extends ConfigurationItem {
		/**
		 * See {@link Scheme#getProvider}.
		 */
		String PROVIDER = "provider";

		/**
		 * See {@link Scheme#getType}.
		 */
		String TYPE = "type";

		/** Getter for {@link Scheme#PROVIDER}. */
		@Name(PROVIDER)
		@Mandatory
		PolymorphicConfiguration<ValueNamingScheme<?>> getProvider();

		/** Getter for {@link Scheme#TYPE}. */
		@Name(TYPE)
		@Mandatory
		Class<?> getType();
	}

	private static HashMap<Class<?>, ValueNamingScheme<?>> _byTargetType = new HashMap<>();

	private static HashMap<String, ValueNamingScheme<?>> _byProviderName = new HashMap<>();

	/**
	 * Creates a new {@link ValueNamingSchemeRegistry}.
	 */
	public ValueNamingSchemeRegistry(InstantiationContext context, Config config) {
		super(context, config);

		for (Scheme scheme : getConfig().getSchemes()) {
			Class<?> type = scheme.getType();
			ValueNamingScheme<?> provider = context.getInstance(scheme.getProvider());

			if (type.isInterface()) {
				Logger.error("Invalid configuration: Providers cannot be configured for interface types: '"
						+ type + "'.", ValueNamingSchemeRegistry.class);
			}

			_byProviderName.put(provider.providerName(), provider);

			if (provider.getModelClass().isAssignableFrom(type)) {
				_byTargetType.put(type, provider);
			} else {
				Logger.error("Invalid configuration: Provider '" + provider.providerName() + "' does not handle '"
					+ type.getTypeName() + "' instances.", ValueNamingSchemeRegistry.class);
			}
		}
	}

	/**
	 * The configured {@link ValueNamingScheme} for the given object type.
	 */
	// Save by construction of the internal map.
	@SuppressWarnings("unchecked")
	static <T> List<ValueNamingScheme<T>> getNameProviders(Class<? extends T> type) {
		List<ValueNamingScheme<T>> result = list();
		Class<?> searchedType = type;
		do {
			ValueNamingScheme<T> naming = (ValueNamingScheme<T>) _byTargetType.get(searchedType);

			if (naming != null) {
				result.add(naming);
			}

			searchedType = searchedType.getSuperclass();
		} while (searchedType != null);
		return result;
	}

	/**
	 * Retrieves a configured {@link ValueNamingScheme} by its {@link ValueNamingScheme#providerName()}.
	 */
	public static ValueNamingScheme<?> getNameProvider(String providerName) {
		ValueNamingScheme<?> result = _byProviderName.get(providerName);
		if (result == null) {
			throw new AssertionError("No ValueNamingScheme found for provider name '" + providerName + "'.");
		}
		return result;
	}

	/**
	 * Module for instantiation of the {@link ValueNamingSchemeRegistry}.
	 */
	public static class Module extends TypedRuntimeModule<ValueNamingSchemeRegistry> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ValueNamingSchemeRegistry> getImplementation() {
			return ValueNamingSchemeRegistry.class;
		}

	}

}
