/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580._7392;

import java.util.Map;

import com.google.inject.Inject;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.WebFolder;

/**
 * Splits the "hasChild" association due to migration instruction in Ticket #7392.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HasChildSplitter extends RewritingEventVisitor {
	
	private MetaObject _hasAttachedDocuments;

	private MetaObject _hasStructureChild;

	private MetaObject _hasFolderContent;

	private MetaObject _hasMailFolderContent;

	private MetaObject _MetaElement_generalizations;

	/**
	 * Initialises the {@link MORepository} of this {@link HasChildSplitter}.
	 */
	@Inject
	public void initMORepository(MORepository types) {
		try {
			_MetaElement_generalizations = types.getMetaObject("MetaElement_generalizations");
			_hasAttachedDocuments = types.getMetaObject("hasAttachedDocuments");
			_hasMailFolderContent = types.getMetaObject("hasMailFolderContent");
			_hasFolderContent = types.getMetaObject(WebFolder.CONTENTS_ASSOCIATION);
			_hasStructureChild = types.getMetaObject(StructuredElementWrapper.CHILD_ASSOCIATION);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException("Unresolvable types.", ex);
		}
	}

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		event.setObjectId(rewriteType(event));
		return super.visitCreateObject(event, arg);
	}

	private ObjectBranchId rewriteType(ItemChange event) {
		ObjectBranchId newObjectId;
		ObjectBranchId objectId = event.getObjectId();
		String objectType = objectId.getObjectType().getName();
		if ("hasChild".equals(objectType)) {
			Map<String, Object> values = event.getValues();
			ObjectKey sourceKey = (ObjectKey) values.get(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
			String sourceType = sourceKey.getObjectType().getName();
			if (WebFolder.OBJECT_NAME.equals(sourceType)) {
				newObjectId = new ObjectBranchId(objectId.getBranchId(), _hasFolderContent, objectId.getObjectName());
			} else if ("MailFolder".equals(sourceType)) {
				newObjectId =
					new ObjectBranchId(objectId.getBranchId(), _hasMailFolderContent, objectId.getObjectName());
			} else if ("Mail".equals(sourceType)) {
				newObjectId =
					new ObjectBranchId(objectId.getBranchId(), _hasAttachedDocuments, objectId.getObjectName());
			} else if (KBBasedMetaElement.META_ELEMENT_KO.equals(sourceType)) {
				newObjectId =
					new ObjectBranchId(objectId.getBranchId(), _MetaElement_generalizations, objectId.getObjectName());
				Object parent = event.getValues().get(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
				Object child = event.getValues().get(DBKnowledgeAssociation.REFERENCE_DEST_NAME);

				Object specialisation = child;
				Object generalisation = parent;

				event.getValues().put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, specialisation);
				event.getValues().put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, generalisation);

				event.getValues().put("order", 0);
			} else {
				newObjectId =
					new ObjectBranchId(objectId.getBranchId(), _hasStructureChild, objectId.getObjectName());
			}
		} else {
			newObjectId = objectId;
		}
		return newObjectId;
	}

	@Override
	public Object visitDelete(ItemDeletion event, Void arg) {
		event.setObjectId(rewriteType(event));
		return super.visitDelete(event, arg);
	}

}

