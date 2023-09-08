/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.component.properties;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * {@link ControlProvider} that dispatches the input control creation to a custom
 * {@link ControlProvider} but delegates label and error controls to
 * {@link DefaultFormFieldControlProvider}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InputOnlyControlProvider implements ControlProvider {

	private ControlProvider _cp;

	/**
	 * Creates a {@link InputOnlyControlProvider}.
	 */
	public InputOnlyControlProvider(ControlProvider cp) {
		_cp = cp;
	}

	@Override
	public Control createControl(Object model, String style) {
		if (style == null || style.equals(FormTemplateConstants.STYLE_DIRECT_VALUE)) {
			return createInputControl(model, style);
		} else {
			return DefaultFormFieldControlProvider.INSTANCE.createControl(model, style);
		}
	}

	private Control createInputControl(Object model, String style) {
		return _cp.createControl(model, style);
	}

}
