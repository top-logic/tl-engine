/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.layout.editor.commands.ConfigureTabsCommand;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * Configuration for in application layout editing.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface LayoutEditorConfig extends ConfigurationItem {

	/**
	 * Relative path to {@link LayoutConstants#LAYOUT_BASE_RESOURCE}.
	 */
	public static final String PATH = "path";

	/**
	 * Inner tag name for {@link #getExcludedLayouts()}.
	 * 
	 * @see #getExcludedLayouts()
	 */
	public static final String LAYOUT = "layout";

	/**
	 * Layouts that are forbidden to insert in the current layout.
	 * 
	 * For example when configuring tabs.
	 * 
	 * @see ConfigureTabsCommand
	 */
	@ListBinding(tag = LAYOUT, attribute = PATH)
	List<String> getExcludedLayouts();

}
