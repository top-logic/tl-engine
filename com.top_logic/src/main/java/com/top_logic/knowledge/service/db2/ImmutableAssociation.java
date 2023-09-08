/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Immutable implementation of {@link KnowledgeAssociation}
 * 
 * @see DBKnowledgeAssociation The mutable variant.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ImmutableAssociation extends StaticImmutableKnowledgeObject implements KnowledgeAssociationInternal {

	/**
	 * Creates a new {@link ImmutableAssociation}.
	 */
	public ImmutableAssociation(DBKnowledgeBase kb, MOKnowledgeItem type) {
		super(kb, type);
	}

	@Override
	public ObjectKey getSourceIdentity() {
		return KnowledgeAssociationImpl.getSourceIdentity(this);
	}

	@Override
	public KnowledgeObject getSourceObject() throws InvalidLinkException {
		return KnowledgeAssociationImpl.getSourceObject(this);
	}

	@Override
	public ObjectKey getDestinationIdentity() {
		return KnowledgeAssociationImpl.getDestinationIdentity(this);
	}

	@Override
	public KnowledgeObject getDestinationObject() throws InvalidLinkException {
		return KnowledgeAssociationImpl.getDestinationObject(this);
	}

}

