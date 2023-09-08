/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import java.util.Comparator;

import com.top_logic.config.xdiff.util.Utils;

/**
 * Stable order of {@link Named} objects.
 * 
 * <p>
 * {@link Named} instances are compared first by {@link Named#getNamespace()} and then by
 * {@link Named#getLocalName()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class QNameOrder implements Comparator<Named> {

	/**
	 * Singleton instance of {@link QNameOrder}.
	 */
	public static final Comparator<Named> INSTANCE = new QNameOrder();
	
	private QNameOrder() {
		// Singleton constructor.
	}
	
	@Override
	public int compare(Named o1, Named o2) {
		int namespaceComparision = Utils.compareNullsafe(o1.getNamespace(), o2.getNamespace());
		if (namespaceComparision != 0) {
			return namespaceComparision;
		}
		
		return o1.getLocalName().compareTo(o2.getLocalName());
	}

}
