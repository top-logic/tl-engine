/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.component.DefaultSelectionProviderConfig;

/**
 * Configuration for displaying tree alike structures.
 *
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface TreeViewConfig extends DefaultSelectionProviderConfig {

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
	 * The configuration parameter for {@link #getRevealSelection()}.
	 */
	String REVEAL_SELECTION_ATTRIBUTE = "revealSelection";

	/**
	 * Whether the selected node should be expanded when selected.
	 *
	 * <p>
	 * This setting only controls expanding the selected node itself, so that its children become
	 * visible. Whether the ancestors of a newly selected node are expanded, so that the selection
	 * itself becomes visible, is controlled by the "make new selection visible" setting.
	 * </p>
	 */
	@Name(EXPAND_SELECTED_ATTRIBUTE)
	@BooleanDefault(true)
	boolean getExpandSelected();

	/**
	 * Whether a newly selected node is made visible by expanding its ancestor nodes.
	 *
	 * <p>
	 * If a selection is set (e.g. from outside, or by creating a new object), the selected node may
	 * be hidden inside a collapsed subtree. If this setting is active, the ancestors of the selected
	 * node are expanded, so that the selection becomes visible to the user. In contrast to the
	 * "expand node on selection" setting, the selected node itself is not expanded, so its children
	 * stay hidden.
	 * </p>
	 */
	@Name(REVEAL_SELECTION_ATTRIBUTE)
	@Label("Make new selection visible")
	@BooleanDefault(true)
	boolean getRevealSelection();

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
