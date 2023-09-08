/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link DBKnowledgeBase} internal implementation of {@link KnowledgeAssociation}.
 * 
 * @see ImmutableAssociation The immutable variant.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBKnowledgeAssociation extends StaticKnowledgeItem implements KnowledgeAssociationInternal {

	/**
	 * The name of the reference for the destination object.
	 */
	public static final String REFERENCE_DEST_NAME = DestinationReference.REFERENCE_DEST_NAME;

	/**
	 * The name of the reference for the source object.
	 */
	public static final String REFERENCE_SOURCE_NAME = SourceReference.REFERENCE_SOURCE_NAME;

	public DBKnowledgeAssociation(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
        super(kb, staticType);
    }

	@Override
	public final ObjectKey getSourceIdentity() {
		return KnowledgeAssociationImpl.getSourceIdentity(this);
	}
	
	@Override
	public ObjectKey getDestinationIdentity() {
		return KnowledgeAssociationImpl.getDestinationIdentity(this);
    }
    
	@Override
	public final KnowledgeObject getSourceObject() throws InvalidLinkException {
		return KnowledgeAssociationImpl.getSourceObject(this);
	}

    @Override
	public final KnowledgeObject getDestinationObject() throws InvalidLinkException {
    	return KnowledgeAssociationImpl.getDestinationObject(this);
    }

	public static void clearDestinationAndRemoveLink(KnowledgeAssociation ka) throws DataObjectException {
		clearReferenceAndRemoveLink(ka, REFERENCE_DEST_NAME);
	}

	public static void clearSourceAndRemoveLink(KnowledgeAssociation ka) throws DataObjectException {
		clearReferenceAndRemoveLink(ka, REFERENCE_SOURCE_NAME);
	}

	private static void clearReferenceAndRemoveLink(KnowledgeAssociation ka, String referenceName)
			throws DataObjectException {
		ka.setAttributeValue(referenceName, null);
		ka.delete();
	}

	public static MOReference getDestinationAttribute(MetaObject associationType) {
		return getReference(associationType, REFERENCE_DEST_NAME);
	}

	public static MOReference getSourceAttribute(MetaObject associationType) {
		return getReference(associationType, REFERENCE_SOURCE_NAME);
	}

	public static MOReference getReference(MetaObject associationType, String name) {
		try {
			return (MOReference) ((MOClass) associationType).getAttribute(name);
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

}

