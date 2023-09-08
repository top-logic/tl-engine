/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.event;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * This Event indicates a veto on a change of the knowledge base.
 *
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class CommitVetoException extends KnowledgeBaseException {
    
    /**
     * The event causing the veto.
     */
	UpdateEvent rootEvent;
    
    /**
     * The listener rising the veto.
     */
    Object rootListener;

    /**
     * Basic constructor
     */
	public CommitVetoException(String aMessage, UpdateEvent aRootEvent, Object aRootListener) {
        super(aMessage, aRootEvent == null ? null : aRootEvent.getKnowledgeBase());
        rootEvent = aRootEvent;
        rootListener = aRootListener;
    }
    public CommitVetoException (String aMessage, KnowledgeBase aKBase) {
        super(aMessage, aKBase);
    }

	public CommitVetoException(UpdateEvent aRootEvent, Object aRootListener) {
        this(null, aRootEvent, aRootListener);
    }

	public CommitVetoException(UpdateEvent aRootEvent) {
        this(aRootEvent, null);
    }

    /**
     * Returns the rootEvent.
     */
	public UpdateEvent getRootEvent() {
        return rootEvent;
    }

    /**
     * Returns the rootListener.
     * @return Object
     */
    public Object getRootListener() {
        return rootListener;
    }

}
