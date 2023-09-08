/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.component.Selectable;

/**
 * Configuration for displaying tree alike structures.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface TreeViewConfig extends Selectable.SelectableConfig {

	/**
	 * The configuration parameter for {@link #getExpandSelected()}.
	 */
	String EXPAND_SELECTED_ATTRIBUTE = "expandSelected";

	/**
	 * The configuration parameter for {@link #getExpandRoot()}.
	 */
	String EXPAND_ROOT_ATTRIBUTE = "expandRoot";

	/**
	 * The configuration parameter for {@link #isRootVisible()}.
	 */
	String ROOT_VISIBLE_ATTRIBUTE = "rootVisible";

	/**
	 * The configuration parameter for {@link #adjustSelectionWhenCollapsing()}.
	 */
	String ADJUST_SELECTION_WHEN_COLLAPSING_ATTRIBUTE = "adjustSelectionWhenCollapsing";

	/**
	 * Whether the selected node should be expanded when selected.
	 */
	@Name(EXPAND_SELECTED_ATTRIBUTE)
	@BooleanDefault(true)
	boolean getExpandSelected();

	/**
	 * Whether root should be expanded initially.
	 */
	@Name(EXPAND_ROOT_ATTRIBUTE)
	@BooleanDefault(true)
	boolean getExpandRoot();

	/**
	 * Whether root should be visible.
	 */
	@Name(ROOT_VISIBLE_ATTRIBUTE)
	@BooleanDefault(true)
	boolean isRootVisible();

	/**
	 * Whether collapsing of a node should adjust the current selection.
	 * 
	 * <p>
	 * If value is <code>true</code>, and a node is a collapsed, and the selection is within the
	 * collapsed subtree, then the selection is changed to the collapsed node, i.e. collapsing a
	 * node does not hide a selection.
	 * </p>
	 */
	@Name(ADJUST_SELECTION_WHEN_COLLAPSING_ATTRIBUTE)
	@BooleanDefault(true)
	boolean adjustSelectionWhenCollapsing();

}
