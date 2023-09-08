/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.scripting.runtime.action.OpenDialogOp;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;

/**
 * {@link ComponentAction} opening a named dialog.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface OpenDialog extends ComponentAction {

	@Override
	@ClassDefault(OpenDialogOp.class)
	Class<OpenDialogOp> getImplementationClass();

	@Mandatory
	@Constraint(QualifiedComponentNameConstraint.class)
	ComponentName getDialogName();
	void setDialogName(ComponentName value);

}
