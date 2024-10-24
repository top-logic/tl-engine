/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutableState;


/**
 * Abstract proxy for {@link CommandHandler}.
 * 
 * @see #impl()
 * 
 * @author Automatically generated by <code>com.top_logic.basic.generate.ProxyGenerator</code>
 */
public abstract class CommandHandlerProxy
		extends AbstractConfiguredInstance<CommandHandler.ConfigBase<? extends CommandHandler>>
		implements CommandHandler {

	/**
	 * Creates a {@link CommandHandlerProxy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommandHandlerProxy(InstantiationContext context, ConfigBase<? extends CommandHandler> config) {
		super(context, config);
	}

	/**
	 * The underlying implementation.
	 */
	protected abstract CommandHandler impl();

	@Override
	public String getID() {
		return impl().getID();
	}

	@Override
	public BoundCommandGroup getCommandGroup() {
		return impl().getCommandGroup();
	}

	@Override
	public String getClique() {
		return impl().getClique();
	}

	@Override
	public boolean isConcurrent() {
		return impl().isConcurrent();
	}

	@Override
	public CommandScriptWriter getCommandScriptWriter(LayoutComponent a1) {
		return impl().getCommandScriptWriter(a1);
	}

	@Override
	public String[] getAttributeNames() {
		return impl().getAttributeNames();
	}

	@Override
	public boolean checkSecurity(LayoutComponent component, Object model, Map<String, Object> a2) {
		return impl().checkSecurity(component, model, a2);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext a1, LayoutComponent a2, Object model, Map<String, Object> a3) {
		return impl().handleCommand(a1, a2, model, a3);
	}

	@Override
	public Object getTargetModel(LayoutComponent component, Map<String, Object> arguments) {
		return impl().getTargetModel(component, arguments);
	}

	@Override
	public CheckScopeProvider checkScopeProvider() {
		return impl().checkScopeProvider();
	}

	@Override
	public ResKey getResourceKey(LayoutComponent component) {
		return impl().getResourceKey(component);
	}

	@Override
	public ResKey getConfirmKey(LayoutComponent component, Map<String, Object> arguments) {
		return impl().getConfirmKey(component, arguments);
	}

	@Override
	public ThemeImage getImage(LayoutComponent component) {
		return impl().getImage(component);
	}

	@Override
	public ThemeImage getNotExecutableImage(LayoutComponent component) {
		return impl().getNotExecutableImage(component);
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent a1, Object model, Map<String, Object> a2) {
		return impl().isExecutable(a1, model, a2);
	}

	@Override
	public boolean mustNotRecordCommand(DisplayContext context, LayoutComponent component,
			Map<String, Object> someArguments) {
		return impl().mustNotRecordCommand(context, component, someArguments);
	}

	@Override
	public boolean operatesOn(String channelName) {
		return impl().operatesOn(channelName);
	}

}
