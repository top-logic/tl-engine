/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.html;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link I18NException} thrown when a HTML string contains unsafe HTML.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UnsafeHTMLException extends I18NException {

	/**
	 * Creates a new {@link UnsafeHTMLException}.
	 */
	public UnsafeHTMLException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	/**
	 * Creates a new {@link UnsafeHTMLException}.
	 */
	public UnsafeHTMLException(ResKey errorKey) {
		super(errorKey);
	}

}

