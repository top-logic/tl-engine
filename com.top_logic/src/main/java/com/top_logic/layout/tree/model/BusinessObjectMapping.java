/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.col.Mapping;

/**
 * Mapping of a {@link TLTreeNode} to its {@link TLTreeNode#getBusinessObject() business object}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BusinessObjectMapping implements Mapping<TLTreeNode<?>, Object> {

	/** Singleton {@link BusinessObjectMapping} instance. */
	public static final BusinessObjectMapping INSTANCE = new BusinessObjectMapping();

	/**
	 * Creates a new {@link BusinessObjectMapping}.
	 */
	protected BusinessObjectMapping() {
		// singleton instance
	}

	@Override
	public Object map(TLTreeNode<?> input) {
		return input.getBusinessObject();
	}

}

