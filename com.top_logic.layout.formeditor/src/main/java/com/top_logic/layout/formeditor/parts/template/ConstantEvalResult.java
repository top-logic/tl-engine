/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
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
