/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.runtime.action.KBRollbackActionOp;

/**
 * Config interface for {@link KBRollbackActionOp}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface KBRollbackAction extends ApplicationAction {

	@Override
	@ClassDefault(KBRollbackActionOp.class)
	Class<? extends KBRollbackActionOp<?>> getImplementationClass();

}