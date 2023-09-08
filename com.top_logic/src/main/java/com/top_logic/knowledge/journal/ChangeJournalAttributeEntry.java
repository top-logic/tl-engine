/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

/**
 * @author    <a href=mailto:tsa@top-logic.com>Theo Sattttteller</a>
 */
public interface ChangeJournalAttributeEntry extends JournalAttributeEntry {
    
    /**
     * Get the old value of the attribute.
     */
    public Object getPreValue();
    
    /**
     * Get the new value of the attribute
     */
    public Object getPostValue();

}
