/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link EditComponent} demonstrating a custom conditional confirmation upon save and apply.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestCustomConfirmComponent extends EditComponent {

	public static final String NUMBER_FIELD = "number";

	/**
	 * Configuration options for {@link TestCustomConfirmComponent}.
	 */
	public interface Config extends EditComponent.Config {

		@Override
		@NullDefault
		String getLockOperation();

		@Override
		@StringDefault(Apply.COMMAND_ID)
		String getApplyCommand();

	}


	public TestCustomConfirmComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

	@Override
	public FormContext createFormContext() {
		FormContext fc = new FormContext(this);
		IntField field = FormFactory.newIntField(NUMBER_FIELD);
		field.initializeField(getModel());
		fc.addMember(field);
		return fc;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(0);
		}
		return super.validateModel(context);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		// See acceptModel().
		return anObject == null || anObject instanceof Integer;
	}

	public static class Apply extends AbstractCommandHandler {

		private static final int DEFAULT_LIMIT = 1000;

		private static final String LIMIT_PARAM = "limit";

		public static final String COMMAND_ID = "TestCustomConfirmComponentApply";

		public Apply(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {

			FormField field = ((FormComponent) aComponent).getFormContext().getField(NUMBER_FIELD);
			Integer value = (Integer) field.getValue();

			Integer limitNumber = (Integer) someArguments.get(LIMIT_PARAM);
			int limit = limitNumber == null ? DEFAULT_LIMIT : limitNumber.intValue();

			if (value == null || value.intValue() < limit) {
				HandlerResult suspended = HandlerResult.suspended();

				MessageBox.confirm(aContext, MessageType.WARNING,
					"Es ist eine Eingabe von mindestens " + limit + " erforderlich.",
						MessageBox.button(getResourceKey(aComponent),
						suspended.resumeContinuation(Collections.<String, Object> singletonMap(LIMIT_PARAM, limit / 2))),
					MessageBox.button(ButtonType.CANCEL));

				return suspended;
			}

			aComponent.setModel(value);

			return HandlerResult.DEFAULT_RESULT;
		}

	}

}
