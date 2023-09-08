/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} for {@link ObjectKey}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ObjectKeyLabelProvider implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		return ((ObjectKey) object).asString();

	}

}

