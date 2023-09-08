/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Collection;
import java.util.List;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Base class fro {@link ComponentChannel}s that combine inputs of multiple source channels.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class MultiInputChannel extends AbstractDerivedComponentChannel {

	private final List<ComponentChannel> _sources;

	private ReceivingChannel[] _inputs;

	/**
	 * Creates a {@link MultiInputChannel}.
	 *
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param name
	 *        See {@link #name()}.
	 * @param sources
	 *        The source channels to take values from.
	 */
	public MultiInputChannel(LayoutComponent component, String name, List<ComponentChannel> sources) {
		super(component, name);

		_sources = sources;
		_inputs = new ReceivingChannel[sources.size()];
		for (int n = 0, cnt = sources.size(); n < cnt; n++) {
			_inputs[n] = new ReceivingChannel(component, name() + "-input" + n);
		}
	}

	/**
	 * The number of input channels.
	 */
	public final int size() {
		return _inputs.length;
	}

	@Override
	public Collection<ComponentChannel> sources() {
		return _sources;
	}

	/**
	 * The value of the nth input channel.
	 */
	public final Object get(int n) {
		if (hasListeners()) {
			return _inputs[n].get();
		} else {
			return _sources.get(n).get();
		}
	}

	@Override
	protected void attach() {
		for (int n = 0, cnt = _sources.size(); n < cnt; n++) {
			ReceivingChannel input = _inputs[n];
			input.link(_sources.get(n));
			link(input);
		}
	}

	@Override
	protected void detach() {
		for (int n = 0, cnt = _sources.size(); n < cnt; n++) {
			ReceivingChannel input = _inputs[n];
			unlink(input);
			input.unlink(_sources.get(n));
		}
	}

}
