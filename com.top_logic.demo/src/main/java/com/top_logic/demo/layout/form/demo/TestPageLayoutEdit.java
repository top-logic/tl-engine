/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.util.Arrays;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Test {@link EditComponent} providing several fields for testing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestPageLayoutEdit extends EditComponent {

	public static final String SELECT_FIELD = "select";

	public static final String STRING_FIELD = "string";

	/**
	 * Configuration options for {@link TestPageLayoutEdit}.
	 */
	public interface Config extends EditComponent.Config {

		@Override
		@NullDefault
		String getLockOperation();

		@Override
		@StringDefault(Apply.COMMAND_ID)
		String getApplyCommand();

	}

	/**
	 * Dummy model implementation.
	 */
	class Model {
		Object _valueString;

		Object _valueSelect;
	}

	public TestPageLayoutEdit(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(new Model());
		}
		return super.validateModel(context);
	}

	@Override
	public FormContext createFormContext() {
		FormContext fc = new FormContext(this);

		Model model = (Model) getModel();

		StringField field1 = FormFactory.newStringField(STRING_FIELD);
		field1.initializeField(model._valueString);
		fc.addMember(field1);

		SelectField field2 = FormFactory.newSelectField(SELECT_FIELD, Arrays.asList("Option1", "Option2", "Option3"));
		field2.initializeField(model._valueSelect);
		fc.addMember(field2);

		return fc;
	}

	/**
	 * Dummy apply for {@link TestPageLayoutEdit}.
	 */
	public static class Apply extends AbstractApplyCommandHandler {

		public static final String COMMAND_ID = "TestPageLayoutEditApply";

		/**
		 * Creates an {@link Apply}.
		 */
		public Apply(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
			Model myModel = (Model) model;

			myModel._valueString = formContext.getField(STRING_FIELD).getValue();
			myModel._valueSelect = formContext.getField(SELECT_FIELD).getValue();

			return true;
		}

	}
}
