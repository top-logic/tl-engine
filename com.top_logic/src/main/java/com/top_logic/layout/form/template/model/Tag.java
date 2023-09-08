/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.StartTagTemplate;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.mig.html.HTMLConstants;


/**
 * Template creating a well-formed HTML tag.
 * 
 * @implNote Interface is kept for legacy compatibility.
 * 
 * @see TagTemplate
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Tag extends HTMLTemplateFragment {

	/**
	 * The start tag.
	 */
	StartTagTemplate getStart();

	/**
	 * The name of the tag to render, e.g. {@value HTMLConstants#DIV}.
	 */
	default String getName() {
		return getStart().getName();
	}

	/**
	 * The tag contents.
	 */
	HTMLTemplateFragment getContent();
}