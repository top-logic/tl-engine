/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.action.op;


import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.template.ScriptTemplateFinder;
import com.top_logic.layout.scripting.template.action.BusinessOperationTemplateAction;

/**
 * The {@link AbstractApplicationActionOp} implementation for the {@link BusinessOperationTemplateAction}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class BusinessOperationTemplateActionOp extends TemplateActionOp<BusinessOperationTemplateAction> {

	/**
	 * Creates a {@link BusinessOperationTemplateActionOp} from a {@link BusinessOperationTemplateAction}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 */
	@CalledByReflection
	public BusinessOperationTemplateActionOp(InstantiationContext context, BusinessOperationTemplateAction config) {
		super(context, config);
	}

	@Override
	protected String getTemplate(ActionContext context) {
		return ScriptTemplateFinder.getInstance().findScriptResourceFor(config.getBusinessObject(),
			config.getBusinessAction());
	}

}
