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
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
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
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
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
				boolean technicalUpdate = analyzeTechnicalUpdate(_changeSet.getRevision(), table, createdKeys, creation);

				List<TLClass> classes = _modelTables.getClassesForTable(table);
				if (classes.isEmpty()) {
					if (stopOnChange && technicalUpdate) {
						return true;
					} else {
						continue;
					}
				}

				TLObject object = resolve(creation.getOriginalObject()).getWrapper();
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

	private boolean analyzeTechnicalUpdate(long revision, MetaObject table, Set<ObjectKey> createdDeletedKeys,
			ItemChange change) {
		Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
		if (descriptors.isEmpty()) {
			return false;
		}

		boolean technicalUpdate = false;
		for (AssociationStorageDescriptor descriptor : descriptors.values()) {
			ObjectKey objId = descriptor.getBaseObjectId(change.getValues());
			if (objId != null) {
				ObjectKey oldId = inRevision(objId, revision - 1);
				ObjectKey newId = inRevision(objId, revision);
				if (createdDeletedKeys.contains(oldId) || createdDeletedKeys.contains(newId)) {
					continue;
				}

				TLObject newObject = resolve(newId).getWrapper();
				if (excludedByModule(newObject) || isPersistentCacheObject(newObject)) {
					continue;
				}
				ObjectKey partId = descriptor.getPartId(change.getValues());
				if (partId == null) {
					Logger.error("Unable to determine part id for update of '"
							+ MetaLabelProvider.INSTANCE.getLabel(newObject) + "' in revision '" + revision
							+ "': Changes: " + change.getValues() + ", table: " + table + ", descriptor: "
							+ descriptor,
						ChangeSetAnalyzer.class);
					continue;
				}
				KnowledgeItem partKI = resolve(partId);
				if (partKI == null) {
					partKI = resolve(inRevision(partId, revision - 1));
					if (partKI == null) {
						partKI = resolve(inRevision(partId, revision));
					}
				}
				TLStructuredTypePart part = partKI.getWrapper();
				if (isPersistentCacheAttribute(part)) {
					continue;
				}

				enter(newObject).add(part);
				technicalUpdate = true;
			}
		}
		return technicalUpdate;
	}

	private KnowledgeItem resolve(ObjectKey key) {
		return _kb.resolveObjectKey(key);
	}

	private static ObjectKey inRevision(ObjectBranchId objId, long rev) {
		return objId.toObjectKey(rev);
	}

	private static ObjectKey inRevision(ObjectKey objId, long rev) {
		return new DefaultObjectKey(objId.getBranchContext(), rev, objId.getObjectType(), objId.getObjectName());
	}

	private boolean excludedByModule(TLObject obj) {
		if (_excludeModules.isEmpty()) {
			return false;
		}
		TLModule module = obj.tType().getModule();
		return _excludeModules.contains(module.getName());
	}

}
