/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.template.gui.ScriptContainer.PersistableScriptContainer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A {@link CommandHandler} that saves the {@link PersistableScriptContainer} that is the selection
 * of its {@link Selectable} component.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SaveScriptCommand extends AbstractCommandHandler {

	/**
	 * The {@link ExecutabilityRule} of the {@link SaveScriptCommand}.
	 */
	public static final class Executability implements ExecutabilityRule {

		@Override
		public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
			if (model == null) {
				return ExecutableState.createDisabledState(I18NConstants.SAVE_SCRIPT_ERROR_NO_SCRIPT_FILE);
			}
			if (!(model instanceof ScriptContainer)) {
				throw new RuntimeException("Invalid model. Expected a " + ScriptContainer.class.getSimpleName()
					+ " but found: " + StringServices.debug(model));
			}
			ScriptContainer script = (ScriptContainer) model;
			if (!(script instanceof PersistableScriptContainer)) {
				return ExecutableState.createDisabledState(I18NConstants.SAVE_SCRIPT_ERROR_SCRIPT_NOT_SAVABLE);
			}
			return ExecutableState.EXECUTABLE;
		}

	}

	/**
	 * The {@link ConfigurationItem} of the {@link SaveScriptCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/** The default value of {@link #getId()}. */
		String DEFAULT_VALUE_ID = COMMAND_ID;

		/** The default value of {@link #getImage()}. */
		String DEFAULT_IMAGE = "theme:SAVE_SCRIPT";

		@Override
		@StringDefault(DEFAULT_VALUE_ID)
		String getId();

		@Override
		@StringDefault(CommandHandlerFactory.ADDITIONAL_APPLY_CLIQUE)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();

		@ListDefault({ ServerScriptSelectorExecutability.class, Executability.class })
		@Override
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		@FormattedDefault(DEFAULT_IMAGE)
		@Override
		ThemeImage getImage();

	}

	/**
	 * The id under which the {@link SaveScriptCommand} is registered at the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "saveScript";

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SaveScriptCommand}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public SaveScriptCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> arguments) {
		ScriptContainer script = (ScriptContainer) model;
		if (script == null) {
			return HandlerResult.error(I18NConstants.SAVE_SCRIPT_ERROR_NO_SCRIPT_FILE);
		}
		if (!(script instanceof PersistableScriptContainer)) {
			return HandlerResult.error(I18NConstants.SAVE_SCRIPT_ERROR_SCRIPT_NOT_SAVABLE);
		}
		PersistableScriptContainer persistableScript = (PersistableScriptContainer) script;
		persistableScript.persist();
		return HandlerResult.DEFAULT_RESULT;
	}

}
