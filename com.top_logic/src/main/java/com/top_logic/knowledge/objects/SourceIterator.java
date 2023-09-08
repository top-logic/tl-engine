/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import java.util.Iterator;

import com.top_logic.dob.DataObjectException;

/**
 * An Iterator that will return the Sources of an Iterator for KAs.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class SourceIterator extends KAIterator {

    /** Create an Iterator based on some inner Iterator */
     public SourceIterator (Iterator<KnowledgeAssociation> anIt) {
        super(anIt);
    }
           
    /** Create an Iterator for associations for given KO. */
    public SourceIterator (KnowledgeObject aKO, String aType) {
        super(aKO.getIncomingAssociations(aType));
    }

    /** Create an Iterator for associations for given KO. */
    public SourceIterator (KnowledgeObject aKO) {
        super(aKO.getIncomingAssociations());
    }

    /**
     * Overridden to return the getSourceObject() of {@link #currentKA()}.
     */
    @Override
	public KnowledgeObject getSide() throws DataObjectException {
        return currentKA.getSourceObject();
    }
}
