/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import org.docx4j.wml.P;
import org.docx4j.wml.Text;


/**
 * Finds the first {@link Text}-element on which a given pattern could be matched.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public abstract class DOCXFindText extends DOCXVisitor {
	private Text _text;

	private P _paragraph;

	/** The pattern to match */
	protected final String _pattern;

	/**
	 * Creates a new {@link DOCXFindText}
	 * 
	 * @param pattern
	 *        The pattern to search for
	 */
	public DOCXFindText(String pattern) {
		_pattern = pattern;
	}

	@Override
	protected boolean onVisit(P paragraph) {
		_paragraph = paragraph;
		return true;
	}

	/**
	 * The {@link P} to which the found {@link Text} belongs.
	 */
	public final P getParagraph() {
		if (_text != null) {
			return (_paragraph);
		}
		return null;
	}
	@Override
	protected boolean onVisit(Text element) {
		if (matches(element.getValue())) {
			_text = element;
			return false; // don't go on with this visitor
		}
		return super.onVisit(element);
	}

	/**
	 * The first {@link Text}-element matching the given {@link #_pattern} using the
	 *         {@link #matches(String)}-method
	 */
	public final Text getText() {
		return _text;
	}

	/**
	 * Matches the given {@link #_pattern} in the given text.
	 * 
	 * @param text
	 *        The String to match the {@link #_pattern} on.
	 * @return <code>true</code> if the {@link #_pattern} could be matched in the given text,
	 *         <code>false</code> otherwise.
	 */
	protected abstract boolean matches(String text);
}