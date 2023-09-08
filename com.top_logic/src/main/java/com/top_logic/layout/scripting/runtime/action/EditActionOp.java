/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;


import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.Editor;
import com.top_logic.layout.scripting.action.FormAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ApplicationActionOp} that performs an edit of an {@link Editor}.
 * 
 * @deprecated Use regular command switching modes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public final class EditActionOp extends FormInputOp {

	public EditActionOp(InstantiationContext context, FormAction config) {
		super(context, config);
	}

	@Override
	protected void processForm(ActionContext actionContext, FormHandler formHandler) {
		Editor editor = (Editor) formHandler;

		HandlerResult editResult = editor.edit(actionContext.getDisplayContext());
		ApplicationAssertions.assertSuccess(config, editResult);

		super.processForm(actionContext, formHandler);

		HandlerResult saveResult = editor.save(actionContext.getDisplayContext());
		ApplicationAssertions.assertSuccess(config, saveResult);
	}
}
