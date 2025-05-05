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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.LongID;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.changelog.model.trans.TransientCreation;
import com.top_logic.element.changelog.model.trans.TransientDeletion;
import com.top_logic.element.changelog.model.trans.TransientModification;
import com.top_logic.element.changelog.model.trans.TransientUpdate;
import com.top_logic.element.meta.AssociationStorage;
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
 * 
 */
public class ChangeLogBuilder {

	private KnowledgeBase _kb;

	private HistoryManager _hm;

	private TLModel _model;

	private Revision _startRev;

	private Revision _stopRev;

	private Map<MOStructure, List<TLClass>> _classesByTable;

	/**
	 * For each type, a mapping that assigns the {@link TLStructuredTypePart} that stores the column
	 * with a given name of the object's table.
	 */
	private Map<TLStructuredType, Map<String, TLStructuredTypePart>> _columnBindingByType = new HashMap<>();

	private Map<MOStructure, AssociationStorage> _storageByTable = new HashMap<>();

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
		ChangeSetReader reader = _kb.getChangeSetReader(readerConfig);
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

			analyzeCreations(changeSet, entry);
			analyzeUpdates(changeSet, entry);
			analyzeDeletions(changeSet, entry);

			log.add(entry);
		}

		return log;
	}

	/**
	 * Analyzes creations in the given {@link ChangeSet} and transfer them to the given model change
	 * set.
	 */
	private void analyzeCreations(ChangeSet changeSet, TransientChangeSet entry) {
		List<ObjectCreation> creations = changeSet.getCreations();

		// All object IDs of objects created in the current change set.
		Set<ObjectKey> createdKeys = creations.stream().map(c -> c.getOriginalObject()).collect(Collectors.toSet());

		for (ObjectCreation creation : creations) {
			MetaObject table = creation.getObjectType();

			List<TLClass> classes = _classesByTable.get(table);
			if (classes == null) {
				// A technical object.
				analyzeTechnicalUpdate(changeSet, entry, table, createdKeys, creation);
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

			entry.addChange(change);
		}
	}

	private void analyzeUpdates(ChangeSet changeSet, TransientChangeSet entry) {
		List<ItemUpdate> updates = changeSet.getUpdates();

		for (ItemUpdate update : updates) {
			MetaObject table = update.getObjectType();

			List<TLClass> classes = _classesByTable.get(table);
			if (classes == null) {
				// A technical object.
				analyzeTechnicalUpdate(changeSet, entry, table, Collections.emptySet(), update);
				continue;
			}

			TLObject newObject = _kb.resolveObjectKey(update.getOriginalObject()).getWrapper();

			TLObject oldObject =
				_kb.resolveObjectKey(update.getObjectId().toObjectKey(changeSet.getRevision() - 1)).getWrapper();

			// Record an update.
			TransientUpdate change = new TransientUpdate();
			change.setObject(newObject);

			Map<String, Object> valueUpdates = update.getValues();
			TLStructuredType type = newObject.tType();

			Map<String, TLStructuredTypePart> partByColumn = lookupColumnBinding(type);
			for (Entry<String, Object> valueUpdate : valueUpdates.entrySet()) {
				String storageAttribute = valueUpdate.getKey();
				TLStructuredTypePart part = partByColumn.get(storageAttribute);
				if (part == null) {
					// A change that has no model representation, ignore.
					continue;
				}

				TransientModification modification = new TransientModification();
				modification.setPart(part);

				modification.setOldValue(oldObject.tValue(part));
				modification.setNewValue(newObject.tValue(part));

				change.addModification(modification);
			}

			entry.addChange(change);
		}
	}

	private void analyzeTechnicalUpdate(ChangeSet changeSet, TransientChangeSet entry, MetaObject table,
			Set<ObjectKey> createdDeletedKeys,
			ItemChange update) {
		AssociationStorage storage = _storageByTable.get(table);
		if (storage != null) {
			// A row that stores (part of) an attribute value of some object.
			ObjectKey objId = storage.getBaseObjectId(update.getValues());
			ObjectKey oldId = inRevision(objId, changeSet.getRevision() - 1);
			ObjectKey newId = inRevision(objId, changeSet.getRevision());
			if (createdDeletedKeys.contains(oldId) || createdDeletedKeys.contains(newId)) {
				// Part of a created or deleted object, no additional change.
				return;
			}

			ObjectKey partId = storage.getPartId(update.getValues());

			TLObject oldObject = _kb.resolveObjectKey(oldId).getWrapper();
			TLObject newObject = _kb.resolveObjectKey(newId).getWrapper();

			TLStructuredTypePart part = _kb.resolveObjectKey(partId).getWrapper();

			TransientUpdate change = new TransientUpdate();
			change.setObject(newObject);

			TransientModification modification = new TransientModification();
			modification.setPart(part);

			modification.setOldValue(oldObject.tValue(part));
			modification.setNewValue(newObject.tValue(part));

			change.addModification(modification);

			entry.addChange(change);
		}
	}

	private static ObjectKey inRevision(ObjectKey objId, long rev) {
		return objId.getHistoryContext() == Revision.CURRENT_REV
			? new DefaultObjectKey(objId.getBranchContext(), rev, objId.getObjectType(), objId.getObjectName())
			: objId;
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

	private void analyzeDeletions(ChangeSet changeSet, TransientChangeSet entry) {
		List<ItemDeletion> deletions = changeSet.getDeletions();

		Set<ObjectKey> deletedKeys = deletions.stream()
			.map(c -> c.getObjectId().toObjectKey(changeSet.getRevision() - 1)).collect(Collectors.toSet());

		for (ItemDeletion deletion : deletions) {
			MetaObject table = deletion.getObjectType();

			List<TLClass> classes = _classesByTable.get(table);
			if (classes == null) {
				// A technical object.
				analyzeTechnicalUpdate(changeSet, entry, table, deletedKeys, deletion);
				continue;
			}

			KnowledgeItem item =
				_kb.resolveObjectKey(deletion.getObjectId().toObjectKey(changeSet.getRevision() - 1));
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

			entry.addChange(change);
		}
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
		_storageByTable = new HashMap<>();

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
						if (storage instanceof AssociationStorage associationStorage) {
							String storageTable = associationStorage.getTable();

							_storageByTable.putIfAbsent(
								(MOStructure) _kb.getMORepository().getType(storageTable),
								associationStorage);
						}
					}
				}
			}
		}
	}
}
