/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Comparator;


/**
 * {@link Comparator} that applies an inner {@link Comparator} to the
 * {@link TLTreeNode#getBusinessObject()} of a {@link TLTreeNode} object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UserObjectNodeComparator implements Comparator<TLTreeNode> {

	private final Comparator<Object> _userObjectComparator;

	/**
	 * Creates a {@link UserObjectNodeComparator}.
	 * 
	 * @param userObjectComparator
	 *        See {@link #getUserObjectComparator()}.
	 */
	public UserObjectNodeComparator(Comparator<Object> userObjectComparator) {
		this._userObjectComparator = userObjectComparator;
	}

	/**
	 * The inner {@link Comparator} to apply to the {@link TLTreeNode#getBusinessObject()}.
	 */
	public Comparator<Object> getUserObjectComparator() {
		return _userObjectComparator;
	}
	
	@Override
	public int compare(TLTreeNode o1, TLTreeNode o2) {
		return _userObjectComparator.compare(o1.getBusinessObject(), o2.getBusinessObject());
	}
}