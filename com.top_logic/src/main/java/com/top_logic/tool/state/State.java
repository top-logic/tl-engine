/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state;

import java.util.Collection;

/**
 * Most basic part of a state machine.
 * 
 * States are kind of singletons (as seen in the {@link StateManager}).
 * 
 * @author    <a href="mailto:tgi@top-logic.com>TGI</a>
 */
public interface State {

	/**
	 * A state must have a unique key for all its implementing classes. 
	 */
	String getKey();
    
    /**
     * The {@link com.top_logic.tool.boundsec.BoundRole}s which may switch to this state.
     * 
     * @return null when everybody is allowed to switch.
     */
    public Collection getRoles();
    
    /**
     * A List of states reachable from this state.
     * 
     * @return null when this is a final state.
     */
    public Collection getSuccessors();
    
    /** A final {@link State} has no successors. */
    public boolean isFinal();
    
    
}