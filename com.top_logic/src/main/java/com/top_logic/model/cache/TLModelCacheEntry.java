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
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.provider.icon.IconProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.composite.CompositeStorage;
import com.top_logic.model.composite.ContainerStorage;
import com.top_logic.model.composite.LinkTable;
import com.top_logic.model.composite.SourceTable;
import com.top_logic.model.composite.TargetTable;
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

	/**
	 * <p>
	 * Mapping from a {@link TLType} to the
	 * {@link com.top_logic.model.cache.TLModelOperations.CompositionStorages} defining in which
	 * composite {@link TLReference}s store data.
	 * </p>
	 * <p>
	 * The map is complete in the following sense: If a type <code>A</code> is the target type of a
	 * composition reference, the map contains an entry for <code>A</code>. The map may not contain
	 * an entry for a subtype <code>B</code> of <code>A</code> whereas the corresponding reference
	 * may contain elements of type <code>B</code>.
	 * </p>
	 * 
	 * <p>
	 * The map contains the {@link ObjectBranchId} of a type instead of its {@link ObjectKey},
	 * because it is expected that this will not change over time.
	 * </p>
	 */
	private volatile Map<ObjectBranchId, CompositionStorages> _compositionStorages = null;

	private final ConcurrentMap<TLType, IconProvider> _iconProviderByType = new ConcurrentHashMap<>();

	private final ConcurrentMap<TLStructuredTypePart, ImmutableSet<TLStructuredTypePart>> _concreteOverridesByPart =
		new ConcurrentHashMap<>();

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
		_concreteOverridesByPart.putAll(otherEntry._concreteOverridesByPart);

		Set<TLClass> otherGlobalAppModelClasses = otherEntry._globalAppModelClasses;
		_globalAppModelClasses = otherGlobalAppModelClasses == null ? null : immutableCopy(otherGlobalAppModelClasses);

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
		return computeIfAbsent(_superClasses, tlClass, key -> immutableCopy(computeSuperClasses(key)));
	}

	@Override
	public synchronized Set<TLClass> getSubClasses(TLClass tlClass) {
		if (!canBeCached(tlClass)) {
			return computeSubClasses(tlClass);
		}
		return computeIfAbsent(_subClasses, tlClass, key -> immutableCopy(computeSubClasses(key)));
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
			key -> immutableCopy(computeAttributesOfSubClasses(key)));
	}

	@Override
	public synchronized Set<TLClass> getGlobalClasses(TLModel tlModel) {
		if (!canModelPartBeCached(tlModel)) {
			return computeGlobalClasses(tlModel);
		}
		if (tlModel.equals(_appModel)) {
			/* Optimization for the 99% case that there is just one TLModel. */
			if (_globalAppModelClasses == null) {
				_globalAppModelClasses = immutableCopy(computeGlobalClasses(_appModel));
			}
			return _globalAppModelClasses;
		}
		if (_globalClasses == null) {
			_globalClasses = map();
		}
		return computeIfAbsent(_globalClasses, tlModel, key -> immutableCopy(computeGlobalClasses(key)));
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

	private static <E> ImmutableSet<E> immutableCopy(Set<E> set) {
		switch (set.size()) {
			case 0:
				return ImmutableSet.of();
			case 1:
				return ImmutableSet.of(set.iterator().next());
			default:
				return ImmutableSet.copyOf(set);
		}
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

	@Override
	public CompositionStorages getCompositionStorages(TLClass type) {
		if (!WrapperHistoryUtils.isCurrent(type)) {
			if (WrapperHistoryUtils.getCurrent(type) == null) {
				// type is deleted in the meanwhile. Search for result
				return super.getCompositionStorages(type);
			}
		}
		Map<ObjectBranchId, CompositionStorages> storages = _compositionStorages;
		if (_compositionStorages == null) {
			storages = compositionStoragesByTargetType();
			_compositionStorages = storages;
		}
		CompositionStoragesImpl result = new CompositionStoragesImpl();
		for (TLClass c : getSuperClasses(type)) {
			CompositionStorages storage = storages.get(ObjectBranchId.toObjectBranchId(c.tId()));
			if (storage == null) {
				continue;
			}
			result.storedInLink().addAll(storage.storedInLink());
			result.storedInSource().addAll(storage.storedInSource());
			result.storedInTarget().addAll(storage.storedInTarget());
		}
		return result;
	}

	private Map<ObjectBranchId, CompositionStorages> compositionStoragesByTargetType() {
		Map<ObjectBranchId, CompositionStorages> result = new HashMap<>();

		doForAllCompositionReferences(_appModel, reference -> {
			TLType targetType = reference.getType();
			ObjectBranchId key = ObjectBranchId.toObjectBranchId(targetType.tId());
			
			CompositionStorages storages = result.computeIfAbsent(key, k -> new CompositionStoragesImpl());

			CompositeStorage storage = (CompositeStorage) reference.getStorageImplementation();
			ContainerStorage container = storage.getContainerStorage(reference);
			if (container instanceof SourceTable) {
				storages.storedInSource().add((SourceTable) container);
			} else if (container instanceof TargetTable) {
				storages.storedInTarget().add((TargetTable) container);
			} else {
				storages.storedInLink().add((LinkTable) container);
			}
		});
		return result;
	}

	@Override
	public <T extends TLStructuredTypePart> Set<T> getDirectConcreteOverrides(T part) {
		if (!canModelPartBeCached(part)) {
			return super.getDirectConcreteOverrides(part);
		}
		@SuppressWarnings("unchecked")
		Set<T> typeSafe = (Set<T>) computeIfAbsent(_concreteOverridesByPart, part,
			key -> immutableCopy(super.getDirectConcreteOverrides(key)));
		return typeSafe;
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
