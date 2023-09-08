/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.html;

import com.top_logic.basic.exception.I18NException;

/**
 * Checker for HTML attributes.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface AttributeChecker {

	/**
	 * Checks the given content for unsafe HTML.
	 * 
	 * @param content
	 *        Value of this attribute.
	 * 
	 * @throws I18NException
	 *         When the given content contains unsafe HTML. it is recommend to throw an
	 *         {@link UnsafeHTMLException}.
	 * 
	 * @see UnsafeHTMLException
	 */
	void check(String content) throws I18NException;
}
