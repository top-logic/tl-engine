/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

/**
 * Result attribute entry that holds a message based result attribute
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 *
 */
public class MessageJournalResultAttributeEntry implements
        JournalResultAttributeEntry {
    /** the name of the attribute */
    private String name;
    /** the type of the attribute */
    private String type;
    /** the cause for the entry */
    private String cause;
    /** the journalled message */
    private String message;

    public MessageJournalResultAttributeEntry(String aName, String aType, String aCause, String aMessage) {
        this.name = aName;
        this.type = aType;
        this.cause = aCause;
        this.message = aMessage;
    }

    /** Getter for name */
    @Override
	public String getName() {
        return this.name;
    }
    /** Getter for type */
    @Override
	public String getType() {
        return this.type;
    }
    /** getter for cause */
    public String getCause() {
        return this.cause;
    }
    /** Getter for message */
    public String getMessage() {
        return this.message;
    }

}
