/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * The IdentityAccessor returns the given object, ignoring the property.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class IdentityAccessor extends ReadOnlyAccessor<Object> {

	/** The {@link IdentityAccessor} instance. */
	public static final IdentityAccessor INSTANCE = new IdentityAccessor();

	@Override
	public Object getValue(Object object, String property) {
		return object;
	}

}
