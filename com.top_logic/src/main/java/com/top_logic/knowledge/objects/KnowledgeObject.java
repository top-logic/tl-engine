/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import java.util.Iterator;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.NamedValues;
import com.top_logic.knowledge.service.FlexDataManager;

/**
 * KnowledgeObject (KOs) live in {@link com.top_logic.knowledge.service.KnowledgeBase}s
 * and are related by {@link com.top_logic.knowledge.objects.KnowledgeAssociation}s.
 *<p>
 * If you want define some roles for this KnowledgeObject then 
 * use the attributes defined in the interface ACLAttribute. 
 * As value put a list of the roles separated by comma.
 * For instance 
 *     setAttributeValue(KnowledgeAssociation.READ_ACCESS,"adim,developer")
 *</p>
 *                                  added addAssociation method
 *                                  added getAssociatons method
 *                                  added getIncomingAssociations() with associationType.
 *
 * @author  Marco Perra
 */
public interface KnowledgeObject extends KnowledgeItem {

    /**
     * Returns an iterator over all KnowledgeAssociations for which
	 * the navigation flag of the association-end belonging to <code>this </code> is true.
	 * This are all associations, whose source object == this.
     *
     * Use {@link #getIncomingAssociations()} to retrieve Associations going "back".
     *
     * @return a java.util.Iterator of KnowlegeAssociations
     */
    public Iterator<KnowledgeAssociation> getOutgoingAssociations ();


    /**
     * Returns an iterator over all KnowledgeAssociations for which
	 * the navigation flag of the association-end belonging to the other object is true.
     * This are all associations, whose destination object == this.
     *
     * @return a java.util.Iterator of KnowlegeAssociations
     */
    public Iterator<KnowledgeAssociation> getIncomingAssociations ();

    /**
     * Same as getIncomingAssociations() but return only KAs of given type.
     *
     * @return a java.util.Iterator of KnowlegeAssociations
     */
    public Iterator<KnowledgeAssociation> getIncomingAssociations (String anAssociationType);

    /**
     * returns an Iterator over KnowledgeAssociations matching to the given
     * AssociationType starting 
     *
     * @param   anAssociationType a java.lang.String with the name of an Association
     *                            class.
     * @return a java.util.Iterator of KnowledegeAssociations
     */
    public Iterator<KnowledgeAssociation> getOutgoingAssociations (String anAssociationType);

    /**
     * Returns an iterator over KnowledgeAssociation, i.e. this KnowledgeObject
     * represent the KO("Address") of the office WI. Then the projection to the
     * association-end with anAssociationEndName "employer" and the KnowledgeObject
     * KO("mpe") returns an iterator of all different employer phases.
     * TODO comment is bull...
     *      and please explain: why should two KOs have multiple KAs of same type? 
     *
     * @param   anAssociationType a type of association (MetaObject name)
     * @param   aKnowledgeObject to which the associations of type 'anAssociationType' points
     * @return  a java.util.Iterator of KnowledgeAssociations
     */
    public Iterator<KnowledgeAssociation> getOutgoingAssociations (String anAssociationType, KnowledgeObject aKnowledgeObject);

	/**
	 * The {@link FlexDataManager} that can associate {@link NamedValues} with
	 * this object.
	 */
	@FrameworkInternal
	public FlexDataManager getFlexDataManager();
}
