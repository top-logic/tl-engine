/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

/**
 * {@link JSONTransformer}, that do not make any transformation at all.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class IdentityTransformer implements JSONTransformer {

	/** Static instance of {@link IdentityTransformer} */
	public static final JSONTransformer INSTANCE = new IdentityTransformer();

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> transformToJSON(Object... rawObjects) {
		return (List<Object>) rawObjects[0];
	}

	@Override
	public List<Object> transformFromJSON(List<Object> jsonObjects) {
		return jsonObjects;
	}
}
