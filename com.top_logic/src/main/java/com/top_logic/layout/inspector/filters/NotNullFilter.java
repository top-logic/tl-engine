/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.filters;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;

/**
 * Accept objects, which have a non-empty {@link InspectorTreeNode business object}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NotNullFilter implements Filter<InspectorTreeNode> {

	/**
	 * Singleton {@link NotNullFilter} instance.
	 */
	public static final NotNullFilter INSTANCE = new NotNullFilter();

	private NotNullFilter() {
		// Singleton constructor.
	}

	@Override
	public boolean accept(InspectorTreeNode anObject) {
		Object value = anObject.getBusinessObject();
		if (value == null) {
			return false;
		}
		if (value instanceof Collection<?> && ((Collection<?>) value).isEmpty()) {
			return false;
		}
		if (value instanceof Map<?, ?> && ((Map<?, ?>) value).isEmpty()) {
			return false;
		}
		if (value instanceof CharSequence && ((CharSequence) value).length() == 0) {
			return false;
		}
		return true;
	}
}