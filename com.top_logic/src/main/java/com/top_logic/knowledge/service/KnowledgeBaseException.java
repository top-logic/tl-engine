/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.dob.DataObjectException;

/**
 * This exception is a super class for all knowledge base related exceptions.
 *
 * @author    tsa
 */
public class KnowledgeBaseException extends DataObjectException {

    /**
     * The knowledge base causing the exception.
     */
    KnowledgeBase kBase; 

    /**
     * Constructor with some other, root Cause.
     */
    public KnowledgeBaseException (Throwable aCause) {
        super(aCause);
        kBase = null;
    }
    
    /**
     * Constructor with a Message and a KnowledgeBase.
     */
    public KnowledgeBaseException (String aMessage, KnowledgeBase aKBase) {
        super(aMessage);
        kBase = aKBase;
    }
    
    /**
     * Constructor with a Message and some other, root Cause.
     */
    public KnowledgeBaseException (String aMessage, Throwable aCause) {
        super(aMessage, aCause);
        kBase = null;
    }
    
    /**
     * Return the knowledge base causing the exception.
     * 
     * @return the knowledge base, can be null.
     */
    public KnowledgeBase getKnowledgeBase() {
        return kBase;
    }
}
