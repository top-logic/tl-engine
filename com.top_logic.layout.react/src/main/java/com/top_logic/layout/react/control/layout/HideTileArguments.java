/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactDashboardControl} hide command: which tile the user removed
 * from the dashboard.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Hide tile {tileId}")
public interface HideTileArguments extends ReactCommand {

	/** @see #getTileId() */
	String TILE_ID = "tileId";

	/**
	 * The id of the tile to hide, as the dashboard published it in its tile descriptors.
	 */
	@Name(TILE_ID)
	@Mandatory
	String getTileId();

}
