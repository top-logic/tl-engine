/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The workflow definition cannot be exported: {0}
	 */
	public static ResKey1 ERROR_EXPORT_FAILED__DETAILS;

	/**
	 * @en Processes of this workflow have been started; it cannot be deleted. Revoke it instead, so
	 *     that no further processes are started.
	 */
	public static ResKey WORKFLOW_IN_USE;

	static {
		initConstants(I18NConstants.class);
	}
}
