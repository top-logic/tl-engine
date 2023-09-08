/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.ref.DataItemValue;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.IncludeScriptActionOp;

/**
 * {@link ApplicationAction} for {@link IncludeScriptActionOp}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public interface IncludeScriptAction extends DynamicAction {

	/** The script, probably a {@link DataItemValue}. */
	@NonNullable
	ModelName getScript();

	@Override
	@ClassDefault(IncludeScriptActionOp.class)
	Class<? extends IncludeScriptActionOp> getImplementationClass();

}
