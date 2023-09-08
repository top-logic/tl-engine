/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import java.util.Iterator;

import com.top_logic.dob.DataObjectException;

/**
 * An Iterator that will return the Destinations of an Iterator for KAs.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DestinationIterator extends KAIterator {

    /** Create an Iterator based on some inner Iterator */
    public DestinationIterator (Iterator<KnowledgeAssociation> anIt) {
        super(anIt);
    }
            
    /** Create an Iterator for all outgoing associations for given KO. */
    public DestinationIterator (KnowledgeObject aKO) {
        super(aKO.getOutgoingAssociations());
    }

    /** Create an Iterator for associations for given KO. */
    public DestinationIterator (KnowledgeObject aKO, String aType) {
        super(aKO.getOutgoingAssociations(aType));
    }

    /** Create an Iterator for associations for given KO and Destination */
    public DestinationIterator (
        KnowledgeObject aKO, String aType, KnowledgeObject aDest) {
        super(aKO.getOutgoingAssociations(aType, aDest));
    }

    /**
     * Overridden to return the getDestinationObject() of @link #currentKA()}.
     */
    @Override
	public KnowledgeObject getSide() throws DataObjectException {
        return currentKA.getDestinationObject();
    }

}
