/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.edit;

import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DemoFormContextModificator}, that creates a {@link StringField} for a custom column of
 * {@link GridComponent}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DemoAddColumnModificator extends DemoFormContextModificator {

	private static final String CUSTOM_STRING_FIELD_COLUMN = "customStringFieldColumn";
	/**
	 * Singleton {@link DemoAddColumnModificator} instance.
	 */
	public static final DemoAddColumnModificator INSTANCE = new DemoAddColumnModificator();

	@Override
	public void postModify(LayoutComponent component, TLClass type, TLObject anAttributed,
			AttributeFormContext aContext, FormContainer currentGroup) {
		super.postModify(component, type, anAttributed, aContext, currentGroup);

		if (component instanceof GridComponent) {
			currentGroup.addMember(FormFactory.newStringField(CUSTOM_STRING_FIELD_COLUMN));
		}
	}

	@Override
	public void clear(LayoutComponent component, TLClass type, TLObject anAttributed,
			AttributeUpdateContainer aContainer, FormContainer currentGroup) {
		super.clear(component, type, anAttributed, aContainer, currentGroup);

		if (currentGroup.hasMember(CUSTOM_STRING_FIELD_COLUMN)) {
			currentGroup.removeMember(currentGroup.getMember(CUSTOM_STRING_FIELD_COLUMN));
		}
	}

}
