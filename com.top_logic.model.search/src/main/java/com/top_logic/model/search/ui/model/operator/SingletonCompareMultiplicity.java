/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;

/**
 * {@link Function0} for the {@link SingletonCompare#getValueMultiplicity() value multiplicity} of
 * {@link SingletonCompare}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SingletonCompareMultiplicity extends Function1<Boolean, SingletonCompareOperation> {

	/** The {@link SingletonCompareMultiplicity} instance. */
	public static final SingletonCompareMultiplicity INSTANCE = new SingletonCompareMultiplicity();

	@Override
	public Boolean apply(SingletonCompareOperation operation) {
		if (operation == null) {
			return null;
		}
		return operation.isMultiple();
	}

}
