/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import java.util.Comparator;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;

/**
 * {@link Comparator} for {@link TLObject}'s creating an ordering where {@link TLType}'s are ordered
 * first with {@link TLClass}'s at the beginning.
 */
public class TLTypesFirstComparator implements Comparator<TLObject> {

	/**
	 * Singleton instance.
	 */
	public static final TLTypesFirstComparator INSTANCE = new TLTypesFirstComparator();

	@Override
	public int compare(TLObject o1, TLObject o2) {
		if (o1 instanceof TLType && !(o2 instanceof TLType)) {
			return -1;
		} else if (!(o1 instanceof TLType) && o2 instanceof TLType) {
			return 1;
		} else if (o1 instanceof TLType && o2 instanceof TLType) {
			if (o1 instanceof TLClass) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return 0;
		}
	}

}
