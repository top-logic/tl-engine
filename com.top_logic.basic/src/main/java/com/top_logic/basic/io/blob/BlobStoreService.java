/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service holding the named {@link BlobStore}s of the application.
 *
 * <p>
 * One of the configured stores is the default store, used whenever content is stored without
 * naming a store. The stores are closed when the service is shut down.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Blob stores")
public class BlobStoreService extends ConfiguredManagedClass<BlobStoreService.Config<?>> {

	/**
	 * The name of the default store, if not configured otherwise.
	 */
	public static final String DEFAULT_STORE_NAME = "default";

	/**
	 * Configuration of the {@link BlobStoreService}.
	 */
	public interface Config<I extends BlobStoreService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration name of {@link #getStores()}.
		 */
		String STORES = "stores";

		/**
		 * Configuration name of {@link #getDefaultStore()}.
		 */
		String DEFAULT_STORE = "default-store";

		/**
		 * Tag name of an entry in {@link #getStores()}.
		 */
		String STORE = "store";

		/**
		 * The configured stores, indexed by their names.
		 */
		@Name(STORES)
		@Key(NamedConfiguration.NAME_ATTRIBUTE)
		@EntryTag(STORE)
		Map<String, BlobStore.Config<?>> getStores();

		/**
		 * The name of the store that is used when no store is named explicitly.
		 *
		 * <p>
		 * The value must be the name of one of the configured stores.
		 * </p>
		 */
		@Name(DEFAULT_STORE)
		@StringDefault(DEFAULT_STORE_NAME)
		String getDefaultStore();

		/**
		 * @see #getDefaultStore()
		 */
		void setDefaultStore(String value);

	}

	private final Map<String, BlobStore> _stores;

	private final BlobStore _defaultStore;

	/**
	 * Creates a {@link BlobStoreService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BlobStoreService(InstantiationContext context, Config<?> config) {
		super(context, config);

		Map<String, BlobStore> stores = new LinkedHashMap<>();
		for (BlobStore.Config<?> storeConfig : config.getStores().values()) {
			BlobStore store = context.getInstance(storeConfig);
			if (store != null) {
				stores.put(storeConfig.getName(), store);
			}
		}
		_stores = Collections.unmodifiableMap(stores);

		String defaultName = config.getDefaultStore();
		_defaultStore = _stores.get(defaultName);
		if (_defaultStore == null) {
			context.error("The default blob store '" + defaultName + "' is not configured, configured stores: "
				+ _stores.keySet());
		}
	}

	/**
	 * The store that is used when no store is named explicitly.
	 */
	public BlobStore getDefaultStore() {
		return _defaultStore;
	}

	/**
	 * Looks up a configured store.
	 *
	 * @param name
	 *        The name of the store. <code>null</code> or the empty string means the
	 *        {@link #getDefaultStore() default store}.
	 * @return The store with the given name.
	 * @throws IllegalArgumentException
	 *         If no store with the given name is configured.
	 */
	public BlobStore getStore(String name) throws IllegalArgumentException {
		if (StringServices.isEmpty(name)) {
			return getDefaultStore();
		}
		BlobStore result = _stores.get(name);
		if (result == null) {
			throw new IllegalArgumentException(
				"Blob store '" + name + "' is not configured, configured stores: " + _stores.keySet());
		}
		return result;
	}

	/**
	 * All configured stores, indexed by their names.
	 */
	public Map<String, BlobStore> getStores() {
		return _stores;
	}

	/**
	 * Closes all configured stores.
	 *
	 * <p>
	 * A store that fails to close is logged; the remaining stores are closed nevertheless.
	 * </p>
	 */
	@Override
	protected void shutDown() {
		for (BlobStore store : _stores.values()) {
			try {
				store.close();
			} catch (IOException | RuntimeException ex) {
				Logger.error("Closing blob store '" + store.getName() + "' failed.", ex, BlobStoreService.class);
			}
		}
		super.shutDown();
	}

	/**
	 * The {@link BlobStoreService} of the application.
	 */
	public static BlobStoreService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link TypedRuntimeModule} of the {@link BlobStoreService}.
	 */
	public static final class Module extends TypedRuntimeModule<BlobStoreService> {

		/**
		 * Singleton module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<BlobStoreService> getImplementation() {
			return BlobStoreService.class;
		}

	}

}
