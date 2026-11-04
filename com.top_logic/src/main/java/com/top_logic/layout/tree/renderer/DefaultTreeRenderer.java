/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeRenderer;


/**
 * Default final {@link TreeRenderer} without configuration options. 
 * 
 * @see ConfigurableTreeRenderer for configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DefaultTreeRenderer extends TreeRenderer {

	/** Name of the HTML element containing the tree. */
	public static final String CONTROL_TAG = DIV;
	/** Name of the HTML element of a tree node. */
	public static final String NODE_TAG = DIV;
	
	/** Singleton {@link DefaultTreeRenderer} instance. */
	public static final TreeRenderer INSTANCE = new DefaultTreeRenderer();

	private DefaultTreeRenderer() {
		// Singleton constructor.
	}

	@Override
	protected String getControlTag(TreeControl control) {
		return CONTROL_TAG;
	}
	
	@Override
	public String getNodeTag() {
		return NODE_TAG;
	}

	@Override
	public TreeContentRenderer getTreeContentRenderer() {
		return DefaultTreeContentRenderer.INSTANCE;
	}

}
