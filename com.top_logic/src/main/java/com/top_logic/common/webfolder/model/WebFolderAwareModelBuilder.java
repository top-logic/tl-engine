/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

import com.top_logic.common.webfolder.ui.WebFolderComponent;
import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Model builder for a {@link WebFolderComponent} using a {@link WebFolderAware} as business model.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderAwareModelBuilder implements ModelBuilder {

	/**
	 * Singleton {@link WebFolderAwareModelBuilder} instance.
	 */
	public static final WebFolderAwareModelBuilder INSTANCE = new WebFolderAwareModelBuilder();

	private WebFolderAwareModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return (businessModel instanceof WebFolderAware) ? ((WebFolderAware) businessModel).getWebFolder()
			: businessModel;
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		if (aModel instanceof WebFolder) {
			return true;
		}
		if (aModel instanceof WebFolderAware) {
			WebFolder webFolder = ((WebFolderAware) aModel).getWebFolder();
			return webFolder != null;
		}
		return false;
    }
}
