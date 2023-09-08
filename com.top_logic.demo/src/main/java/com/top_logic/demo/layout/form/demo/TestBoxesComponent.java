/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.util.Date;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.PlainKeyResources;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Test for box layout with input fields.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBoxesComponent extends FormComponent {

	public static final String GROUP1_GROUP = "group1";

	public static final String GROUP2_GROUP = "group2";

	public static final String STRING_FIELD = "string";

	public static final String TEXT_FIELD = "text";

	public static final String BOOLEAN_FIELD = "boolean";

	public static final String DATA_FIELD = "data";

	public static final String DATE_FIELD = "date";

	public static final String RESET_BUTTON = "reset";

	public static final String TOGGLE_BUTTON = "toggle";

	/**
	 * {@link FormComponent} configuration to display all occurrences even without a model.
	 *
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	public interface Config extends FormComponent.Config {
		@Override
		@Name(DISPLAY_WITHOUT_MODEL)
		@BooleanDefault(true)
		boolean getDisplayWithoutModel();
	}

	public TestBoxesComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null;
	}

	@Override
	public FormContext createFormContext() {
		FormContext fc = new FormContext(this);
		fc.setResources(PlainKeyResources.INSTANCE);

		final FormGroup group1 = add(fc, new FormGroup(GROUP1_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group2 = add(fc, new FormGroup(GROUP2_GROUP, PlainKeyResources.INSTANCE));

		add(group1, FormFactory.newStringField(STRING_FIELD));
		add(group1, FormFactory.newBooleanField(BOOLEAN_FIELD));
		add(group1, FormFactory.newDateField(DATE_FIELD, new Date(), false));
		CommandField toggleButton = FormFactory.newCommandField(TOGGLE_BUTTON, new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				group1.setCollapsed(!group1.isCollapsed());
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		add(group1, toggleButton);

		add(group2, FormFactory.newStringField(TEXT_FIELD));
		add(group2, FormFactory.newDataField(DATA_FIELD));
		CommandField resetButton = FormFactory.newCommandField(RESET_BUTTON, new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				getFormContext().reset();
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		resetButton.setImage(com.top_logic.tool.export.Icons.REFRESH);
		add(group2, resetButton);

		return fc;
	}

	private static <F extends FormMember> F add(FormContainer fc, F field) {
		fc.addMember(field);
		return field;
	}

}
