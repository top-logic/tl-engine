/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers.toggle;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link StateHandler} linking the state of a {@link ToggleCommandByExpression} to a component
 * channel.
 * 
 * <p>
 * The linked component channel is expected to contain boolean values (<code>true</code> for an
 * activated button and <code>false</code> for an inactive button). Whenever the button is pressed,
 * the value of the component channel is changed.
 * </p>
 */
public class ChannelStateHandler extends AbstractConfiguredInstance<ChannelStateHandler.Config<?>>
		implements StateHandler {

	/**
	 * Configuration options for {@link ChannelStateHandler}.
	 */
	public interface Config<I extends ChannelStateHandler> extends PolymorphicConfiguration<I> {

		/**
		 * The component channel to take button states from and to store state updates to.
		 */
		ModelSpec getStateChannel();

	}

	private ChannelLinking _stateChannel;

	/**
	 * Creates a {@link ChannelStateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChannelStateHandler(InstantiationContext context, Config<?> config) {
		super(context, config);

		_stateChannel = context.getInstance(config.getStateChannel());
	}


	@Override
	public boolean getState(LayoutComponent component, Object model) {
		Object value = ChannelLinking.eval(component, _stateChannel);
		return Utils.isTrue((Boolean) value);
	}

	@Override
	public void setState(LayoutComponent component, Object model, boolean state) {
		Boolean value = Boolean.valueOf(state);
		_stateChannel.resolveChannel(new LogProtocol(ToggleCommandByExpression.class), component)
			.set(value);
	}

}
