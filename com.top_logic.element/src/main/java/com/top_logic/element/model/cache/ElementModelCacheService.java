/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.model.cache;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.model.cache.TLModelCache;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.util.model.ModelService;

/**
 * {@link TLModelCacheService} accessing objects from tl-element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementModelCacheService extends TLModelCacheService {

	/** {@link ConfigurationItem} for the {@link TLModelCacheService}. */
	public interface Config extends TLModelCacheService.Config {

		@Override
		@ImplementationClassDefault(ElementModelCacheImpl.class)
		PolymorphicConfiguration<TLModelCache> getCache();
	}

	/**
	 * Creates a new {@link ElementModelCacheService}.
	 */
	public ElementModelCacheService(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * The model tables for the application model.
	 */
	public static ModelTables getModelTables() {
		if (!Module.INSTANCE.isActive()) {
			throw new IllegalStateException(TLModelCacheService.class.getName() + " not active!");
		}
		ElementModelCacheEntry cacheValue = (ElementModelCacheEntry) implementationInstance().getCache().getValue();
		return cacheValue.getModelTables();
	}

	/**
	 * The model tables for the application model, also when this service is not active.
	 * 
	 * <p>
	 * When this service is active, the cached {@link #getModelTables()} are returned. Otherwise,
	 * the model tables are computed from the current application model.
	 * </p>
	 */
	public static ModelTables getApplicationModelTables() {
		if (Module.INSTANCE.isActive()) {
			return getModelTables();
		}
		return new ModelTables(ModelService.getApplicationModel());
	}

	private static ElementModelCacheService implementationInstance() {
		return (ElementModelCacheService) Module.INSTANCE.getImplementationInstance();
	}

}

