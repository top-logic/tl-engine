/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.db2.KBCache;

/**
 * A {@link KBCache} based {@link TLModelCache}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLModelCacheImpl extends AbstractTLModelCache<TLModelCacheEntry, TLModelCacheImpl.Config<?>>
		implements TLModelCache {

	/** {@link ConfigurationItem} for the {@link TLModelCacheImpl}. */
	public interface Config<T extends TLModelCacheImpl> extends AbstractTLModelCache.Config<T> {

		@Override
		@ClassDefault(TLModelCacheImpl.class)
		Class<? extends T> getImplementationClass();

	}

	/** {@link TypedConfiguration} constructor for {@link TLModelCacheImpl}. */
	@CalledByReflection
	public TLModelCacheImpl(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected TLModelCacheEntry newLocalCacheValue() {
		return new TLModelCacheEntry(kb());
	}

}
