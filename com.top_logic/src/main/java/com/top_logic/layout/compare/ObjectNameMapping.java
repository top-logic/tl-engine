/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;

/**
 * Mapping that maps an {@link IdentifiedObject} to the {@link ObjectKey#getObjectName() name} of
 * its identifier.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class ObjectNameMapping implements Mapping<IdentifiedObject, Object> {

	/** Singleton {@link ObjectNameMapping}. */
	public static final ObjectNameMapping INSTANCE = new ObjectNameMapping();

	@Override
	public Object map(IdentifiedObject input) {
		return input.tId().getObjectName();
	}

}
