/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo;

import java.util.Collections;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.SelectionControlProvider;

/**
 * Default implementation of {@link AbstractStaticInfoPlugin}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StaticInfoPlugin extends AbstractStaticInfoPlugin<Object> {

	/**
	 * Creates a {@link StaticInfoPlugin}.
	 */
	public StaticInfoPlugin(Object model, ResPrefix resPrefix, String internalName) {
		super(model, resPrefix, internalName);
	}

	/**
	 * Creates a {@link StaticInfoPlugin} with an explicitly given inspection model.
	 */
	public StaticInfoPlugin(Object model, ResPrefix resPrefix, String internalName, Object inspectModel) {
		super(model, resPrefix, internalName, inspectModel);
	}

	@Override
	protected FormField createValueField(Object model, String fieldName) {
		SelectField valueField = FormFactory.newSelectField(fieldName, Collections.emptyList());
		valueField.setOptionLabelProvider(DebugResourceProvider.INSTANCE);
		valueField.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
		valueField.initializeField(Collections.singletonList(model));
		valueField.setImmutable(true);
		return valueField;
	}

}
