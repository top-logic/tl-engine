
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLObject;

/**
 * This interface specifies an association between two KnowledgeObjects.
 * <p>
 * KnowledgeAssociations (KAs) do not carry security, instead access to a KA
 * is only allowed when both KOs are accessible. 
 * </p><p>
 * Be aware that KAs, as of TopLogic 4.0. may refer to a KO in some
 * other Knowledgebase. These KAs are called <code>BridgeAssociation</code>
 * event though there is no such class or interface (yet).
 * The BridgeAssociation is an asymetric feature (for now).
 * </p>
 * 
 * @author  Klaus Halfmann / Jörg Connotte
 */
public interface KnowledgeAssociation extends KnowledgeItem, TLObject {

    /**
	 * The {@link KnowledgeItem#tId()} of the source object.
	 * 
	 * <p>
	 * In contrast to {@link #getSourceObject()}, the result is not local to the current transaction
	 * and the method may still be called, even if the link has been deleted.
	 * </p>
	 */
    ObjectKey getSourceIdentity();

    /**
	 * The {@link KnowledgeObject} representing the beginning of the association.
	 * 
	 * @throws InvalidLinkException
	 *         when Source may not be available.
	 */
	KnowledgeObject getSourceObject() throws InvalidLinkException;
    
    /**
	 * The {@link KnowledgeItem#tId()} of the destination object.
	 * 
	 * <p>
	 * In contrast to {@link #getDestinationObject()}, the result is not local to the current
	 * transaction and the method may still be called, even if the link has been deleted.
	 * </p>
	 */
    ObjectKey getDestinationIdentity();

    /**
	 * The {@link KnowledgeObject} representing the end of the association.
	 * 
	 * @throws InvalidLinkException
	 *         when Source may not be available.
	 */
	KnowledgeObject getDestinationObject() throws InvalidLinkException;

 }
