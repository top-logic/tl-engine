/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * {@link IdentifierSource} based on a simple unsynchronized counter.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultIdentifierSource implements IdentifierSource {

	private int _currentId;

	@Override
	public String createNewID() {
		return new StringBuilder(6).append('c').append(_currentId++).toString();
	}

}