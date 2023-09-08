/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.SelectField;

/**
 * {@link ValueListener} that sets a given {@link SelectField} enabled if its single selection
 * equals the given {@link String} or disabled else.
 * 
 * @author <a href=mailto:jes@top-logic.com>Jens Schäfer</a>
 */
public class ScalingTypeListener implements ValueListener {
	private SelectField _targetField;
	private String _referencedOption;

	/** Creates a new {@link ScalingTypeListener} */
	public ScalingTypeListener(SelectField optionsField, String optionToSetTargetVisible) {
		_targetField = optionsField;
		_referencedOption = optionToSetTargetVisible;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (field instanceof SelectField) {
			Object scalingTypeSelection = ((SelectField) field).getSingleSelection();
			if (_referencedOption.equals(scalingTypeSelection)) {
				_targetField.setDisabled(false);
			} else {
				_targetField.setDisabled(true);
			}
		}
	}
}
