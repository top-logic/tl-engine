/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.misc;

/**
 * Generates unique names.
 * <p>
 * <b>Warning:</b> This class is NOT thread-safe.
 * </p>
 * <p>
 * The names contain only digits.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NameGenerator {

	private long _nextId = 1;

	/**
	 * Generate a new name. Never null.
	 */
	public String newName() {
		return Long.toString(_nextId++);
	}

}
