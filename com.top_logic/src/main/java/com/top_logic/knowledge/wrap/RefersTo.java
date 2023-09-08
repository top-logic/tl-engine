/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Wrapper for the "refersTo" {@link com.top_logic.knowledge.objects.KnowledgeAssociation}.
 * <p>
 *  This is not a Wrapper as with KOs but just a Collection of constants and
 *  some utility functions.
 * </p>
 * 
 * @deprecated This is an over-generic class and should not be used as of #2481 .
 *             It should be removed in TL 5.7
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@Deprecated
public class RefersTo {

    /* ANme of the Association */
    public static final String OBJECT_NAME       = "refersTo";

    /** Name of the Attribute that qualifies the Relation */
    public static final String REFERENCE_TYPE    = "referenceType";

    /** As of now there is now way to create a RefersTo Object */
    private RefersTo()  {
        super ();
    }

    /** Create a new "refersTo" Association.
     *
     * @param    aType      The referenceType of the new association.
     * @param    aSource    The source object of the association.
     * @param    aDest      The destination object of the association.
     *
     * @throws  DataObjectException on all sorts of nasty events.
     * @return  The newly created association or null, if fails.
     */
    public static final KnowledgeAssociation create (
            KnowledgeBase base, String aType,
            KnowledgeObject aSource, KnowledgeObject aDest)
            throws DataObjectException {

		KnowledgeAssociation result = base.createAssociation(aSource, aDest, OBJECT_NAME);
        result.setAttributeValue(REFERENCE_TYPE, aType);
        return result;
    }
    
    /**
     * true when aWrapper is {@link RefersTo referred to} by some other KO. 
     */
    public static boolean isReferedTo(Wrapper aWrapper) {
		return aWrapper.tHandle().getIncomingAssociations(OBJECT_NAME).hasNext();
    }

    /**
     * true when aWrapper {@link RefersTo} to some other KO. 
     */
    public static boolean refersTo(Wrapper aWrapper) {
		return aWrapper.tHandle().getOutgoingAssociations(OBJECT_NAME).hasNext();
    }
    
    
}
