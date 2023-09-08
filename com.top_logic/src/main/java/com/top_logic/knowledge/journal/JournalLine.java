/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.util.ArrayList;

/**
 * Represents a complete set of Journal Entries.
 * 
 * This is ususally crated during a commit.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class JournalLine extends ArrayList<JournalEntry> {

    /** The Id of the Person that commited the data. */
    String  userId;

    /** The time the line was created. */
    long  time;
    
    /** 
     * Create a new journal Line.
     * 
     * @param aUser         The user (name/id ?) that commited the data
     * @param assumedSize   The number of expected entries.
     */
    public JournalLine(String aUser, int assumedSize) {
        this(aUser, assumedSize, System.currentTimeMillis());
    }
    
    public JournalLine(String aUser, int assumedSize, long aTime) {
        super(assumedSize);
        this.userId = aUser;
        this.time   = aTime;
    }
    
    /**
     * Getter for the userId
     * 
     * @return the id of the user who initiated the commit the journal line is for.
     */
    public String getUserId() {
        return this.userId;
    }
    
    /**
     * Getter for the creation time
     * 
     * @return the time the journal line was created
     */
    public long getTime() {
        return this.time;
    }
}