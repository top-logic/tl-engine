/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Implementation of a component channel.
 * 
 * <p>
 * A component channel is the standard interface for exchanging data between
 * {@link LayoutComponent}s.
 * </p>
 * 
 * <p>
 * A {@link LayoutComponent} must define available channel kinds by overriding its
 * {@link LayoutComponent#channels()} method.
 * </p>
 * 
 * @see com.top_logic.layout.channel.linking.Channel#getName()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ChannelSPI {

	private final Property<ComponentChannel> _key;

	/**
	 * Creates a {@link ChannelSPI}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 */
	public ChannelSPI(String name) {
		_key = TypedAnnotatable.property(ComponentChannel.class, name);
	}

	/**
	 * The name of channels this {@link ChannelSPI} creates.
	 * 
	 * @see LayoutComponent#getChannel(String)
	 */
	public String getName() {
		return _key.getName();
	}

	/**
	 * Fetches the contents of this channel in the given component.
	 * 
	 * @param sourceComponent
	 *        The component from which to resolve the value of this channel.
	 * @return The channel value.
	 */
	public Object resolveModel(LayoutComponent sourceComponent) {
		return sourceComponent.getChannel(getName()).get();
	}

	/**
	 * Resolves the {@link ComponentChannel} of the given {@link LayoutComponent} that belongs to
	 * {@link ChannelSPI} channel kind.
	 * 
	 * @param component
	 *        The {@link LayoutComponent} to lookup a {@link ComponentChannel} from.
	 * @return The {@link ComponentChannel} of the given component belonging to this
	 *         {@link ChannelSPI} kind.
	 */
	public ComponentChannel lookup(LayoutComponent component) {
		ComponentChannel result = component.get(_key);
		if (result == null) {
			result = createImpl(component);
			component.set(_key, result);
		}
		return result;
	}

	/**
	 * Factory method for creating a {@link ComponentChannel} implementation for this
	 * {@link ChannelSPI} kind.
	 * 
	 * @param component
	 *        The {@link LayoutComponent} to create a {@link ComponentChannel} for.
	 * @return The new {@link ComponentChannel}, if the given component supports this
	 *         {@link ChannelSPI} kind.
	 */
	protected abstract ComponentChannel createImpl(LayoutComponent component);

}
