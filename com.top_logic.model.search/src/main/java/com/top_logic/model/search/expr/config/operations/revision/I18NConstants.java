/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.revision;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The given commit number {1} must not be negative: {0}
	 */
	public static ResKey2 ERROR_NEGATIVE_COMMIT_NR__EXPR_COMMIT;

	/**
	 * @en The given commit number {1} is unknown: {0}
	 */
	public static ResKey2 ERROR_UNKNOWN_COMMIT_NR__EXPR_COMMIT;

	/**
	 * @en The given revision is null in: {0}.
	 */
	public static ResKey1 ERROR_REVISION_ARGUMENT_NULL__EXPR;

	static {
		initConstants(I18NConstants.class);
	}
}

