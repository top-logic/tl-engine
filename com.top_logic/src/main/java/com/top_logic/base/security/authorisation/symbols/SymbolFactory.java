/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols;

import java.util.List;


/**
 * SymbolFactory interface for providing access to the various implementations 
 * of the symbol factories.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface SymbolFactory {

	public static final String SERVICE_NAME = "SymbolFactory";
    /**
     * Method to create a new instance of symbol
     *
     * @return    A new, empty symbol object.
     */
    public Symbol createSymbol ();

    /**
     * Method to load the requested symbol from the system.
     *
     * @param 	aName    The name of the required symbol.
     * @return	The symbol, contains user information for the requested name.
     */
    public Symbol loadSymbol (String aName);

    /**
     * Return all symbols known by the system.
     *
     * @return    A list of known symbols.
     */
    public List getAllSymbols ();

    /**
     * Method to save alterations to a symbols information to the ldap directory.
     *
     * @param    aSymbol    The object containing the symbol information 
     * 					    to be saved to the directory.
     */
    public void saveSymbol (Symbol aSymbol);

    /**
     * Method to add a new Symbol to the directory.
     *
     * @param    aSymbol    The user details to be added to the directory.
     */
    public void addSymbol (Symbol aSymbol);

    /**
      * Method to make multiple searches to the directory.
      *
      * @param    aName     The name of the symbol's attribute.
      * @param    aValue    The value of the symbol's attribute.
      * @return   A list of symbol objects. 
      */
    public List getSelectedSymbols (String aName, String aValue);

    /**
     * Method to delete a named Symbol from the directory.
     *
     * @param    aName    The name of the Symbol to be deleted.
     */
    public void deleteSymbol (String aName);

}// SymbolFactory()---------------------------------------------------
