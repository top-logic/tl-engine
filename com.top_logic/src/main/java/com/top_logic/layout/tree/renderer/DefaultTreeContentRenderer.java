/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Default {@link TreeContentRenderer} without configuration options.
 * 
 * @see ConfigurableTreeContentRenderer for configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DefaultTreeContentRenderer extends AbstractTreeContentRenderer {

	public static final TreeContentRenderer INSTANCE = new DefaultTreeContentRenderer();
	
	private DefaultTreeContentRenderer() {
		// Singleton constructor.
	}
	
	public String getNodeTag() {
		return DIV;
	}

	@Override
	protected  TreeImageProvider getTreeImages() {
		return DefaultTreeImageProvider.INSTANCE;
	}
	
	@Override
	public ResourceProvider getResourceProvider() {
		return MetaResourceProvider.INSTANCE;
	}

}
