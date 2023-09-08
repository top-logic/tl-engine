/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

/**
 * @author    <a href=mailto:tsa@top-logic.com>Seo Tattler</a>
 */
public class MessageJournalAttributeEntryImpl implements
        MessageJournalAttributeEntry {

    /** the attribute name the entry is for */
    private String name;

    /** the attribute type the entry is for (Nobody really cares about this) */
    private String type;
    
    /** the cause for the entry */
    private String cause;
    /** the message to be journalled */
    private String message;

    /**
     * Constructor
     */
    public MessageJournalAttributeEntryImpl(String aName, String aType, String aCause, String aMassage) {
        this.name = aName;
        if (this.name != null && this.name.length() > 70) {
        	this.name = this.name.substring(0, 63) + "...";
        }
        this.type = aType;
        this.cause = aCause;
        this.message = aMassage;
    }
    /** Getter for message */
    @Override
	public String getJournalMessage() {
        return this.message;
    }
    /** Getter for cause */
    @Override
	public String getCause() {
        return this.cause;
    }
    /** Getter for name */
    @Override
	public String getName() {
        return this.name;
    }
    
    /** Nobody really cares about this) */
    @Override
	public String getType() {
        return this.type;
    }

}
