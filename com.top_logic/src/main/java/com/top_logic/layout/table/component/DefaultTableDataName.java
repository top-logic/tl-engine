/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableDataOwner;

/**
 * The {@link DefaultTableDataName} consists of a {@link ModelName} for the {@link TableDataOwner}
 * of the {@link DefaultTableData}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface DefaultTableDataName extends ModelName {

	ModelName getDefaultTableDataOwner();

	void setDefaultTableDataOwner(ModelName defaultTableDataOwner);

}
