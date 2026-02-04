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

	private static ElementModelCacheService implementationInstance() {
		return (ElementModelCacheService) Module.INSTANCE.getImplementationInstance();
	}

}

