/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.model.cache;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.model.cache.TLModelCacheEntry;
import com.top_logic.model.cache.TLModelCacheImpl;

/**
 * {@link TLModelCacheImpl} for tl-element.
 * 
 * @see ElementModelCacheService
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementModelCacheImpl extends TLModelCacheImpl {

	/** {@link ConfigurationItem} for the {@link TLModelCacheImpl}. */
	public interface Config<T extends ElementModelCacheImpl> extends TLModelCacheImpl.Config<T> {

		@Override
		@ClassDefault(ElementModelCacheImpl.class)
		Class<? extends T> getImplementationClass();

	}

	/**
	 * Creates a new {@link ElementModelCacheImpl}.
	 */
	public ElementModelCacheImpl(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected TLModelCacheEntry newLocalCacheValue() {
		return new ElementModelCacheEntry(kb());
	}

}

