/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.table;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.TableData;

/**
 * Whether a table column can be sorted.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableColumnSortabilityNaming
		extends TableColumnAspectNaming<Boolean, TableColumnSortabilityNaming.TableColumnSortabilityName> {

	/**
	 * The {@link ModelName} of the {@link TableColumnSortabilityNaming}.
	 */
	public interface TableColumnSortabilityName extends TableColumnAspectNaming.TableColumnAspectName {

		// Nothing needed by the type itself.

	}

	/** Creates a {@link TableColumnSortabilityNaming}. */
	public TableColumnSortabilityNaming() {
		super(Boolean.class, TableColumnSortabilityName.class);
	}

	@Override
	protected Boolean locateTableColumnAspect(TableData tableData, String columnName) {
		return ScriptTableUtil.canBeSorted(tableData, columnName);
	}

}
