/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.layout.DisplayContext;

/**
 * A {@link ConstantEvalResult} with a given value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResultValue extends ConstantEvalResult {

	private Object _value;

	/**
	 * Creates a {@link ResultValue}.
	 */
	public ResultValue(Object value) {
		_value = value;
	}

	@Override
	public Object getValue(DisplayContext displayContext) {
		return _value;
	}

}
