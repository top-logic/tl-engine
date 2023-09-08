/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.Channel;

/**
 * {@link ChannelLinking} with multiple input channels.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMultiChannelLinking<C extends AbstractMultiChannelLinking.Config>
		extends AbstractCachedChannelLinking<C> {

	/**
	 * Configuration options for {@link AbstractMultiChannelLinking}.
	 */
	public interface Config extends ModelSpec {
		/**
		 * {@link ModelSpec}s to combine.
		 * 
		 * @implNote This property is <b>not</b> named <code>channels</code> to avoid a tag-name
		 *           conflict with the {@link Channel} configuration of {@link DirectLinking}, which
		 *           uses the tag name <code>channel</code>.
		 */
		@DefaultContainer
		@Label("Channels")
		List<ModelSpec> getSpecs();
	}

	private final List<ChannelLinking> _inputs;

	/**
	 * Creates a {@link AbstractMultiChannelLinking} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractMultiChannelLinking(InstantiationContext context, C config) {
		super(context, config);

		_inputs = TypedConfiguration.getInstanceList(context, config.getSpecs());
	}

	/**
	 * The {@link ChannelLinking}s for the input channels.
	 */
	public List<ChannelLinking> inputs() {
		return _inputs;
	}

}
