/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderModelBuilder implements ModelBuilder {

	/**
	 * Singleton {@link WebFolderModelBuilder} instance.
	 */
	public static final WebFolderModelBuilder INSTANCE = new WebFolderModelBuilder();

	private WebFolderModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return businessModel;
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return (aModel instanceof WebFolder);
    }
}

