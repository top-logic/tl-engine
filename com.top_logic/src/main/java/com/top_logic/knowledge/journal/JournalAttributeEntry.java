/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

/**
 * A journal attribute entry holds the change information
 * of a single knowledge object attribute.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public interface JournalAttributeEntry {
    
    /**
     * Get the name of the refered attribute.
     * 
     * @return the name of the attribute
     */
    public String getName();
    
    /**
     * Get the type of the attribute, TODO TSA what exactly is a type ?
     */
    public String getType();
}