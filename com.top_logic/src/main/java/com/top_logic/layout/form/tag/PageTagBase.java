/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import jakarta.servlet.jsp.JspException;

/**
 * Common base class for sub tags of {@link PageAreaTag}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PageTagBase extends AbstractTag {

	private PageAreaTag _pageTag;

	/**
	 * The parent {@link PageAreaTag}.
	 */
	protected final PageAreaTag pageTag() {
		return _pageTag;
	}

	@Override
	protected void setup() throws JspException {
		super.setup();

		_pageTag = (PageAreaTag) findAncestorWithClass(this, PageAreaTag.class);
	}

	@Override
	protected void teardown() {
		_pageTag = null;

		super.teardown();
	}

}
