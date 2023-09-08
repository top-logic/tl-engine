/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;
import java.util.List;

/**
 * Comparator that compares lists lexicographically.
 * 
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ListComparator<E> extends AbstractListComparator<E> implements Comparator<List<E>> {

	@Override
	public int compare(List<E> o1, List<E> o2) {
		if (o1 == o2) {
			return 0;
		}
		return compareLists(o1, o2);
	}


}

