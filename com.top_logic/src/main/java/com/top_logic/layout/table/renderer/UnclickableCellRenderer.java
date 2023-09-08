/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;

/**
 * A marker interface for {@link CellRenderer} that do not write clickable content.
 * <p>
 * Every concrete {@link CellRenderer} has to be a subclass of the {@link AbstractCellRenderer}.
 * This is just an additional interface to implement in addition to that super class.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface UnclickableCellRenderer extends CellRenderer {

	// Nothing needed for a marker interface.

}
