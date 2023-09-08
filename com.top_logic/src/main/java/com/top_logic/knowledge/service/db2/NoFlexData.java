/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.dob.DataObjectException;

/**
 * Unmodifiable empty {@link FlexData}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class NoFlexData implements FlexData {

	/**
	 * Singleton {@link NoFlexData} instance.
	 */
	public static final NoFlexData INSTANCE = new NoFlexData();

	private NoFlexData() {
		// Singleton constructor.
	}

	@Override
	public Object setAttributeValue(String attributeName, Object value) throws DataObjectException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttributeValue(String attributeName) {
		return null;
	}

	@Override
	public Collection<String> getAttributes() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		return false;
	}

	@Override
	public long lastModified(String attributeName) {
		return -1;
	}
}
