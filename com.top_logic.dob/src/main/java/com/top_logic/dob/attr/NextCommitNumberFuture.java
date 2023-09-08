/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

/**
 * Class that represents the value of a current commit. 
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class NextCommitNumberFuture {

	/**
	 * Object that can used as value that represents the commit number of the
	 * current commit.
	 */
	public static final NextCommitNumberFuture INSTANCE = new NextCommitNumberFuture();

	private NextCommitNumberFuture() {
		// singleton instance
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
