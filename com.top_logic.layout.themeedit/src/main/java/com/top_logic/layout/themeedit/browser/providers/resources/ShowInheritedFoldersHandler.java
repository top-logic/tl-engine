/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * Show inherited folders.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ShowInheritedFoldersHandler extends ToggleCommandHandler {

	/**
	 * Creates a {@link ShowInheritedFoldersHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ShowInheritedFoldersHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(DisplayContext context, LayoutComponent component) {
		return ThemeDirectoryTreeBuilder.showInheritedFolders(component);
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		ThemeDirectoryTreeBuilder.setShowInheritedFolders(component, newValue);

		((TreeComponent) component).resetTreeModel();
	}

}
