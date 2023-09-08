/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import java.security.MessageDigest;

/**
 * XML fragment consisting of a list of {@link Node}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Fragment extends FragmentBase {

	/* package protected */Fragment() {
		// Default constructor.
	}

	@Override
	protected int getLocalWeight() {
		return 0;
	}
	
	@Override
	protected void updateWithLocalData(MessageDigest md5) {
		// Ignore.
	}
	
	@Override
	public <R, A> R visit(NodeVisitor<R, A> v, A arg) {
		return v.visitFragment(this, arg);
	}

}
