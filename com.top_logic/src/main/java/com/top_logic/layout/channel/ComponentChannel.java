/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Collection;

import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Communication interface between {@link LayoutComponent}s.
 * 
 * <p>
 * A {@link LayoutComponent} communicates models to other {@link LayoutComponent}s through
 * {@link ComponentChannel}s.
 * </p>
 * 
 * <ul>
 * <li>The current model offered by a {@link ComponentChannel} can be accessed through
 * {@link #get()}.</li>
 * <li>A {@link ComponentChannel} can be requested to accept a new model by calling
 * {@link #set(Object)}.</li>
 * <li>{@link ComponentChannel}s of different components can be {@link #link(ComponentChannel)
 * connected} to automatically synchronize their models.</li>
 * <li>A {@link ComponentChannel} can be observed by a {@link ChannelListener}, see
 * {@link #addListener(ChannelListener)}.</li>
 * </ul>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ComponentChannel {

	/**
	 * The {@link LayoutComponent} this {@link ComponentChannel} belongs to.
	 */
	LayoutComponent getComponent();

	/**
	 * The channel name for debugging.
	 */
	String name();

	/**
	 * The model object that is offered by this {@link ComponentChannel}.
	 */
	Object get();

	/**
	 * Requests this {@link ComponentChannel} to accept the given model object.
	 * 
	 * @param value
	 *        The new model value.
	 * @return Whether the channel's value has actually changed.
	 */
	boolean set(Object value);

	/**
	 * The {@link ComponentChannel}s this channel was connected to.
	 * 
	 * @see #link(ComponentChannel)
	 */
	Collection<ComponentChannel> sources();

	/**
	 * The inverse relation to {@link #sources()}.
	 */
	Collection<ComponentChannel> destinations();

	/**
	 * Links this {@link ComponentChannel} with the given {@link ComponentChannel}.
	 * 
	 * <p>
	 * Linked {@link ComponentChannel}s try to synchronize their {@link #get() offered} models.
	 * </p>
	 * 
	 * @param source
	 *        The {@link ComponentChannel} to link.
	 */
	void link(ComponentChannel source);

	/**
	 * Notifies this {@link ComponentChannel} that the given channel has connected to it.
	 */
	@FrameworkInternal
	void internalLinkBack(ComponentChannel destination);

	/**
	 * Service method to {@link #link(ComponentChannel)} this channel with the resolved channel
	 * defined by the given {@link ChannelLinking} and the given {@link LayoutComponent}.
	 * 
	 * @param log
	 *        {@link Log} to write messages to.
	 * @param contextComponent
	 *        Component used to resolve {@link ChannelLinking}.
	 * @param targetChannel
	 *        Definition of the target channel. May be <code>null</code>. Nothing happens.
	 * 
	 * @see ChannelLinking#resolveChannel(Log, LayoutComponent)
	 */
	default void linkChannel(Log log, LayoutComponent contextComponent, ChannelLinking targetChannel) {
		if (targetChannel == null) {
			return;
		}

		ComponentChannel otherChannel = targetChannel.resolveChannel(log, contextComponent);
		if (otherChannel != null) {
			link(otherChannel);
		}
	}

	/**
	 * Releases the {@link #link(ComponentChannel)} between this and the given other
	 * {@link ComponentChannel}.
	 */
	void unlink(ComponentChannel source);

	/**
	 * Notifies this {@link ComponentChannel} that the given destination has decoupled itself from
	 * this channel.
	 */
	@FrameworkInternal
	void internalUnlinkFrom(ComponentChannel destination);

	/**
	 * Releases all {@link #link(ComponentChannel) links} between this and formerly linked channels.
	 */
	@FrameworkInternal
	void unlinkAll();

	/**
	 * Clears all registered listeners.
	 */
	@FrameworkInternal
	void resetListeners();

	/**
	 * Adds the given {@link ChannelListener} for observing this {@link ComponentChannel}.
	 * 
	 * @param listener
	 *        The {@link ChannelListener} to add.
	 * @return Whether the given {@link ChannelListener} was not already added before.
	 */
	boolean addListener(ChannelListener listener);

	/**
	 * Removes the given {@link ChannelListener}.
	 * 
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the given {@link ChannelListener} was {@link #addListener(ChannelListener)
	 *         added} before.
	 */
	boolean removeListener(ChannelListener listener);

	/**
	 * Adds the given {@link ChannelValueFilter} for observing this {@link ComponentChannel}.
	 * 
	 * @param listener
	 *        The {@link ChannelValueFilter} to add.
	 * @return Whether the given {@link ChannelValueFilter} was not already added before.
	 */
	boolean addVetoListener(ChannelValueFilter listener);

	/**
	 * Removes the given {@link ChannelValueFilter}.
	 * 
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the given {@link ChannelValueFilter} was
	 *         {@link #addVetoListener(ChannelValueFilter) added} before.
	 */
	boolean removeVetoListener(ChannelValueFilter listener);

	/**
	 * Destroys this channel.
	 * 
	 * <p>
	 * When a channel is destroyed, it {@link #unlinkAll() unlinks} from other
	 * {@link ComponentChannel}s, removes all listeners and does not accept new listeners.
	 * </p>
	 * 
	 * @see #unlinkAll()
	 */
	@FrameworkInternal
	void destroy();

	/**
	 * Observer of a {@link ComponentChannel}.
	 */
	interface ChannelListener {
		/**
		 * Callback invoked, if the {@link ComponentChannel#get() offered model} of the given sender
		 * changed.
		 * 
		 * @param sender
		 *        The {@link ComponentChannel} that has an updated model.
		 * @param oldValue
		 *        The old model the given {@link ComponentChannel} had before.
		 * @param newValue
		 *        The new model of the given {@link ComponentChannel}.
		 */
		void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue);
	}

	/**
	 * Checker whether a {@link ComponentChannel} accepts a value.
	 */
	interface ChannelValueFilter {
		/**
		 * Callback invoked before a {@link ComponentChannel} accepts a new value.
		 * 
		 * @param sender
		 *        The {@link ComponentChannel} that has an updated model.
		 * @param oldValue
		 *        The old model the given {@link ComponentChannel} had before.
		 * @param newValue
		 *        The new model of the given {@link ComponentChannel}.
		 * 
		 * @return Whether the new value is accepted. A result of <code>false</code> prevents the
		 *         channel from accepting the new value.
		 */
		boolean accept(ComponentChannel sender, Object oldValue, Object newValue);
	}
}
