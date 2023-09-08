/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import com.top_logic.basic.Log;
import com.top_logic.basic.MessageEnhancingLog;
import com.top_logic.knowledge.event.ChangeSet;

/**
 * Implementation of {@link Migration}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrationImpl extends MessageEnhancingLog implements Migration {

	private Log _impl;

	/**
	 * Creates a new {@link MigrationImpl}.
	 * 
	 * @param impl
	 *        Log to delegate {@link Log} API to.
	 */
	public MigrationImpl(Log impl) {
		_impl = impl;
	}

	@Override
	protected Log impl() {
		return _impl;
	}

	private ChangeSet _cs;

	private long _origRevision;

	/**
	 * Updates {@link #current()}
	 */
	public void setChangeSet(ChangeSet cs) {
		_cs = cs;
		_origRevision = cs.getRevision();
	}

	@Override
	public ChangeSet current() {
		return _cs;
	}

	@Override
	protected String enhance(String message) {
		if (_cs  != null) {
			return "Current ChangeSet: " + _cs.getRevision() + ", original revision: " + _origRevision + ": " + message;
		}
		return message;
	}

}

