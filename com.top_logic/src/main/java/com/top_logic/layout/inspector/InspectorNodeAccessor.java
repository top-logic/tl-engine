/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;

/**
 * Provide access to the values in an {@link InspectorTreeNode}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class InspectorNodeAccessor {

	/**
	 * @see InspectorTreeNode#staticType()
	 */
	public static class StaticType extends ReadOnlyAccessor<InspectorTreeNode> {
		@Override
		public Object getValue(InspectorTreeNode node, String property) {
			return node.staticType();
		}
	}

	/**
	 * @see InspectorTreeNode#visibility()
	 */
	public static class Visibility extends ReadOnlyAccessor<InspectorTreeNode> {
		@Override
		public Object getValue(InspectorTreeNode node, String property) {
			return node.visibility();
		}
	}

	/**
	 * @see InspectorTreeNode#isStatic()
	 */
	public static class IsStatic extends ReadOnlyAccessor<InspectorTreeNode> {
		@Override
		public Object getValue(InspectorTreeNode node, String property) {
			return node.isStatic();
		}
	}

}
