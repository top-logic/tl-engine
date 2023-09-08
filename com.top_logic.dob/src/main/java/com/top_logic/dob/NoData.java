/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Unmodifiable empty {@link NamedValues}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class NoData implements NamedValues {

	private static final String[] NO_ATTRIBUTES = {};

	/**
	 * Singleton {@link NoData} instance.
	 */
	public static final NoData INSTANCE = new NoData();

	private NoData() {
		// Singleton constructor.
	}

	@Override
	public Object setAttributeValue(String attrName, Object value) throws DataObjectException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttributeValue(String attrName) throws NoSuchAttributeException {
		return null;
	}

	@Override
	public String[] getAttributeNames() {
		return NO_ATTRIBUTES;
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		return false;
	}
}
