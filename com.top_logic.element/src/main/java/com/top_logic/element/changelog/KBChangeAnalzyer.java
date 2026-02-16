/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.cs.TLObjectChange;
import com.top_logic.model.cs.TLObjectChangeSet;
import com.top_logic.model.cs.TLObjectCreation;
import com.top_logic.model.cs.TLObjectDeletion;
import com.top_logic.model.cs.TLObjectUpdate;

/**
 * Analyzer of changes from the {@link KnowledgeBase}.
 * 
 * <p>
 * The {@link KBChangeAnalzyer} gets an {@link UpdateEvent} for the current commit and creates a
 * {@link TLObjectChangeSet} of them.
 * </p>
 */
public class KBChangeAnalzyer {

	private ModelTables _modelTables;

	private final Map<TLObject, Set<TLStructuredTypePart>> _updatesByItem = new HashMap<>();

	private final List<TLObjectChange> _objectChanges = new ArrayList<>();

	private final UpdateEvent _event;

	/**
	 * Creates a new {@link KBChangeAnalzyer}.
	 */
	public KBChangeAnalzyer(UpdateEvent event) {
		_event = event;
	}

	private KnowledgeBase kb() {
		return _event.getKnowledgeBase();
	}

	private void reset() {
		_modelTables = ElementModelCacheService.getModelTables();
		_objectChanges.clear();
		_updatesByItem.clear();
	}

	/**
	 * Starts the analysis process and returns the {@link TLObjectChangeSet} from it.
	 */
	public TLObjectChangeSet analyze() {
		reset();

		analyzeCreations();
		analyzeUpdates();
		analyzeDeletions();

		processUpdates();

		TLObjectChangeSet cs = new TLObjectChangeSet(_event.getKnowledgeBase(), _event.getCommitNumber());
		_objectChanges.forEach(cs::addChange);

		return cs;
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> value(Map<String, Object> values) {
		return descriptor -> descriptor.getBaseObjectId(values);
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> noValue() {
		return descriptor -> null;
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> part(Map<String, Object> values) {
		return descriptor -> descriptor.getPartId(values);
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> noPart() {
		return descriptor -> null;
	}

	private void analyzeCreations() {
		Map<ObjectKey, KnowledgeItem> createdObjects = _event.getCreatedObjects();
		if (createdObjects.isEmpty()) {
			return;
		}

		// All object IDs of objects created in the current change set.
		Set<ObjectKey> createdKeys = createdObjects.keySet();
		for (ObjectCreation creation : _event.getChanges().getCreations()) {
			MetaObject table = creation.getObjectType();

			Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
			if (!descriptors.isEmpty()) {
				// Table is used to store value of foreign objects.
				analyzeTechnicalCreation(descriptors, table, createdKeys, creation);
			}

			List<TLClass> classes = _modelTables.getClassesForTable(table);
			if (classes.isEmpty()) {
				// A pure technical object.
				continue;
			}

			TLObject object = createdObjects.get(objectKey(creation)).getWrapper();
			if (isPersistentCacheObject(object)) {
				continue;
			}

			// Record a creation.
			registerChange(new TLObjectCreation(object));
		}
	}

	private static ObjectKey objectKey(ItemEvent evt) {
		return evt.getObjectId().toCurrentObjectKey();
	}

	private void analyzeUpdates() {
		Map<ObjectKey, KnowledgeItem> updatedObjects = _event.getUpdatedObjects();
		for (ItemUpdate update : _event.getChanges().getUpdates()) {
			MetaObject table = update.getObjectType();

			Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
			if (!descriptors.isEmpty()) {
				// Table is used to store value of foreign objects.
				analyzeTechnicalUpdate(descriptors, table, update);
			}

			List<TLClass> classes = _modelTables.getClassesForTable(table);
			if (classes.isEmpty()) {
				// A pure technical object.
				continue;
			}

			TLObject newObject = updatedObjects.get(objectKey(update)).getWrapper();
			if (isPersistentCacheObject(newObject)) {
				continue;
			}

			TLStructuredType type = newObject.tType();

			// TLStructuredTypeParts that store in the table of the KnowledgeItem
			Map<String, Object> values = update.getValues();
			Map<String, Object> oldValues = update.getOldValues();
			Map<String, TLStructuredTypePart> partByColumn = _modelTables.lookupColumnBinding(type);
			for (Entry<String, Object> valueUpdate : values.entrySet()) {
				String storageAttribute = valueUpdate.getKey();

				Object newValue = valueUpdate.getValue();
				Object oldValue = oldValues.get(storageAttribute);

				if (Utils.equals(newValue, oldValue)) {
					// A value was provided in an update event for technical reasons, without
					// the value being changed.
					continue;
				}

				TLStructuredTypePart part = partByColumn.get(storageAttribute);
				if (part == null) {
					// A change that has no model representation, ignore.
					continue;
				}
				if (isPersistentCacheAttribute(part)) {
					// Value is just a persistent cache, ignore.
					continue;
				}

				enter(newObject).add(part);
			}

		}
	}

	private void analyzeDeletions() {
		// All object IDs of objects deleted in the current change set.
		Set<ObjectKey> deletedKeys = _event.getDeletedObjectKeys();
		if (deletedKeys.isEmpty()) {
			return;
		}

		Map<ObjectKey, KnowledgeItem> deletedObjects = new HashMap<>();
		for (KnowledgeItem deleted : _event.getCachedDeletedObjects()) {
			deletedObjects.put(deleted.tId(), deleted);
		}

		for (ItemDeletion deletion : _event.getChanges().getDeletions()) {
			MetaObject table = deletion.getObjectType();

			Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
			if (!descriptors.isEmpty()) {
				// Table is used to store value of foreign objects.
				analyzeTechnicalDeletion(descriptors, table, deletedKeys, deletion);
			}

			ObjectKey deletedKey = objectKey(deletion);
			List<TLClass> classes = _modelTables.getClassesForTable(table);
			if (classes.isEmpty()) {
				// A pure technical object. remove it from later processing.
				deletedObjects.remove(deletedKey);
			} else {
				/* add null as placeholder when deleted object is not delivered by
				 * #getCachedDeletedObjects() */
				deletedObjects.putIfAbsent(deletedKey, null);
			}
		}

		kb().withoutModifications(() -> {
			for (Entry<ObjectKey, KnowledgeItem> deletion : deletedObjects.entrySet()) {
				TLObject deleted;
				if (deletion.getValue() != null) {
					deleted = deletion.getValue().getWrapper();
				} else {
					deleted = kb().resolveObjectKey(deletion.getKey()).getWrapper();
				}
				if (isPersistentCacheObject(deleted)) {
					continue;
				}
				// Record a deletion.
				registerChange(new TLObjectDeletion(deleted));
			}
			return null;
		});

	}

	private void processUpdates() {
		Set<ObjectKey> created = _event.getCreatedObjectKeys();
		Set<ObjectKey> deleted = _event.getDeletedObjectKeys();
		for (Entry<TLObject, Set<TLStructuredTypePart>> updates : _updatesByItem.entrySet()) {
			TLObject object = updates.getKey();
			TLObjectUpdate change = new TLObjectUpdate(object);
			kb().withoutModifications(() -> {
				for (TLStructuredTypePart part : updates.getValue()) {
					if (created.contains(part.tId())) {
						// Part is created in the revision: The old value is not filled.
						continue;
					}
					change.oldValues().put(part, object.tValue(part));
				}
				return null;	
			});
			for (TLStructuredTypePart part : updates.getValue()) {
				if (deleted.contains(part.tId())) {
					// Part is deleted in the revision: The new value is not filled.
					continue;
				}
				change.newValues().put(part, object.tValue(part));
			}
			
			registerChange(change);
		}

	}

	private void analyzeTechnicalCreation(Map<String, AssociationStorageDescriptor> descriptors, MetaObject table,
			Set<ObjectKey> createdKeys, ObjectCreation creation) {
		Map<String, Object> values = creation.getValues();
		analyzeTechnicalUpdate(descriptors, table, createdKeys,
			value(values), part(values),
			noValue(), noPart());
	}

	private void analyzeTechnicalDeletion(Map<String, AssociationStorageDescriptor> descriptors, MetaObject table, Set<ObjectKey> deletedKeys,
			ItemDeletion deletion) {
		Map<String, Object> oldValues = deletion.getValues();
		analyzeTechnicalUpdate(descriptors, table, deletedKeys,
			noValue(), noPart(),
			value(oldValues), part(oldValues));
	}

	private void analyzeTechnicalUpdate(Map<String, AssociationStorageDescriptor> descriptors, MetaObject table,
			ItemUpdate update) {
		Map<String, Object> values = update.getValues();
		Map<String, Object> oldValues = update.getOldValues();
		analyzeTechnicalUpdate(descriptors, table, Collections.emptySet(),
			value(values), part(values),
			value(oldValues), part(oldValues));
	}

	private void analyzeTechnicalUpdate(Map<String, AssociationStorageDescriptor> descriptors, MetaObject table,
			Set<ObjectKey> createdDeletedKeys, Function<AssociationStorageDescriptor, ObjectKey> newValue,
			Function<AssociationStorageDescriptor, ObjectKey> newPart,
			Function<AssociationStorageDescriptor, ObjectKey> oldValue,
			Function<AssociationStorageDescriptor, ObjectKey> oldPart) {
		for (AssociationStorageDescriptor descriptor : descriptors.values()) {
			// A row that stores (part of) an attribute value of some object.
			ObjectKey newId = newValue.apply(descriptor);
			ObjectKey newPartId = newPart.apply(descriptor);
			ObjectKey oldId = oldValue.apply(descriptor);
			ObjectKey oldPartId = oldPart.apply(descriptor);

			if (newId == null) {
				if (newPartId != null) {
					Logger.error("There is a part id '" + newPartId + "' but no item for update in: table :'" + table
							+ "', descriptor: " + descriptor,
						KBChangeAnalzyer.class);
				}
				if (oldId == null) {
					continue;
				} else {
					enterChange(table, createdDeletedKeys, descriptor, oldId, oldPartId, true);
				}
			} else {
				if (oldId == null) {
					enterChange(table, createdDeletedKeys, descriptor, newId, newPartId, false);
				} else if (oldId.equals(newId) && oldPartId.equals(newPartId)) {
					// No change in descriptor.
					continue;
				} else {
					enterChange(table, createdDeletedKeys, descriptor, oldId, oldPartId, true);
					enterChange(table, createdDeletedKeys, descriptor, newId, newPartId, false);
				}
			}
		}
	}

	private void enterChange(MetaObject table, Set<ObjectKey> createdDeletedKeys,
			AssociationStorageDescriptor descriptor, ObjectKey objId, ObjectKey partId, boolean oldValue) {
		if (createdDeletedKeys.contains(objId)) {
			// Part of a deleted or created object, no additional change.
			return;
		}
		TLObject object = resolve(objId).getWrapper();
		if (isPersistentCacheObject(object)) {
			return;
		}
		if (partId == null) {
			Logger.error("Unable to determine part id for update of '"
					+ MetaLabelProvider.INSTANCE.getLabel(object) + "': table: " + table
					+ ", descriptor: " + descriptor,
				KBChangeAnalzyer.class);
			return;
		}


		TLStructuredTypePart part;

		if (oldValue) {
			Set<ObjectKey> deletedKeys = createdDeletedKeys;
			if (deletedKeys.contains(partId)) {
				part = kb().withoutModifications(() -> resolvePart(partId));
			} else {
				part = resolvePart(partId);
			}
		} else {
			part = resolvePart(partId);
		}

		if (part == null) {
			// just a persistent cache attribute!
			return;
		}
		enter(object).add(part);
	}

	private TLStructuredTypePart resolvePart(ObjectKey key) {
		KnowledgeItem partKI = kb().resolveObjectKey(key);
		TLStructuredTypePart part = partKI.getWrapper();
		if (isPersistentCacheAttribute(part)) {
			return null;
		}
		return part;
	}

	private void registerChange(TLObjectChange change) {
		_objectChanges.add(change);
	}

	private KnowledgeItem resolve(ObjectKey key) {
		return kb().resolveObjectKey(key);
	}

	private Set<TLStructuredTypePart> enter(TLObject newObject) {
		return _updatesByItem.computeIfAbsent(newObject, x -> new HashSet<>());
	}

	private boolean isPersistentCacheObject(TLObject object) {
		TLStructuredType type = object.tType();
		if (type == null) {
			MOStructure table = object.tTable();
			throw new NullPointerException("No type found for object from table " + table + ": " + object);
		}
		return TLAnnotations.isPersistentCache(type);
	}

	private boolean isPersistentCacheAttribute(TLStructuredTypePart part) {
		return TLAnnotations.isPersistentCache(part);
	}

}

