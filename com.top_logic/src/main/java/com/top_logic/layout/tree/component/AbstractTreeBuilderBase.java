/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Common base class for {@link StructureModelBuilder} implmentations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class AbstractTreeBuilderBase<N> implements TreeBuilderBase<N> {

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return businessModel;
	}

	@Override
	public boolean canExpandAll() {
		return true;
	}

	@Override
	public boolean isLeaf(LayoutComponent contextComponent, N node) {
		return !getChildIterator(contextComponent, node).hasNext();
	}
}
