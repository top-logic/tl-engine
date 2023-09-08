/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import org.docx4j.wml.Text;


/**
 * Visits the {@link Text}-elements and can return the first found {@link Text}-element.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXFindFirstText extends DOCXVisitor {
	/** @see #getText() */
	private Text _text;

	/**
	 * The {@link Text}-element that was found first
	 */
	public Text getText() {
		return _text;
	}

	@Override
	protected boolean onVisit(Text element) {
		_text = element;
		return false;
	}
}