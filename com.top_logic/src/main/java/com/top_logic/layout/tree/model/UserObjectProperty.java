/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.ReadOnlyPropertyAccessor;

/**
 * {@link PropertyAccessor} that unwraps a {@link TLTreeNode}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UserObjectProperty extends ReadOnlyPropertyAccessor<TLTreeNode> {
	
	/**
	 * Singleton {@link UserObjectProperty} instance.
	 */
	public static final UserObjectProperty INSTANCE = new UserObjectProperty();

	private UserObjectProperty() {
		// Singleton constructor.
	}
	
	@Override
	public Object getValue(TLTreeNode anObject) {
		return anObject.getBusinessObject();
	}
	
}