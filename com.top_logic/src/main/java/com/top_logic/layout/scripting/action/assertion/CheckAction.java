/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import java.util.List;

import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.check.ValueCheck;
import com.top_logic.layout.scripting.check.ValueCheck.ValueCheckConfig;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.assertion.CheckActionOp;

/**
 * {@link ApplicationAction} that test an actual application value with constraints.
 * 
 * @see ValueCheck
 * @see CheckActionOp
 * @see ValueAssertion A simple way of comparing two values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CheckAction extends GuiAssertion {

	/**
	 * Description of the value to check.
	 */
	ModelName getActualValue();

	/** @see #getActualValue() */
	void setActualValue(ModelName value);

	/**
	 * Description of the constraints to apply.
	 */
	List<ValueCheckConfig> getConstraints();

	/** @see #getConstraints() */
	void setConstraints(List<ValueCheckConfig> value);

}
