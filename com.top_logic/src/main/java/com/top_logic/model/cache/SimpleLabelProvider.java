/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

import com.top_logic.layout.LabelProvider;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLType;
import com.top_logic.util.Resources;

/**
 * Fall-back label provider for a model element that does not define a name attribute.
 */
class SimpleLabelProvider implements LabelProvider {

	private TLType _type;

	/**
	 * Creates a {@link SimpleLabelProvider}.
	 */
	public SimpleLabelProvider(TLType type) {
		_type = type;
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof TLNamed) {
			String name = ((TLNamed) object).getName();
			if (name != null) {
				return name;
			}
		}

		return Resources.getInstance()
			.getString(com.top_logic.util.model.check.I18NConstants.OBJECT_WITHOUT_NAME__TYPE.fill(_type));
	}

}
