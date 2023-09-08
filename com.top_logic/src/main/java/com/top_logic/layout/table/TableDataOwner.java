/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.scripting.recorder.ref.NamedModel;

/**
 * This class is used by the scripting framework to find the owner of the {@link TableData}. The
 * owner is needed to retrieve/find the {@link TableData} when the script is executed.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TableDataOwner extends NamedModel {

	/**
	 * Getter for the owned {@link TableData}.
	 */
	public TableData getTableData();

}
