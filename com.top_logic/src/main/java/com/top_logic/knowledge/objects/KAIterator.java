/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObjectException;

/**
 * A generic iterator for associations.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class KAIterator implements Iterator<KnowledgeObject> {

    /** The Iterator used internally */
    protected Iterator<KnowledgeAssociation> iterator;

    /** The KnowledgeAssociation last used */
    protected KnowledgeAssociation currentKA;

    /** Create an Iterator based on some inner Iterator */
    public KAIterator (Iterator<KnowledgeAssociation> anIt) {
        this.iterator = anIt;
    }
    
    /**
     * Show currentKA for debugging.
     */
    @Override
	public String toString() {
        return this.getClass().getSimpleName() + '@' + currentKA;
    }

    /** Fall back to inner iterator */
    @Override
	public boolean hasNext () {
        return this.iterator.hasNext ();
    }

    /** Fall back to nextKO. */
    @Override
	public KnowledgeObject next () {
        return nextKO();
    }

    /** allow subclasses to return either src or destination of {@link #currentKA()} */
    public abstract KnowledgeObject getSide() throws DataObjectException;

    public KnowledgeObject nextKO() {
        try {
            currentKA = this.iterator.next ();
            return getSide();
        }
        catch (Exception ex) {
            Logger.error("Failed to getDestinationObject()", ex, this);
            throw (NoSuchElementException) new NoSuchElementException("Failed to getDestinationObject").initCause(ex);
        }
    }

    /** Allow access to the KA currently in scope. */
    public KnowledgeAssociation currentKA() {
        return currentKA;
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
	public void remove () {
        throw new UnsupportedOperationException("Can't remove KAs this way.");
    }

}
