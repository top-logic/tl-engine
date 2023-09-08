/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.runtime.action.KBRollbackXRevisionsOp;

/**
 * Config interface for {@link KBRollbackXRevisionsOp}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface KBRollbackXRevisions extends KBRollbackAction {

	@Override
	@ClassDefault(KBRollbackXRevisionsOp.class)
	Class<? extends KBRollbackXRevisionsOp> getImplementationClass();

	/**
	 * Number of revisions to revert.
	 */
	long getNumberRevisions();

	/**
	 * @see #getNumberRevisions()
	 */
	void setNumberRevisions(long numberRevisions);

}
