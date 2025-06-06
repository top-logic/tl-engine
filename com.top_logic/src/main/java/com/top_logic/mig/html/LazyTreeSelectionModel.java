/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * {@link TreeSelectionModel} that does not store its base {@link TLTreeModel tree model} but
 * fetches it from a {@link Supplier}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LazyTreeSelectionModel<N> extends TreeSelectionModel<N> {

	private Supplier<? extends TLTreeModel<N>> _modelSupplier;

	/**
	 * Creates a {@link LazyTreeSelectionModel}.
	 * 
	 * @param modelSupplier
	 *        Supplier the is used to determine {@link #model()}. The supplier must return the same
	 *        {@link TLTreeModel} at each call. Changing the model is allowed, after call of
	 *        {@link #clear()}.
	 */
	public LazyTreeSelectionModel(SelectionModelOwner owner, Class<N> nodeType,
			Supplier<? extends TLTreeModel<N>> modelSupplier) {
		super(owner, nodeType);
		_modelSupplier = modelSupplier;
	}

	/**
	 * Base {@link TLTreeModel}.
	 */
	protected TLTreeModel<N> model() {
		return _modelSupplier.get();
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
