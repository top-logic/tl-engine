/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates.node;

/**
 * A folder where script templates are stored in the web application.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateFolder extends TemplateLocation {

	/**
	 * Creates a {@link TemplateFolder}.
	 * 
	 * @param resourceSuffix
	 *        See {@link #getResourceSuffix()}.
	 */
	public TemplateFolder(String resourceSuffix) {
		super(resourceSuffix);
	}


	@Override
	public boolean isTemplate() {
		return false;
	}

}
