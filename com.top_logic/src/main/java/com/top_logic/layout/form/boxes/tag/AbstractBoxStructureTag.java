/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import jakarta.servlet.jsp.JspException;

/**
 * {@link AbstractBoxContainerTag} that does not allow any contents expect other {@link BoxTag}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBoxStructureTag extends AbstractBoxContainerTag {

	@Override
	public int doAfterBody() throws JspException {
		assertNoTextContent();

		return super.doAfterBody();
	}

	/**
	 * Check whether {@link #getBodyContent()} does not contain any non-whitespace.
	 * 
	 * @throws IllegalStateException
	 *         If invalid content is detected in {@link #getBodyContent()}.
	 */
	protected final void assertNoTextContent() throws IllegalStateException {
		String trimmedContent = getBodyContent().getString().trim();
		if (!trimmedContent.isEmpty()) {
			throw new IllegalStateException("Content was produced, where it is not expected: " + trimmedContent);
		}
	}

}
