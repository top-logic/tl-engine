/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.basic.func.Function0;

/**
 * Generic wrapper function to supply a given result.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ResultSupplier<T> extends Function0<T> {

	T _result;

	/**
	 * Creates a {@link ResultSupplier} with the given result.
	 */
	public ResultSupplier(T result) {
		_result = result;
	}

	@Override
	public T apply() {
		return _result;
	}

}
