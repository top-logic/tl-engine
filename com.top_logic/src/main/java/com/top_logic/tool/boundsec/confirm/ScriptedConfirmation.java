/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.confirm;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.IFunction3;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyN;
import com.top_logic.layout.ScriptFunction3;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Dynamic {@link CommandConfirmation} that create a confirmation message with TL-Script.
 */
@InApp
public class ScriptedConfirmation extends AbstractConfiguredInstance<ScriptedConfirmation.Config<?>>
		implements CommandConfirmation {

	/**
	 * Configuration options for {@link ScriptedConfirmation}.
	 */
	@DisplayOrder({
		Config.MESSAGE,
		Config.FUNCTION,
	})
	public interface Config<I extends ScriptedConfirmation> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getMessage()
		 */
		String MESSAGE = "message";

		/**
		 * @see #getFunction()
		 */
		String FUNCTION = "function";

		/**
		 * A static message that contain placeholders for arguments.
		 * 
		 * <p>
		 * This message can be filled with arguments by the given confirmation function.
		 * </p>
		 */
		@Name(MESSAGE)
		ResKeyN getMessage();

		/**
		 * A function filling the given message with arguments.
		 * 
		 * <p>
		 * The function can also decide not to return a confirmation method on certain conditions.
		 * </p>
		 * 
		 * <p>
		 * The following arguments can be used:
		 * </p>
		 * 
		 * <pre>
		 * <code>message -> model -> command -> $message.fill($model, $command)</code>
		 * </pre>
		 * 
		 * <dl>
		 * <dt><code>message</code></dt>
		 * <dd>The static message configured above (may contain placeholders such as '{0}').</dd>
		 * <dt><code>model</code></dt>
		 * <dd>The target model of the command being executed.</dd>
		 * <dt><code>command</code></dt>
		 * <dd>The label of the command being executed.</dd>
		 * </dl>
		 */
		@Name(FUNCTION)
		ScriptFunction3<ResKey, ResKeyN, Object, ResKey> getFunction();

	}

	private IFunction3<ResKey, ResKeyN, Object, ResKey> _function;

	/**
	 * Creates a {@link ScriptedConfirmation} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptedConfirmation(InstantiationContext context, Config<?> config) {
		super(context, config);

		_function = context.getInstance(config.getFunction());
	}

	@Override
	public ResKey getConfirmation(LayoutComponent component, ResKey commandLabel, Object model,
			Map<String, Object> arguments) {
		return _function.apply(getConfig().getMessage(), model, commandLabel);
	}

}
