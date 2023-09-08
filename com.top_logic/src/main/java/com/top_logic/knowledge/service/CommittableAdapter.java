/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.sql.CommitContext;

/**
 * Convenience base class for {@link Committable}s that only a subset of the 
 * callbacks defined by the {@link Committable} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommittableAdapter implements Committable {

	@Override
	public boolean commit(CommitContext context) {
		return true;
	}

	@Override
	public void complete(CommitContext context) {
		// Ignore. 
	}

	@Override
	public boolean prepare(CommitContext context) {
		return true;
	}

	@Override
	public boolean prepareDelete(CommitContext context) {
		return true;
	}

	@Override
	public boolean rollback(CommitContext context) {
		return true;
	}

}
