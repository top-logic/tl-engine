/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.LabeledButtonAction;

/**
 * Type-bound version of {@link AbstractLabeledButtonActionOp}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LabeledButtonActionOp extends AbstractLabeledButtonActionOp<LabeledButtonAction> {

	@CalledByReflection
	public LabeledButtonActionOp(InstantiationContext context, LabeledButtonAction config) {
		super(context, config);
	}

}