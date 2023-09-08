/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * One node in a role rule path
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class PathElement {

    /** the meta attribute defining the content */
    private TLStructuredTypePart metaAttribute;
    
    /** the association defining the content */
    private final String association;
    /** 
     * indicates that the attribute is to be resolved in revers,
     * i.e. get all objects that hold a given object via the given meta attribute.
     */
    private boolean       inverse;

	private AssociationSetQuery<KnowledgeAssociation> sourceQuery;

	private AssociationSetQuery<KnowledgeAssociation> destQuery;
    
    /**
     * Constructor
     */
    public PathElement(TLStructuredTypePart aMA, boolean isInvers) {
        this.metaAttribute = aMA;
        this.inverse       = isInvers;
        this.association   = null;
        // TODO TSA: add consistency checks: type of attribute, ...
    }
    
    /**
     * Constructor
     */
    public PathElement(String anAssociationType, boolean isInvers) {
        this.association = anAssociationType;
        this.inverse     = isInvers;
        // TODO TSA: add consistency checks: type of attribute, ...

        this.sourceQuery = AssociationQuery.createIncomingQuery(this.association, this.association);
        this.destQuery   = AssociationQuery.createOutgoingQuery(this.association, this.association);
    }
    
    /**
     * Getter
     */
    public TLStructuredTypePart getMetaAttribute() {
        return (metaAttribute);
    }
    
    /**
     * Getter
     */
    public String getAssociation() {
        return (association);
    }
    
    /**
     * Getter
     */
    public boolean isInverse() {
        return (inverse);
    }
    
    /**
     * An object referenced by the rule path so far. This object is the base for the lookup
     * either as source or destination (in case of an inverse path element).
     * 
     * @param aBase   the object to containing the attribute
     * @return the Objects referd to via the attribute, never <code>null</code>
     */
    public Collection getValues(Wrapper aBase) {
        return getValues(aBase, true);
    }
    
    private Collection getValues(Wrapper aBase, boolean isForward) {
        Collection theResult;
        
        if (this.metaAttribute != null) {   
            if (this.isInverse() == isForward) {
				theResult = AttributeOperations.getReferers(aBase, this.metaAttribute);
            } else {
                Object theContent = aBase.getValue(metaAttribute.getName());
                if (theContent instanceof Collection) {
                    theResult = (Collection) theContent;
                } else if (theContent != null) {
                    theResult = Collections.singleton(theContent);
                } else {
                    theResult = Collections.EMPTY_LIST;
                }
            }
        }
        else {
			AssociationSetQuery<KnowledgeAssociation> theQuery =
				(this.isInverse() == isForward) ? this.sourceQuery : this.destQuery;

            theResult = AbstractWrapper.resolveWrappers(aBase, theQuery);
        }
        return theResult != null ? theResult : Collections.EMPTY_SET;
    }
    
    public Collection getSources(Wrapper aDestination) {
        return this.getValues(aDestination, false);
    }
}
