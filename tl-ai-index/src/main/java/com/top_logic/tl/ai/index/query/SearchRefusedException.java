/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

/**
 * A search that must not run, because nobody is named whose rights could decide what it shows.
 *
 * <p>
 * In a system context the engine answers every read check with "allowed", whatever account it is
 * asked about. A search there would disclose every indexed text, so it is refused instead.
 * </p>
 */
public class SearchRefusedException extends RuntimeException {

	/**
	 * Creates a {@link SearchRefusedException}.
	 */
	public SearchRefusedException(String message) {
		super(message);
	}

}
