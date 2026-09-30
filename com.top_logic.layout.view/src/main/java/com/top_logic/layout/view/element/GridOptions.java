/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;

/**
 * The layout of a grid of equally built elements.
 *
 * <p>
 * A grid places its elements in as many columns as fit next to each other and reflows them as the
 * available width changes, so that the same configuration serves a wide screen and a phone. What
 * the configuration fixes is not the number of columns but how wide a column has to be at least,
 * optionally how many columns are wanted at most, how much space separates them, and how wide the
 * grid as a whole may grow.
 * </p>
 */
public interface GridOptions extends ConfigurationItem {

	/** Configuration name for {@link #getMinColumnWidth()}. */
	String MIN_COLUMN_WIDTH = "min-column-width";

	/** Configuration name for {@link #getMaxColumns()}. */
	String MAX_COLUMNS = "max-columns";

	/** Configuration name for {@link #getGap()}. */
	String GAP = "gap";

	/** Configuration name for {@link #getMaxWidth()}. */
	String MAX_WIDTH = "max-width";

	/**
	 * The width a column must have at least, as a CSS length such as "16rem".
	 *
	 * <p>
	 * A column is never narrower than this, so the number of columns follows the available width:
	 * the wider the grid, the more columns it places. Where not even one column of this width
	 * fits, the elements are shown full-width instead of overflowing.
	 * </p>
	 */
	@Name(MIN_COLUMN_WIDTH)
	@StringDefault("16rem")
	String getMinColumnWidth();

	/**
	 * The largest number of columns to place, or nothing for as many as fit.
	 *
	 * <p>
	 * A bounded grid still drops columns as it gets narrower; it just does not grow beyond the
	 * given number however wide it gets, which keeps a handful of elements from spreading into a
	 * thin row on a wide screen.
	 * </p>
	 */
	@Name(MAX_COLUMNS)
	@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 1)
	Integer getMaxColumns();

	/**
	 * The space between the elements of the grid.
	 */
	@Name(GAP)
	StackGap getGap();

	/**
	 * The largest width the elements take together, as a CSS length such as "60rem".
	 *
	 * <p>
	 * Bounded this way, the arrangement is centered in the space it is given, the space left over
	 * split between its two sides, which keeps a handful of elements together in the middle of a
	 * wide screen instead of spreading them across it. Below the bound nothing changes, so the
	 * elements still fill a narrow screen.
	 * </p>
	 *
	 * <p>
	 * Unset, the elements take the width their container offers.
	 * </p>
	 */
	@Name(MAX_WIDTH)
	@Nullable
	String getMaxWidth();

}
