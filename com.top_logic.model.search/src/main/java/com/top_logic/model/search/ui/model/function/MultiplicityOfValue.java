/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.function;

import java.util.Collection;

import com.top_logic.basic.func.Function1;

/**
 * Whether the given value is {@link Collection}-valued.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class MultiplicityOfValue extends Function1<Boolean, Object> {

	@Override
	public Boolean apply(Object object) {
		return object instanceof Collection;
	}
}
