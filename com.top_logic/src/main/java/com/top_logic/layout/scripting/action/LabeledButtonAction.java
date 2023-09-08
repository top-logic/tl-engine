/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.button.LabeledButtonNaming.LabeledButtonName;
import com.top_logic.layout.scripting.runtime.action.AbstractLabeledButtonActionOp;
import com.top_logic.layout.scripting.runtime.action.LabeledButtonActionOp;

/**
 * Configuration for an {@link AbstractLabeledButtonActionOp} that represents a button click.
 * 
 * <p>
 * The button is identified by its label. (It is additionally identified by being visible and
 * executable.)
 * </p>
 * 
 * @author <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
public interface LabeledButtonAction extends ComponentAction {

	@Override
	@ClassDefault(LabeledButtonActionOp.class)
	Class<? extends AbstractLabeledButtonActionOp<?>> getImplementationClass();

	String getLabel();

	void setLabel(String label);

	/**
	 * @see LabeledButtonName#getBusinessObject()
	 */
	ModelName getBusinessObject();

	/**
	 * @see #getBusinessObject()
	 */
	void setBusinessObject(ModelName name);

}
