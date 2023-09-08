/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.keystorages.KeyStorageChecker;
import com.top_logic.basic.keystorages.KeyStorageCheckerRegistry;
import com.top_logic.basic.util.Utils;

/**
 * Base class for a configurable registry of values associated to a key.
 * 
 * <p>
 * The values for a {@link KeyStorage} can be configured through the global {@link Configuration}.
 * The storage provides lookup of values associated to a {@link TypeKeyProvider.Key} in the storage.
 * If there is no value defined for some key, there is a multi-level fall-back mechanism to find a
 * best-matching default.
 * </p>
 * 
 * <p>
 * For an example usage see: <code>LabelProviderRegistry</code>.
 * </p>
 * 
 * @since 5.7.5
 * 
 * @see TypeKeyProvider.Key for the default lookup mechanism.
 * @see Configuration
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class KeyStorage<T> extends AbstractReloadable {
    
    private static final String PLAIN_KEY_PREFIX = "plain:";

	/**
	 * A plain {@link TypeKeyProvider.Key} implementation that are directly specified by their
	 * name and that have no default but the global default.
	 */
	public static final class PlainKey implements TypeKeyProvider.Key {
		private final String localConfigurationName;

		/**
		 * Create a new {@link KeyStorage.PlainKey} with the given local configuration
		 * name.
		 * 
		 * @param localConfigurationName
		 *     The name to look up in the {@link Configuration} (prefixed
		 *     with {@link KeyStorage#PLAIN_KEY_PREFIX}.
		 */
		public PlainKey(String localConfigurationName) {
			this.localConfigurationName = localConfigurationName;
		}

		@Override
		public String getConfigurationName() {
			return PLAIN_KEY_PREFIX + localConfigurationName;
		}

		@Override
		public TypeKeyProvider.Key getDefaultKey() {
			// No other default than the global default.
			return null;
		}
		
		@Override
		public boolean isHistoric() {
			return false;
		}
	}
	
    /**
	 * Cache for the {link {@link TypeKeyProvider.Key#getConfigurationName()} to
	 * entry mapping of this SingletonRegistry.
	 */
    private final Map<String, T> entryForKey = new HashMap<>();

	/**
	 * Creates a new {@link KeyStorage}.
	 */
    public KeyStorage() {
    }

	@Override
	protected void checkConfiguration(IterableConfiguration config) {
		super.checkConfiguration(config);
		for (Object untypedKey : config.getNames()) {
			checkEntry(untypedKey, config);
		}
	}

	private void checkEntry(Object untypedKey, Configuration config) {
		if (!(untypedKey instanceof String)) {
			errorNonStringKey(untypedKey);
			return;
		}
		String key = (String) untypedKey;
		String value = config.getValue(key);
		checkEntry(key, value);
	}

	private void errorNonStringKey(Object untypedKey) {
		String message = "Key has to be a String but is: " + Utils.debug(untypedKey);
		Logger.error(message, new ClassCastException(message), KeyStorage.class);
	}

	private void checkEntry(String key, String value) {
		for (KeyStorageChecker checker : getCheckers()) {
			checkEntry(key, value, checker);
		}
	}

	/**
	 * The {@link KeyStorageChecker} for checking the entries.
	 */
	protected List<KeyStorageChecker> getCheckers() {
		return KeyStorageCheckerRegistry.getInstance().getCheckers();
	}

	private void checkEntry(String key, String value, KeyStorageChecker checker) {
		Exception problem = checker.check(key, value);
		if (problem != null) {
			errorInvalidEntry(key, value, problem);
		}
	}

	private void errorInvalidEntry(String key, String value, Exception problem) {
		String message = "Invalid " + getClass().getSimpleName() + " entry: "
			+ " Key: '" + key + "'. Value: '" + value + "'. Cause: " + problem.getMessage();
		Logger.error(message, new RuntimeException(message, problem), KeyStorage.class);
	}

	/**
	 * Lookup an entry matching the given key in this storage.
	 * 
	 * <p>
	 * If no entry is found in the {@link #entryForKey cache}, a new entry is
	 * created by a call to {@link #createEntry(TypeKeyProvider.Key)}.
	 * </p>
	 * 
	 * @param  aKey The key to use for lookup.
	 * @return The entry associated to the given key.
	 */
    protected synchronized T lookupEntry(TypeKeyProvider.Key aKey) {
    	// Lookup entry in the cache.
    	T result = entryForKey.get(aKey.getConfigurationName());

    	if (result == null) {
    		// Entry not yet instantiated. Look up the entry class from
			// the configuration.
        	result = createEntry(aKey);

			// Remember entry for key.
			entryForKey.put(aKey.getConfigurationName(), result);
    	}
    	
    	return result;
    }

    /**
	 * Read the configuration for the given key and create a new entry, or follow the fallback chain
	 * of the given key, if no configuration value is provided.
	 * 
	 * <p>
	 * If the configuration provides a value for the given key, the method {@link #newEntry(String, String)}
	 * is called to create a new entry for this {@link KeyStorage} associated to the given key. If
	 * the configuration does not provide a value, the value associated with the
	 * {@link TypeKeyProvider.Key#getDefaultKey() default key} is returned, or
	 * {@link #getGlobalDefault()}, if there is no default for the given key.
	 * </p>
	 * 
	 * @param key
	 *        The key for which a new entry is requested.
	 * @return The new entry.
	 */
	protected T createEntry(TypeKeyProvider.Key key) {
        String configurationName = key.getConfigurationName();
		String configurationValue = getConfiguration().getValue(configurationName);
		
		if (!isDeactivated(configurationValue)) {
			try {
				return newEntry(configurationName, configurationValue);
			} catch (Exception ex) {
				Logger.error("Cannot create entry for configuration entry '" + configurationValue + "' for key  '"
					+ configurationName + "'", ex, KeyStorage.class);
			}
		}

		// There is no configuration entry for the given key. Try to find a
		// best-matching default.
		TypeKeyProvider.Key defaultKey = key.getDefaultKey();
		if (defaultKey != null) {
			// Recursively look up the fallback. This assures that all
			// fallback entries are entered into the cache.
			return lookupEntry(defaultKey);
		} else {
			// Fallback, if nothing is configured.
			return getGlobalDefault();
		}
	}

	/**
	 * Whether the entry is deactivated.
	 * <p>
	 * It is necessary to deactivate entries, for example when a factory-xml is overridden and a
	 * {@link KeyStorage} contains entries for MetaElements that don't exist in the overridden
	 * factory-xml. The <code>com.top_logic.element.meta.ElementTypeKeyResolvabilityChecker</code>
	 * would report an error for those, if they would not be deactivated.
	 * </p>
	 */
	public static boolean isDeactivated(String configValue) {
		return isEmpty(configValue);
	}

	/**
	 * Hook for creating a new entry for the given value provided by the configuration.
	 * 
	 * <p>
	 * The default implementation is to use the given configuration value as class name and lookup
	 * the instantiate as a singleton (by looking up a <code>INSTANCE</code> field or calling a
	 * <code>getInstance()</code> method) or create new instance, if the concrete class is not a
	 * singleton implementation.
	 * </p>
	 * 
	 * @param propertyName
	 *        The name of the property with the given configuration value.
	 * @param configurationValue
	 *        The string from the configuration that describes the new entry to be created.
	 * 
	 * @return The newly created entry.
	 */
	protected abstract T newEntry(String propertyName, String configurationValue) throws Exception;


	/**
	 * The global default entry to use, if the fallback chain
	 * {@link TypeKeyProvider.Key#getDefaultKey()} does not provide a default.
	 */
	protected abstract T getGlobalDefault();

	/**
	 * Drop all cached mappings. These are re-populated on demand.
	 */
	@Override
	public synchronized boolean reload() {
		boolean result = super.reload();
		
		entryForKey.clear();
		
		return result;
	}

}
