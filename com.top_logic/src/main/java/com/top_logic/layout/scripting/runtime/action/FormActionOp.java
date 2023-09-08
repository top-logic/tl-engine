/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;


import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.scripting.action.AbstractFormAction;
import com.top_logic.layout.scripting.action.FormAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ValueResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ApplicationActionOp} that updates a {@link FormHandler}.
 * 
 * @deprecated Use {@link AbstractFormAction}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public abstract class FormActionOp<S extends FormAction> extends AbstractApplicationActionOp<S> {

	protected static final ValueResolver VALUE_RESOLVER = new ValueResolver();

	public FormActionOp(InstantiationContext context, S config) {
		super(context, config);
	}

	@Override
	public final Object processInternal(ActionContext context, Object argument) {
		FormHandler formHandler = findFormHandler(context);
		processForm(context, formHandler);
		return argument;
	}

	private FormHandler findFormHandler(ActionContext actionContext) {
		ModelName modelName = config.getModelName();
		FormHandler result = (FormHandler) ModelResolver.locateModel(actionContext, modelName);
		ApplicationAssertions.assertNotNull(modelName, "Form handler not found.", result);
		return result;
	}

	/**
	 * Subclasses have to override this method instead of
	 * {@link #processInternal(ActionContext, Object)}, as this method gives them the
	 * {@link FormHandler} subject to this {@link FormAction}.
	 */
	protected abstract void processForm(ActionContext actionContext, FormHandler formHandler);

}
