/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.model.cache;

import java.util.concurrent.atomic.AtomicReference;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.cache.TLModelCacheEntry;
import com.top_logic.util.model.ModelService;

/**
 * {@link TLModelCacheEntry} for {@link ElementModelCacheImpl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementModelCacheEntry extends TLModelCacheEntry {
	
	private final AtomicReference<ModelTables> _modelTables = new AtomicReference<>();

	/**
	 * Creates a new {@link ElementModelCacheEntry}.
	 */
	protected ElementModelCacheEntry(KnowledgeBase knowledgeBase) {
		super(knowledgeBase);
	}

	@Override
	public synchronized TLModelCacheEntry copy() {
		ElementModelCacheEntry copy = new ElementModelCacheEntry(getKnowledgeBase());
		copy.initFrom(this);
		return copy;
	}
	
	@Override
	protected void initFrom(TLModelCacheEntry otherEntry) {
		super.initFrom(otherEntry);
		_modelTables.set(((ElementModelCacheEntry) otherEntry)._modelTables.get());
	}

	@Override
	public synchronized void clear() {
		super.clear();
		_modelTables.set(null);
	}

	/**
	 * The {@link ModelTables} for the {@link ModelService#getApplicationModel() application model}.
	 */
	public ModelTables getModelTables() {
		ModelTables modelTables = _modelTables.get();
		if (modelTables == null) {
			ModelTables newModelTables = new ModelTables(ModelService.getApplicationModel());
			boolean success = _modelTables.compareAndSet(null, newModelTables);
			if (!success) {
				// Changed concurrently
				modelTables = _modelTables.get();
			} else {
				modelTables = newModelTables;
			}
		}
		return modelTables;
	}

}

