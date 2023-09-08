/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

/**
 * This interface provides a journal entry that consists of a message and a cause.
 * 
 * The legitimation for this type of entry is the ability of wrappers
 * to store information about created and deleted associations in textual form
 * in such entries.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 *
 */
public interface MessageJournalAttributeEntry extends
        JournalAttributeEntry {

    /**
     * Get the massage that is to be journalled.
     * 
     * Every implementation can return whatever is desired, 
     * but most often these are keys for some i18n.
     * 
     * @return null is OK, but makes no sense
     */
    public String getJournalMessage();
    
    /**
     * Get the cause for this entry .
     * 
     * Every implementation can return whatever is desired, 
     * but most often these are keys for some i18n.
     * 
     * @return null is OK, but makes no sense
     */
    public String getCause();
}
