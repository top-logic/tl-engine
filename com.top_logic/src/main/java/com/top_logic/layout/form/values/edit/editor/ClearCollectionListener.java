/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.basic.util.Utils.*;

import com.top_logic.layout.form.values.Listener;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * A {@link Listener} that clears the value of its {@link ValueModel}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class ClearCollectionListener implements Listener {

	private final ValueModel _valueModel;

	ClearCollectionListener(ValueModel valueModel) {
		_valueModel = requireNonNull(valueModel);
	}

	@Override
	public void handleChange(Value<?> sender) {
		_valueModel.clearValue();
	}

}
