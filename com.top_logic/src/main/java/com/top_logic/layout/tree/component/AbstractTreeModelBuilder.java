/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.col.TreeView;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.export.NoPreload;
import com.top_logic.model.export.PreloadOperation;

/**
 * Common base class for {@link TreeModelBuilder} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTreeModelBuilder<N> extends AbstractTreeBuilderBase<N> implements TreeModelBuilder<N>,
		TreeView<N> {

	// No additional default implementations.

	@Override
	public PreloadOperation loadForExpansion() {
		return NoPreload.INSTANCE;
	}

	@Override
	public Iterator<? extends N> getChildIterator(LayoutComponent contextComponent, N node) {
		return getChildIterator(node);
	}

	@Override
	public boolean isLeaf(LayoutComponent contextComponent, N node) {
		return isLeaf(node);
	}

	@Override
	public boolean isLeaf(N node) {
		return !getChildIterator(node).hasNext();
	}

	@Override
	public boolean isFinite() {
		return canExpandAll();
	}

	@Override
	public Collection<? extends N> getNodesToUpdate(LayoutComponent contextComponent, Object businessObject) {
		return Collections.emptySet();
	}

	@Override
	public Set<TLStructuredType> getTypesToObserve() {
		return Collections.emptySet();
	}

}
