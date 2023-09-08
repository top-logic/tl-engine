/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.wrap;

import java.util.Collection;

import com.top_logic.base.workItem.WorkItem;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.state.State;

/**
 * {@link Wrapper} implementing {@link WorkItem} without any context.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class PersistentWrapperWorkItem extends AttributedWrapper implements WorkItem {

    public static final String KO_TYPE = "WorkItem";

    public static final String SUBJECT_ATTRIBUTE     = "subject";
    public static final String TYPE_ATTRIBUTE        = "type";
    public static final String ASSIGNEES_ATTRIBUTE   = "assignees";
    public static final String RESPONSIBLE_ATTRIBUTE = "responsible";

    public PersistentWrapperWorkItem(KnowledgeObject ko) {
        super(ko);
    }

	public static PersistentWrapperWorkItem createWorkItem(String aName, Wrapper aSubject, String aType,
			Person aResponsible, Collection someAssignees) {
        PersistentWrapperWorkItem theItem = PersistentWorkItemFactory.getInstance().createWorkItem();

        theItem.setValue(PersistentWrapperWorkItem.NAME_ATTRIBUTE,        aName);
        theItem.setValue(PersistentWrapperWorkItem.RESPONSIBLE_ATTRIBUTE, aResponsible);
        theItem.setValue(PersistentWrapperWorkItem.ASSIGNEES_ATTRIBUTE,   someAssignees);
        theItem.setValue(PersistentWrapperWorkItem.SUBJECT_ATTRIBUTE,     aSubject);
        theItem.setValue(PersistentWrapperWorkItem.TYPE_ATTRIBUTE,        aType);

        return theItem;
    }

    public static Collection<Wrapper> getWorkitemsOfSubject(Wrapper subject) {
        return getRefereringWorkitems(PersistentWrapperWorkItem.SUBJECT_ATTRIBUTE, subject);
    }

    public static Collection<Wrapper> getWorkitemsOfResponsible(Person responsible) {
        return getRefereringWorkitems(PersistentWrapperWorkItem.RESPONSIBLE_ATTRIBUTE, responsible);
    }

    private static Collection<Wrapper> getRefereringWorkitems(String attribute, Wrapper refereredObject) {
        try {
            TLClass theME = MetaElementFactory.getInstance().getGlobalMetaElement(PersistentWorkItemFactory.STRUCTURE_NAME);
			TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, attribute);
			Collection theWrappers = AttributeOperations.getReferers(refereredObject, theMA);
            return (Collection<Wrapper>) theWrappers;
        }
        catch (NoSuchAttributeException e) {
            throw new ConfigurationError("Attribute " + attribute + " not found.", e);
        }
    }

    @Override
	public String getName() {
        return (String) this.getValue(NAME_ATTRIBUTE);
    }

    @Override
	public Object getSubject() {
        return this.getValue(SUBJECT_ATTRIBUTE);
    }

    @Override
	public String getWorkItemType() {
		String theType = (String) this.getValue(TYPE_ATTRIBUTE);

		if (StringServices.isEmpty(theType)) {
			theType = "defaultType";
		}

		return (KO_TYPE + '.' + TYPE_ATTRIBUTE + '.' + theType);
    }

    @Override
	public Collection getAssignees() {
        return (Collection) this.getValue(ASSIGNEES_ATTRIBUTE);
    }

    @Override
	public State getState() {
        return null;
    }

    @Override
	public Person getResponsible() {
        Person theResponsible = (Person) this.getValue(RESPONSIBLE_ATTRIBUTE);
        if (theResponsible == null) {
			theResponsible = this.getCreator();
        }
        return theResponsible;
    }

}
