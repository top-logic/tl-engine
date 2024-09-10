/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.tool.boundsec.CommandHandler.ConfirmConfig.VisibleIf;

/**
 * {@link ToolbarOptions} that allow to hide the toolbar.
 */
public interface OptionalToolbarOptions extends ToolbarOptions {

	/**
	 * @see #hasToolbar()
	 */
	String TOOLBAR = "toolbar";

	/**
	 * Whether a toolbar should be displayed above this component.
	 * 
	 * <p>
	 * Not displaying a toolbar prevents this component from being maximizable and removes the
	 * possibility of collapsing the component within a flexible layout.
	 * </p>
	 * 
	 * <p>
	 * Without a toolbar, some commands in this component may only be accessible through the context
	 * menu.
	 * </p>
	 * 
	 * @see com.top_logic.layout.structure.LayoutControlFactory.Config#getAutomaticToolbars()
	 */
	@Name(TOOLBAR)
	@BooleanDefault(true)
	boolean hasToolbar();

	@Override
	@DynamicMode(fun = VisibleIf.class, args = { @Ref(TOOLBAR) })
	Decision getShowMaximize();

	@Override
	@DynamicMode(fun = VisibleIf.class, args = { @Ref(TOOLBAR) })
	Decision getShowMinimize();

	@Override
	@DynamicMode(fun = VisibleIf.class, args = { @Ref(TOOLBAR) })
	Decision getShowPopOut();

}
