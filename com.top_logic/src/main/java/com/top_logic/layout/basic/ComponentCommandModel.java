/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.DefaultCheckScope;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link DynamicCommandModel} calling a {@link CommandHandler} on a specific
 * {@link LayoutComponent} using given arguments.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentCommandModel extends DynamicDelegatingCommandModel {

	/**
	 * Creates a {@link ComponentCommandModel}. All arguments must not be <code>null</code>
	 * 
	 * @param command
	 *        The {@link CommandHandler} to invoke when calling
	 *        {@link #executeCommand(DisplayContext)}.
	 * @param component
	 *        The component on which the given handler is invoked.
	 * @param someArguments
	 *        The arguments to pass to the given handler, see
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param label
	 *        The label of the button.
	 */
	public ComponentCommandModel(CommandHandler command, LayoutComponent component,
			Map<String, Object> someArguments, ResKey label) {
		super(ComponentCommand.newInstance(command, component, someArguments));
		if (label == null) {
			throw new IllegalArgumentException("'label' must not be 'null'.");
		}
		setNotExecutableImage(command.getNotExecutableImage(component));
		setCssClasses(command.getCssClasses(component));
		setShowProgress(true);
	}

	@Override
	public String getLabel() {
		String labelOverride = super.getLabel();
		if (labelOverride != null) {
			return labelOverride;
		}

		Resources resources = Resources.getInstance();
		return resources.getString(getCommandHandler().getResourceKey(getComponent()), null);
	}

	@Override
	public String getTooltip() {
		String tooltipOverride = super.getTooltip();
		if (tooltipOverride != null) {
			return tooltipOverride;
		}

		Resources resources = Resources.getInstance();
		return resources.getString(getCommandHandler().getResourceKey(getComponent()).tooltipOptional());
	}

	@Override
	public ThemeImage getImage() {
		ThemeImage imageOverride = super.getImage();
		if (imageOverride != null) {
			return imageOverride;
		}
		return getCommandHandler().getImage(getComponent());
	}

	/**
	 * If some {@link CheckScope} was set to this command model, it has
	 * priority, otherwise the {@link #getCommandHandler() command handler}
	 * determines the {@link CheckScope}.
	 * 
	 * @see AbstractCommandModel#getCheckScope()
	 */
	@Override
	public CheckScope getCheckScope() {
		CheckScope checkScope = super.getCheckScope();
		if (checkScope != null) {
			return checkScope;
		}
		return new DefaultCheckScope(AbstractCommandHandler.getCheckScopeProvider(getCommandHandler()), getComponent());
	}
	
	/**
	 * No need to use dirty handling, because command execution calls {@link CommandDispatcher}
	 * which also uses dirty handling.
	 */
	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		return internalExecuteCommand(context);
	}

	@Override
	public void updateExecutabilityState() {
		MainLayout mainLayout = getComponent().getMainLayout();
		if (mainLayout == null) {
			CommandModelRegistry.getRegistry().deregisterCommandModel(this);
			return;
		}
		super.updateExecutabilityState();
	}

	/**
	 * This method returns the command which will be executing by calling
	 * {@link #executeCommand(DisplayContext)}
	 * 
	 * @return a {@link BoundCommand}. never <code>null</code>.
	 */
	public final CommandHandler getCommandHandler() {
		return defaultExecutable().getCommand();
	}

	private final ComponentCommand defaultExecutable() {
		return (ComponentCommand) unwrap();
	}

	/**
	 * This method returns the {@link LayoutComponent}, the command will be executed on.
	 * 
	 * @return a {@link LayoutComponent}. never <code>null</code>.
	 */
	public final LayoutComponent getComponent() {
		return defaultExecutable().getComponent();
	}

	/**
	 * This method returns an unmodifiable view to the arguments which are needed for executing the
	 * command.
	 * 
	 * @return an unmodifiable view to the arguments which are needed by the command given by
	 *         {@link #getCommandHandler()}. never <code>null</code>.
	 */
	public final Map<String, Object> getArguments() {
		return defaultExecutable().getArguments();
	}
	
}
