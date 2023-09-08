/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.Control;

/**
 * The DefaultRowObjectRemover is a simple RowObjectRemover which doesn't do any cleanup
 * work.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class DefaultRowObjectRemover implements RowObjectRemover {

    @Override
	public void removeRow(Object aRowObject, Control aControl) {
        // no cleanup to do
    }

}
