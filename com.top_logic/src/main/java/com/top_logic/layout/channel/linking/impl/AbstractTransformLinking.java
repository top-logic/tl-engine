/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import java.util.function.BiFunction;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.TransformingChannel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelLinking} creating a {@link TransformingChannel}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTransformLinking<C extends AbstractTransformLinking.Config>
		extends AbstractCachedChannelLinking<C> {

	/**
	 * Configuration options for {@link AbstractTransformLinking}.
	 */
	public interface Config extends ModelSpec {

		/**
		 * The channel providing the input value of the transformation.
		 */
		@DefaultContainer
		ModelSpec getInput();

	}

	private final ChannelLinking _input;

	/**
	 * Creates a {@link AbstractTransformLinking}.
	 */
	public AbstractTransformLinking(InstantiationContext context, C config) {
		super(context, config);

		_input = context.getInstance(config.getInput());
	}

	/**
	 * The input channel.
	 */
	protected ChannelLinking input() {
		return _input;
	}

	@Override
	public ComponentChannel createChannel(Log log, LayoutComponent contextComponent) {
		return new TransformingChannel(contextComponent, _input.resolveChannel(log, contextComponent),
			transformation());
	}

	@Override
	public Object eval(LayoutComponent component) {
		return transformation().apply(_input.eval(component), null);
	}

	/**
	 * The transformation to apply to the value of the input channel.
	 * 
	 * <p>
	 * The function receives the value of the input channel as first argument and the old channel
	 * value as second argument.
	 * </p>
	 */
	protected abstract BiFunction<Object, Object, ?> transformation();

}
