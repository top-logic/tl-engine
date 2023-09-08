/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.util.List;

import com.top_logic.basic.TLID;

/**
 * A JournalEntry holds the change information for a single knowledge object.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public interface JournalEntry {
    
    /**
     * Add change information for an attribute.
     * 
     * @param anAttribute   the change information on a single attribute
     */
    public void addAttribute(JournalAttributeEntry anAttribute);
    
    /**
     * Get all changed attributes.
     * @return  the result contains a journal attribute entry (@see JournalAttributeEntry)
     *     for each changed attribute.
     */
    public List getAttributes();
    
    /**
     * The identifier of the knowledge object the entry refers to.
     * 
     * @return the identifier
     */
    public TLID getIdentifier();
    
    /**
     * get the type of the knowledge object the entry refers to.
     * 
     * @return   the ko type
     */
    public String getType();
}