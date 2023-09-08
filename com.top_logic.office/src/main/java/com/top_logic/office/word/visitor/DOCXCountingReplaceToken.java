/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import org.docx4j.wml.Text;


/**
 * Replaces all {@link Text}-values that contain the given token with the given replacement and adds
 * a counter number to the {@link Text}-value.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXCountingReplaceToken extends DOCXReplaceToken {

	private int _counter;

	/**
	 * Creates a new {@link DOCXCountingReplaceToken}
	 * 
	 * @param token
	 *        The string to replace
	 * @param replacement
	 *        The replacement to replace the token with
	 */
	public DOCXCountingReplaceToken(String token, String replacement) {
		super(token, replacement);
		_counter = 0;
	}

	@Override
	protected String getNewText(String oldText) {
		String newText = oldText.replaceAll(_token, _replacement + "_" + _counter);
		_counter++;
		return newText;
	}
}