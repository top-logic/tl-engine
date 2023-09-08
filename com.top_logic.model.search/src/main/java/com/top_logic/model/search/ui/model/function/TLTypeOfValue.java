/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.function;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;

/**
 * A {@link Function1} for {@link TLObject#tType()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLTypeOfValue extends Function1<TLType, TLObject> {

	@Override
	public TLType apply(TLObject object) {
		if (object == null) {
			return null;
		}
		return object.tType();
	}
}
