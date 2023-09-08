/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link POIRowGroupDescription} allows to define a sheet name and ranges of rows that should be
 * grouped and collapsed or expanded.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class POIRowGroupDescription extends POISheetDescription {

	static final class GroupDescription {
		final int _startRow;

		final int _endRow;

		final boolean _collapsed;

		public GroupDescription(int startRow, int endRow, boolean collapsed) {
			_startRow = startRow;
			_endRow = endRow;
			_collapsed = collapsed;
		}

	}

	private final List<POIRowGroupDescription.GroupDescription> _groups = new ArrayList<>();

	/** Creates a new {@link POIRowGroupDescription} */
	public POIRowGroupDescription(String sheetName) {
		super(sheetName);
	}

	/**
	 * Allows to define a new range of rows (defined by a start and an end row) that should be
	 * grouped.
	 * 
	 * @param startRow
	 *        The first row in the group. 0-based, i.e. 0 means the first excel row.
	 * @param endRow
	 *        The last row in the group. 0-based, i.e. 0 means the first excel row.
	 */
	public void addGroup(int startRow, int endRow, boolean collapsed) {
		if (startRow > endRow) {
			throw new IllegalArgumentException(
				"Start row '" + startRow + "' must not be grater than end row '" + endRow + "'.");
		}
		if (startRow < 0) {
			throw new IllegalArgumentException(
				"Start row '" + startRow + "' must not be smaller than 0.");
		}
		GroupDescription group = new GroupDescription(startRow, endRow, collapsed);
		if (collapsed) {
			/* Adding "collapsed" group first is due to a bug in POI: If a group is defined as
			 * collapsed, *all* formerly defined groups are also collapsed. Therefore the expanded
			 * groups must be defined after the collapsed groups. */
			_groups.add(0, group);
			return;
		}
		_groups.add(group);
	}

	/**
	 * Returns the {@link List} of row-ranges that should be grouped.
	 */
	List<POIRowGroupDescription.GroupDescription> getGroups() {
		return _groups;
	}

}
