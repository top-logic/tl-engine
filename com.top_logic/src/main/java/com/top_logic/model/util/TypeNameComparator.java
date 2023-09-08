/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.Comparator;

import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLType;

/**
 * {@link Comparator} that sorts {@link TLNamedPart}s according to their
 * {@link TLNamedPart#getName()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeNameComparator implements Comparator<TLType> {

	/**
	 * Singleton {@link TypeNameComparator} instance that is stable for types in
	 * named scopes.
	 */
	public static final TypeNameComparator INSTANCE = new TypeNameComparator(ScopeNameComaprator.INSTANCE);
	
	private final Comparator<? super TLType> stableOrder;

	/**
	 * Creates a {@link TypeNameComparator}.
	 * 
	 * @param stableOrder
	 *        The comparator that is used, if local names are equal, but parts
	 *        are not the same.
	 */
	public TypeNameComparator(Comparator<? super TLType> stableOrder) {
		this.stableOrder = stableOrder;
	}
	
	@Override
	public int compare(TLType  p1, TLType p2) {
		if (p1 == p2) {
			return 0;
		}
		
		String p1Name = p1.getName();
		if (p1Name == null) {
			return -1;
		}
		
		String p2Name = p2.getName();
		if (p2Name == null) {
			return 1;
		}
		
		int nameComparision = p1Name.compareTo(p2Name);
		if (nameComparision != 0) {
			return nameComparision;
		}
		
		return stableOrder.compare(p1, p2);
	}

}
