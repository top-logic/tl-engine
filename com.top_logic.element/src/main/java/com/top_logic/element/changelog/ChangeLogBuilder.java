/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.LongID;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.changelog.model.trans.TransientCreation;
import com.top_logic.element.changelog.model.trans.TransientDeletion;
import com.top_logic.element.changelog.model.trans.TransientModification;
import com.top_logic.element.changelog.model.trans.TransientUpdate;
import com.top_logic.element.meta.SeparateTableStorage;
import com.top_logic.element.meta.kbbased.storage.ColumnStorage;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.ModelKind;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Algorithm to analyze technical changes reported by a {@link KnowledgeBase} an build a model
 * change log.
 */
public class ChangeLogBuilder {

	private final KnowledgeBase _kb;

	private final HistoryManager _hm;

	private final TLModel _model;

	private Revision _startRev;

	private Revision _stopRev;

	/**
	 * The classes that store their instances in a certain table.
	 */
	private Map<MOStructure, List<TLClass>> _classesByTable;

	/**
	 * For each type, a mapping that assigns the {@link TLStructuredTypePart} that stores the column
	 * with a given name of the object's table.
	 */
	private Map<TLStructuredType, Map<String, TLStructuredTypePart>> _columnBindingByType = new HashMap<>();

	/**
	 * For each table and column, the storage that uses that column to store values for foreign
	 * objects (if any).
	 */
	private Map<MOStructure, Map<String, SeparateTableStorage>> _storagesByTable = new HashMap<>();

	/**
	 * Creates a {@link ChangeLogBuilder}.
	 */
	public ChangeLogBuilder(KnowledgeBase kb, TLModel model) {
		_kb = kb;
		_model = model;
		_hm = kb.getHistoryManager();

		_startRev = _hm.getRevision(1);
		_stopRev = _hm.getRevision(_hm.getLastRevision());
	}

	/**
	 * The first {@link Revision} to analyze.
	 */
	public Revision getStartRev() {
		return _startRev;
	}

	/**
	 * The last {@link Revision} to analyze.
	 */
	public Revision getStopRev() {
		return _stopRev;
	}

	/**
	 * @see #getStartRev()
	 */
	public ChangeLogBuilder setStartRev(Revision startRev) {
		_startRev = startRev;
		return this;
	}

	/**
	 * @see #getStopRev()
	 */
	public ChangeLogBuilder setStopRev(Revision stopRev) {
		_stopRev = stopRev;
		return this;
	}

	/**
	 * Retrieves the change sets.
	 */
	public Collection<com.top_logic.element.changelog.model.ChangeSet> build() {
		List<com.top_logic.element.changelog.model.ChangeSet> log = new ArrayList<>();

		analyzeModel();

		ReaderConfig readerConfig = ReaderConfigBuilder.createConfig(_startRev, _stopRev);
		try (ChangeSetReader reader = _kb.getChangeSetReader(readerConfig)) {
			while (true) {
				ChangeSet changeSet = reader.read();
				if (changeSet == null) {
					break;
				}

				Revision revision = _hm.getRevision(changeSet.getRevision());

				TransientChangeSet entry = new TransientChangeSet();
				entry.setDate(new Date(revision.getDate()));
				entry.setRevision(revision);
				entry.setMessage(revision.getLog());
				entry.setAuthor(resolveAuthor(revision));

				new ChangeSetAnalyzer(changeSet, entry).analyze();

				log.add(entry);
			}
		}

		return log;
	}

	private class ChangeSetAnalyzer {

		private final ChangeSet _changeSet;

		private final TransientChangeSet _entry;

		private final Map<TLObject, Set<TLStructuredTypePart>> _updates = new HashMap<>();

		/**
		 * Creates a {@link ChangeSetAnalyzer}.
		 */
		public ChangeSetAnalyzer(ChangeSet changeSet, TransientChangeSet entry) {
			_changeSet = changeSet;
			_entry = entry;
		}

		public void analyze() {
			analyzeCreations();
			analyzeUpdates();
			analyzeDeletions();

			for (Entry<TLObject, Set<TLStructuredTypePart>> entry : _updates.entrySet()) {
				TransientUpdate change = new TransientUpdate();

				TLObject newObject = entry.getKey();
				change.setObject(newObject);

				TLObject oldObject =
					_kb.resolveObjectKey(inRevision(newObject.tId(), _changeSet.getRevision() - 1)).getWrapper();

				for (TLStructuredTypePart part : entry.getValue()) {
					TransientModification modification = new TransientModification();
					modification.setPart(part);

					// Cast should not be necessary, since a setter of a multiple property should
					// not expect modifyable collections.
					modification.setOldValue((Collection<Object>) CollectionUtil.asList(oldObject.tValue(part)));
					modification.setNewValue((Collection<Object>) CollectionUtil.asList(newObject.tValue(part)));

					change.addModification(modification);
				}

				_entry.addChange(change);
			}
		}

		/**
		 * Analyzes creations in the given {@link ChangeSet} and transfer them to the given model change
		 * set.
		 */
		private void analyzeCreations() {
			List<ObjectCreation> creations = _changeSet.getCreations();
		
			// All object IDs of objects created in the current change set.
			Set<ObjectKey> createdKeys = creations.stream().map(c -> c.getOriginalObject()).collect(Collectors.toSet());
		
			for (ObjectCreation creation : creations) {
				MetaObject table = creation.getObjectType();
				analyzeTechnicalUpdate(_changeSet, table, createdKeys, creation);
		
				List<TLClass> classes = _classesByTable.get(table);
				if (classes == null) {
					// A pure technical object.
					continue;
				}
		
				TLObject object = _kb.resolveObjectKey(creation.getOriginalObject()).getWrapper();
		
				TLObject container = object.tContainer();
				if (container != null && createdKeys.contains(container.tId())) {
					// Only a part of some other object, ignore.
					continue;
				}
		
				// Record a creation.
				TransientCreation change = new TransientCreation();
				change.setObject(object);
		
				_entry.addChange(change);
			}
		}

		private void analyzeUpdates() {
			List<ItemUpdate> updates = _changeSet.getUpdates();
		
			for (ItemUpdate update : updates) {
				MetaObject table = update.getObjectType();
				analyzeTechnicalUpdate(_changeSet, table, Collections.emptySet(), update);
		
				List<TLClass> classes = _classesByTable.get(table);
				if (classes == null) {
					// A pure technical object.
					continue;
				}
		
				TLObject newObject = _kb.resolveObjectKey(update.getOriginalObject()).getWrapper();
		
				// Record an update.
				Set<TLStructuredTypePart> changedParts = enter(newObject);
		
				Map<String, Object> valueUpdates = update.getValues();
				Map<String, Object> oldValues = update.getOldValues();
				TLStructuredType type = newObject.tType();
		
				Map<String, TLStructuredTypePart> partByColumn = lookupColumnBinding(type);
				for (Entry<String, Object> valueUpdate : valueUpdates.entrySet()) {
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

					changedParts.add(part);
				}
			}
		}

		/**
		 * Marks the given object as changed and retrieves the set of changed parts.
		 */
		private Set<TLStructuredTypePart> enter(TLObject newObject) {
			return _updates.computeIfAbsent(newObject, x -> new HashSet<>());
		}

		private void analyzeDeletions() {
			List<ItemDeletion> deletions = _changeSet.getDeletions();
		
			Set<ObjectKey> deletedKeys = deletions.stream()
				.map(c -> c.getObjectId().toObjectKey(_changeSet.getRevision() - 1)).collect(Collectors.toSet());
		
			for (ItemDeletion deletion : deletions) {
				MetaObject table = deletion.getObjectType();
				analyzeTechnicalUpdate(_changeSet, table, deletedKeys, deletion);
		
				List<TLClass> classes = _classesByTable.get(table);
				if (classes == null) {
					// A pure technical object.
					continue;
				}
		
				KnowledgeItem item =
					_kb.resolveObjectKey(deletion.getObjectId().toObjectKey(_changeSet.getRevision() - 1));
				TLObject object = item.getWrapper();
		
				TLObject container = object.tContainer();
				if (container != null && deletedKeys.contains(container.tId())) {
					// Only a part of some other object, ignore.
					continue;
				}
		
				// TODO: There might be surviving parts of the deleted object, if composition references
				// of the deleted object were modified in the same transaction immediately before
				// deletion.
		
				// Record a deletion.
				TransientDeletion change = new TransientDeletion();
				change.setObject(object);
		
				_entry.addChange(change);
			}
		}

		private void analyzeTechnicalUpdate(ChangeSet changeSet, MetaObject table, Set<ObjectKey> createdDeletedKeys,
				ItemChange change) {
			Map<String, SeparateTableStorage> storages = _storagesByTable.get(table);
			if (storages == null) {
				// Table is not used to store value of foreign objects.
				return;
			}

			for (SeparateTableStorage storage : storages.values()) {
				// A row that stores (part of) an attribute value of some object.
				ObjectKey objId = storage.getBaseObjectId(change.getValues());
				if (objId != null) {
					// Note: A table storing values for other objects is not required to do so for
					// every row. An example is the inline collection storage, which may optionally
					// associate value objects with container objects by storing a foreign key value
					// in the table of the value object.

					ObjectKey oldId = inRevision(objId, changeSet.getRevision() - 1);
					ObjectKey newId = inRevision(objId, changeSet.getRevision());
					if (createdDeletedKeys.contains(oldId) || createdDeletedKeys.contains(newId)) {
						// Part of a created or deleted object, no additional change.
						return;
					}

					TLObject newObject = _kb.resolveObjectKey(newId).getWrapper();
					ObjectKey partId = storage.getPartId(change.getValues());
					TLStructuredTypePart part = _kb.resolveObjectKey(partId).getWrapper();

					enter(newObject).add(part);
				}
			}
		}
	}

	private static ObjectKey inRevision(ObjectKey objId, long rev) {
		return new DefaultObjectKey(objId.getBranchContext(), rev, objId.getObjectType(), objId.getObjectName());
	}

	private Map<String, TLStructuredTypePart> lookupColumnBinding(TLStructuredType type) {
		Map<String, TLStructuredTypePart> partByColumn = _columnBindingByType.get(type);

		if (partByColumn == null) {
			partByColumn = new HashMap<>();
			List<? extends TLStructuredTypePart> parts = type.getAllParts();
			for (TLStructuredTypePart part : parts) {
				StorageDetail storage = part.getStorageImplementation();
				if (storage instanceof ColumnStorage columnStorage) {
					partByColumn.put(columnStorage.getStorageAttribute(), part);
				}
			}
			_columnBindingByType.put(type, partByColumn);
		}

		return partByColumn;
	}

	/**
	 * Lookup the account that was the author of the given {@link Revision}, or <code>null</code>
	 * for a technical transaction.
	 */
	private Person resolveAuthor(Revision revision) {
		String authorSpec = revision.getAuthor();
		Person author;
		if (authorSpec.startsWith("person:")) {
			author = _kb.resolveObjectKey(
				new DefaultObjectKey(
					_hm.getTrunk().getBranchId(), revision.getCommitNumber(),
					_kb.getMORepository().getMetaObject(Person.OBJECT_NAME),
					LongID.fromExternalForm(authorSpec.substring("person:".length()))))
				.getWrapper();
		} else {
			author = null;
		}
		return author;
	}

	/**
	 * Analyze the application model to map technical changes to model changes.
	 */
	private void analyzeModel() {
		_classesByTable = new HashMap<>();
		_storagesByTable = new HashMap<>();

		for (TLModule module : _model.getModules()) {
			for (TLType type : module.getTypes()) {
				if (type.getModelKind() == ModelKind.CLASS) {
					TLClass classType = (TLClass) type;

					MOStructure table = TLModelUtil.getTable(classType);
					_classesByTable.computeIfAbsent(table, x -> new ArrayList<>()).add(classType);
					
					for (TLStructuredTypePart part : classType.getLocalParts()) {
						StorageDetail storage = part.getStorageImplementation();
						if (storage.isReadOnly()) {
							// Skip derived storages.
							continue;
						}
						if (storage instanceof SeparateTableStorage associationStorage) {
							String storageTable = associationStorage.getTable();
							String storageColumn = associationStorage.getStorageColumn();

							MOStructure storageType = (MOStructure) _kb.getMORepository().getType(storageTable);
							Map<String, SeparateTableStorage> storageByColumn =
								_storagesByTable.computeIfAbsent(storageType, x -> new HashMap<>());

							storageByColumn.putIfAbsent(storageColumn, associationStorage);
						}
					}
				}
			}
		}
	}
}
