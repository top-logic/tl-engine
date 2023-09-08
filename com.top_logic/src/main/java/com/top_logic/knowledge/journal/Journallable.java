/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.util.Map;

import com.top_logic.model.TLObject;

/**
 * Provides methods for journalizable objects
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public interface Journallable extends TLObject {
    
    /**
     * Get a journal entry representing the changes 
     * to the journalable made since last commit.
     * 
     * This is called by the JournalMananager during the commit. Be carefull
     * not to call <em>any</em> function that uses the Knowledgebase, it
     * will lead to a certain deadlock.
     * 
     * @param someChanged a map of changed objects commited in the same context
     *                    indexed by ko id, never null.
     * @param someCreated a map of created objects commited in the same context
     *                    indexed by ko id, never null.
     * @param someRemoved a map of removed objects commited in the same context
     *                    indexed by ko id, never null.
     *                    
     * @return null when nothing cna be journaled.
     */
    public JournalEntry getJournalEntry(Map someChanged, Map someCreated, Map someRemoved);
    
    /**
     * Get the ko type the journallable is associated with.
     * This type is used to determin if a givven jounallable is to be journalled.
     */
    public String getJournalType();
}
