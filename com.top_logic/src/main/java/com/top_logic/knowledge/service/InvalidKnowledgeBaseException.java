/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

/**
 * This exception indicates that the given knowledge base is
 * not valid in certain given context.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class InvalidKnowledgeBaseException extends KnowledgeBaseException {

    /**
     * Constructor.
     * 
     * @param   aMessage            a message describing the exception
     * @param   aKnowledgeBase      the knowledge base throwing the exception
     */
    public InvalidKnowledgeBaseException(String aMessage, 
                                         KnowledgeBase aKnowledgeBase) {
        super (aMessage, aKnowledgeBase);
    }
}
