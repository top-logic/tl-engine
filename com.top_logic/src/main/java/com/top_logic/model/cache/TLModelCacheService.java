/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

import static java.util.Objects.*;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModel;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * {@link ConfiguredManagedClass} for caching expensive queries about the {@link TLModel}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@ServiceDependencies({
	CompatibilityService.Module.class,
	PersistencyLayer.Module.class
})
@ServiceExtensionPoint(ModelService.Module.class)
public class TLModelCacheService extends ConfiguredManagedClass<TLModelCacheService.Config> {

	/** {@link ConfigurationItem} for the {@link TLModelCacheService}. */
	public interface Config extends ConfiguredManagedClass.Config<TLModelCacheService> {

		/** The name of the {@link #getCache()} property. */
		String CACHE = "cache";

		/** The {@link TLModelCache} to use. */
		@Name(CACHE)
		@NonNullable
		@ItemDefault
		@ImplementationClassDefault(TLModelCacheImpl.class)
		PolymorphicConfiguration<TLModelCache> getCache();

	}

	private final TLModelCache _cache;

	/** {@link TypedConfiguration} constructor for {@link TLModelCacheService}. */
	@CalledByReflection
	public TLModelCacheService(InstantiationContext context, Config config) {
		super(context, config);
		_cache = requireNonNull(context.getInstance(config.getCache()));
	}

	/**
	 * The configured {@link TLModelCache}.
	 * 
	 * @return Never null.
	 */
	protected TLModelCache getCache() {
		return _cache;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("config", getConfig())
			.add("cache", getCache().toString())
			.build();
	}

	/**
	 * Convenience getter for direct access to the cached values.
	 * 
	 * @return Never null.
	 */
	public static TLModelOperations getOperations() {
		if (Module.INSTANCE.isActive()) {
			return requireNonNull(getInstance().getCache().getValue());
		}
		/* If the cache is not active, the data is computed uncached. */
		return TLModelOperations.INSTANCE;
	}

	/** The singleton {@link TLModelCacheService} instance. */
	private static TLModelCacheService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/** {@link TypedRuntimeModule} of the {@link TLModelCacheService}. */
	public static final class Module extends TypedRuntimeModule<TLModelCacheService> {

		/** Singleton module instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<TLModelCacheService> getImplementation() {
			return TLModelCacheService.class;
		}

	}

}
