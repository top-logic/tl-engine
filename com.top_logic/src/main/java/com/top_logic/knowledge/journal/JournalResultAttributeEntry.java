/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

/**
 * TODO TSA this interface ...
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public interface JournalResultAttributeEntry {
    
    /**
     * the name of the attribute journaled.
     */
    public String getName();
    
    /**
     * TODO tsa the type of a JournalResultAttributeEntry is a classname
     */
    public String getType();

}
