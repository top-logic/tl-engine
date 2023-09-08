/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

import static com.top_logic.basic.col.map.MultiMaps.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Collections.*;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.commons.collections4.map.ListOrderedMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.provider.icon.IconProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.internal.PersistentModelPart;
import com.top_logic.util.model.ModelService;

/**
 * An entry of the {@link TLModelCacheImpl}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLModelCacheEntry extends TLModelOperations implements AbstractTLModelCacheEntry<TLModelCacheEntry> {

	private final Map<TLClass, ImmutableSet<TLClass>> _superClasses = map();

	private final Map<TLClass, ImmutableSet<TLClass>> _subClasses = map();

	private final Map<TLClass, ImmutableMap<String, Set<TLClass>>> _potentialTables = map();

	private final Map<TLClass, ListOrderedMap<String, TLStructuredTypePart>> _allAttributes = map();

	private final Map<TLClass, ImmutableSet<TLClassPart>> _attributesOfSubClasses = map();

	private final ConcurrentMap<TLType, IconProvider> _iconProviderByType = new ConcurrentHashMap<>();

	/** Cached for performance. */
	private final TLModel _appModel = ModelService.getApplicationModel();

	/**
	 * Optimization for the default {@link #_appModel application model}.
	 * <p>
	 * In 99% of the cases, there is just one {@link TLModel}. This is an optimization for these
	 * 99%. The {@link #_globalClasses generic approach} works for multiple {@link TLModel}s but has
	 * to indirect everything through a {@link Map}.
	 * </p>
	 */
	private ImmutableSet<TLClass> _globalAppModelClasses;

	/**
	 * This is only used when there is more than one persistent {@link TLModel}, which happens in
	 * less than 1% of the cases. Therefore, this variable is not initialized in the constructor. It
	 * would be wasted in 99% of the cases.
	 */
	private Map<TLModel, ImmutableSet<TLClass>> _globalClasses;

	private final KnowledgeBase _knowledgeBase;

	TLModelCacheEntry(KnowledgeBase knowledgeBase) {
		_knowledgeBase = knowledgeBase;
	}

	private TLModelCacheEntry(TLModelCacheEntry otherEntry) {
		_knowledgeBase = otherEntry._knowledgeBase;
		/* The unsynchronized access to the fields of the other entry is correct here, as the caller
		 * (the copy() method) is synchronized on the otherEntry. And just reusing the Map values of
		 * the other entry without copying them, too, is correct, as they are immutable and can
		 * therefore be safely shared. */
		_superClasses.putAll(otherEntry._superClasses);
		_subClasses.putAll(otherEntry._subClasses);
		_potentialTables.putAll(otherEntry._potentialTables);
		_allAttributes.putAll(otherEntry._allAttributes);
		_attributesOfSubClasses.putAll(otherEntry._attributesOfSubClasses);
		_iconProviderByType.putAll(otherEntry._iconProviderByType);

		Set<TLClass> otherGlobalAppModelClasses = otherEntry._globalAppModelClasses;
		_globalAppModelClasses = otherGlobalAppModelClasses == null ? null : ImmutableSet.copyOf(otherGlobalAppModelClasses);

		_globalClasses = (otherEntry._globalClasses == null) ? null : map(otherEntry._globalClasses);
	}

	@Override
	public synchronized TLModelCacheEntry copy() {
		return new TLModelCacheEntry(this);
	}

	@Override
	public synchronized Set<TLClass> getSuperClasses(TLClass tlClass) {
		if (!canBeCached(tlClass)) {
			return computeSuperClasses(tlClass);
		}
		return computeIfAbsent(_superClasses, tlClass, key -> ImmutableSet.copyOf(computeSuperClasses(key)));
	}

	@Override
	public synchronized Set<TLClass> getSubClasses(TLClass tlClass) {
		if (!canBeCached(tlClass)) {
			return computeSubClasses(tlClass);
		}
		return computeIfAbsent(_subClasses, tlClass, key -> ImmutableSet.copyOf(computeSubClasses(key)));
	}

	@Override
	public synchronized Map<String, Set<TLClass>> getPotentialTables(TLClass tlClass) {
		if (!canBeCached(tlClass)) {
			return computePotentialTables(tlClass);
		}
		return computeIfAbsent(_potentialTables, tlClass, key -> unmodifiableMultiMap(computePotentialTables(key)));
	}

	@Override
	public synchronized TLStructuredTypePart getAttribute(TLClass tlClass, String name) {
		if (!canBeCached(tlClass)) {
			return super.getAttribute(tlClass, name);
		}
		return partMap(tlClass).get(name);
	}

	@Override
	public synchronized List<? extends TLStructuredTypePart> getAllAttributes(TLClass tlClass) {
		if (!canBeCached(tlClass)) {
			return super.getAllAttributes(tlClass);
		}
		/* Here no ImmutableList is used, as a Map is needed for getAttribute(TLClass, String), but
		 * a List is needed for this method. And there is no immutable ListOrderedMap. */
		return unmodifiableList(partMap(tlClass).valueList());
	}

	private ListOrderedMap<String, TLStructuredTypePart> partMap(TLClass tlClass) {
		ListOrderedMap<String, TLStructuredTypePart> cacheEntry = _allAttributes.get(tlClass);
		if (cacheEntry != null) {
			return cacheEntry;
		}

		ListOrderedMap<String, TLStructuredTypePart> newEntry = computeAllAttributes(tlClass);
		return putIfAbsent(_allAttributes, tlClass, newEntry);
	}

	private static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
		assert value != null;

		V formerlyValue = map.putIfAbsent(key, value);
		if (formerlyValue == null) {
			return value;
		} else {
			return formerlyValue;
		}
	}

	@Override
	public synchronized Set<TLClassPart> getAttributesOfSubClasses(TLClass tlClass) {
		if (!canBeCached(tlClass)) {
			return computeAttributesOfSubClasses(tlClass);
		}
		return computeIfAbsent(_attributesOfSubClasses, tlClass,
			key -> ImmutableSet.copyOf(computeAttributesOfSubClasses(key)));
	}

	@Override
	public synchronized Set<TLClass> getGlobalClasses(TLModel tlModel) {
		if (!canModelPartBeCached(tlModel)) {
			return computeGlobalClasses(tlModel);
		}
		if (tlModel.equals(_appModel)) {
			/* Optimization for the 99% case that there is just one TLModel. */
			if (_globalAppModelClasses == null) {
				_globalAppModelClasses = ImmutableSet.copyOf(computeGlobalClasses(_appModel));
			}
			return _globalAppModelClasses;
		}
		if (_globalClasses == null) {
			_globalClasses = map();
		}
		return computeIfAbsent(_globalClasses, tlModel, key -> ImmutableSet.copyOf(computeGlobalClasses(key)));
	}

	/**
	 * Alternative to {@link HashMap#computeIfAbsent(Object, Function)} that allows recursive
	 * functions for value computation (which seems to be disallowed in certain JDK implementations
	 * and results in a {@link ConcurrentModificationException}).
	 */
	private static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<K, V> fun) {
		V result = map.get(key);
		if (result == null) {
			result = fun.apply(key);
			map.put(key, result);
		}
		return result;
	}

	/** Whether data about the given {@link TLClass} can be cached. */
	private boolean canBeCached(TLClass tlClass) {
		if (!canModelPartBeCached(tlClass)) {
			return false;
		}
		/* 1. There can be too many local classes for the cache. They could increase its size by a
		 * large factor, which would cause the system to run out of memory. Therefore, local classes
		 * are excluded. */
		/* 2. This check has to happen AFTER the other checks, as this method is very expensive for
		 * a TLClass that cannot be cached. */
		return isGlobal(tlClass);
	}

	/**
	 * This method must only be called for a {@link TLClass} that
	 * {@link #canModelPartBeCached(TLModelPart) can be cached}.
	 * <p>
	 * Otherwise, it will be extremely expensive.
	 * </p>
	 */
	private boolean isGlobal(TLClass tlClass) {
		return getGlobalClasses(tlClass.getModel()).contains(tlClass);
	}

	@Override
	public IconProvider getIconProvider(TLType type) {
		IconProvider cachedResult = _iconProviderByType.get(type);
		if (cachedResult != null) {
			return cachedResult;
		}
		IconProvider computedResult = super.getIconProvider(type);
		if (!canModelPartBeCached(type)) {
			return computedResult;
		}
		return MapUtil.putIfAbsent(_iconProviderByType, type, computedResult);
	}

	/** Whether data about the given {@link TLModelPart} can be cached. */
	private boolean canModelPartBeCached(TLModelPart modelPart) {
		if (!(modelPart instanceof PersistentModelPart)) {
			/* The cache is only informed about changes of persistent TLModelParts. Therefore, all
			 * other TLModelParts have to be excluded. */
			return false;
		}
		if (!WrapperHistoryUtils.isCurrent(modelPart)) {
			/* Historic TLModelParts must not be cached. There can be too many historic versions for
			 * each object. They could increase the size of the cache by a large factor, which would
			 * drive the system out of memory. Therefore, they are excluded from the cache. */
			return false;
		}
		/* The cache is only informed about changes from ITS KnowledgeBase. Therefore, it can only
		 * update itself or become invalid, if the TLModelParts are from ITS KnowledgeBase. Objects
		 * from other KnowledgeBases are therefore excluded. */
		return getKnowledgeBase().equals(modelPart.tKnowledgeBase());
	}

	private KnowledgeBase getKnowledgeBase() {
		return _knowledgeBase;
	}

	@Override
	public synchronized void clear() {
		_superClasses.clear();
		_subClasses.clear();
		_potentialTables.clear();
		_allAttributes.clear();
		_attributesOfSubClasses.clear();
		_iconProviderByType.clear();
		_globalAppModelClasses = null;
		_globalClasses = null;
	}

	@Override
	public synchronized String toString() {
		return new NameBuilder(this)
			.add("superClasses", _superClasses.size())
			.add("subClasses", _subClasses.size())
			.add("potentialTables", _potentialTables.size())
			.add("allAttributes", _allAttributes.size())
			.add("attributesOfSubClasses", _attributesOfSubClasses.size())
			.add("iconProviderByType", _iconProviderByType.size())
			.add("globalAppModelClasses", _globalAppModelClasses == null ? "null" : _globalAppModelClasses.size())
			.add("globalClasses", _globalClasses == null ? null : _globalClasses.size())
			.build();
	}

}
