/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.KBRollbackToRevisionOp;

/**
 * Config interface for {@link KBRollbackToRevisionOp}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface KBRollbackToRevision extends KBRollbackAction {

	@Override
	@ClassDefault(KBRollbackToRevisionOp.class)
	Class<? extends KBRollbackToRevisionOp> getImplementationClass();

	/**
	 * The revision to revert to.
	 */
	ModelName getRevision();

	/**
	 * @see #getRevision()
	 */
	void setRevision(ModelName revision);

}
