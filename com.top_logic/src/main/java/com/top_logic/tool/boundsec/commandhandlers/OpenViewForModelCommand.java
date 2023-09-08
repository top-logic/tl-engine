/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Opens the default view for the given target model.
 * <p>
 * The advantage of this command over the {@link GotoHandler} is, that this command uses standard
 * configuration option {@link Config#getTarget()}, not a custom unconfigurable parameter in the
 * argument maps. That makes it easy to replace this command with another command that uses the
 * standard {@link Config#getTarget()} mechanism.
 * </p>
 * <p>
 * An example for a replacement command is {@link SetComponentModelCommand}, which just sets the
 * target model as the components model.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class OpenViewForModelCommand extends AbstractCommandHandler {

	/** {@link ConfigurationItem} for the {@link OpenViewForModelCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		/** The default value for {@link #getId()}. */
		String ID = "openViewForModel";

		/** Property name of {@link #getGotoCommandId()}. */
		String GOTO_COMMAND_ID = "gotoCommandId";

		@Override
		@StringDefault(ID)
		String getId();

		@Override
		@StringDefault(CommandHandlerFactory.INTERNAL_GROUP)
		String getClique();

		/** The command id of the {@link GotoHandler} that should be used. */
		@Name(GOTO_COMMAND_ID)
		@StringDefault(GotoHandler.COMMAND)
		String getGotoCommandId();

	}

	/** {@link TypedConfiguration} constructor for {@link OpenViewForModelCommand}. */
	public OpenViewForModelCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent departureComponent, Object model,
			Map<String, Object> arguments) {
		GotoHandler gotoHandler = getGotoCommand(departureComponent);
		return gotoHandler.executeGoto(context, departureComponent, null, model);
	}

	/**
	 * Lookup the {@link GotoHandler} that should be used.
	 * <p>
	 * The {@link GotoHandler} is first searched in the component, and if there is none, in the
	 * {@link CommandHandlerFactory}.
	 * </p>
	 * 
	 * @return Never null.
	 */
	protected GotoHandler getGotoCommand(LayoutComponent component) {
		GotoHandler gotoOnComponent = (GotoHandler) component.getCommandById(getGotoCommandId());
		if (gotoOnComponent != null) {
			return gotoOnComponent;
		}
		GotoHandler globalGoto = (GotoHandler) CommandHandlerFactory.getInstance().getHandler(getGotoCommandId());
		if (globalGoto == null) {
			throw new RuntimeException("There is no command registered with id '" + getGotoCommandId()
				+ "'. Neither at the component '" + component.getName() + "' nor in the CommandHandlerFactory.");
		}
		return globalGoto;
	}

	/**
	 * The ID of the {@link GotoHandler} that should be used.
	 * 
	 * @return Never null.
	 */
	protected String getGotoCommandId() {
		return getConfigTyped().getGotoCommandId();
	}

	/** Variant of {@link #getConfig()} that returns the declares the correct type. */
	public Config getConfigTyped() {
		return (Config) super.getConfig();
	}

}
