/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import org.docx4j.wml.R;


/**
 * Visits the {@link R}-elements and can return the first found {@link R}-element.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXFindFirstRun extends DOCXVisitor {
	/** @see #getRun() */
	private R _run;

	/**
	 * The {@link R}-element that was found first
	 */
	public R getRun() {
		return _run;
	}

	@Override
	protected boolean onVisit(R element) {
		_run = element;
		return false;
	}
}
