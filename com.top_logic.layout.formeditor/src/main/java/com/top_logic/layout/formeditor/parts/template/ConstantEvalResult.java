/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.layout.formeditor.parts.template.VariableDefinition.EvalResult;

/**
 * {@link EvalResult} that does never becomes invalid.
 */
public abstract class ConstantEvalResult implements EvalResult {

	@Override
	public boolean addInvalidateListener(InvalidateListener listener) {
		return false;
	}

	@Override
	public boolean removeInvalidateListener(InvalidateListener listener) {
		return false;
	}

}
