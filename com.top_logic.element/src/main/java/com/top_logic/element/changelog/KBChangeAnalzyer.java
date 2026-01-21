/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.changelog.model.trans.TransientCreation;
import com.top_logic.element.changelog.model.trans.TransientDeletion;
import com.top_logic.element.changelog.model.trans.TransientModification;
import com.top_logic.element.changelog.model.trans.TransientUpdate;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;

/**
 * Analyzer of changes from the {@link KnowledgeBase}.
 * 
 * <p>
 * The {@link KBChangeAnalzyer} gets the updates, creations, and deletions of the current commit and
 * creates a {@link ChangeSet} of them.
 * </p>
 */
public class KBChangeAnalzyer {

	private final KnowledgeBase _kb;

	private final Map<TLID, KnowledgeItem> _updates;

	private final Map<TLID, KnowledgeItem> _creations;

	private final Map<TLID, KnowledgeItem> _deletions;

	private final long _commitNumber;

	private ModelTables _modelTables;

	private final Map<TLObject, Set<TLStructuredTypePart>> _updatesByItem = new HashMap<>();

	/**
	 * {@link Change}s to apply to a {@link ChangeSet}.
	 */
	private final List<Change> _changes = new ArrayList<>();

	/**
	 * Creates a new {@link KBChangeAnalzyer}.
	 * 
	 * @param kb
	 *        {@link KnowledgeBase} that triggered the change.
	 * @param commitNumber
	 *        The commit number of the change.
	 * @param changes
	 *        Changed elements.
	 * @param creations
	 *        Newly created elements.
	 * @param deletions
	 *        Deleted elements.
	 */
	public KBChangeAnalzyer(KnowledgeBase kb, long commitNumber, Map<TLID, KnowledgeItem> changes,
			Map<TLID, KnowledgeItem> creations,
			Map<TLID, KnowledgeItem> deletions) {
		_kb = kb;
		_commitNumber = commitNumber;
		_updates = changes;
		_creations = creations;
		_deletions = deletions;
	}

	private void reset() {
		_modelTables = ElementModelCacheService.getModelTables();
		_changes.clear();
		_updatesByItem.clear();
	}

	/**
	 * Starts the analysis process and returns the {@link ChangeSet} from it.
	 */
	public TransientChangeSet analyze() {
		reset();

		analyzeCreations();
		analyzeUpdates();
		analyzeDeletions();

		processUpdates();

		TransientChangeSet cs = new TransientChangeSet();
		_changes.forEach(cs::addChange);

		return cs;
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> currentValue(KnowledgeItem item) {
		return descriptor -> descriptor.getBaseObjectId(item);
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> noValue() {
		return descriptor -> null;
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> oldValue(KnowledgeItem item) {
		return descriptor -> item.getKnowledgeBase().withoutModifications(() -> descriptor.getBaseObjectId(item));
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> currentPart(KnowledgeItem item) {
		return descriptor -> descriptor.getPartId(item);
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> noPart() {
		return descriptor -> null;
	}

	private static Function<AssociationStorageDescriptor, ObjectKey> oldPart(KnowledgeItem item) {
		return descriptor -> item.getKnowledgeBase().withoutModifications(() -> descriptor.getPartId(item));
	}

	private boolean analyzeCreations() {
		if (_creations.isEmpty()) {
			return false;
		}
		// All object IDs of objects created in the current change set.
		Set<ObjectKey> createdKeys = _creations.values().stream()
			.map(KnowledgeItem::tId)
			.collect(Collectors.toSet());
		for (KnowledgeItem creation : _creations.values()) {
			MetaObject table = creation.tTable();

			Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
			if (!descriptors.isEmpty()) {
				// Table is not used to store value of foreign objects.
				analyzeTechnicalUpdate(descriptors, table, createdKeys,
					currentValue(creation), noValue(), currentPart(creation), noPart());
			}

			List<TLClass> classes = _modelTables.getClassesForTable(table);
			if (classes.isEmpty()) {
				// A pure technical object.
				continue;
			}

			TLObject object = creation.getWrapper();
			if (isPersistentCacheObject(object)) {
				continue;
			}

			// Record a creation.
			TransientCreation change = new TransientCreation();
			change.setObject(object);

			updateImplicit(change, object, createdKeys);

			registerChange(change);
		}
		return false;
	}

	private boolean analyzeUpdates() {
		for (KnowledgeItem update : _updates.values()) {
			MetaObject table = update.tTable();

			Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
			if (!descriptors.isEmpty()) {
				// Table is not used to store value of foreign objects.
				analyzeTechnicalUpdate(descriptors, table, Collections.emptySet(),
					currentValue(update), oldValue(update), currentPart(update), oldPart(update));
			}

			List<TLClass> classes = _modelTables.getClassesForTable(table);
			if (classes.isEmpty()) {
				// A pure technical object.
				continue;
			}

			TLObject newObject = update.getWrapper();
			if (isPersistentCacheObject(newObject)) {
				continue;
			}

			// Record an update.
			Set<TLStructuredTypePart> changedParts = enter(newObject);
			TLStructuredType type = newObject.tType();

			// TLStructuredTypeParts that store in the table of the KnowledgeItem
			Map<String, TLStructuredTypePart> partsByColumn = _modelTables.lookupColumnBinding(type);

			for (Entry<String, TLStructuredTypePart> partByColumn : partsByColumn.entrySet()) {
				if (isPersistentCacheAttribute(partByColumn.getValue())) {
					// Value is just a persistent cache, ignore.
					continue;
				}

				Object newValue = update.getAttributeValue(partByColumn.getKey());
				Object oldValue = update.getKnowledgeBase()
					.withoutModifications(() -> update.getAttributeValue(partByColumn.getKey()));

				if (Utils.equals(newValue, oldValue)) {
					// No change for part partByColumn.getValue().
					continue;
				}
				changedParts.add(partByColumn.getValue());
			}

		}
		return false;
	}

	private boolean analyzeDeletions() {
		if (_deletions.isEmpty()) {
			return false;
		}
		// All object IDs of objects deleted in the current change set.
		Set<ObjectKey> deletedKeys = _deletions.values().stream()
			.map(KnowledgeItem::tId)
			.collect(Collectors.toSet());
		for (KnowledgeItem deletion : _deletions.values()) {
			MetaObject table = deletion.tTable();

			Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
			if (!descriptors.isEmpty()) {
				// Table is not used to store value of foreign objects.
				analyzeTechnicalUpdate(descriptors, table, deletedKeys,
					noValue(), oldValue(deletion), noPart(), oldPart(deletion));
			}

			List<TLClass> classes = _modelTables.getClassesForTable(table);
			if (classes.isEmpty()) {
				// A pure technical object.
				continue;
			}
			KnowledgeItem item = resolve(inPreviousRevision(deletion.tId()));
			TLObject object = item.getWrapper();
			if (isPersistentCacheObject(object)) {
				continue;
			}

			// Record a deletion.
			TransientDeletion change = new TransientDeletion();
			change.setObject(object);

			updateImplicit(change, object, deletedKeys);
			registerChange(change);
		}
		return false;
	}

	private void processUpdates() {
		for (Entry<TLObject, Set<TLStructuredTypePart>> entry : _updatesByItem.entrySet()) {
			TransientUpdate change = new TransientUpdate();

			TLObject newObject = entry.getKey();
			change.setObject(newObject);

			TLObject oldObject = resolve(inPreviousRevision(newObject.tId())).getWrapper();
			change.setOldObject(oldObject);

			for (TLStructuredTypePart part : entry.getValue()) {
				TransientModification modification = new TransientModification();
				modification.setPart(part);

				// Cast should not be necessary, since a setter of a multiple property should
				// not expect modifyable collections.
				modification.setOldValue((Collection<Object>) CollectionUtil.asList(oldObject.tValue(part)));
				modification.setNewValue((Collection<Object>) CollectionUtil.asList(newObject.tValue(part)));

				change.addModification(modification);
			}

			registerChange(change);
		}
	}

	private boolean analyzeTechnicalUpdate(Map<String, AssociationStorageDescriptor> descriptors, MetaObject table,
			Set<ObjectKey> createdDeletedKeys, Function<AssociationStorageDescriptor, ObjectKey> newValue,
			Function<AssociationStorageDescriptor, ObjectKey> oldValue,
			Function<AssociationStorageDescriptor, ObjectKey> newPart,
			Function<AssociationStorageDescriptor, ObjectKey> oldPart) {
		boolean technicalUpdate = false;
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
					enterChange(table, createdDeletedKeys, descriptor, oldId, oldPartId);
				}
			} else {
				if (oldId == null) {
					enterChange(table, createdDeletedKeys, descriptor, newId, newPartId);
				} else if (oldId.equals(newId) && oldPartId.equals(newPartId)) {
					// No change in descriptor.
					continue;
				} else {
					enterChange(table, createdDeletedKeys, descriptor, oldId, oldPartId);
					enterChange(table, createdDeletedKeys, descriptor, newId, newPartId);
				}
			}

			technicalUpdate = true;
		}
		return technicalUpdate;
	}

	private void enterChange(MetaObject table, Set<ObjectKey> createdDeletedKeys,
			AssociationStorageDescriptor descriptor, ObjectKey objId, ObjectKey partId) {
		if (createdDeletedKeys.contains(objId)) {
			// Part of a deleted or created object, no additional change.
			return;
		} else {
			TLObject oldObject = resolve(objId).getWrapper();
			if (isPersistentCacheObject(oldObject)) {
				return;
			}
			if (partId == null) {
				Logger.error("Unable to determine part id for update of '"
						+ MetaLabelProvider.INSTANCE.getLabel(oldObject) + "': table: " + table
						+ ", descriptor: " + descriptor,
					KBChangeAnalzyer.class);
				return;
			}

			KnowledgeItem partKI = resolve(partId);
			if (partKI == null) {
				// part is deleted, but not the object itself
				assert createdDeletedKeys.contains(partId) : "Part " + partId
						+ " can not be resolved whereas it was not deleted: " + createdDeletedKeys;
				partKI = resolve(inPreviousRevision(partId));
			}
			TLStructuredTypePart part = partKI.getWrapper();
			if (isPersistentCacheAttribute(part)) {
				// Value is just a persistent cache, ignore.
				return;
			}

			enter(oldObject).add(part);
		}
	}

	private void updateImplicit(Change change, TLObject object, Set<ObjectKey> keys) {
		TLObject container = object.tContainer();
		change.setImplicit(container != null && keys.contains(container.tId()));
	}

	private void registerChange(Change change) {
		_changes.add(change);
	}

	private KnowledgeItem resolve(ObjectKey key) {
		return _kb.resolveObjectKey(key);
	}

	private long previousRevision() {
		return _commitNumber - 1;
	}

	private ObjectKey inPreviousRevision(ObjectKey objId) {
		return inRevision(objId, previousRevision());
	}

	private static ObjectKey inRevision(ObjectKey objId, long rev) {
		return new DefaultObjectKey(objId.getBranchContext(), rev, objId.getObjectType(), objId.getObjectName());
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

