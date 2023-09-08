/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.SelectSelectableActionOp;

/**
 * The {@link ApplicationAction} representing a selection that uses the {@link Selectable} interface.
 */
public interface SelectSelectableAction extends ComponentAction {

	@Override
	@ClassDefault(SelectSelectableActionOp.class)
	Class<SelectSelectableActionOp> getImplementationClass();

	ModelName getSelection();

	void setSelection(ModelName selectionRef);

}
