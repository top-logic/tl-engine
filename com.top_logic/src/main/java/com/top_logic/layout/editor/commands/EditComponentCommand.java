/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.editor.ComponentConfigurationDialogBuilder;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.tool.execution.UniqueToolbarCommandRule;

/**
 * Opens a dialog with configuration options for the current view.
 * 
 * <p>
 * Buttons can be added to the current view from within this configuration dialog. Buttons that
 * represent opening buttons of dialogs can be edited by this command executed from within the open
 * dialog.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Label("Edit view")
public class EditComponentCommand extends AbstractCommandHandler {

	/**
	 * Identifier under which an instance of this class is configured in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "editComponentCommand";

	/**
	 * Creates a {@link EditComponentCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public EditComponentCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> arguments) {
		LayoutComponent editedComponent = EditableComponentExecutability.resolveEditedComponent(component, arguments);

		return openEditComponentDialog(context, editedComponent);
	}

	private static HandlerResult openEditComponentDialog(DisplayContext context, LayoutComponent contextComponent) {
		String layoutKey = LayoutTemplateUtils.getNonNullNameScope(contextComponent);
		TLLayout layout = LayoutStorage.getInstance().getLayout(layoutKey);

		return openEditComponentDialog(context, contextComponent, layoutKey, layout);
	}

	static HandlerResult openEditComponentDialog(DisplayContext context, LayoutComponent contextComponent,
			String layoutKey, TLLayout layout) {
		Optional<DynamicComponentDefinition> definition =
			LayoutTemplateUtils.getTemplateComponentDefinition(layout.getTemplateName());

		if (definition.isPresent()) {
			try {
				ConfigurationItem arguments = LayoutTemplateUtils.getReparsedArguments(layoutKey, layout);

				return createEditComponentDialog(contextComponent, definition.get(), arguments).open(context);
			} catch (ConfigurationException exception) {
				return HandlerResult
					.error(com.top_logic.layout.editor.I18NConstants.ERROR_LAYOUT_CONFIGURATION_INVALID__LAYOUT
						.fill(layoutKey), exception);
			}
		}

		return HandlerResult.error(I18NConstants.COMPONENT_DEFINITION_PARSE_ERROR);
	}

	private static ComponentConfigurationDialogBuilder createEditComponentDialog(LayoutComponent contextComponent,
			DynamicComponentDefinition definition, ConfigurationItem arguments) {
		return new ComponentConfigurationDialogBuilder(contextComponent, definition, arguments,
			new ResourceText(I18NConstants.EDIT_COMPONENT_TITLE),
			createComponentFinisher(contextComponent, definition));
	}

	private static Function<ConfigurationItem, HandlerResult> createComponentFinisher(LayoutComponent component,
			DynamicComponentDefinition definition) {
		return (arguments) -> {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction()) {
				LayoutTemplateUtils.createComponent(component, definition, arguments, Collections.emptyList());

				tx.commit();
			}

			return HandlerResult.DEFAULT_RESULT;
		};
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(InDesignModeExecutable.INSTANCE, new UniqueToolbarCommandRule(this),
			EditableComponentExecutability.INSTANCE, super.intrinsicExecutability());
	}

}
