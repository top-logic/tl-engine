/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.tree.model.AbstractTLTreeNode;

/**
 * {@link AbstractGridTypeProvider} for a tree grid.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeGridTypeProvider extends AbstractGridTypeProvider {
	
	/**
	 * Singleton {@link TreeGridTypeProvider} instance.
	 */
	public static final TreeGridTypeProvider INSTANCE = new TreeGridTypeProvider();

	private TreeGridTypeProvider() {
		// Singleton constructor.
	}

	@Override
	protected Object toModel(Object row) {
		return asModel(asRow(asNode(row)));
	}

	private Object asModel(FormMember row) {
		return row.get(GridComponent.PROP_ATTRIBUTED);
	}

	private FormMember asRow(AbstractTLTreeNode<?> node) {
		return (FormMember) node.getBusinessObject();
	}

	private AbstractTLTreeNode<?> asNode(Object anObject) {
		return (AbstractTLTreeNode<?>) anObject;
	}


}
