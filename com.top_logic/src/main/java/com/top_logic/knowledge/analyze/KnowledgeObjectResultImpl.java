/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;


import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Default implementation of the SearchResult
 * interface for a search result item.
 * 
 * A search result consists of a KnowledgeObject and
 * a relevance value.
 * The relevance is a double value between 0 and 1 where
 * 0 means the lowest relevance and 1 denotes maximal relevance.
 * The KnowledgeObject must not be null and represents an arbitrary
 * KnowledgeObject from the queried KnowledgeBase. The current user must
 * have at least read access to the KnowledgeObject.
 *
 * @author  <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class KnowledgeObjectResultImpl implements KnowledgeObjectResult  {

    // Private fields

    /** The KnowledgeObject representing the search result. */
    private KnowledgeObject knowledgeObject;
    
    /** The relevance of the search result. */
    private double          relevance;

    /**
     * Constructor that initialises all fields.
     *
     * @param   aKnowledgeObject    the search result KnowledgeObject.
     *                              Must not be null!
     * @param   aRelevance          the relevance value.
     *                              Values outside of [0,1] are mapped to 0
     *                              respectively 1.
     * @throws IllegalArgumentException if the arguments don't
     *          comply with the specification.
     */
    public KnowledgeObjectResultImpl (KnowledgeObject aKnowledgeObject, double aRelevance) 
            throws IllegalArgumentException {
        // Check specification compliance
        if (aKnowledgeObject == null) {
            throw new IllegalArgumentException ("KnowledgeObject must not be null!");
        }
            
        if (aRelevance < 0.0) {
            Logger.warn ("A relevance of less than 0% was found (" + 
                         aRelevance + ").", this);
            aRelevance = 0;
        }
        else if (aRelevance > 1.0) {
            Logger.warn ("A relevance of more than 100% was found (" + 
                         aRelevance + ").", this);
            aRelevance = 1;
        }

        // Init fields
        this.knowledgeObject = aKnowledgeObject;
        this.relevance       = aRelevance;
    }

    /**
     * Get the KnowledgeObject that represents the search result as an Object.
     * The KnowledgeObject must not be null and represents an arbitrary
     * KnowledgeObject from the queried KnowledgeBase. The current user must
     * have at least read access to the KnowledgeObject.
     *
     * @return  the KnowledgeObject
     */
    @Override
	public Object getResult () {
        return (this.knowledgeObject);
    }

    /**
     * Get the KnowledgeObject that represents the search result.
     * The KnowledgeObject must not be null and represents an arbitrary
     * KnowledgeObject from the queried KnowledgeBase. The current user must
     * have at least read access to the KnowledgeObject.
     *
     * @return  the KnowledgeObject
     */
    @Override
	public KnowledgeObject getKnowledgeObject () {
        return (this.knowledgeObject);
    }

    /**
     * Get the relevance of the search result.
     * The relevance is a double value between 0 and 1 where
     * 0 means the lowest relevance and 1 denotes maximal relevance.
     *
     * @return the relevance value.
     */
    @Override
	public double getRelevance () {
        return (this.relevance);
    }

    /**
     * Test whether this object is equals to the given one.
     * True, if the object is a KnowledgeObjectResult and
     * the KnowledgeObjects and the Relevance are equal.
     *
     * @param   anObject    an object
     * @return  true if the objects are equal according to the definition above.
     */
    @Override
	public boolean equals (Object anObject) {
		if (anObject == this) {
			return true;
		}
        if (!(anObject instanceof KnowledgeObjectResult)) {
            return false;
        }
        KnowledgeObjectResult theResult = (KnowledgeObjectResult) anObject;
        return ((this.getRelevance () == theResult.getRelevance ())
             && (this.getKnowledgeObject ().equals (theResult.getKnowledgeObject ())));
    }

    /**
     * Return a String that shows the attribute values.
     *
     * @return a String that shows the attribute values.
     */
    @Override
	public String toString () {
        StringBuffer theBuf = new StringBuffer (1024);
        theBuf.append ("KnowledgeObject: ");
        theBuf.append (this.getKnowledgeObject ().toString ());
        theBuf.append ("\nRelevance: ");
        theBuf.append (this.getRelevance ());
        theBuf.append ('\n');
        
        return (theBuf.toString ());
    }

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getKnowledgeObject().hashCode();
	}

}

