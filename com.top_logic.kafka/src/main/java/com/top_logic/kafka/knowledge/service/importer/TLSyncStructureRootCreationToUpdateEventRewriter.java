/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.importer;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.kafka.knowledge.service.exporter.TLSyncStructureRootCreationMarkerEventRewriter.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.kafka.knowledge.service.exporter.TypeFilterRewriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.objects.identifier.ExtReferenceFormat;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.ExtIDFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.util.TLAnnotations;

/**
 * An {@link EventRewriter} that converts the {@link ObjectCreation} of {@link StructuredElement}
 * {@link StructuredElement#getRoot() roots} to updates and sets the {@link ExtID} of the
 * corresponding local root element to that of the remote root element.
 * <p>
 * Root elements need to be treated specially, as they are automatically created during startup even
 * in the receiving system. That means, their create cannot be applied, as that would create a
 * second root element. Therefore, the {@link ExtID} of the root element in the sending system is
 * set as the {@link ExtID} of the root element in the receiving system.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class TLSyncStructureRootCreationToUpdateEventRewriter implements EventRewriter {

	@Override
	public void rewrite(ChangeSet changeSet, EventWriter out) {
		List<ObjectCreation> rootCreations = removeRootCreations(changeSet);
		addRootUpdates(changeSet, rootCreations);
		out.write(changeSet);
	}

	private void addRootUpdates(ChangeSet changeSet, List<ObjectCreation> rootCreations) {
		for (ObjectCreation creation : rootCreations) {
			changeSet.addUpdate(convertRootCreationToUpdate(creation));
		}
	}

	private List<ObjectCreation> removeRootCreations(ChangeSet changeSet) {
		List<ObjectCreation> rootCreations = list();
		Iterator<ObjectCreation> creations = changeSet.getCreations().iterator();
		while (creations.hasNext()) {
			ObjectCreation creation = creations.next();
			if (creation.getValues().containsKey(STRUCTURE_ROOT_MARKER_ATTRIBUTE)) {
				rootCreations.add(creation);
				creations.remove();
			}
		}
		return rootCreations;
	}

	private ItemUpdate convertRootCreationToUpdate(ObjectCreation creation) {
		Map<String, Object> values = creation.getValues();
		String structureName = (String) values.remove(STRUCTURE_ROOT_MARKER_ATTRIBUTE);
		TLObject root = getRoot(structureName);
		replaceExternalIdAttribute(root, values);
		markForPlainImport(values);
		return createItemUpdate(creation, root, values);
	}

	private TLObject getRoot(String structureName) {
		StructuredElementFactory factory = (StructuredElementFactory) DynamicModelService.getFactoryFor(structureName);
		return factory.getRoot();
	}

	private void replaceExternalIdAttribute(TLObject root, Map<String, Object> values) {
		String externalIdAttribute = ExtIDFactory.getInstance().getExternalIDAttribute(getTable(root));
		ExtReference extReference = (ExtReference) values.remove(TypeFilterRewriter.EXT_REFERENCE_ATTRIBUTE);
		values.put(externalIdAttribute, ExtReferenceFormat.INSTANCE.format(extReference));
	}

	private MOClass getTable(TLObject root) {
		TLType type = root.tType();
		return getTable(type);
	}

	private MOClass getTable(TLType type) {
		String tableName = TLAnnotations.getTable(type);
		return (MOClass) getKnowledgeBase(type).getMORepository().getMetaObject(tableName);
	}

	private KnowledgeBase getKnowledgeBase(TLObject type) {
		return type.tHandle().getKnowledgeBase();
	}

	private void markForPlainImport(Map<String, Object> values) {
		values.put(TypeFilterRewriter.PLAIN_IMPORT_MARKER, true);
	}

	private ItemUpdate createItemUpdate(ObjectCreation creation, TLObject root, Map<String, Object> values) {
		long branch = creation.getOwnerBranch();
		ObjectBranchId objectBranchId = createObjectBranchId(branch, root);
		ItemUpdate update = new ItemUpdate(creation.getRevision(), objectBranchId, false);
		update.getValues().putAll(values);
		return update;
	}

	private ObjectBranchId createObjectBranchId(long branch, TLObject root) {
		MOClass table = getTable(root);
		TLID tlId = root.tIdLocal();
		return new ObjectBranchId(branch, table, tlId);
	}

}
