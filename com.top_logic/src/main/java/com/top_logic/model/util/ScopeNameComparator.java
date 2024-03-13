/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.Comparator;

import com.top_logic.basic.col.Equality;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;

/**
 * Comparator that compares {@link TLType}s according to their scope names.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScopeNameComparator implements Comparator<TLType> {

	/**
	 * Singleton {@link ScopeNameComparator} instance.
	 */
	public static final ScopeNameComparator INSTANCE = new ScopeNameComparator(Equality.INSTANCE);
	
	private final Comparator<? super TLType> stableOrder;

	private ScopeNameComparator(Comparator<? super TLType> stableOrder) {
		this.stableOrder = stableOrder;
	}
	
	@Override
	public int compare(TLType t1, TLType t2) {
		TLScope scope1 = t1.getScope();
		TLScope scope2 = t2.getScope();
		if (scope1 != scope2) {
			if (scope1 == null) {
				return -1;
			}
			
			if (scope2 == null) {
				return 1;
			}
			
			if ((scope1 instanceof TLModule) && (scope2 instanceof TLModule)) {
				int moduleComparision = ((TLModule) scope1).getName().compareTo(((TLModule) scope2).getName());
				if (moduleComparision != 0) {
					return moduleComparision;
				}
			}
		}
		
		return stableOrder.compare(t1, t2);
	}

}
