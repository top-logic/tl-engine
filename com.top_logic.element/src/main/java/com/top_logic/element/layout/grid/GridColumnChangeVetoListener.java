/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.layout.basic.DirtyHandlingVeto;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.table.ColumnChangeEvt;
import com.top_logic.layout.table.DefaultColumnChangeVetoListener;

/**
 * {@link DefaultColumnChangeVetoListener} for {@link GridComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GridColumnChangeVetoListener extends DefaultColumnChangeVetoListener {

	/**
	 * Creates a new {@link GridColumnChangeVetoListener}.
	 */
	public GridColumnChangeVetoListener(GridComponent handler) {
		super(handler);
	}

	/**
	 * Checks that no new columns are removed (in which case the value must be saved) or added (in
	 * which case new {@link FormField}s must be created).
	 */
	@Override
	protected void internalCheck(ColumnChangeEvt evt) throws DirtyHandlingVeto {
		if (!grid().renewSelectedGroupOnColumnChange(evt.oldValue(), evt.newValue())) {
			return;
		}
		super.internalCheck(evt);
	}

	private GridComponent grid() {
		return (GridComponent) getHandler();
	}

}

