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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.trans.TransientCreation;
import com.top_logic.element.changelog.model.trans.TransientDeletion;
import com.top_logic.element.changelog.model.trans.TransientModification;
import com.top_logic.element.changelog.model.trans.TransientUpdate;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;

/**
 * Converts a {@link ChangeSet knowledge base change set} into a
 * {@link com.top_logic.element.changelog.model.ChangeSet model change set}.
 *
 * <p>
 * The analyzer resolves the technical {@link ChangeSet} events to model-level {@link Change}s
 * (creations, updates, deletions), filtering out persistent cache attributes and technical-only
 * rows. A {@link ChangeFilter} can be attached to further restrict which changes are kept.
 * </p>
 *
 * <p>
 * Not thread-safe. One instance per {@link ChangeSet}.
 * </p>
 */
public class ChangeSetAnalyzer {

	/**
	 * Analysis state of a {@link ChangeSetAnalyzer}.
	 */
	private static enum AnalyseState {

		/** Not analysed yet. */
		NO,

		/** {@link #hasChanges()} is accurate, but {@link #applyChanges(com.top_logic.element.changelog.model.ChangeSet)} would be incomplete. */
		PARTIAL,

		/** Fully analysed. */
		FULL;
	}

	private final KnowledgeBase _kb;

	private final ModelTables _modelTables;

	private final Set<String> _excludeModules;

	private final ChangeSet _changeSet;

	private final Map<TLObject, Set<TLStructuredTypePart>> _updates = new HashMap<>();

	private final List<Change> _changes = new ArrayList<>();

	private final List<Runnable> _adaptions = new ArrayList<>();

	private final Iterator<ObjectCreation> _remainingCreations;

	private final Iterator<ItemUpdate> _remainingUpdates;

	private final Iterator<ItemDeletion> _remainingDeletions;

	private AnalyseState _state = AnalyseState.NO;

	private ChangeFilter _filter;

	/**
	 * Creates a {@link ChangeSetAnalyzer}.
	 */
	public ChangeSetAnalyzer(KnowledgeBase kb, ModelTables modelTables, Set<String> excludeModules,
			ChangeSet changeSet) {
		_kb = kb;
		_modelTables = modelTables;
		_excludeModules = excludeModules;
		_changeSet = changeSet;
		_remainingCreations = _changeSet.getCreations().iterator();
		_remainingUpdates = _changeSet.getUpdates().iterator();
		_remainingDeletions = _changeSet.getDeletions().iterator();
	}

	/**
	 * Configures an optional {@link ChangeFilter} restricting the emitted {@link Change}s.
	 *
	 * @param filter
	 *        the filter to apply, or {@code null} to emit all changes.
	 * @return this instance for chaining.
	 */
	public ChangeSetAnalyzer setFilter(ChangeFilter filter) {
		_filter = filter;
		return this;
	}

	/**
	 * Whether the {@link ChangeSet} contains relevant changes (after applying any configured
	 * {@link #setFilter(ChangeFilter) filter}).
	 */
	public boolean hasChanges() {
		if (_filter != null) {
			// With a filter, any early-exit heuristic below would observe unfiltered changes
			// (including changes accumulated in _updates before processUpdates), so force a full
			// analysis to ensure the filter is applied before answering.
			analyze(AnalyseState.FULL);
			return !_changes.isEmpty();
		}
		switch (_state) {
			case FULL:
				return !_changes.isEmpty();
			case NO:
				analyze(AnalyseState.PARTIAL);
				assert _state != AnalyseState.NO;
				return hasChanges();
			case PARTIAL:
				return !(_changes.isEmpty() && _updates.isEmpty());
		}
		throw new IllegalStateException("No such enum " + _state);
	}

	/**
	 * Applies the detected {@link Change}s to the given model {@link com.top_logic.element.changelog.model.ChangeSet}.
	 */
	public void applyChanges(com.top_logic.element.changelog.model.ChangeSet out) {
		analyze(AnalyseState.FULL);
		_adaptions.forEach(Runnable::run);
		_changes.forEach(out::addChange);
	}

	private void analyze(AnalyseState completeness) {
		if (_state.ordinal() >= completeness.ordinal()) {
			return;
		}
		switch (completeness) {
			case FULL:
				analyzeCreations(false);
				analyzeUpdates(false);
				analyzeDeletions(false);

				processUpdates();

				_state = AnalyseState.FULL;
				break;
			case NO:
				assert false;
				break;
			case PARTIAL:
				boolean creationFound = analyzeCreations(true);
				if (creationFound) {
					_state = AnalyseState.PARTIAL;
					break;
				}
				boolean updatesFound = analyzeUpdates(true);
				if (updatesFound) {
					_state = AnalyseState.PARTIAL;
					break;
				}
				boolean deletionsFound = analyzeDeletions(true);
				if (deletionsFound) {
					_state = AnalyseState.PARTIAL;
					break;
				}
				assert _updates.isEmpty();
				assert _changes.isEmpty();
				assert _adaptions.isEmpty();
				_state = AnalyseState.FULL;
				break;
		}
		assert _state.ordinal() >= completeness.ordinal();
	}

	private void processUpdates() {
		for (Entry<TLObject, Set<TLStructuredTypePart>> entry : _updates.entrySet()) {
			TransientUpdate change = new TransientUpdate();

			TLObject newObject = entry.getKey();
			change.setObject(newObject);

			TLObject oldObject = resolve(inRevision(newObject.tId(), previousRevision())).getWrapper();
			change.setOldObject(oldObject);

			for (TLStructuredTypePart part : entry.getValue()) {
				TransientModification modification = new TransientModification();
				modification.setPart(part);

				modification.setOldValue((Collection<Object>) CollectionUtil.asList(oldObject.tValue(part)));
				modification.setNewValue((Collection<Object>) CollectionUtil.asList(newObject.tValue(part)));

				change.addModification(modification);
			}

			registerChange(change);
		}
	}

	private long previousRevision() {
		return _changeSet.getRevision() - 1;
	}

	private boolean analyzeCreations(boolean stopOnChange) {
		if (_remainingCreations.hasNext()) {
			Set<ObjectKey> createdKeys =
				_changeSet.getCreations()
					.stream()
					.map(ObjectCreation::getOriginalObject)
					.collect(Collectors.toSet());
			do {
				ObjectCreation creation = _remainingCreations.next();
				MetaObject table = creation.getObjectType();
				boolean versionedChange = MetaObjectUtils.isVersioned(table);
				boolean technicalUpdate;
				if (versionedChange) {
					technicalUpdate = analyzeTechnicalUpdate(_changeSet.getRevision(), table, createdKeys, creation);
				} else {
					/* The creation may at most be an update of an "real" object. In this case it is
					 * not possible to report a change. Therefore it is not necessary to analyze
					 * it! */
					technicalUpdate = false;
				}

				List<TLClass> classes = _modelTables.getClassesForTable(table);
				if (classes.isEmpty()) {
					if (stopOnChange && technicalUpdate) {
						return true;
					} else {
						continue;
					}
				}

				TLObject object;
				if (versionedChange) {
					object = resolve(creation.getOriginalObject()).getWrapper();
				} else {
					// Access with historic key will fail. Try resolve object in current.
					KnowledgeItem currentItem = resolve(inCurrent(creation.getOriginalObject()));
					if (currentItem == null) {
						// deleted in the meanwhile
						continue;
					}
					object = currentItem.getWrapper();
				}
				if (excludedByModule(object) || isPersistentCacheObject(object)) {
					if (stopOnChange && technicalUpdate) {
						return true;
					} else {
						continue;
					}
				}

				TransientCreation change = new TransientCreation();
				change.setObject(object);

				updateImplicit(change, object, createdKeys);

				registerChange(change);
				if (stopOnChange) {
					return true;
				}
			} while (_remainingCreations.hasNext());
		}
		return false;
	}

	private void updateImplicit(Change change, TLObject object, Set<ObjectKey> keys) {
		_adaptions.add(() -> {
			TLObject container = object.tContainer();
			change.setImplicit(container != null && keys.contains(container.tId()));
		});
	}

	private boolean isPersistentCacheObject(TLObject object) {
		return TLAnnotations.isPersistentCache(object.tType());
	}

	private boolean isPersistentCacheAttribute(TLStructuredTypePart part) {
		return TLAnnotations.isPersistentCache(part);
	}

	private boolean analyzeUpdates(boolean stopOnChange) {
		while (_remainingUpdates.hasNext()) {
			ItemUpdate update = _remainingUpdates.next();
			MetaObject table = update.getObjectType();
			if (!MetaObjectUtils.isVersioned(table)) {
				/* Note: It is possible that an update event may be reported even though the cleanup
				 * task has not yet been executed. */
				/* It is not possible to fetch changes later for the "previous rev" to fill change
				 * -> Ignore change. */
				continue;
			}
			boolean technicalUpdate =
				analyzeTechnicalUpdate(_changeSet.getRevision(), table, Collections.emptySet(), update);

			List<TLClass> classes = _modelTables.getClassesForTable(table);
			if (classes.isEmpty()) {
				if (stopOnChange && technicalUpdate) {
					return true;
				} else {
					continue;
				}
			}

			TLObject newObject = resolve(update.getOriginalObject()).getWrapper();
			if (excludedByModule(newObject) || isPersistentCacheObject(newObject)) {
				if (stopOnChange && technicalUpdate) {
					return true;
				} else {
					continue;
				}
			}

			Set<TLStructuredTypePart> changedParts = enter(newObject);

			Map<String, Object> valueUpdates = update.getValues();
			Map<String, Object> oldValues = update.getOldValues();
			TLStructuredType type = newObject.tType();

			Map<String, TLStructuredTypePart> partByColumn = _modelTables.lookupColumnBinding(type);
			for (Entry<String, Object> valueUpdate : valueUpdates.entrySet()) {
				String storageAttribute = valueUpdate.getKey();

				Object newValue = valueUpdate.getValue();
				Object oldValue = oldValues.get(storageAttribute);

				if (Utils.equals(newValue, oldValue)) {
					continue;
				}

				TLStructuredTypePart part = partByColumn.get(storageAttribute);
				if (part == null) {
					addOppositeEnds(changedParts, type, table, storageAttribute, valueUpdates, oldValues);
					continue;
				}
				if (isPersistentCacheAttribute(part)) {
					continue;
				}

				changedParts.add(part);
			}
			if (stopOnChange) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Records the opposite ends of the references whose values are stored in the given column of
	 * the changed object's own row.
	 *
	 * <p>
	 * A reference stored in the rows of the referenced objects (e.g. an inline collection storage)
	 * keeps its value in a container column of the target table. Changing that column moves the
	 * object from one container to another. For the moved object, this is a change of the opposite
	 * end of the reference, if the opposite end is modelled.
	 * </p>
	 *
	 * @param changedParts
	 *        Receives the opposite ends that are changed.
	 * @param type
	 *        The type of the changed object.
	 * @param table
	 *        The table of the changed object.
	 * @param column
	 *        The changed column that is not bound to an attribute of the object.
	 * @param newValues
	 *        The values of the row after the change.
	 * @param oldValues
	 *        The values of the row before the change, or <code>null</code> if not available.
	 */
	private void addOppositeEnds(Set<TLStructuredTypePart> changedParts, TLStructuredType type, MetaObject table,
			String column, Map<String, Object> newValues, Map<String, Object> oldValues) {
		AssociationStorageDescriptor descriptor = _modelTables.getDescriptorsForTable(table).get(column);
		if (descriptor == null) {
			return;
		}
		addOppositeEnd(changedParts, type, descriptor.getPartId(newValues));
		if (oldValues != null) {
			addOppositeEnd(changedParts, type, descriptor.getPartId(oldValues));
		}
	}

	private void addOppositeEnd(Set<TLStructuredTypePart> changedParts, TLStructuredType type, ObjectKey referenceId) {
		if (referenceId == null) {
			return;
		}
		KnowledgeItem referenceItem = resolvePart(referenceId, _changeSet.getRevision());
		if (referenceItem == null) {
			return;
		}
		if (!(referenceItem.getWrapper() instanceof TLReference reference)) {
			return;
		}
		if (isPersistentCacheAttribute(reference)) {
			return;
		}
		TLReference opposite = reference.getOpposite();
		if (opposite == null) {
			return;
		}
		TLStructuredTypePart oppositeEnd = type.getPart(opposite.getName());
		if (oppositeEnd == null || oppositeEnd.getDefinition() != opposite.getDefinition()) {
			return;
		}
		if (isPersistentCacheAttribute(oppositeEnd)) {
			return;
		}
		changedParts.add(oppositeEnd);
	}

	private Set<TLStructuredTypePart> enter(TLObject newObject) {
		return _updates.computeIfAbsent(newObject, x -> new HashSet<>());
	}

	private void registerChange(Change change) {
		if (_filter != null && !_filter.accept(change)) {
			return;
		}
		_changes.add(change);
	}

	private boolean analyzeDeletions(boolean stopOnChange) {
		if (_remainingDeletions.hasNext()) {
			long previousRev = previousRevision();
			Set<ObjectKey> deletedKeys = _changeSet.getDeletions()
				.stream()
				.map(c -> inRevision(c.getObjectId(), previousRev))
				.collect(Collectors.toSet());
			do {
				ItemDeletion deletion = _remainingDeletions.next();
				MetaObject table = deletion.getObjectType();
				if (!MetaObjectUtils.isVersioned(table)) {
					/* Note: It is possible that a deletion event may be reported even though the
					 * cleanup task has not yet been executed. */
					continue;
				}
				boolean technicalUpdate =
					analyzeTechnicalUpdate(_changeSet.getRevision(), table, deletedKeys, deletion);

				List<TLClass> classes = _modelTables.getClassesForTable(table);
				if (classes.isEmpty()) {
					if (stopOnChange && technicalUpdate) {
						return true;
					} else {
						continue;
					}
				}

				KnowledgeItem item = resolve(inRevision(deletion.getObjectId(), previousRev));
				TLObject object = item.getWrapper();
				if (excludedByModule(object) || isPersistentCacheObject(object)) {
					if (stopOnChange && technicalUpdate) {
						return true;
					} else {
						continue;
					}
				}

				TransientDeletion change = new TransientDeletion();
				change.setObject(object);

				updateImplicit(change, object, deletedKeys);

				registerChange(change);
				if (stopOnChange) {
					return true;
				}
			} while (_remainingDeletions.hasNext());
		}
		return false;
	}

	/**
	 * Records the changes of the objects whose attribute values are stored in the changed row of a
	 * foreign table.
	 *
	 * <p>
	 * For an updated row, the owner is determined from the values of the row before and after the
	 * change. If the row moves from one owner to another, both owners get an update of the
	 * attribute stored in the row.
	 * </p>
	 *
	 * @return Whether a change of an owner was recorded.
	 */
	private boolean analyzeTechnicalUpdate(long revision, MetaObject table, Set<ObjectKey> createdDeletedKeys,
			ItemChange change) {
		Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
		if (descriptors.isEmpty()) {
			return false;
		}

		Map<String, Object> oldValues = change instanceof ItemUpdate update ? update.getOldValues() : null;

		boolean technicalUpdate = false;
		for (AssociationStorageDescriptor descriptor : descriptors.values()) {
			technicalUpdate |=
				enterOwner(revision, table, createdDeletedKeys, descriptor, change.getValues(), change.getValues());
			if (oldValues != null) {
				technicalUpdate |=
					enterOwner(revision, table, createdDeletedKeys, descriptor, oldValues, change.getValues());
			}
		}
		return technicalUpdate;
	}

	/**
	 * Records an update of the owner of a row of a foreign table.
	 *
	 * @param row
	 *        The values of the row from which the owner and the changed attribute are determined.
	 * @param changedValues
	 *        The values reported for the change, for diagnostics only.
	 * @return Whether a change of the owner was recorded.
	 */
	private boolean enterOwner(long revision, MetaObject table, Set<ObjectKey> createdDeletedKeys,
			AssociationStorageDescriptor descriptor, Map<String, Object> row, Map<String, Object> changedValues) {
		ObjectKey objId = descriptor.getBaseObjectId(row);
		if (objId == null) {
			return false;
		}
		ObjectKey oldId = inRevision(objId, revision - 1);
		ObjectKey newId = inRevision(objId, revision);
		if (createdDeletedKeys.contains(oldId) || createdDeletedKeys.contains(newId)) {
			return false;
		}

		KnowledgeItem newItem = resolve(newId);
		if (newItem == null) {
			// The former owner is deleted in this revision, its deletion is reported instead.
			return false;
		}
		TLObject newObject = newItem.getWrapper();
		if (excludedByModule(newObject) || isPersistentCacheObject(newObject)) {
			return false;
		}
		ObjectKey partId = descriptor.getPartId(row);
		if (partId == null) {
			Logger.error("Unable to determine part id for update of '"
					+ MetaLabelProvider.INSTANCE.getLabel(newObject) + "' in revision '" + revision
					+ "': Changes: " + changedValues + ", table: " + table + ", descriptor: "
					+ descriptor,
				ChangeSetAnalyzer.class);
			return false;
		}
		TLStructuredTypePart part = resolvePart(partId, revision).getWrapper();
		if (isPersistentCacheAttribute(part)) {
			return false;
		}

		enter(newObject).add(part);
		return true;
	}

	/**
	 * Resolves the attribute definition with the given ID, also if it is created or deleted in the
	 * given revision.
	 */
	private KnowledgeItem resolvePart(ObjectKey partId, long revision) {
		KnowledgeItem partKI = resolve(partId);
		if (partKI == null) {
			partKI = resolve(inRevision(partId, revision - 1));
			if (partKI == null) {
				partKI = resolve(inRevision(partId, revision));
			}
		}
		return partKI;
	}

	private KnowledgeItem resolve(ObjectKey key) {
		return _kb.resolveObjectKey(key);
	}

	private static ObjectKey inRevision(ObjectBranchId objId, long rev) {
		return objId.toObjectKey(rev);
	}

	private static ObjectKey inRevision(ObjectKey objId, long rev) {
		return KBUtils.ensureHistoryContext(objId, rev);
	}

	private static ObjectKey inCurrent(ObjectKey objId) {
		return inRevision(objId, Revision.CURRENT_REV);
	}

	private boolean excludedByModule(TLObject obj) {
		if (_excludeModules.isEmpty()) {
			return false;
		}
		TLModule module = obj.tType().getModule();
		return _excludeModules.contains(module.getName());
	}

}
