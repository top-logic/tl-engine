/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

/**
 * Simple implementation of a journal attribute entry.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class JournalAttributeEntryImpl implements ChangeJournalAttributeEntry {
    /** The name of the attribute */
    String attrName;
    /** The old value of the attribute */
    Object oldValue;
    /** The new value of the attribute */
    Object newValue;

    /**
     * Constructor
     * 
     * @param aName   the attribute name
     * @param aPre    the old value
     * @param aPost   the new value
     */
    public JournalAttributeEntryImpl(String aName, Object aPre, Object aPost) {
        this.attrName = aName;
        this.oldValue = aPre;
        this.newValue = aPost;
    }
   
    /**
     * Get the Name
     */
    @Override
	public String getName() {
        return this.attrName;
    }

    /**
     * Get the old value
     */
    @Override
	public Object getPostValue() {
        return this.newValue;
    }

    /**
     * Get the new value
     */
    @Override
	public Object getPreValue() {
        return this.oldValue;
    }

    /**
     * Get the Type
     */
    @Override
	public String getType() {
		final Object value;
		if (newValue != null) {
			value = newValue;
		} else {
			value = oldValue;
		}
		assert value != null : "The new and the old value are both null, but an entry was written";
		return value.getClass().getName();
    }

}
