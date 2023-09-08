/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.text.Collator;
import java.util.Comparator;

import com.top_logic.layout.LabelProvider;

/**
 * A {@link Comparator} implementation that compares arbitrary objects using a
 * {@link Collator} by first converting them into a textual representation.
 * 
 * <p>
 * For converting compared objects into a textual representation, a
 * {@link LabelProvider} is used.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PlainTextComparator implements Comparator {

	private LabelProvider provider;
	private Collator collator;
	
	public PlainTextComparator(LabelProvider provider, Collator collator) {
		this.provider = provider;
		this.collator = collator;
	}

	@Override
	public int compare(Object o1, Object o2) {
		return collator.compare(provider.getLabel(o1), provider.getLabel(o2));
	}
	
}
