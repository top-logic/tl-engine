/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.CheckLabeledExecutabilityOp;

/**
 * {@link ReasonKeyAssertion} for executability of a {@link CommandModel} identified by its label.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CheckLabeledExecutability extends ReasonKeyAssertion {

	@Override
	@ClassDefault(CheckLabeledExecutabilityOp.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The {@link ModelName} identifying the {@link CommandModel}.
	 */
	ModelName getModelName();

	/**
	 * Setter for {@link #getModelName()}
	 */
	void setModelName(ModelName modelName);

	/**
	 * Whether the {@link CommandModel} is executable.
	 * 
	 * @see CommandModel#isExecutable()
	 */
	boolean isExecutable();

	/**
	 * Setter for {@link #isExecutable()}.
	 */
	void setExecutable(boolean executable);

}

