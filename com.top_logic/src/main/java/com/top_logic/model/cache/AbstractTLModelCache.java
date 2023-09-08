/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

import java.util.Arrays;
import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.ConcatenatedIterable;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseName;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.SimpleKBCache;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;

/**
 * A {@link SimpleKBCache} for data based on the {@link TLModel}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractTLModelCache<E extends AbstractTLModelCacheEntry<E>, C extends AbstractTLModelCache.Config<?>>
		extends SimpleKBCache<E> implements ConfiguredInstance<C> {

	/** {@link ConfigurationItem} for the {@link AbstractTLModelCache}. */
	public interface Config<T extends AbstractTLModelCache<?, ?>>
			extends PolymorphicConfiguration<T>, KnowledgeBaseName {

		/** No content required. Only the type declaration itself is necessary. */

	}

	private final C _config;

	private final DBKnowledgeBase _knowledgeBase;

	/** {@link TypedConfiguration} constructor for {@link AbstractTLModelCache}. */
	@CalledByReflection
	public AbstractTLModelCache(InstantiationContext context, C config) {
		_config = config;
		KnowledgeBaseFactory kbFactory = KnowledgeBaseFactory.getInstance();
		_knowledgeBase = (DBKnowledgeBase) kbFactory.getKnowledgeBase(config.getKnowledgeBase());
		if (_knowledgeBase == null) {
			context.error("Unknown knowledge base '" + config.getKnowledgeBase() + "'.");
		}
	}

	@Override
	protected E handleEvent(E cacheValue, UpdateEvent event,
			boolean copyOnChange) {
		Iterable<KnowledgeItem> items = ConcatenatedIterable.concat(Arrays.asList(
			event.getCreatedObjects().values(),
			event.getUpdatedObjects().values(),
			event.getCachedDeletedObjects()));
		for (KnowledgeItem item : items) {
			if (isRelevantChange(item)) {
				if (copyOnChange) {
					return newLocalCacheValue();
				} else {
					cacheValue.clear();
					return cacheValue;
				}
			}
		}
		return null;
	}

	@Override
	protected void handleChanges(E cacheValue, Collection<? extends KnowledgeItem> items) {
		for (KnowledgeItem item : items) {
			if (isRelevantChange(item)) {
				cacheValue.clear();
				return;
			}
		}
	}

	@Override
	protected void handleCreation(E localCacheValue, KnowledgeItem item) {
		if (isRelevantChange(item)) {
			localCacheValue.clear();
		}
	}

	@Override
	protected void handleDeletion(E localCacheValue, KnowledgeItem item) {
		if (isRelevantChange(item)) {
			localCacheValue.clear();
		}
	}

	/** Whether a change on the given {@link KnowledgeItem} is relevant for this cache. */
	protected boolean isRelevantChange(KnowledgeItem item) {
		boolean isModelPart = item.getWrapper() instanceof TLModelPart;

		if (isModelPart) {
			return true;
		}

		if (item instanceof KnowledgeAssociation) {
			return ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS.equals(item.tTable().getName());
		}

		return false;
	}

	@Override
	protected E copy(E cacheEntry) {
		return cacheEntry.copy();
	}

	@Override
	protected DBKnowledgeBase kb() {
		return _knowledgeBase;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("knowledgeBase", kb().getName())
			.add("config", getConfig())
			.build();
	}

}
