/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.editor.config.DialogTemplateParameters;
import com.top_logic.layout.editor.scripting.Identifiers;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;

/**
 * {@link CommandHandler} to add dialogs on this component.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AddDialogCommand extends AbstractComponentConfigurationCommandHandler {

	/**
	 * Identifier under which an instance of this class is configured in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "addDialogCommand";

	/**
	 * Creates a {@link AddDialogCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AddDialogCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult createComponent(LayoutComponent component, DynamicComponentDefinition definition,
			ConfigurationItem componentConfig, List<AdditionalComponentDefinition> additional) {
		String scope = LayoutTemplateUtils.getNonNullNameScope(component);
		TLLayout layout = LayoutTemplateUtils.getOrCreateLayout(scope);

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			Identifiers identifiers = LayoutTemplateUtils.replaceInnerTemplates(componentConfig);
			String dialogIdentifier = LayoutTemplateUtils.createNewComponentLayoutKey();
			identifiers.addComponentKey(dialogIdentifier);
			LayoutTemplateUtils.recordComponentCreation(ButtonType.OK.getButtonLabelKey(), identifiers, component);

			LayoutTemplateUtils.storeLayout(dialogIdentifier, definition.definitionFile(), componentConfig);

			addDialogToComponent(scope, layout, dialogIdentifier);

			tx.commit();
		}

		LayoutTemplateUtils.replaceComponent(scope, component);

		return HandlerResult.DEFAULT_RESULT;
	}

	private void addDialogToComponent(String scope, TLLayout layout, String dialogIdentifier) {
		LayoutReference referenceToDialog = LayoutTemplateUtils.createLayoutReference(dialogIdentifier);
		try {
			ConfigurationItem arguments = layout.getArguments();
			LayoutTemplateUtils.getDialogsProperty(arguments).add(referenceToDialog);

			LayoutTemplateUtils.storeLayout(scope, layout.getTemplateName(), arguments);
		} catch (ConfigurationException exception) {
			throw new ConfigurationError(exception);
		}
	}

	@Override
	protected List<DynamicComponentDefinition> getComponentTemplates(LayoutComponent context) {
		return LayoutTemplateUtils.getTemplates(Collections.singleton(DialogTemplateParameters.DIALOG_TEMPLATE_GROUP),
			context);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return InDesignModeExecutable.INSTANCE;
	}

}
