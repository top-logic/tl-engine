/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.BoundRole;

/**
 * Simple implementaion of a BoundRole.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class SimpleBoundRole implements BoundRole {

    /** ID of the role */
	private TLID id;

    /** name of the role */
	private String name;

	private TLModule _scope;
    
    /** Construct an new SimpleBoundCommand */
	public SimpleBoundRole(String anID) {
		name = anID;
		id = StringID.valueOf(anID);
    }

    /** Return something reasonable for debugging */ 
    @Override
	public String toString() {
		return IdentifierUtil.toExternalForm(id);
    }

    /** Standard way of computing a hashCode. */ 
    @Override
	public int hashCode() {
        return id.hashCode();
    }

    /** Standard way of computing equals */ 
    @Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
        if (o instanceof BoundRole) {
            BoundRole br = (BoundRole) o;
            return id.equals(br.getID());
        }
        return false;
    }

    /**
     * Get the ID of this command.
     * @return the ID of this command
     */
    @Override
	public TLID getID() {
         return id;
     }
     
    /**
     * @see com.top_logic.tool.boundsec.BoundRole#getName()
     */
    @Override
	public String getName() {
        return name;
    }
    
    /**
     * @see com.top_logic.tool.boundsec.BoundRole#getScope()
     */
    @Override
	public TLModule getScope() {
		return _scope;
    }
    
    @Override
	public void bind(TLModule scope) throws IllegalStateException, IllegalArgumentException {
		if (this._scope != null && this._scope != scope) {
            throw new IllegalStateException("Role already bound to another object!");
        }
        
		if (scope == null) {
            throw new IllegalArgumentException("Cannot bind to null object");
        }
        
		this._scope = scope;
    }
    
    /**
     * @see com.top_logic.tool.boundsec.BoundRole#unbind()
     */
    @Override
	public void unbind() {
		this._scope = null;
    }
    
}
