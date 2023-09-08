/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.recorder.ref.value.StringValue;
import com.top_logic.layout.scripting.template.action.ScriptTemplateAction;
import com.top_logic.layout.scripting.template.action.TemplateAction.Parameter;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} that creates an invocation of a selected template.
 * 
 * @see TemplateParameterComponent
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateInstantiationCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link TemplateInstantiationCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateInstantiationCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {
		FormHandler formHandler = (FormHandler) aComponent;
		FormContext formContext = formHandler.getFormContext();
		boolean ok = formContext.checkAll();
		if (!ok) {
			return AbstractApplyCommandHandler.createErrorResult(formContext);
		}

		TemplateResource templateResource = IsTemplateSelected.template(model);

		ScriptTemplateAction action = TypedConfiguration.newConfigItem(ScriptTemplateAction.class);

		action.setTemplate("script:" + templateResource.getResourceSuffix());
		action.setTemplateNameComment(TemplateLocationResources.INSTANCE.getLabel(templateResource));

		Template template = templateResource.tryLoad();

		List<Parameter> parameters = action.getParameters();
		for (Iterator<FormField> it = formContext.getFields(); it.hasNext();) {
			FormField field = it.next();
			if (!template.getType().hasAttribute(field.getName())) {
				continue;
			}
			String value = (String) field.getValue();
			if (StringServices.isEmpty(value)) {
				continue;
			}

			Parameter parameter = TypedConfiguration.newConfigItem(Parameter.class);
			parameter.setName(field.getName());
			StringValue valueRef = TypedConfiguration.newConfigItem(StringValue.class);
			valueRef.setString(value);
			parameter.setValue(valueRef);

			parameters.add(parameter);
		}

		aComponent.fireModelCreatedEvent(action, aComponent);

		return HandlerResult.DEFAULT_RESULT;
	}

}
