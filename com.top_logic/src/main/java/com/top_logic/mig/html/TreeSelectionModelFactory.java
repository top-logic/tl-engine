/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link DefaultSelectionModelFactory} that unwraps {@link TLTreeNode}s before passing the nodes to
 * the configured filter.
 */
public class TreeSelectionModelFactory extends DefaultSelectionModelFactory {

	/**
	 * Creates a {@link TreeSelectionModelFactory}.
	 */
	public TreeSelectionModelFactory(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected Filter<Object> getSelectionFilter() {
		Filter<Object> result = super.getSelectionFilter();
		if (result == null) {
			return null;
		}

		return value -> result.accept(unwrap(value));
	}

	/**
	 * Unwraps a raw selection value before passing it to the configured filter.
	 */
	protected Object unwrap(Object value) {
		return value instanceof TLTreeNode<?> node ? node.getBusinessObject() : value;
	}

}
