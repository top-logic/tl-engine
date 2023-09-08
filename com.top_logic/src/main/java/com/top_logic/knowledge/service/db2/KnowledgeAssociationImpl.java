/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;

/**
 * Static mix-in implementation of the {@link KnowledgeAssociation} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
public class KnowledgeAssociationImpl extends KnowledgeItemImpl {

	/**
	 * @see KnowledgeAssociation#getSourceObject()
	 */
	public static KnowledgeObjectInternal getSourceObject(KnowledgeAssociationInternal self) throws InvalidLinkException {
		DBKnowledgeBase kb = self.getKnowledgeBase();
		ObjectKey identity = self.getSourceIdentity();
		
		KnowledgeObjectInternal result = (KnowledgeObjectInternal) kb.resolveObjectKey(identity);
		checkResult(self, identity, result);
		
		return result;
	}

	/**
	 * @see KnowledgeAssociation#getSourceIdentity()
	 */
	public static ObjectKey getSourceIdentity(KnowledgeAssociationInternal self) {
		MOReference sourceAttribute = getAttribute(self, DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		ObjectKey sourceId = self.getReferencedKey(sourceAttribute);
		if (sourceId == null) {
			throw new IllegalStateException("No source in " + self);
		}
		return sourceId;
	}

	private static MOReference getAttribute(KnowledgeAssociationInternal self, String attributeName) {
		MOClass associationType = (MOClass) self.tTable();
		MOReference attribute = (MOReference) associationType.getAttributeOrNull(attributeName);
		if (attribute == null) {
			throw new UnreachableAssertion(attributeName + " is a known, internal attribute.");
		}
		return attribute;
	}

	/**
	 * @see KnowledgeAssociation#getDestinationObject()
	 */
	public static KnowledgeObjectInternal getDestinationObject(KnowledgeAssociationInternal self)
			throws InvalidLinkException {
		DBKnowledgeBase kb = self.getKnowledgeBase();
		ObjectKey identity = self.getDestinationIdentity();
		
		KnowledgeObjectInternal result = (KnowledgeObjectInternal) kb.resolveObjectKey(identity);
		checkResult(self, identity, result);

		return result;
	}

	/**
	 * @see KnowledgeAssociation#getDestinationIdentity()
	 */
	public static ObjectKey getDestinationIdentity(KnowledgeAssociationInternal self) {
		MOReference destAttribute = getAttribute(self, DBKnowledgeAssociation.REFERENCE_DEST_NAME);
		ObjectKey destId = self.getReferencedKey(destAttribute);
		if (destId == null) {
			throw new IllegalStateException("No destination in " + self);
		}
		return destId;
	}

	private static void checkResult(KnowledgeAssociationInternal self, ObjectKey identity,
			KnowledgeObjectInternal result) throws InvalidLinkException {
		if (result == null) {
			throw new InvalidLinkException("Dangling identifier: " + identity);
		}
		
	    DBContext context = self.getKnowledgeBase().getCurrentDBContext();
	    if (context != null) {
	        if (context.isRemovedKey(identity)) {
	            throw new InvalidLinkException("Dangling identifier: " + identity);
	        }
	    }
	}

}
