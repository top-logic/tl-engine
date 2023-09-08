/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.list;

import java.util.Comparator;

import com.top_logic.basic.col.FuzzyListComparator;
import com.top_logic.util.TLCollator;

/**
 * Comparator for lists of {@link FastListElement}s. The lists are compared lexicographically.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FastListElementCollectionComparator extends FuzzyListComparator {

	private final Comparator _fleComparator;

	/**
	 * Creates a new {@link FastListElementCollectionComparator}.
	 * 
	 * @param collator
	 *        The {@link TLCollator} to compare {@link FastListElement} by label if necessary.
	 */
	public FastListElementCollectionComparator(TLCollator collator) {
		_fleComparator = new FastListElementComparator(collator);
	}
	
	@Override
	protected int elementCompare(Object o1, Object o2) {
		return _fleComparator.compare(o1, o2);
	}
	
}
