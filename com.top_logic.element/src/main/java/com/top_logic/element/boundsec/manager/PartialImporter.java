/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface PartialImporter {
    
    /**
	 * Get the handlers for the part to import
	 * 
	 * @return {@link Map} with the name of the tag as key and the
	 *         {@link org.xml.sax.ContentHandler} to use as value.
	 */
    public Map getHandlers();
    
    /**
     * The result of the successfull parsing
     */
    public Object getResult();

    /**
     * Initialize the importer 
     * 
     * @param aStack       the stack of parent handlers used in the environment
     * @param someProblems a list to add the problems to
     */
    public void init(Stack aStack, List someProblems);
}

