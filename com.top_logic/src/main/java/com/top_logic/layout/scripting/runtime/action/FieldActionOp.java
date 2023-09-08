/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.FieldMode;
import com.top_logic.layout.scripting.action.AbstractFormAction;
import com.top_logic.layout.scripting.action.FormAction;
import com.top_logic.layout.scripting.recorder.ref.FieldResolver;
import com.top_logic.layout.scripting.recorder.ref.field.FieldRef;
import com.top_logic.layout.scripting.recorder.ref.misc.FieldValue;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Base class for {@link FormActionOp}s targeting certain {@link FormField}s.
 * 
 * @deprecated Use {@link AbstractFormAction}.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public abstract class FieldActionOp extends FormActionOp<FormAction> {

	private FormHandler formHandler;
	private FieldResolver fieldResolver;

	/**
	 * Called by the typed configuration for creating an {@link FieldActionOp} from the
	 * {@link FormAction}.
	 */
	@CalledByReflection
	public FieldActionOp(InstantiationContext context, FormAction config) {
		super(context, config);
	}

	@Override
	protected void processForm(ActionContext actionContext, FormHandler form) {
		this.formHandler = form;
		fieldResolver = new FieldResolver(actionContext);
		for (FieldValue input : config.getFieldValues()) {
			FormField field = findField(input);
			if (!field.isActive()) {
				String fieldNotActiveMessage =
					"Tried to edit a field that is not active! State: " + FieldMode.getMode(field) + "; Field: " + field;
				throw new RuntimeException(fieldNotActiveMessage);
			}
			Object value = findValue(field, input, actionContext);
			processField(field, value);
		}
	}

	private Object findValue(FormField field, FieldValue input, ActionContext actionContext) {
		return actionContext.resolve(input.getValue(), field);
	}

	private FormField findField(FieldValue input) {
		FormContext formContext = formHandler.getFormContext();
		List<FieldRef> fieldPath = input.getFieldPath();
		return (FormField) fieldResolver.resolveFormMember(formContext, fieldPath);
	}

	protected abstract void processField(FormField field, Object value);
}
