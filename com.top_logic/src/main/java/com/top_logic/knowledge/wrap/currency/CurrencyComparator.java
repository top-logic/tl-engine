/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.currency;

import java.util.Comparator;

/**
 * Compare two Currencies by theire identifier.
 * 
 * (Use a sapcila class in case the idea of ising the ID proves not so good)
 * 
 * @author    <a href="mailto:fma@top-logic.com></a>
 */
public class CurrencyComparator implements Comparator<Currency> {
    
    public static final CurrencyComparator INSTANCE = new CurrencyComparator();
    
    /** Enforce usage of INSTANCE */
    private CurrencyComparator() {
        /* Enforce usage of INSTANCE */
    }
    
    /**
     * Copmpare two Currencies by theire code.
     */
    @Override
	public int compare(Currency o1, Currency o2) {
		return o1.getName().compareTo(o2.getName());
    }
    
}