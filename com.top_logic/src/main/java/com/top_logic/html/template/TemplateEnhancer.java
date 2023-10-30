/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

/**
 * An algorithm building a special template out of an annotated start tag.
 */
public interface TemplateEnhancer {

	/**
	 * Creates the final template out of an annotated simple template.
	 *
	 * @param inner
	 *        The content template to enhance with functionality.
	 * @return The final template.
	 */
	HTMLTemplateFragment build(HTMLTemplateFragment inner);

}
