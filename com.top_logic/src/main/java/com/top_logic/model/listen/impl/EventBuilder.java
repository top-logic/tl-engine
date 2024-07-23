/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Utils;

/**
 * Builder for a {@link ModelChangeEvent}.
 */
public class EventBuilder {

	private final class ModelChangeEventImpl implements ModelChangeEvent {

		private final Map<TLStructuredType, List<TLStructuredType>> _typeIndex;

		private final Set<ObjectKey> _updatedKeys;

		private ModelChangeEventImpl(Map<TLStructuredType, List<TLStructuredType>> typeIndex, Set<ObjectKey> updatedKeys) {
			_typeIndex = typeIndex;
			_updatedKeys = updatedKeys;
		}

		@Override
		public ChangeType getChange(TLObject existingObject) {
			if (_updatedKeys.contains(existingObject.tId())) {
				return ChangeType.UPDATED;
			}
			if (_deletes.containsKey(existingObject.tId())) {
				return ChangeType.DELETED;
			}
			if (_creates.containsKey(existingObject.tId())) {
				return ChangeType.CREATED;
			}
			return ChangeType.NONE;
		}

		@Override
		public Stream<? extends TLObject> getUpdated() {
			return _updates.values().stream();
		}

		@Override
		public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
			return _typeIndex.getOrDefault(type, Collections.emptyList()).stream()
				.flatMap(specialization -> _updatedByType.get(specialization).stream());
		}

		@Override
		public Stream<? extends TLObject> getCreated() {
			return _creates.values().stream();
		}

		@Override
		public Stream<? extends TLObject> getCreated(TLStructuredType type) {
			return _typeIndex.getOrDefault(type, Collections.emptyList()).stream()
				.flatMap(specialization -> _createdByType.get(specialization).stream());
		}

		@Override
		public Stream<? extends TLObject> getDeleted() {
			return _deletes.values().stream();
		}

		@Override
		public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
			return _typeIndex.getOrDefault(type, Collections.emptyList()).stream()
				.flatMap(specialization -> _deletedByType.get(specialization).stream());
		}

		@Override
		public String toString() {
			NameBuilder builder = new NameBuilder(this);
			if (!_creates.isEmpty()) {
				builder.add("creates", _creates.size());
			}
			if (!_updates.isEmpty()) {
				builder.add("updates", _updates.size());
			}
			if (!_deletes.isEmpty()) {
				builder.add("deletes", _deletes.size());
			}
			return builder.build();
		}

	}

	private final ModelEventSettings _settings;

	private final Map<ObjectKey, Set<ModelListener>> _objectListeners;

	private final Map<TLStructuredType, Set<ModelListener>> _typeListeners;

	private final Set<ModelListener> _globalListeners;

	private final Map<TLStructuredType, List<TLObject>> _updatedByType = new HashMap<>();

	private final Map<TLStructuredType, List<TLObject>> _createdByType = new HashMap<>();

	private final Map<TLStructuredType, List<TLObject>> _deletedByType = new HashMap<>();

	private final Map<ObjectKey, TLObject> _creates = new HashMap<>();

	private final Set<ObjectKey> _recreates = new HashSet<>();

	private final Map<ObjectKey, TLObject> _updates = new HashMap<>();

	private final Map<ObjectKey, TLObject> _deletes = new HashMap<>();

	/**
	 * Creates an {@link EventBuilder}.
	 */
	public EventBuilder(ModelEventSettings settings, Map<ObjectKey, Set<ModelListener>> objectListeners,
			Map<TLStructuredType, Set<ModelListener>> typeListeners, Set<ModelListener> globalListeners) {
		super();

		_settings = settings;
		_objectListeners = objectListeners;
		_typeListeners = typeListeners;
		_globalListeners = globalListeners;
	}

	/**
	 * Adds changes from the given {@link UpdateEvent}.
	 */
	public void add(UpdateEvent updateEvent) {
		// Remember creations and drop deletes of newly (re-)created objects.
		handleCreations(updateEvent);

		// Collect modified object keys.
		handleUpdates(updateEvent);

		if (!updateEvent.getDeletedObjectKeys().isEmpty()) {
			long commitNumber = updateEvent.getCommitNumber();

			// Process in context where the deleted object is still accessible.
			_settings.kb().inRevision(commitNumber - 1, () -> {
				// Drop updates and creations of deleted objects.
				handleDeletions(updateEvent);

				return null;
			});
		}
	}

	private void handleUpdates(UpdateEvent event) {
		for (Entry<ObjectKey, KnowledgeItem> entry : event.getUpdatedObjects().entrySet()) {
			ObjectKey key = entry.getKey();
	
			if (_settings.isAssociationType(key.getObjectType())) {
				// Optimization: Ignore pure links.
				continue;
			}
	
			if (_creates.containsKey(key)) {
				// Drop updates to not-yet-published creates.
				continue;
			}
	
			_updates.put(key, entry.getValue().getWrapper());
		}
	
		// Referenced objects of deletions are modified objects.
		addReferenceEnds(event.getChanges().getDeletions());
	
		// Referenced objects of creations are modified objects.
		addReferenceEnds(event.getChanges().getCreations());
	
		// Referenced objects of updates are modified objects.
		addReferenceEnds(event.getChanges().getUpdates());
	}

	private void handleCreations(UpdateEvent event) {
		KnowledgeBase kb = _settings.kb();
		for (ObjectKey key : event.getCreatedObjectKeys()) {
			if (_settings.isAssociationType(key.getObjectType())) {
				// Optimization: Ignore links by type.
				continue;
			}
	
			KnowledgeItem object = kb.resolveObjectKey(key);
			_creates.put(key, object.getWrapper());
	
			// If an object was deleted and re-created later on, only the create must be published.
			TLObject recreated = _deletes.remove(key);
			if (recreated != null) {
				_recreates.add(key);
			}
		}
	}

	private void handleDeletions(UpdateEvent event) {
		for (KnowledgeItem item : event.getCachedDeletedObjects()) {
			ObjectKey key = item.tId();

			if (_settings.isAssociationType(key.getObjectType())) {
				// Optimization: Ignore pure links.
				continue;
			}

			// Cancel creation and update. Do not try to resolve object that is already deleted.
			_creates.remove(key);
			_recreates.remove(key);
			_updates.remove(key);

			TLObject object = item.getWrapper();
			_deletes.put(key, object);
			addToTypeList(_deletedByType, object);
		}
	}

	/**
	 * Adds all reference ends of the given created, deleted, or updated objects to the given
	 * collection of updated object keys.
	 * 
	 * @param changeEvents
	 *        A collection of {@link ItemEvent}s. The touched reference valued attributes are
	 *        considered.
	 */
	private void addReferenceEnds(Iterable<? extends ItemChange> changeEvents) {
		for (ItemChange change : changeEvents) {
			MetaObject linkType = change.getObjectType();

			List<? extends MOReference> referenceAttributes = MetaObjectUtils.getReferences(linkType);
			if (referenceAttributes.isEmpty()) {
				// No ends to check.
				continue;
			}

			Map<MOReference, Boolean> relevanceByReference = _settings.relevanceByReference(linkType);
			if (relevanceByReference == null) {
				// nothing defined for type: treat as nothing is configured
				relevanceByReference = Collections.emptyMap();
			}

			Map<String, Object> values = change.getValues();
			Map<String, Object> oldValues;
			if (change instanceof ItemUpdate) {
				oldValues = change.getValues();
				if (oldValues == null) {
					// ItemUpdates may not keep old values: ItemUpdate#ItemUpdate(long,
					// ObjectBranchId, boolean)
					oldValues = Collections.emptyMap();
				}
			} else {
				oldValues = Collections.emptyMap();
			}
			for (MOReference attribute : referenceAttributes) {
				Boolean isRelevant = relevanceByReference.get(attribute);
				if (Utils.isFalse(isRelevant)) {
					// reference attribute is marked as not relevant.
					continue;
				}
				String attrName = attribute.getName();
				ObjectKey formerReference = (ObjectKey) oldValues.get(attrName);
				ObjectKey newReference = (ObjectKey) values.get(attrName);

				if (formerReference != null) {
					addEndKey(formerReference);
				}
				if (newReference != null) {
					addEndKey(newReference);
				}
			}
		}
	}

	private void addEndKey(ObjectKey key) {
		// Created objects are not also updated.
		if (_creates.containsKey(key)) {
			// Object was created in the same event, it must not be marked as updated.
			return;
		}
		if (_deletes.containsKey(key)) {
			// Object was deleted in the same event, it must not be marked as updated.
			return;
		}
		KnowledgeItem object = _settings.kb().resolveCachedObjectKey(key);
		if (object != null) {
			_updates.put(key, object.getWrapper());
		}
	}

	/**
	 * Adds the types and all their super-types of all given items to the given set.
	 */
	private static void indexItems(Map<TLStructuredType, List<TLObject>> objectsByType,
			Collection<TLObject> items) {
		for (TLObject item : items) {
			addToTypeList(objectsByType, item);
		}
	}

	/**
	 * Adds the given type and all of it's super-types to the given set.
	 */
	private static void addToTypeList(Map<TLStructuredType, List<TLObject>> objectsByType, TLObject object) {
		TLStructuredType type = object.tType();

		// Filter out legacy object without type.
		if (type != null) {
			List<TLObject> objects = objectsByType.computeIfAbsent(type, x -> new ArrayList<>());
			objects.add(object);
		}
	}

	/**
	 * Forwards the built {@link ModelChangeEvent} to relevant listeners.
	 * 
	 * @return Whether a listener was triggered.
	 */
	public boolean notifyListeners() {
		indexItems(_updatedByType, _updates.values());
		indexItems(_createdByType, _creates.values());

		Set<TLStructuredType> touchedTypes = collectTouchedTypes();
		Collection<TLStructuredType> deletedTypes = findDeletedObjects(touchedTypes);
		touchedTypes.removeAll(deletedTypes);
		Set<ModelListener> listeners = new HashSet<>(_globalListeners);

		/* All touched sub-types (reflexive, transitive) of the key type. */
		Map<TLStructuredType, List<TLStructuredType>> typeIndex = new HashMap<>();
		for (TLStructuredType touchedType : touchedTypes) {
			computeListenersAndIndex(typeIndex, listeners, touchedType);
		}

		addObjectListeners(listeners);

		if (listeners.isEmpty()) {
			return false;
		}
		notifyListeners(typeIndex, listeners);
		return true;
	}

	private Set<TLStructuredType> collectTouchedTypes() {
		Set<TLStructuredType> touchedTypes = new HashSet<>();
		touchedTypes.addAll(_updatedByType.keySet());
		touchedTypes.addAll(_createdByType.keySet());
		touchedTypes.addAll(_deletedByType.keySet());
		return touchedTypes;
	}

	private void addObjectListeners(Set<ModelListener> listeners) {
		Set<ObjectKey> updatedKeys = _updates.keySet();
		Set<ObjectKey> deletedKeys = _deletes.keySet();

		if (!_objectListeners.isEmpty()) {
			int changeSize = updatedKeys.size() + deletedKeys.size();
			if (changeSize > _objectListeners.size()) {
				for (Entry<ObjectKey, Set<ModelListener>> entry : _objectListeners.entrySet()) {
					ObjectKey key = entry.getKey();
					if (updatedKeys.contains(key) || deletedKeys.contains(key)) {
						listeners.addAll(entry.getValue());
					}
				}
			} else {
				addListenersFor(listeners, updatedKeys);
				addListenersFor(listeners, deletedKeys);
			}
		}
	}

	private void addListenersFor(Set<ModelListener> result, Set<ObjectKey> updatedKeys) {
		for (ObjectKey key : updatedKeys) {
			Set<ModelListener> listeners = _objectListeners.get(key);
			if (listeners != null) {
				result.addAll(listeners);
			}
		}
	}

	private <E extends TLObject> Collection<E> findDeletedObjects(Collection<? extends E> objects) {
		Iterator<? extends E> typesIterator = objects.iterator();
		/* In most applications, it will almost never happen that a type is deleted while the
		 * application is running. The collection creation happens therefore lazily. */
		Collection<E> deletedTypes = null;
		while (typesIterator.hasNext()) {
			E type = typesIterator.next();
			if (!type.tValid()) {
				typesIterator.remove();
				if (deletedTypes == null) {
					deletedTypes = new ArrayList<>();
				}
				deletedTypes.add(type);
			}
		}
		return (deletedTypes == null) ? List.of() : deletedTypes;
	}

	private void computeListenersAndIndex(Map<TLStructuredType, List<TLStructuredType>> typeIndex,
			Set<ModelListener> listeners, TLStructuredType touchedType) {
		if (touchedType.getModelKind() == ModelKind.CLASS) {
			TLClass touchedClass = (TLClass) touchedType;
			Set<TLClass> superClasses = TLModelUtil.getReflexiveTransitiveGeneralizations(touchedClass);
			superClasses.forEach(superClass -> addDirect(typeIndex, listeners, superClass, touchedType));
		} else {
			addDirect(typeIndex, listeners, touchedType, touchedType);
		}
	}

	private void addDirect(Map<TLStructuredType, List<TLStructuredType>> typeIndex, Set<ModelListener> listeners,
			TLStructuredType generalization, TLStructuredType specialization) {
		addToIndex(typeIndex, generalization, specialization);
		addToListeners(listeners, generalization);
	}

	private void addToListeners(Set<ModelListener> listeners, TLStructuredType generalization) {
		Set<ModelListener> typeListeners = _typeListeners.get(generalization);
		if (typeListeners != null) {
			listeners.addAll(typeListeners);
		}
	}

	private void addToIndex(Map<TLStructuredType, List<TLStructuredType>> typeIndex, TLStructuredType generalization, TLStructuredType specialization) {
		typeIndex.computeIfAbsent(generalization, x -> new ArrayList<>()).add(specialization);
	}

	private void notifyListeners(Map<TLStructuredType, List<TLStructuredType>> typeIndex, Set<ModelListener> listeners) {
		ModelChangeEvent change = new ModelChangeEventImpl(typeIndex, _updates.keySet());

		for (ModelListener listener : listeners) {
			listener.notifyChange(change);
		}
	}

}