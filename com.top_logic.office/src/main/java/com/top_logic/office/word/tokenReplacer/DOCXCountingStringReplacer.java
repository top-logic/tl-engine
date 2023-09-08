/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.tokenReplacer;

import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Text;

import com.top_logic.base.office.AbstractOffice.TokenReplacer;
import com.top_logic.base.office.ppt.StyledValue;
import com.top_logic.office.word.visitor.DOCXCountingReplaceToken;
import com.top_logic.office.word.visitor.DOCXVisitor;

/**
 * {@link TokenReplacer} that replaces all {@link Text}s matching the given token with the given
 * replacement and adds a counting index to each replaced text.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXCountingStringReplacer extends DOCXStringReplacer {

	@Override
	protected boolean doTextReplacement(String token, Object replacement, ContentAccessor part) {
		if (replacement instanceof StyledValue) {
			return replaceCountingText(token, (String) ((StyledValue) replacement).getValue(), part);
		}
		return replaceCountingText(token, (String) replacement, part);
	}

	/**
	 * Replaces all {@link Text}s in the given {@link ContentAccessor} that matches the given token
	 * with the given replacement and adds a counting to each similar replacement.
	 * 
	 * @param token
	 *        The pattern to search for.
	 * @param replacement
	 *        The replacement used to replace the found tokens with.
	 * @param part
	 *        The {@link ContentAccessor} in which the replacements are performed.
	 * @return <code>true</code> if a replacement has been performed, <code>false</code> otherwise.
	 */
	private boolean replaceCountingText(String token, String replacement, ContentAccessor part) {
		DOCXCountingReplaceToken visitor = new DOCXCountingReplaceToken(token, replacement);
		DOCXVisitor.visit(part, visitor);
		return visitor.isTokenReplaced();
	}
}