/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.editor.AbstractComponentConfigurationDialogBuilder.GlobalConfig;
import com.top_logic.layout.editor.ComponentConfigurationDialogBuilder;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleSelectDialog;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class for specialized "add component" commands.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractComponentConfigurationCommandHandler extends AbstractCommandHandler {

	/**
	 * Special {@link SimpleSelectDialog} that selects a {@link DynamicComponentDefinition}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ComponentSelectionDialog extends SimpleSelectDialog<DynamicComponentDefinition> {

		ComponentSelectionDialog(Iterable<? extends DynamicComponentDefinition> options) {
			super(I18NConstants.ADD_COMPONENT_SELECT_DIALOG, options);
		}

		@Override
		protected void fillButtons(List<CommandModel> buttons) {
			super.fillButtons(buttons);
			/* Confirming the selection is recorded later, when the follow-up configuration dialog
			 * for the selected component becomes visible. This is necessary because the follow-up
			 * dialog is not necessarily opened. It may be that it is programmatically filled and
			 * finished. In this case a special action with more informations is recorded. That
			 * action has the same label as the "Ok" button, so that executing the special action
			 * actually triggers "Ok" of this select dialog. */
			ScriptingRecorder.annotateAsDontRecord(_okButton);
		}

		/**
		 * Records the click on the "OK" button of a {@link ComponentSelectionDialog}.
		 */
		public static void recordOkButton(DisplayContext context) {
			if (ScriptingRecorder.isRecordingActive()) {
				ComponentName mainLayoutName = context.getLayoutContext().getMainLayout().getName();
				ResKey label = ButtonType.OK.getButtonLabelKey();
				ScriptingRecorder.recordAction(ActionFactory.buttonAction(mainLayoutName, null, label));
			}
		}

	}

	/**
	 * Creates a {@link AbstractComponentConfigurationCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractComponentConfigurationCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		LayoutComponent configuredComponent = getActuallyAdaptedComponent(component);
		List<DynamicComponentDefinition> templates = getComponentTemplates(configuredComponent);

		if (templates.size() == 1) {
			DynamicComponentDefinition definition = templates.get(0);
			return createComponentConfigurator(configuredComponent, definition).open(context);
		} else {
			SimpleSelectDialog<DynamicComponentDefinition> selector = new ComponentSelectionDialog(templates);

			selector.setSelectionHandler(
				(innerContext, definition) -> createComponentConfigurator(configuredComponent, definition)
					.open(innerContext));

			return selector.open(context);
		}
	}

	/**
	 * Determines the component which is actually configured.
	 */
	protected LayoutComponent getActuallyAdaptedComponent(LayoutComponent component) {
		return component;
	}

	private ComponentConfigurationDialogBuilder createComponentConfigurator(LayoutComponent baseComponent,
			DynamicComponentDefinition definition) {
		return new ComponentConfigurationDialogBuilder(baseComponent, definition,
			componentConfig -> {
			
				Optional<String> assistentFor = definition.getAssistentFor();
				if (assistentFor.isPresent()) {
					DynamicComponentDefinition generalDefinition =
						DynamicComponentService.getInstance().getComponentDefinition(assistentFor.get());

					List<AdditionalComponentDefinition> additional = new ArrayList<>();
					ConfigurationItem generalArguments;
					try {
						generalArguments = definition.createTemplateBody(componentConfig, additional);
					} catch (ConfigurationException exception) {
						return HandlerResult
							.error(com.top_logic.layout.editor.I18NConstants.INVALID_LAYOUT_TEMPLATE_BODY, exception);
					}
			
					// Ensure that initializer of the general template are applied.
					ConfiguredAnnotationCustomizations customizations = GlobalConfig.newCustomizations();
					InitializerUtil.initProperties(customizations, generalArguments);
					additional.stream().map(AdditionalComponentDefinition::getArguments)
						.forEach(config -> InitializerUtil.initProperties(customizations, config));
			
					return createComponent(baseComponent, generalDefinition, generalArguments, additional);
				} else {
					return createComponent(baseComponent, definition, componentConfig, Collections.emptyList());
				}
			});

	}

	/**
	 * Actually instantiates the newly configured component.
	 *
	 * @param baseComponent
	 *        The {@link LayoutComponent} this command was executed on.
	 * @param definition
	 *        The selected component template.
	 * @param componentConfig
	 *        The configuration filled by the user.
	 * @param additional
	 *        Additional definitions of components to create.
	 * @return The result of this command.
	 */
	protected abstract HandlerResult createComponent(LayoutComponent baseComponent,
			DynamicComponentDefinition definition, ConfigurationItem componentConfig,
			List<AdditionalComponentDefinition> additional);

	/**
	 * Creates a list of {@link DynamicComponentDefinition} options to select from.
	 * 
	 * @see LayoutTemplateUtils#getTemplates(java.util.Collection, LayoutComponent)
	 */
	protected abstract List<DynamicComponentDefinition> getComponentTemplates(LayoutComponent context);

}
