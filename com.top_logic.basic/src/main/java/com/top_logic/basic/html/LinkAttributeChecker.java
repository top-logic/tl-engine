/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.html;

import java.util.regex.Pattern;

import com.top_logic.basic.exception.I18NException;

/**
 * Checker for the href attribute of a HTML anchor tag.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LinkAttributeChecker implements AttributeChecker {

	private static final Pattern JAVASCRIPT = Pattern.compile("\\s*javascript\\s*:", Pattern.CASE_INSENSITIVE);

	@Override
	public void check(String content) throws I18NException {
		if (JAVASCRIPT.matcher(content).lookingAt()) {
			throw new UnsafeHTMLException(I18NConstants.NO_JAVASCRIPT_ALLOWED);
		}
	}

}
