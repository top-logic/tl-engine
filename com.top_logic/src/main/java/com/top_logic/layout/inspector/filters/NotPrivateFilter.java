/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.filters;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.inspector.model.nodes.InspectProperty;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;

/**
 * Accept objects, which have a none private {@link InspectorTreeNode}.
 * 
 * @see InspectProperty#isPrivate()
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class NotPrivateFilter implements Filter<InspectorTreeNode> {

	/**
	 * Singleton {@link NotPrivateFilter} instance.
	 */
	public static final NotPrivateFilter INSTANCE = new NotPrivateFilter();

	private NotPrivateFilter() {
		// Singleton constructor.
	}

	@Override
	public boolean accept(InspectorTreeNode anObject) {
		return !anObject.getProperty().isPrivate();
	}
}