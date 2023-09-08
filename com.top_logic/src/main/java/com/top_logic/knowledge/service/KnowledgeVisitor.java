/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Iterator;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * This is a gerenal base class you can use to traverse the Knowledgebase.
 *
 * @author  Klaus Halfmann
 */
public class KnowledgeVisitor {

    /** Return this to indicate normal progress */
    public static final int PROCEED     = 0x00000;

    /** Return this bit to indicate that the Visit should stop */
    public static final int STOP        = 0x00001;

    /** Return this bit to indicate that No "deeper" Object shold be visited */
    public static final int STOP_DOWN   = 0x00002;

    /** Maximum depth of recursion (to prevent Stack overflow) */
    public static final int MAX_RECURS  = 64;

    /** Current recursive depth */
    protected int depth;

    /** 
     * By default returns all (outgoing) associations.  
     * 
     * Override to return the Associions you actually desire.
     * 
     * @return an Iterator for KnowldeAssocations
     */
    protected Iterator getAssociations(KnowledgeObject from) {
        return from.getOutgoingAssociations();   
    }

    /** Always returns the destination Object.
     * 
     * Ovverride in case you want to use incoming associations.
     */
    protected KnowledgeObject getOtherEnd(KnowledgeAssociation aKA) 
        throws InvalidLinkException
    {
        return aKA.getDestinationObject();
    }
    
    /** Main, recursive visitor function */
    public int visit(KnowledgeObject from) 
        throws InvalidLinkException, DataObjectException {
        if (++depth > MAX_RECURS)
            throw new StackOverflowError("Maximum recursion depth reached");
        int result = visitKO(from);
        if (0 != (result & (STOP | STOP_DOWN)))
            return result;
        result |= visit(getAssociations(from));
        --depth;
        return result;
    }

    /** Visit all Members of the given Iterator
     * 
     * Can be used to iterate over more than one association,
     * when needed by a subclass
     */
    protected int visit(Iterator it) 
        throws InvalidLinkException, DataObjectException {
        int result   = 0;
        while (it.hasNext())
        {
            KnowledgeAssociation ka     = (KnowledgeAssociation) it.next();
            int subResult = visitKA(ka);
            result |= subResult;
            if (0 != (subResult & STOP))
                return result;
            if (0 == (subResult & STOP_DOWN))
                result |= visit(getOtherEnd(ka));
        }
        return result;
    }

    // actual visitor functions

    /** This function is called for every KO found */
    protected int visitKO(KnowledgeObject aKO)
        throws DataObjectException, InvalidLinkException
    {
        return PROCEED;   // Go on visiting the Objects
    }
    
    /** This function is called for every KA found */
    protected int visitKA(KnowledgeAssociation aKA)
        throws DataObjectException, InvalidLinkException
    {
        return PROCEED;    // Go on visiting the Objects      
    }

    // Some examples how to use the vistor you might find usefull 
    
    /** 
     * Search recursivly for an Object with given attribute Value. 
     * 
     * Search will stop in case Attribute is not found.
     * 
     * @param aValue can be null and will be found, too
     */
    public static KnowledgeObject searchByAttribute(KnowledgeObject start, 
            String attrName, Object aValue)
    {
        try {
            AttributeSearcher search = new AttributeSearcher(attrName, aValue);
			search.visit(start);
            return search.found;
		} catch(InvalidLinkException ilx) { /* ignored */
		} catch(DataObjectException  dox) { /* ignored */
        }
        return null;
    }  

    /** Vistor that recursivly searches for an Attribute Values. */
    public static class AttributeSearcher extends KnowledgeVisitor {

        /** The object eventually found. */
        KnowledgeObject found;
        
        /** Name of the attribute we are looking for */        
        String          attrName;  

        /** Desired value of the attribute we are looking for */        
        Object          value;  

        public AttributeSearcher(String aName, Object aValue)
        {
            attrName = aName;   
            value   = aValue;   
        }
        
        /** Check for the name and stop if Found */
        @Override
		protected int visitKO(KnowledgeObject aKO) 
            throws DataObjectException
        {
            Object val = aKO.getAttributeValue(attrName);
            if (val == null) {
                if (value == null) {
                    found = aKO;
                    return STOP;
                }
            }
            else {
                if (val.equals(value)) {
                    found = aKO;
                    return STOP;
                }
            }       
            return PROCEED;   // Go on visiting the Objects
        }
        
        /**
         * Acessor to the object eventually found.
         */
        public KnowledgeObject getFound() {
            return found;
        }
    }

}
