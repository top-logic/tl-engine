/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols;

import java.util.Comparator;

/**
 * Comparator for symbols.
 *
 * This comparator compares the name of the symbol.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SymbolComparator implements Comparator {

	/**
	 * Singleton {@link SymbolComparator} instance.
	 */
	public static final SymbolComparator INSTANCE = new SymbolComparator();

	private SymbolComparator() {
		// Singleton constructor.
	}

    @Override
	public int compare (Object obj1, Object obj2) {
        String theName1 = ((Symbol) obj1).getSymbolName ();
        String theName2 = ((Symbol) obj2).getSymbolName ();

        return (theName1.compareTo (theName2));
    }
}
