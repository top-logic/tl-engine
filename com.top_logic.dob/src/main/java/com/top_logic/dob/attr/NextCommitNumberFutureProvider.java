/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import com.top_logic.dob.MOAttribute;

/**
 * {@link MODefaultProvider} returning {@link NextCommitNumberFuture#INSTANCE}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NextCommitNumberFutureProvider implements MODefaultProvider {

	/** Singleton {@link NextCommitNumberFutureProvider} instance. */
	public static final NextCommitNumberFutureProvider INSTANCE = new NextCommitNumberFutureProvider();

	/**
	 * Creates a new {@link NextCommitNumberFutureProvider}..
	 */
	private NextCommitNumberFutureProvider() {
		// No configuration here
	}

	@Override
	public Object createDefault(MOAttribute attribute) {
		return NextCommitNumberFuture.INSTANCE;
	}

}

