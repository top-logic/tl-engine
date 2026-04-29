/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link LabelProvider} defined by ID attributes.
 */
class IDColumnLabelProvider implements LabelProvider {

	private TLStructuredTypePart _idAttr;

	/**
	 * Creates a {@link IDColumnLabelProvider}.
	 */
	public IDColumnLabelProvider(TLStructuredTypePart idAttr) {
		_idAttr = idAttr;
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof TLObject tlObj) {
			return toString(tlObj.tValue(_idAttr));
		}
		return null;
	}

	private String toString(Object value) {
		if (value == null) {
			return StringServices.EMPTY_STRING;
		}
		if (value instanceof CharSequence) {
			return ((CharSequence) value).toString();
		}

		/* Value may be a complex object, e.g. a ResKey. */
		return MetaResourceProvider.INSTANCE.getLabel(value);
	}
	
}
