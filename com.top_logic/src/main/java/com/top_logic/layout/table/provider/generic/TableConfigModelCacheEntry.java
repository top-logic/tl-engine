/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.cache.AbstractTLModelCacheEntry;

/**
 * An entry of the {@link TableConfigModelCache}.
 * <p>
 * Represents the cache for one range of revision in which the {@link TLModel} did not change.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableConfigModelCacheEntry implements AbstractTLModelCacheEntry<TableConfigModelCacheEntry> {

	private final Map<Set<TLClass>, TableConfigModelInfo> _modelInfos = new HashMap<>();

	private final TableConfigModelInfoFactory _modelInfoFactory;

	/**
	 * Creates a {@link TableConfigModelCacheEntry}.
	 * <p>
	 * Convenience variant of
	 * {@link #TableConfigModelCacheEntry(Collection, TableConfigModelInfoFactory)}.
	 * </p>
	 */
	protected TableConfigModelCacheEntry(TableConfigModelInfoFactory modelInfoFactory) {
		this(Collections.<TableConfigModelInfo> emptyList(), modelInfoFactory);
	}

	/**
	 * Creates a {@link TableConfigModelCacheEntry}.
	 */
	protected TableConfigModelCacheEntry(Collection<? extends TableConfigModelInfo> collection,
			TableConfigModelInfoFactory modelInfoFactory) {
		_modelInfoFactory = requireNonNull(modelInfoFactory);
		addAll(collection);
	}

	private synchronized void addAll(Collection<? extends TableConfigModelInfo> collection) {
		for (TableConfigModelInfo entry : collection) {
			_modelInfos.put(entry.getContentTypes(), entry);
		}
	}

	@Override
	public TableConfigModelCacheEntry copy() {
		return new TableConfigModelCacheEntry(getModelInfos(), getModelInfoFactory());
	}

	/**
	 * The {@link TableConfigModelInfo} entries.
	 * 
	 * @return A new {@link Collection} that does not need to be synchronized.
	 */
	protected synchronized Collection<TableConfigModelInfo> getModelInfos() {
		return list(_modelInfos.values());
	}

	/**
	 * The {@link TableConfigModelInfo} for the given {@link Set} of content {@link TLClass
	 * classes}.
	 * <p>
	 * If there is no cache yet, it is created and stored automatically.
	 * </p>
	 * 
	 * @return Never null.
	 */
	protected TableConfigModelInfo getModelInfo(Set<? extends TLClass> contentTypes) {
		TableConfigModelInfo result = getModelInfoInternal(contentTypes);
		if (result == null) {
			/* Calculating the entry unsynchronized and inserting it synchronized, can cause
			 * duplicate calculations, but prevents locking while calculating the infos, which
			 * prevents deadlocks caused by locks deep in the entry calculation code (for example in
			 * the KnowledgeBase). */
			result = createModelInfo(contentTypes);
			/* Don't use the set-object given as parameter as key for the new map entry, as the
			 * caller can change that afterwards, breaking the map. */
			putModelInfoInternal(result);
		}
		return result;
	}

	private synchronized TableConfigModelInfo getModelInfoInternal(Set<? extends TLClass> contentTypes) {
		return _modelInfos.get(contentTypes);
	}

	/**
	 * Create a {@link TableConfigModelInfo} for the given {@link Set} of content {@link TLClass
	 * classes}.
	 */
	protected TableConfigModelInfo createModelInfo(Set<? extends TLClass> contentTypes) {
		return getModelInfoFactory().create(contentTypes);
	}

	private synchronized TableConfigModelInfo putModelInfoInternal(TableConfigModelInfo newCache) {
		return _modelInfos.put(newCache.getContentTypes(), newCache);
	}

	@Override
	public synchronized void clear() {
		_modelInfos.clear();
	}

	/**
	 * The {@link TableConfigModelInfoFactory} to use.
	 * 
	 * @return Never null.
	 */
	protected TableConfigModelInfoFactory getModelInfoFactory() {
		return _modelInfoFactory;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("number of entries", getModelInfos().size())
			.add("factory", getModelInfoFactory())
			.build();
	}

}
