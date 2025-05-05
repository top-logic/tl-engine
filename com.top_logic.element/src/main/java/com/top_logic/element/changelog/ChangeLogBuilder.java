/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
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
				// Only a technical object, ignore.
				continue;
			}

			KnowledgeItem item = _kb.resolveObjectKey(creation.getOriginalObject());
			TLObject object = item.getWrapper();

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

	private void analyzeDeletions(ChangeSet changeSet, TransientChangeSet entry) {
		List<ItemDeletion> deletions = changeSet.getDeletions();

		Set<ObjectKey> deletedKeys = deletions.stream()
			.map(c -> c.getObjectId().toObjectKey(changeSet.getRevision() - 1)).collect(Collectors.toSet());

		for (ItemDeletion deletion : deletions) {
			MetaObject table = deletion.getObjectType();

			List<TLClass> classes = _classesByTable.get(table);
			if (classes == null) {
				// Only a technical object, ignore.
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

		for (TLModule module : _model.getModules()) {
			for (TLType type : module.getTypes()) {
				if (type.getModelKind() == ModelKind.CLASS) {
					TLClass classType = (TLClass) type;

					MOStructure table = TLModelUtil.getTable(classType);
					_classesByTable.computeIfAbsent(table, x -> new ArrayList<>()).add(classType);
				}
			}
		}
	}
}
