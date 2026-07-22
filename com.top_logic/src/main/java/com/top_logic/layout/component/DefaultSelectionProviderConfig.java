/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.component.Selectable.SelectableConfig;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Configuration of a data component that can compute its default selection from the component's
 * model through a {@link DefaultSelectionProvider}.
 */
public interface DefaultSelectionProviderConfig extends SelectableConfig {

	/** @see #getDefaultSelectionProvider() */
	String DEFAULT_SELECTION_PROVIDER = "defaultSelectionProvider";

	/**
	 * Algorithm computing the object to select, if {@link #getDefaultSelection()} is enabled and the
	 * user has not yet chosen an object.
	 *
	 * <p>
	 * If {@link #getDefaultSelection()} is enabled but no value is given here, the first selectable
	 * row or node is chosen by default.
	 * </p>
	 */
	@Name(DEFAULT_SELECTION_PROVIDER)
	@Options(fun = AllInAppImplementations.class)
	@DynamicMode(fun = CommandHandler.ConfirmConfig.VisibleIf.class, args = @Ref(DEFAULT_SELECTION))
	PolymorphicConfiguration<? extends DefaultSelectionProvider> getDefaultSelectionProvider();

}
