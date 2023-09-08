/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols;

import com.top_logic.basic.AbstractReloadable;

/**
 * TODO TRI AbstractAuthorisation cares about {@link com.top_logic.basic.Reloadable}.
 *
 * @author    <a href="mailto:tri@top-logic.com">Thomas Trichter</a>
 */
public abstract class AbstractAuthorisation extends AbstractReloadable implements Authorisation {

	/**
	 * Adds this to the ReloadableManager.
	 */
	protected AbstractAuthorisation() {
    	// Not allowed, see super-class constructor. Call addReloadable() from 
    	// the getInstance() method creating an instance of a descendant class:
    	// Registration must be performed after initialization.
    	//
        // ReloadableManager.getInstance ().addReloadable (this);
	}

	/**
	 * a SymbolFactory used to retrieve Symbols.
	 */
	protected abstract SymbolFactory getSymbolFactory();
}