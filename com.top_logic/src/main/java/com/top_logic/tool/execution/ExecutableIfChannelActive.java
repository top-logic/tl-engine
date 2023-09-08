/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that displays a command only if some configured channel delivers a
 * <code>true</code> value.
 * 
 * <p>
 * This can e.g. be used to display a command only if some other component is in edit mode by
 * observing its edit mode channel.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExecutableIfChannelActive<C extends ExecutableIfChannelActive.Config<?>>
		extends AbstractConfiguredInstance<C> implements ExecutabilityRule {

	/**
	 * Configuration options for {@link ExecutableIfChannelActive}.
	 */
	public interface Config<I extends ExecutableIfChannelActive<?>> extends PolymorphicConfiguration<I> {
		/**
		 * The edit mode channel to observe.
		 */
		@Name("channel")
		@Mandatory
		ModelSpec getChannel();
	}

	private final ChannelLinking _channel;

	/**
	 * Creates a {@link ExecutableIfChannelActive} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExecutableIfChannelActive(InstantiationContext context, C config) {
		super(context, config);
		_channel = context.getInstance(config.getChannel());
	}


	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		Boolean result = (Boolean) ChannelLinking.eval(aComponent, _channel);
		return (result != null && result.booleanValue()) ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}

}
