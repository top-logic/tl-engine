/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import org.docx4j.wml.Text;


/**
 * Finds the first {@link Text}-element who's value contains the given pattern.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXFindTextContaining extends DOCXFindText {

	/**
	 * Creates a new {@link DOCXFindTextContaining}.
	 * 
	 * @param pattern
	 *        The pattern to be used for matching
	 */
	public DOCXFindTextContaining(String pattern) {
		super(pattern);
	}

	@Override
	protected boolean matches(String text) {
		return text.contains(_pattern);
	}
}