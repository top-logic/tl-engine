/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import com.top_logic.base.office.excel.ExcelValue.MergeRegion;

/**
 * A {@link POIColumnFilterDescription} allows to define a sheet name and the row-/column-indices on
 * which column filters should be applied.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class POIColumnFilterDescription extends POISheetDescription {

	private final MergeRegion _region;

	/** Creates a new {@link POIColumnFilterDescription} */
	public POIColumnFilterDescription(String sheetName, int rowStartIndex, int rowEndIndex, int colStartIndex,
			int colEndIndex) {
		super(sheetName);
		_region = new MergeRegion(rowStartIndex, colStartIndex, rowEndIndex, colEndIndex);
	}

	/**
	 * Returns the represented {@link MergeRegion}.
	 */
	public MergeRegion getRegion() {
		return _region;
	}

}
