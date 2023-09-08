/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.filters;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;

/**
 * Accept objects, which have a none static {@link InspectorTreeNode}.
 * 
 * @see InspectorTreeNode#isStatic()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NotStaticFilter implements Filter<InspectorTreeNode> {

	/**
	 * Singleton {@link NotStaticFilter} instance.
	 */
	public static final NotStaticFilter INSTANCE = new NotStaticFilter();

	private NotStaticFilter() {
		// Singleton constructor.
	}

	@Override
	public boolean accept(InspectorTreeNode anObject) {
		return !anObject.getProperty().isStatic();
	}

}