/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.TLModule;

/**
 * Representation of a {@link com.top_logic.knowledge.wrap.person.Person}'s role.
 * 
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
@Label("role")
public interface BoundRole {
    
    /**
	 * The ID of this role
	 */
    public TLID getID();
    
    /**
     * The name of the role.
     */
    public String getName();
    
    /**
	 * Binds this role to the given scope.
	 * 
	 * @param scope
	 *        The role defining scope. Must not be <code>null</code>.
	 * @throws IllegalStateException
	 *         if the role is already bound to another object
	 */
	public void bind(TLModule scope) throws IllegalStateException;
    
    /**
	 * Removes this role from the object it is {@link #bind(TLModule) bound} to.
	 */
    public void unbind();
    
    /**
	 * The scope to which this role is {@link #bind(TLModule) bound}.
	 * 
	 * @return The scope of this role, <code>null</code> for a global role.
	 */
    public TLModule getScope();
}
