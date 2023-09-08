/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.workItem;

import java.util.Collection;

import com.top_logic.tool.state.State;

/**
 * This is a very simple implementation of {@link State}. 
 * It's just used to allow arbitrary {@link WorkItem} implementations to provide
 * some sort of a state. 
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class SimpleState implements State {
    
    String     key;
    Collection successors;
    boolean    isFinal;

    public SimpleState(String aKey, Collection someSuccessors, boolean isFinal) {
        super();
        this.key        = aKey;
        this.successors = someSuccessors;
        this.isFinal    = isFinal;
    }

    @Override
	public String getKey() {
        return this.key;
    }

    @Override
	public Collection getRoles() {
        return null;
    }

    @Override
	public Collection getSuccessors() {
        return this.successors;
    }

    @Override
	public boolean isFinal() {
        return this.isFinal;
    }

}
