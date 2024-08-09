/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Uses the value from an arbitrary component channel as input to the UI action.
 * 
 * <p>
 * The command result is ignored by this transformation.
 * </p>
 */
public class ValueFromChannel extends AbstractConfiguredInstance<ValueFromChannel.Config<?>>
		implements ValueTransformation {

	/**
	 * Configuration options for {@link ValueFromChannel}.
	 */
	public interface Config<I extends ValueFromChannel> extends PolymorphicConfiguration<I> {
		/**
		 * Component channel to take the value from that is used in the UI action.
		 */
		@NonNullable
		@ItemDefault(Channel.class)
		ModelSpec getSource();
	}

	private ChannelLinking _source;

	/**
	 * Creates a {@link ValueFromChannel} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ValueFromChannel(InstantiationContext context, Config<?> config) {
		super(context, config);

		_source = context.getInstance(config.getSource());
	}

	@Override
	public Object transform(LayoutComponent component, Object model) {
		return _source.eval(component);
	}

}
