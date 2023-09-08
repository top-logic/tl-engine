/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * The CellObjectLabelProvider extracts the value from the CellObject and uses the
 * MetalabelProvider to get the labels.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class CellObjectLabelProvider implements LabelProvider {

    @Override
	public String getLabel(Object aObject) {
		return MetaLabelProvider.INSTANCE.getLabel(((CellObject) aObject).getValue());
    }

}
