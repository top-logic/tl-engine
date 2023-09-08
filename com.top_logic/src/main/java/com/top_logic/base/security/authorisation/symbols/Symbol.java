/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols;

import com.top_logic.base.security.authorisation.roles.ACL;


/**
 * Symbol interface. The methods of this class can be used to access 
 * and manipulate symbols.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface Symbol {

    /** The name of the symbol. */
    public static final String SYMBOL_NAME = "symbolname";

    /** The ACL for allowed access to the symbol. */
    public static final String ALLOW_ACL   = "allowacl";

    /** The ACL for denied access to the symbol. */
    public static final String DENY_ACL    = "denyacl";

    /**
     * Method to return the name of the symbol.
     *
     * @return    The name of the symbol.
     */
    public String getSymbolName ();
    
    /**
     * Method to set the name of the symbol.
     *
     * @param    aName    The name of the symbol.
     */
    public void setSymbolName (String aName);

    /**
     * Method to return the value of the allow ACL.
     *
     * @return    The allow ACL.
     */    
    public ACL getAllow ();

    /**
     * Method to set the value of the allow ACL.
     *
     * @param    anACL    The ACL to be used.
     */
    public void setAllow (ACL anACL);

    /**
     * Method to return the value of the deny ACL.
     *
     * @return    The deny ACL.
     */        
    public ACL getDeny ();
    
    /**
     * Method to set the value of the deny ACL.
     *
     * @param    anACL    The deny ACL.
     */
    public void setDeny (ACL anACL);
}
