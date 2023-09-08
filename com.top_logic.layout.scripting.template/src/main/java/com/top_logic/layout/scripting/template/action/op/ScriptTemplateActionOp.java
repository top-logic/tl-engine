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
import com.top_logic.layout.scripting.template.action.ScriptTemplateAction;

/**
 * The {@link AbstractApplicationActionOp} implementation for the {@link ScriptTemplateAction}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptTemplateActionOp extends TemplateActionOp<ScriptTemplateAction> {

	/**
	 * Creates a {@link ScriptTemplateActionOp} from a {@link ScriptTemplateAction}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 */
	@CalledByReflection
	public ScriptTemplateActionOp(InstantiationContext context, ScriptTemplateAction config) {
		super(context, config);
	}

	@Override
	protected String getTemplate(ActionContext context) {
		return config.getTemplate();
	}

}
