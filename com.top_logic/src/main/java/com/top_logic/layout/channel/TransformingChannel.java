/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;

import com.top_logic.layout.channel.linking.impl.AbstractTransformLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link AbstractDerivedComponentChannel} offering transformed input values.
 * 
 * @see AbstractTransformLinking
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransformingChannel extends AbstractDerivedComponentChannel {

	private final ComponentChannel _source;

	private ReceivingChannel _in;

	private final BiFunction<Object, Object, ?> _transform;

	/**
	 * Creates a {@link TransformingChannel}.
	 *
	 * @param component
	 *        The context component.
	 * @param source
	 *        The source channel to take input values from.
	 * @param transform
	 *        The transformation to apply to input values.
	 */
	public TransformingChannel(LayoutComponent component, ComponentChannel source,
			BiFunction<Object, Object, ?> transform) {
		super(component, "transform(" + transform.getClass().getName() + ")");
		_in = new ReceivingChannel(component, name() + "-input");
		_source = source;
		_transform = transform;
	}

	@Override
	public Collection<ComponentChannel> sources() {
		return Collections.singletonList(_source);
	}

	@Override
	protected void attach() {
		_in.link(_source);

		link(_in);
	}

	@Override
	protected void detach() {
		unlink(_in);

		_in.unlink(_source);
	}

	@Override
	protected Object lookupValue() {
		return transform(source().get(), oldValue());
	}

	private ComponentChannel source() {
		return _source;
	}

	/**
	 * Transforms the given input model to an output model sent to linked channels.
	 */
	private Object transform(Object model, Object oldValue) {
		return _transform.apply(model, oldValue);
	}

}