/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.table.grid;

import com.top_logic.basic.col.Filter;
import com.top_logic.demo.model.types.A;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.layout.form.model.FormGroup;

/**
 * Filter accepting all {@link A} where attribute {@link A#getBooleanMandatory()} is set to
 * <code>true</code>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoSelectableObjectsFilter implements Filter<Object> {

	/** Singleton {@link DemoSelectableObjectsFilter} instance. */
	public static final DemoSelectableObjectsFilter INSTANCE = new DemoSelectableObjectsFilter();

	/**
	 * Creates a new {@link DemoSelectableObjectsFilter}.
	 */
	protected DemoSelectableObjectsFilter() {
		// singleton instance
	}

	@Override
	public boolean accept(Object anObject) {
		if (anObject instanceof FormGroup) {
			anObject = ((FormGroup) anObject).get(GridComponent.PROP_ATTRIBUTED);
		}
		if (!(anObject instanceof A)) {
			return true;
		}
		return ((A) anObject).getBooleanMandatory();
	}

}

