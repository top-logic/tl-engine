/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;

/**
 * {@link CommandHandler} to add a new component to the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AddComponentCommand extends AbstractComponentConfigurationCommandHandler {

	/**
	 * {@link AbstractCommandHandler#getID() Default ID} under which an instance of this class is
	 * configured in the {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "addComponentCommand";

	/**
	 * Creates a {@link AddComponentCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AddComponentCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected List<DynamicComponentDefinition> getComponentTemplates(LayoutComponent configuredComponent) {
		String layoutKey = LayoutTemplateUtils.getNonNullNameScope(configuredComponent);
		Collection<String> usableGroups = getUsableTemplateGroups(layoutKey);

		return LayoutTemplateUtils.getTemplates(usableGroups, configuredComponent);
	}

	private Collection<String> getUsableTemplateGroups(String layoutKey) {
		TLLayout layout = LayoutTemplateUtils.getOrCreateLayout(layoutKey);
		String templateName = layout.getTemplateName();
		DynamicComponentDefinition compDef = LayoutTemplateUtils.getTemplateComponentDefinition(templateName).get();
		return compDef.getUsableGroups();
	}


	@Override
	protected HandlerResult createComponent(LayoutComponent component, DynamicComponentDefinition definition,
			ConfigurationItem parmeters, List<AdditionalComponentDefinition> additional) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (Transaction tx = kb.beginTransaction()) {
			LayoutTemplateUtils.createComponent(component, definition, parmeters, additional);

			tx.commit();
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(InDesignModeExecutable.INSTANCE, DynamicComponentsAvailableRule.INSTANCE);
	}

}
