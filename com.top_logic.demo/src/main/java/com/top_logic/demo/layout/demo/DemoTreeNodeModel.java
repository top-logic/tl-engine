/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.LabelResourceProvider;

/**
 * Demo objects building a tree.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoTreeNodeModel {
	boolean selectable;

	private String _name;

	private int _depth;

	private DemoTreeNodeModel _parent;

	public DemoTreeNodeModel(DemoTreeNodeModel parent, String name, int depth) {
		this(parent, name, depth, true);
	}
	
	public DemoTreeNodeModel(DemoTreeNodeModel parent, String name, int depth, boolean selectable) {
		_parent = parent;
		_name = name;
		_depth = depth;
		this.selectable = selectable;
	}

	/**
	 * The parent node.
	 */
	public DemoTreeNodeModel getParent() {
		return _parent;
	}

	public String getName() {
		return _name;
	}

	public String getType() {
		return "Depth " + _depth;
	}

	public boolean isSelectable() {
		return selectable;
	}

	@Override
	public String toString() {
		return getName();
	}

	public int getDepth() {
		return _depth;
	}

	public static class LP implements LabelProvider {

		/**
		 * Singleton {@link DemoTreeNodeModel.LP} instance.
		 */
		public static final ResourceProvider INSTANCE = LabelResourceProvider.toResourceProvider(new LP());

		private LP() {
			// Singleton constructor.
		}

		@Override
		public String getLabel(Object object) {
			return ((DemoTreeNodeModel) object).getName();
		}

	}
}