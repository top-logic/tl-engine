/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.action.op;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.DynamicActionOp;
import com.top_logic.layout.scripting.template.ScriptUtil;
import com.top_logic.layout.scripting.template.action.TemplateAction;
import com.top_logic.layout.scripting.template.action.TemplateAction.Parameter;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * The {@link AbstractApplicationActionOp} implementation for the {@link TemplateAction}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class TemplateActionOp<T extends TemplateAction> extends DynamicActionOp<T> {

	/**
	 * Creates a {@link TemplateActionOp} from a {@link TemplateAction}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 */
	@CalledByReflection
	public TemplateActionOp(InstantiationContext context, T config) {
		super(context, config);
	}

	@Override
	public List<ApplicationAction> createActions(ActionContext context) {
		String template = getTemplate(context);
		Map<String, Object> parameters = resolveParameters(context);
		ApplicationAction actions = ScriptUtil.DEFAULT.createActionFromTemplate(template, parameters);
		return Collections.singletonList(actions);
	}

	private Map<String, Object> resolveParameters(ActionContext context) {
		Map<String, Object> result = new HashMap<>();
		for (Parameter parameter : getConfig().getParameters()) {
			result.put(parameter.getName(), resolveValue(parameter, context));
		}
		return result;
	}

	private Object resolveValue(Parameter parameter, ActionContext context) {
		try {
			return context.resolve(parameter.getValue());
		} catch (Exception ex) {
			String message = "Failed to resolve value of parameter '" + parameter.getName() + "': "
				+ parameter.getValue() + " Reason: " + ex.getMessage();
			throw new RuntimeException(message, ex);
		}
	}

	/**
	 * Builds the resource name of the template.
	 * 
	 * <p>
	 * The resource name of the templates is expected to be resolvable using
	 * {@link TemplateSourceFactory}.
	 * </p>
	 */
	protected abstract String getTemplate(ActionContext context);

}
