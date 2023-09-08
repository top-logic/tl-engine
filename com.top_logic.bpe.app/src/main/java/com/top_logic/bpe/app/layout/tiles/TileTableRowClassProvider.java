/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.renderer.DefaultRowClassProvider;

/**
 * {@link DefaultRowClassProvider} using special row and font design.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
@SuppressWarnings("rawtypes")
public class TileTableRowClassProvider extends DefaultRowClassProvider {

	/** CSS class name to use to layout odd rows */
	public static final String ODD_ROW_BACKGROUND_COLOR = "tileTableOddRow";

	/** CSS class name to use to layout even rows */
	public static final String EVEN_ROW_BACKGROUND_COLOR = "tileTableEvenRow";

	/** CSS class name to use to adapt font layout of rows */
	public static final String COCKPIT_TABLE_FONT = "tileTableFont";

	/** Singleton instance of this {@link TileTableRowClassProvider} */
	@SuppressWarnings("hiding")
	public static final TileTableRowClassProvider INSTANCE = new TileTableRowClassProvider();

	@Override
	public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row) {
		String trClass = super.getTRClass(view, rowOptions, displayedRow, row);

		if (Math.abs(row) % 2 == 1) {
			trClass = trClass + StringServices.BLANK_CHAR + ODD_ROW_BACKGROUND_COLOR;
		} else {
			trClass = trClass + StringServices.BLANK_CHAR + EVEN_ROW_BACKGROUND_COLOR;
		}

		trClass = trClass + StringServices.BLANK_CHAR + COCKPIT_TABLE_FONT;

		return trClass;
	}

}
