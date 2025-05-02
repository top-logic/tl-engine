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
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * 
 */
public class ChangeLogBuilder implements ListModelBuilder {

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		List<com.top_logic.element.changelog.model.ChangeSet> log = new ArrayList<>();

		Map<MOStructure, List<TLClass>> classesByTable = new HashMap<>();

		TLModel model = ModelService.getApplicationModel();
		for (TLModule module : model.getModules()) {
			for (TLType type : module.getTypes()) {
				if (type.getModelKind() == ModelKind.CLASS) {
					TLClass classType = (TLClass) type;

					MOStructure table = TLModelUtil.getTable(classType);
					classesByTable.computeIfAbsent(table, x -> new ArrayList<>()).add(classType);
				}
			}
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		HistoryManager hm = kb.getHistoryManager();
		Revision startRev = hm.getRevisionAt(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
		if (startRev.getCommitNumber() < 1) {
			startRev = hm.getRevision(1);
		}
		Revision stopRev = hm.getRevision(hm.getLastRevision());
		ReaderConfig readerConfig = ReaderConfigBuilder.createConfig(startRev, stopRev);
		ChangeSetReader reader = kb.getChangeSetReader(readerConfig);
		while (true) {
			ChangeSet changeSet = reader.read();
			if (changeSet == null) {
				break;
			}

			Revision revision = hm.getRevision(changeSet.getRevision());
			String authorSpec = revision.getAuthor();
			Person author;
			if (authorSpec.startsWith("person:")) {
				author = kb.resolveObjectKey(
					new DefaultObjectKey(
						hm.getTrunk().getBranchId(), revision.getCommitNumber(),
						kb.getMORepository().getMetaObject(Person.OBJECT_NAME),
						LongID.fromExternalForm(authorSpec.substring("person:".length()))))
					.getWrapper();
			} else {
				author = null;
			}

			TransientChangeSet entry = new TransientChangeSet();
			entry.setDate(new Date(revision.getDate()));
			entry.setRevision(revision);
			entry.setMessage(revision.getLog());
			entry.setAuthor(author);

			List<ObjectCreation> creations = changeSet.getCreations();

			Set<ObjectKey> createdKeys = creations.stream().map(c -> c.getOriginalObject()).collect(Collectors.toSet());

			for (ObjectCreation creation : creations) {
				MetaObject table = creation.getObjectType();

				List<TLClass> classes = classesByTable.get(table);
				if (classes == null) {
					continue;
				}

				KnowledgeItem item = kb.resolveObjectKey(creation.getOriginalObject());
				TLObject object = item.getWrapper();

				TLObject container = object.tContainer();
				if (container != null && createdKeys.contains(container.tId())) {
					// Only a part of some other object, ignore.
					continue;
				}

				TransientCreation change = new TransientCreation();
				change.setObject(object);

				entry.addChange(change);
			}

			List<ItemDeletion> deletions = changeSet.getDeletions();

			Set<ObjectKey> deletedKeys = deletions.stream()
				.map(c -> c.getObjectId().toObjectKey(changeSet.getRevision() - 1)).collect(Collectors.toSet());

			for (ItemDeletion deletion : deletions) {
				MetaObject table = deletion.getObjectType();

				List<TLClass> classes = classesByTable.get(table);
				if (classes == null) {
					continue;
				}

				KnowledgeItem item =
					kb.resolveObjectKey(deletion.getObjectId().toObjectKey(changeSet.getRevision() - 1));
				TLObject object = item.getWrapper();

				TLObject container = object.tContainer();
				if (container != null && deletedKeys.contains(container.tId())) {
					// Only a part of some other object, ignore.
					continue;
				}

				TransientDeletion change = new TransientDeletion();
				change.setObject(object);

				entry.addChange(change);
			}

			log.add(entry);
		}

		return log;
	}

	@Override
	public boolean supportsListElement(LayoutComponent component, Object candidate) {
		return candidate instanceof com.top_logic.element.changelog.model.ChangeSet;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object candidate) {
		return null;
	}
}
