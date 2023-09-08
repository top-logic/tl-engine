/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.list;

import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Accessor for {@link FastList}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FastListAccessor extends ReadOnlyAccessor<Object> {

	private static final String CLASSIFICATION_TYPE = "classificationType";

	private static final String ORDERED = "ordered";

	private static final String SYSTEM = "system";

	private static final String MULTI_SELECT = "multiSelect";

	@Override
	public Object getValue(Object object, String property) {
		FastList fl = (FastList) object;
		if (CLASSIFICATION_TYPE.equals(property)) {
			return fl.getClassificationType();
		}
		if (ORDERED.equals(property)) {
			return fl.isOrdered();
		}
		if (SYSTEM.equals(property)) {
			return fl.isSystem();
		}
		if (MULTI_SELECT.equals(property)) {
			return fl.isMultiSelect();
		}
		return WrapperAccessor.INSTANCE.getValue(object, property);
	}

}

