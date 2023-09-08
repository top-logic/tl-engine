/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols;

import com.top_logic.base.security.authorisation.roles.ACL;

/**
 * Abstract implementation of the {@link com.top_logic.base.security.authorisation.symbols.Symbol} 
 * interface.  The methods of this class can be used to access and manipulate 
 * symbols. As extending class you only have to provide the methods to get a
 * value with a specified key and to set that value.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractSymbolImpl implements Symbol {

    /**
     * Method to get a value from the held map.
     *
     * @param    aName    The name of the requested value.
     * @return   The requested value.
     */
    protected abstract Object getValue (String aName);

    /**
     * Method to set a value to the held map.
     *
     * @param    aName        The name of the value to be set.
     * @param    aValue       The value to be set.
     * @param    overwrite    Flag if overwrite old values.
     */
    protected abstract void setValue (String aName, 
                                      Object aValue, 
                                      boolean overwrite);

    @Override
	public String toString () {
        return (this.getClass ().getName () + " [" +
                    "name: " + this.getSymbolName () +
                    ", allow: (" + this.getAllow ().getACLString () +
                    "), deny: (" + this.getDeny ().getACLString () +
                    ")]");
    }

    /**
     * Method to return the name of the symbol.
     *
     * @return    The name of the symbol.
     */
    @Override
	public String getSymbolName () {
        return ((String) this.getValue (SYMBOL_NAME));
    }

    /**
     * Method to set the name of the symbol.
     *
     * @param    aName    The name of the symbol.
     */
    @Override
	public void setSymbolName (String aName) {
        this.setValue (SYMBOL_NAME, aName, true);
    }

     /**
     * Method to return the value of the allow ACL.
     *
     * @return    The allow ACL.
     */
    @Override
	public ACL getAllow () {
        return ((ACL) this.getValue (ALLOW_ACL));
    }


    /**
     * Method to set the value of the allow ACL.
     *
     * @param    anACL    The allow ACL.
     */
    @Override
	public void setAllow (ACL anACL) {
        this.setValue (ALLOW_ACL, anACL, true);
    }

 
    /**
     * Method to return the value of the deny ACL.
     *
     * @return    The deny ACL.
     */
    @Override
	public ACL getDeny () {
        return ((ACL) this.getValue (DENY_ACL));
    }

    /**
     * Method to set the value of the deny ACL.
     *
     * @param    anACL    The deny ACL.
     */
    @Override
	public void setDeny (ACL anACL) {
        this.setValue (DENY_ACL, anACL, true);
    }

}
