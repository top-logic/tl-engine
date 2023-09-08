/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols.file;

import com.top_logic.base.security.authorisation.roles.ACL;
import com.top_logic.base.security.authorisation.symbols.Symbol;

/**
 * Implementation for an file based symbol.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class FileSymbol implements Symbol {

    /** The name of the symbol. */
    private String name;

    /** The allow value. */
    private ACL allow;

    /** The deny value. */
    private ACL deny;

    /**
     * Method to return the name of the symbol.
     *
     * @return    The name of the symbol.
     */
    @Override
	public String getSymbolName () {
        return (this.name);
    }
    
    /**
     * Method to set the name of the symbol.
     *
     * @param    aName    The name of the symbol.
     */
    @Override
	public void setSymbolName (String aName) {
        this.name = aName;
    }

     /**
     * Method to return the value of the allow ACL.
     *
     * @return    The allow ACL.
     */    
    @Override
	public ACL getAllow () {
        return (this.allow);
    }

     /**
     * Method to set the value of the allow ACL.
     *
     * @param    anACL    The ACL to be used.
     */
    @Override
	public void setAllow (ACL anACL) {
        this.allow = anACL;
    }

    /**
     * Method to return the value of the deny ACL.
     *
     * @return    The deny ACL.
     */        
    @Override
	public ACL getDeny () {
        return (this.deny);
    }
    
     /**
     * Method to set the value of the deny ACL.
     *
     * @param    anACL    The deny ACL.
     */
    @Override
	public void setDeny (ACL anACL) {
        this.deny = anACL;
    }
}
