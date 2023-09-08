/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * {@link IdSource} based on an integer counter.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IntIdSource implements IdSource {

	private int _lastId = 0;

	@Override
	public String newId() {
		return Integer.toString(++_lastId);
	}

	@Override
	public void setLastId(String lastId) {
		int lastInt = Integer.parseInt(lastId);
		_lastId = Math.max(_lastId, lastInt);
	}

}
