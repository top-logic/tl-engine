/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.list;

import java.util.Comparator;

import com.top_logic.knowledge.gui.layout.FastListElementResourceProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.util.TLCollator;

/**
 * Comparator for {@link TLClassifier}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FastListElementComparator implements Comparator<TLClassifier> {

	private final TLCollator _collator;

	private final LabelProvider _fleLabels = FastListElementResourceProvider.INSTANCE;

	/**
	 * Creates a new {@link FastListElementComparator}.
	 * 
	 * @param collator
	 *        The {@link TLCollator} to compare {@link TLClassifier} when comparison depends on the
	 *        labels of the elements.
	 */
	public FastListElementComparator(TLCollator collator) {
		_collator = collator;
	}

	@Override
	public int compare(TLClassifier o1, TLClassifier o2) {
		if (o1 == o2) {
			return 0;
		}
		TLEnumeration o2List = o2.getOwner();
		TLEnumeration o1List = o1.getOwner();

		if (o1List.equals(o2List)) {
			if (o1List.isOrdered()) {
				return compareByOrder(o1, o2);
			} else {
				return compareByLabel(o1, o2);
			}
		} else {
			return compareList(o2List, o1List);
		}
	}

	private int compareList(TLEnumeration o2List, TLEnumeration o1List) {
		return o1List.getName().compareTo(o2List.getName());
	}

	private int compareByLabel(TLClassifier o1, TLClassifier o2) {
		return _collator.compare(_fleLabels.getLabel(o1), _fleLabels.getLabel(o2));
	}

	private int compareByOrder(TLClassifier o1, TLClassifier o2) {
		int indexComparison = Integer.compare(o1.getIndex(), o2.getIndex());
		if (indexComparison == 0) {
			return o1.getName().compareTo(o2.getName());
		}
		return indexComparison;
	}

}

