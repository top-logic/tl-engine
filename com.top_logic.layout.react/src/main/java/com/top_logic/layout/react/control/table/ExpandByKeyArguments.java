/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ModelKeyArguments;

/**
 * The {@link TableViewControl} expand-by-key command: expands or collapses the tree row whose
 * business object has the given {@link #getKey() identity}.
 *
 * <p>
 * The key is the identity the headless projection puts on each row, so an agent copies a row's
 * {@code key} verbatim into this command.
 * </p>
 */
@Label("Set row {key} expanded to {expanded}")
public interface ExpandByKeyArguments extends ModelKeyArguments {

	/** @see #isExpanded() */
	String EXPANDED = ExpandArguments.EXPANDED;

	/**
	 * {@code true} to expand the row, {@code false} to collapse it.
	 */
	@Name(EXPANDED)
	@Mandatory
	boolean isExpanded();

	/** @see #isExpanded() */
	void setExpanded(boolean value);

}
