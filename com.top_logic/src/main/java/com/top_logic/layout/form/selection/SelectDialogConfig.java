/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.list.ListRenderer;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Configuration options for {@link SelectDialogBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SelectDialogConfig extends PolymorphicConfiguration<SelectDialogProvider> {

	/**
	 * Constant, that indicates, all tree nodes shall be expanded.
	 *
	 * @see #getInitialTreeExpansionDepth()
	 **/
	public static final int SHOW_ALL_NODES = -1;

	/** Name of the {@link #getHeight()} property. */
	String HEIGHT = "height";

	/** Name of the {@link #getWidth()} property. */
	String WIDTH = "width";

	/** Name of the {@link #getShowOptions()} property. */
	String SHOW_OPTIONS = "show-options";

	/** Name of the {@link #getOptionsPerPage()} property. */
	String OPTIONS_PER_PAGE = "options-per-page";

	/** Name of the {@link #getPagableOptionTreshold()} property. */
	String PAGEABLE_OPTION_TRESHOLD = "pageable-option-treshold";

	/** Name of the {@link #isLeftToRight()} property. */
	String LEFT_TO_RIGHT = "left-to-right";

	/** Name of the {@link #getAllowTreeAddAll()} property. */
	String ALLOW_TREE_ADD_ALL = "allow-tree-add-all";

	/** Name of the {@link #getInitialTreeExpansionDepth()} property. */
	String INITIAL_TREE_EXPANSION_DEPTH = "initial-tree-expansion-depth";

	/** Name of the {@link #getDialogListRenderer()} property. */
	String DIALOG_LIST_RENDERER = "dialog-list-renderer";

	/**
	 * Returns the width of the dialog.
	 */
	@Name(WIDTH)
	@IntDefault(720)
	int getWidth();

	/** @see #getWidth() */
	void setWidth(int value);

	/**
	 * Returns the height of the dialog.
	 */
	@Name(HEIGHT)
	@IntDefault(470)
	int getHeight();

	/** @see #getHeight() */
	void setHeight(int value);

	/**
	 * Whether the selection dialog should display select options at all.
	 * 
	 * <p>
	 * A select dialog without select options is only usable for a {@link SelectField} with
	 * {@link SelectField#hasCustomOrder() custom order} to modify the order of the predefined
	 * selection.
	 * </p>
	 */
	@Name(SHOW_OPTIONS)
	@BooleanDefault(true)
	boolean getShowOptions();

	/** @see #getShowOptions() */
	void setShowOptions(boolean value);

	/**
	 * Returns the number of options to show maximal on one page.
	 */
	@Name(OPTIONS_PER_PAGE)
	@IntDefault(150)
	int getOptionsPerPage();

	/** @see #getOptionsPerPage() */
	void setOptionsPerPage(int value);

	/**
	 * Returns the number of options a select field must have, to show not all options on the same
	 * page.
	 */
	@Name(PAGEABLE_OPTION_TRESHOLD)
	@IntDefault(500)
	int getPagableOptionTreshold();

	/** @see #getPagableOptionTreshold() */
	void setPagableOptionTreshold(int value);

	/**
	 * Whether selection dialog should display options on the left side.
	 */
	@Name(LEFT_TO_RIGHT)
	@BooleanDefault(true)
	boolean isLeftToRight();

	/** @see #isLeftToRight() */
	void setLeftToRight(boolean value);

	/**
	 * Whether tree selection dialog should display the add all button.
	 */
	@Name(ALLOW_TREE_ADD_ALL)
	@BooleanDefault(true)
	boolean getAllowTreeAddAll();

	/** @see #getAllowTreeAddAll() */
	void setAllowTreeAddAll(boolean value);

	/**
	 * Tree level depth, up to which tree nodes shall be made visible by expanding their parents.
	 * 
	 * <p>
	 * The level count begins at the first visible tree level. If {@link TreeUIModel#isRootVisible()
	 * the root node is visible}, the root node is level zero, otherwise the root's children are
	 * level zero. Minimum expansion depth is 0, which means, only the first tree level is visible
	 * (no nodes are expanded). Special value {@link #SHOW_ALL_NODES} means, all tree nodes will be
	 * expanded.
	 * </p>
	 * 
	 * @see TreeUIModel#isRootVisible()
	 */
	@Name(INITIAL_TREE_EXPANSION_DEPTH)
	@IntDefault(1)
	int getInitialTreeExpansionDepth();

	/** @see #getInitialTreeExpansionDepth() */
	void setInitialTreeExpansionDepth(int value);

	/**
	 * {@link ListRenderer}, used for display of option list and selection list of the dialog.
	 */
	@Name(DIALOG_LIST_RENDERER)
	@InstanceDefault(OptionRenderer.class)
	@InstanceFormat
	ListRenderer getDialogListRenderer();

	/** @see #getDialogListRenderer() */
	void setDialogListRenderer(ListRenderer value);

	@Override
	@ClassDefault(DefaultSelectDialogProvider.class)
	Class<? extends SelectDialogProvider> getImplementationClass();
}
