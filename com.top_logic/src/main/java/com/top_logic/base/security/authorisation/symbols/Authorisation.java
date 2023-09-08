/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols;

/**
 * Authorisation tells if a Entity is allowed to do something.
 *
 * <p>In top-logic the creates a context
 * describing the entity (usually a user). Classes implementing
 * this interface will return the symbol for and object identified 
 * by the given symbol in the security context.</p>
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface Authorisation {

	/**
     * Method gets the symbol for a particular name.
	 * 
	 * @param		aSymbol    Identifies the object to be accessed.
	 * @return		The requested symbol.
	 * @exception	SymbolException    If the symbol is unknown.
	 */
	public Symbol getSymbol (String aSymbol) throws SymbolException;
	
}
