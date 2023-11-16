/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ButtonUIConfig;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * Configuration of a {@link ToolBarGroup} in the context of a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ToolBarGroupConfig extends NamedConfigMandatory {

	/**
	 * The buttons in the group.
	 */
	List<ButtonConfig> getButtons();

	/**
	 * Configuration of a single toolbar button in the context of a {@link LayoutComponent}.
	 */
	public interface ButtonConfig extends ButtonUIConfig {

		/**
		 * Name of the {@link CommandHandler} to invoke.
		 * 
		 * <p>
		 * A command with the given name must be defined by the context component.
		 * </p>
		 * 
		 * @see LayoutComponent#getCommandById(String)
		 */
		String getCommandId();

		/**
		 * Constant arguments to pass to the command invocation.
		 * 
		 * @see CommandHandler#handleCommand(com.top_logic.layout.DisplayContext, LayoutComponent,
		 *      Object, Map)
		 */
		@MapBinding(tag = "argument", key = "name", attribute = "value", valueFormat = StringValueProvider.class)
		Map<String, Object> getArguments();

		/**
		 * Utilities for creating {@link CommandModel}s from a {@link ButtonConfig}.
		 */
		public class Factory {

			/**
			 * Allocates a {@link CommandModel} for the given {@link ButtonConfig} in the context of
			 * the given {@link LayoutComponent}.
			 */
			public static CommandModel createButton(ButtonConfig buttonConfig, LayoutComponent component) {
				return applyUI(toCommandModel(resolveCommand(buttonConfig, component), buttonConfig, component),
					buttonConfig);
			}

			private static CommandHandler resolveCommand(ButtonConfig buttonConfig, LayoutComponent component) {
				String id = buttonConfig.getCommandId();
				CommandHandler result = component.getCommandById(id);
				if (result == null) {
					Logger.error("No such command '" + id + "' in component '" + component + "'.", Factory.class);
				}
				return result;
			}

			private static CommandModel toCommandModel(CommandHandler command, ButtonConfig buttonConfig,
					LayoutComponent component) {
				if (command == null) {
					return null;
				}
				// Prevent a missing key warning, if the default key is not defined but was
				// overridden in the the toolbar-local UI config.
				return CommandModelFactory.commandModel(command, component, buttonConfig.getArguments(), toLabel(component, command, buttonConfig));
			}

			private static ResKey toLabel(LayoutComponent component, CommandHandler command,
					ButtonConfig buttonConfig) {
				ResKey labelKey;
				if (isSet(buttonConfig, ButtonUIConfig.LABEL_KEY)) {
					labelKey = buttonConfig.getLabelKey();
				} else {
					labelKey =
						ResKey.fallback(component.getResPrefix().key(command.getID()),
							command.getResourceKey(component));
				}
				return labelKey;
			}

			private static CommandModel applyUI(CommandModel button, ButtonUIConfig uiConfig) {
				if (button == null) {
					return null;
				}
				if (isSet(uiConfig, ButtonUIConfig.TOOLTIP_KEY)) {
					button.setTooltip(resolve(uiConfig.getTooltipKey()));
				}
				if (isSet(uiConfig, ButtonUIConfig.TOOLTIP_CAPTION_KEY)) {
					button.setTooltipCaption(resolve(uiConfig.getTooltipCaptionKey()));
				}
				if (isSet(uiConfig, ButtonUIConfig.ACCESS_KEY)) {
					button.setAccessKey(uiConfig.getAccessKey());
				}

				if (isSet(uiConfig, ButtonUIConfig.IMAGE)) {
					button.setImage(uiConfig.getImage());
				}
				if (isSet(uiConfig, ButtonUIConfig.NOT_EXECUTABLE_IMAGE)) {
					button.setNotExecutableImage(uiConfig.getNotExecutableImage());
				}
				if (isSet(uiConfig, ButtonUIConfig.ALT_KEY)) {
					button.setAltText(resolve(uiConfig.getAltKey()));
				}

				if (isSet(uiConfig, ButtonUIConfig.CSS_CLASSES)) {
					button.setCssClasses(uiConfig.getCssClasses());
				}
				return button;
			}

			private static boolean isSet(ButtonUIConfig uiConfig, String property) {
				return uiConfig.valueSet(uiConfig.descriptor().getProperty(property));
			}

			private static String resolve(ResKey key) {
				return Resources.getInstance().getString(key);
			}

		}
	}

}
