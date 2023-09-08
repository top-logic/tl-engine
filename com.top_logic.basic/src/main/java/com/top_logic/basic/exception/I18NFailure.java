/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.exception;

import com.top_logic.basic.util.ResKey;

/**
 * Common interface of {@link Exception}s and {@link RuntimeException}s carrying a {@link ResKey}
 * describing the problem.
 * 
 * @see #getErrorKey()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface I18NFailure {

	/**
	 * The {@link ResKey} describing the problem.
	 */
	ResKey getErrorKey();

}
