/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
@Deprecated
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix CHART = legacyPrefix("reporting.chart.");

	public static ResKey ERROR_COMMIT_FAILED;

	public static ResKey ERROR_CREATE_FAILED = legacyKey("reporting.create.failed");

	static {
		initConstants(I18NConstants.class);
	}
}
