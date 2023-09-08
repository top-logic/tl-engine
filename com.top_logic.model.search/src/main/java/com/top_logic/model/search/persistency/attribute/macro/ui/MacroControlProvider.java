/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.macro.ui;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.model.TLObject;

/**
 * {@link ControlProvider} creating a {@link MacroDisplayControl}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MacroControlProvider extends DefaultFormFieldControlProvider {

	private TLObject _self;

	/**
	 * Creates a {@link MacroControlProvider}.
	 */
	public MacroControlProvider(TLObject self) {
		_self = self;
	}

	@Override
	public Control visitComplexField(ComplexField member, Void arg) {
		return new MacroDisplayControl(_self, member);
	}

	@Override
	public Control visitHiddenField(HiddenField member, Void arg) {
		return new MacroDisplayControl(_self, member);
	}

}
