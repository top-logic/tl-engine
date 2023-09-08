/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function1;

/**
 * {@link CommandHandlerProxy} that delegates to a {@link CommandHandler} registered in the
 * {@link CommandHandlerFactory} referenced by its {@link CommandHandler.Config#getId()}.
 * 
 * @see CallCommand Dynamically calling another command of the same or another component.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandHandlerReference extends CommandHandlerProxy {

	/**
	 * Configuration of an referenced {@link CommandHandler}.
	 * 
	 * @see AbstractCommandHandler#getInstance(InstantiationContext, PolymorphicConfiguration) To
	 *      resolve the {@link CommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.DEFAULT_TAG_NAME)
	public interface Config extends ConfigBase<CommandHandlerReference>, CommandReferenceConfig {

		/** Default tag name for a {@link CommandHandlerReference.Config} */
		String DEFAULT_TAG_NAME = "reference";

		@Override
		@Derived(fun = Identity.class, args = @Ref({ Config.COMMAND_ID }))
		String getCollapsedTitle();

		/**
		 * Algorithm computing the title for a collapsed {@link CheckerProxyHandler} configuration.
		 */
		class Identity extends Function1<String, String> {
			@Override
			public String apply(String arg) {
				return arg;
			}
		}
	}

	private CommandHandler _impl;

	/**
	 * Creates a {@link CommandHandlerReference} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommandHandlerReference(InstantiationContext context, Config config) {
		super(context, config);
		_impl = CommandHandlerFactory.getInstance().getHandler(config.getCommandId());
	}

	@Override
	protected CommandHandler impl() {
		return _impl;
	}

}

