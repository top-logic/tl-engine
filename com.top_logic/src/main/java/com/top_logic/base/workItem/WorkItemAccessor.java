/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.workItem;

import com.top_logic.basic.Logger;
import com.top_logic.layout.Accessor;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class WorkItemAccessor implements Accessor {

    public static final String ATTRIBUTE_KEY_NAME          = "name";
    public static final String ATTRIBUTE_KEY_SUBJECT       = "subject";
    public static final String ATTRIBUTE_KEY_TYPE          = "type";
    public static final String ATTRIBUTE_KEY_ASSIGNEE      = "assignee";
    public static final String ATTRIBUTE_KEY_STATE         = "state";
    public static final String ATTRIBUTE_SELF              = "self";
    public static final String ATTRIBUTE_KEY_RESPONSIBLE   = "responsible";

    /** 
     * Create a new instance of this class.
     */
    public WorkItemAccessor() {
        super();
    }

    /** 
     * Provide the mapping from constants defined by this class to the matching methods in {@link WorkItem}.
     * 
     * @param    anObject    The object to get a value from, must be a {@link WorkItem}.
     * @param    aKey        One of the constants defined by this class, must not be <code>null</code>. 
     * @return   The value from the item, may be <code>null</code>.
     * @see      com.top_logic.layout.Accessor#getValue(java.lang.Object, java.lang.String)
     */
    @Override
	public Object getValue(Object anObject, String aKey) {
        WorkItem theItem = (WorkItem) anObject;

        if (ATTRIBUTE_SELF.equals(aKey)) {
            return theItem;
        }
        else if (ATTRIBUTE_KEY_NAME.equals(aKey)) {
            return theItem.getName();
        }
        else if (ATTRIBUTE_KEY_SUBJECT.equals(aKey)) {
            return theItem.getSubject();
        }
        else if (ATTRIBUTE_KEY_TYPE.equals(aKey)) {
			return theItem.getWorkItemType();
        }
        else if (ATTRIBUTE_KEY_ASSIGNEE.equals(aKey)) {
            return theItem.getAssignees();
        }
        else if (ATTRIBUTE_KEY_STATE.equals(aKey)) {
            return theItem.getState();
        }
        else if (ATTRIBUTE_KEY_RESPONSIBLE.equals(aKey)) {
            return theItem.getResponsible();
        }
        else {
            Logger.error("Invalid atribute for work item.", this);
            return null;
        }
    }

    /** 
     * Method will do nothing, because here we don't know, how to store something 
     * to a {@link WorkItem}.
     * 
     * @param    object      Will be ignored.
     * @param    property    Will be ignored.
     * @param    value       Will be ignored.
     * @see      com.top_logic.layout.Accessor#setValue(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
	public void setValue(Object object, String property, Object value) {
        // just do nothing
    }

}
