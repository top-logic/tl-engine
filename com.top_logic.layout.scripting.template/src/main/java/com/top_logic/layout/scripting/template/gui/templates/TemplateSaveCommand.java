/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link CommandHandler} for saving an edited template.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateSaveCommand extends TemplateApplyCommand {

	/**
	 * Creates a {@link TemplateSaveCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateSaveCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected TemplateResource getTemplateResource(LayoutComponent component) {
		TemplateEditModel editModel = getTemplateEditModel(component);
		return new TemplateResource(editModel.getName());
	}

}
