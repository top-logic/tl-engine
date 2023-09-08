/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.table;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.provider.FlexibleCellControlProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link FieldProvider} demonstrating how to render more than a single field in a table with
 * width-adjustable columns.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoTwoFieldProvider extends AbstractFieldProvider {

	private static final FlexibleCellControlProvider LAYOUT =
		new FlexibleCellControlProvider("text", "40%", "const", "0px", "date", "60%");

	/**
	 * Singleton {@link DemoTwoFieldProvider} instance.
	 */
	public static final DemoTwoFieldProvider INSTANCE = new DemoTwoFieldProvider();

	private DemoTwoFieldProvider() {
		// Singleton constructor.
	}

	@Override
	public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
		FormGroup group = new FormGroup(aProperty, I18NConstants.DEMO_TWO_FIELDS);
		group.addMember(FormFactory.newIntField("text"));
		group.addMember(FormFactory.newStringField("const", "%" + HTMLConstants.NBSP, true));
		group.addMember(FormFactory.newDateField("date", null, false));

		group.setControlProvider(LAYOUT);
		return group;
	}

}
