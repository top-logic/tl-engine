/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import static com.top_logic.basic.StringServices.*;

import com.top_logic.basic.Named;
import com.top_logic.layout.LabelProvider;

/**
 * A {@link LabelProvider} for {@link Named} objects that uses their name as label.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class NamedLabelProvider implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		if (!(object instanceof Named)) {
			throw new IllegalArgumentException("Expected a " + Named.class.getName() + " but got: " + debug(object));
		}
		Named named = (Named) object;
		return nonNull(named.getName());
	}

}
