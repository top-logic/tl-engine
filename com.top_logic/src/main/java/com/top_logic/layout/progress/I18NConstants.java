/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstants} for this package.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Used as disabled reason when progress not yet finished.
	 */
	public static ResKey PROGRESS_NOT_FINISHED;
	
	public static ResKey PROGRESS_FAILED;

	public static ResKey PROGRESS = legacyKey("tl.common.progress");

	static {
		initConstants(I18NConstants.class);
	}
}
