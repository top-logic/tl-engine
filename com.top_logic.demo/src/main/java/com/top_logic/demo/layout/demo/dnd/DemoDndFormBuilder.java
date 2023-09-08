/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.dnd;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Arrays;
import java.util.List;

import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} for the drop-target form of the DND demo.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoDndFormBuilder implements ModelBuilder {

	/**
	 * Singleton {@link DemoDndFormBuilder} instance.
	 */
	public static final DemoDndFormBuilder INSTANCE = new DemoDndFormBuilder();

	private DemoDndFormBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		FormContext result = new FormContext(aComponent);
		List<String> options = Arrays.asList("A1", "A2", "A31", "A32", "L1", "L2", "L3");

		SelectField field1 = FormFactory.newSelectField("field1", options);
		field1.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
		result.addMember(field1);

		SelectField field2 = FormFactory.newSelectField("field2", options, true, false);
		field2.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
		result.addMember(field2);

		template(result, div(items(div(label(), div(self())))));
		return result;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}
