/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.edit;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandHandler} that updates a boolean component channel to a {@link #newValue() new
 * value}.
 * 
 * <p>
 * Before the actual mode switch, a {@link Config#getDelegate() delegate} command can be executed.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetModeCommandHandler extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link SetModeCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * The channel to update.
		 * 
		 * @see SetModeCommandHandler#newValue()
		 */
		@Name("channel")
		ModelSpec getChannel();

		/**
		 * The new mode to set.
		 * 
		 * @see #getChannel()
		 */
		boolean getMode();

		/**
		 * Optional ID of a {@link CommandHandler} of the same component to trigger before updating
		 * the mode.
		 */
		@Nullable
		String getDelegate();

	}

	/**
	 * Creates a {@link SetModeCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SetModeCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {

		String delegateId = config().getDelegate();
		if (delegateId != null) {
			CommandHandler delegate = component.getCommandById(delegateId);
			if (delegate == null) {
				throw new IllegalStateException(
					"Delegate handler '" + delegateId + "' not found in '" + component + "'.");
			}
			HandlerResult result = CommandHandlerUtil.handleCommand(delegate, context, component, arguments);

			if (result.isSuccess()) {
				return switchMode(component);
			} else {
				if (result.isSuspended()) {
					result.appendContinuation(c -> switchMode(component));
				}

				return result;
			}
		} else {
			return switchMode(component);
		}
	}

	private HandlerResult switchMode(LayoutComponent component) {
		ModelSpec editChannel = channel();
		ChannelLinking.updateModel(editChannel, component, Boolean.valueOf(newValue()));
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The new value to update the {@link Config#getChannel() channel} to.
	 */
	final boolean newValue() {
		return config().getMode();
	}

	final ModelSpec channel() {
		return config().getChannel();
	}

	private Config config() {
		return (Config) getConfig();
	}
	
	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		ChannelLinking channel = TypedConfigUtil.createInstance(channel());
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), new ExecutabilityRule() {
			@Override
			public ExecutableState isExecutable(LayoutComponent component, Object model,
					Map<String, Object> someValues) {
				Boolean result = (Boolean) ChannelLinking.eval(component, channel);

				return newValue() ^ (result != null && result.booleanValue()) ? ExecutableState.EXECUTABLE
					: ExecutableState.NOT_EXEC_HIDDEN;
			}
		});
	}

}
