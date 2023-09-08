/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.TLID;

/**
 * Simple implementation of a journal entry
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class JournalEntryImpl implements JournalEntry {
    
    List   attributes;

	TLID identifier;
    String type;
    
    /**
     * Constructor
     */
    public JournalEntryImpl(TLID anIdentifier, String aType, int assumedSize) {
        this.identifier = anIdentifier;
        this.type = aType;
        this.attributes = new ArrayList(assumedSize);
    }
    
    /**
     * @see com.top_logic.knowledge.journal.JournalEntry#addAttribute(com.top_logic.knowledge.journal.JournalAttributeEntry)
     */
    @Override
	public void addAttribute(JournalAttributeEntry anAttribute) {
        this.attributes.add(anAttribute);
    }
    
    /**
     * @see com.top_logic.knowledge.journal.JournalEntry#getAttributes()
     */
    @Override
	public List getAttributes() {
        return this.attributes;
    }
    
    @Override
	public TLID getIdentifier() {
        return this.identifier;
    }
    
    /**
     * @see com.top_logic.knowledge.journal.JournalEntry#getType()
     */
    @Override
	public String getType() {
        return this.type;
    }
}
