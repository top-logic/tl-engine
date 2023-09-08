/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.id.factory;

import com.top_logic.knowledge.service.db2.PersistentIdFactory;

/**
 * Transient alternative to the {@link PersistentIdFactory}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TransientIdFactory implements IdFactory {

	private long _nextId;

	/** Creates a TransientIdFactory which returns the given id as the next id. */
	public TransientIdFactory(long nextId) {
		_nextId = nextId;
	}

	@Override
	public long createId() {
		long nextId = _nextId;
		_nextId += 1;
		return nextId;
	}

}
