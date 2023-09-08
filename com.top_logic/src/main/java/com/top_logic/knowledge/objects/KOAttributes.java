
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;


/**
 * This interface holds different names of attributes a KnowledgeObject can have.
 * 
 * @author  Klaus Halfmann / Marco Perra
 */
public interface KOAttributes {

	/** Name of the identifier  attribute. */
    public static final String IDENTIFIER = "identifier";
    
    /** Name of the physical resource attribute. */
    public static final String PHYSICAL_RESOURCE = "physicalResource";

    /** Index of the physical resource attribute in Database */
    public static final int    PHYSICAL_RESOURCE_IDX = 2;
}
