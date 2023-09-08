/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.util;

/**
 * Callback interface for {@link LazyEventRewriter} which wants to rollback actions done since the
 * last commit.
 * 
 * @see NeedRollback
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Rollback {

	/**
	 * Rolls all changes back which have been done since the last commit.
	 */
	public void rollbackCurrentChanges();
}
