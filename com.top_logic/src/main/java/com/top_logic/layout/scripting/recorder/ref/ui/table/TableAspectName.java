/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.table;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Super interface for things working with tables.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TableAspectName extends ConfigurationItem {

	/** Reference to the table. */
	ModelName getTable();

	/** @see #getTable() */
	void setTable(ModelName table);

}

