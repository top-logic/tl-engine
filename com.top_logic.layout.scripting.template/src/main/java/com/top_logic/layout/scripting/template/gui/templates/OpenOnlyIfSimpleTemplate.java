/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} displaying the {@link TemplateParameterComponent} dialog.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OpenOnlyIfSimpleTemplate extends OpenModalDialogCommandHandler {

	/**
	 * Creates a {@link OpenOnlyIfSimpleTemplate} command from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OpenOnlyIfSimpleTemplate(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {

		// Note: This check is not done in an executablility rule, since it is quite expensive and
		// executablity rules are evaluated with a very high frequency.
		TemplateResource resource = IsTemplateSelected.template(((Selectable) aComponent).getSelected());
		Template template = resource.tryLoad();
		for (MOAttribute attr : template.getType().getAttributes()) {
			if (attr.getMetaObject() != MOPrimitive.STRING) {
				throw new TopLogicException(I18NConstants.ERROR_TEMPLATE_WITH_NON_STRING_PARAMETER);
			}
		}

		return super.handleCommand(aContext, aComponent, model, someArguments);
	}

}