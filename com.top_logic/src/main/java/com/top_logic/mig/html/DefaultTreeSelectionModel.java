/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.List;

import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * Default {@link TreeSelectionModel} implementation with constant {@link TLTreeModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultTreeSelectionModel<N> extends SubtreeSelectionModel<N> {

	private TLTreeModel<N> _model;

	/**
	 * Creates a {@link DefaultTreeSelectionModel}.
	 */
	public DefaultTreeSelectionModel(SelectionModelOwner owner, Class<N> nodeType, TLTreeModel<N> model) {
		super(owner, nodeType);
		_model = model;
	}

	/**
	 * Base {@link TLTreeModel}.
	 */
	protected TLTreeModel<N> model() {
		return _model;
	}

	@Override
	protected N parent(N node) {
		return model().getParent(node);
	}

	@Override
	protected List<? extends N> children(N node) {
		return model().getChildren(node);
	}

}
