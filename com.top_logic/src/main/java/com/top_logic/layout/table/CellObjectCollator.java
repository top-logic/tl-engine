/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.text.Collator;
import java.util.Comparator;

import com.top_logic.util.TLContext;

/**
 * String value comparator that unwrapps {@link CellObject}s before comparison.
 * 
 * @since 5.7.3
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CellObjectCollator implements Comparator<CellObject> {
	private final Collator collator = Collator.getInstance(TLContext.getLocale());

	@Override
	public int compare(CellObject o1, CellObject o2) {
		return collator.compare(o1.getValue(), o2.getValue());
	}

	@Override
	public int hashCode() {
		return 31 * 1 + collator.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellObjectCollator other = (CellObjectCollator) obj;
		if (!collator.equals(other.collator))
			return false;
		return true;
	}
}