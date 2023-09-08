/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.workItem;

import java.util.Collection;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.state.State;

/**
 * Wrapper for a work item which belongs to another work item.
 * 
 * This is used, when an EWE work item has been created by an other work item. 
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SubWorkItem implements WorkItem {

    /** The parent item, the other one belongs to. */
    WorkItem parent;

    /** The child item represented by this. */
    WorkItem child;

    /** 
     * Create a new instance of this class.
     * 
     * @param    aParent    The parent item.
     * @param    aChild     The child item.
     */
    public SubWorkItem(WorkItem aParent, WorkItem aChild) {
        this.parent = aParent;
        this.child  = aChild;
    }

    // Implememtantion of interface WorkItem

    /** 
     * @see com.top_logic.base.workItem.WorkItem#getAssignees()
     */
    @Override
	public Collection getAssignees() {
        return this.child.getAssignees();
    }

    /** 
     * @see com.top_logic.base.workItem.WorkItem#getName()
     */
    @Override
	public String getName() {
        return this.child.getName();
    }

    /** 
     * @see com.top_logic.base.workItem.WorkItem#getState()
     */
    @Override
	public State getState() {
        return this.child.getState();
    }

    /** 
     * @see com.top_logic.base.workItem.WorkItem#getSubject()
     */
    @Override
	public Object getSubject() {
        return this.child.getSubject();
    }

    @Override
	public String getWorkItemType() {
		return this.child.getWorkItemType();
    }

    /** 
     * Return a debugging output of this object.
     * 
     * @return    A debugging description of this object.
     * @see       java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " [" + this.toStringValues() + ']';
    }

    /** 
     * Return the values of this object.
     * 
     * @return    The values of this object.
     * @see       #toString()
     */
    protected String toStringValues() {
        return "parent: " + this.parent + ", child: " + this.child;
    }

    public WorkItem getOriginalWorkItem() {
        return (child);
    }

    public WorkItem getParent() {
        return (parent);
    }
    
    @Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
        if (obj instanceof SubWorkItem) {
            return this.child.equals(((SubWorkItem) obj).child);
        } else {
            return false;
        }
    }
    
    @Override
	public Person getResponsible() {
        return this.child.getResponsible();
    }
    
    @Override
	public int hashCode() {
        return this.child.hashCode();
    }
    
    public int compareTo(Object o) {
        return this.getName().compareTo(((WorkItem) o).getName());
    }
}
