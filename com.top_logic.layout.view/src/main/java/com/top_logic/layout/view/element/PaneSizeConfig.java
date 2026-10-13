/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.DisplayUnit;

/**
 * Size of one side of a split: the share it takes, the unit that share is measured in, and the
 * size it is never shrunk below.
 *
 * <p>
 * Shared by the {@link SplitPanelElement.PaneConfig panes} of a {@link SplitPanelElement} and the
 * two sides of an {@link AdaptiveDetailElement}.
 * </p>
 */
public interface PaneSizeConfig extends ConfigurationItem {

	/** Configuration name for {@link #getSize()}. */
	String SIZE = "size";

	/** Configuration name for {@link #getUnit()}. */
	String UNIT = "unit";

	/** Configuration name for {@link #getMinSize()}. */
	String MIN_SIZE = "min-size";

	/**
	 * The size of the pane, measured in the {@link #getUnit() unit}.
	 *
	 * <p>
	 * Percent sizes of the panes of one split are weights: the panes share the available width in
	 * the ratio of their sizes.
	 * </p>
	 */
	@Name(SIZE)
	@FloatDefault(50f)
	float getSize();

	/**
	 * The unit the {@link #getSize() size} is measured in: percent of the available space or
	 * pixels.
	 */
	@Name(UNIT)
	@FormattedDefault("%")
	DisplayUnit getUnit();

	/**
	 * The minimum size in pixels the pane is never shrunk below (0 for no minimum).
	 */
	@Name(MIN_SIZE)
	@IntDefault(0)
	int getMinSize();

}
