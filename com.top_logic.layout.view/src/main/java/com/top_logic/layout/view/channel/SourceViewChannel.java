/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.Logger;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.listen.ModelScope;

/**
 * A writable value channel that additionally receives the values a {@link ChannelSource}
 * publishes.
 *
 * <p>
 * The channel is subscribed to its source only while the view declaring it is displayed: a source
 * typically holds resources per subscriber - a queue consumer, a timer, a listener on a bus - that
 * a tab nobody looks at must not keep busy, the same rule a derived channel follows for its
 * {@link DerivedChannelConfig#getObservedTypes() observed types}. Each {@link #attach(ModelScope)
 * attach} subscribes and catches up through the current state the source publishes right away;
 * each {@link #detach() detach} ends the subscription; {@link #release()} ends it for good.
 * </p>
 *
 * <p>
 * The values the source publishes are delivered by a {@link ChannelPublisher} created on attach. It
 * delivers into the sub-session and the window of the request that attached the view, and under
 * the window's interaction, so a value is written to the channel like a command writes it.
 * </p>
 *
 * <p>
 * A source whose subscription fails leaves the channel unsubscribed; the failure is logged and the
 * view works as with a plain value channel.
 * </p>
 */
public class SourceViewChannel extends DefaultViewChannel implements ObservingChannel {

	private final String _name;

	private final ViewContext _context;

	private final ChannelSource _source;

	private final long _updateInterval;

	/** The publisher of the current subscription, {@code null} while not subscribed. */
	private ChannelPublisher _publisher;

	/** The registration of the current subscription, {@code null} while not subscribed. */
	private Registration _registration;

	/**
	 * Creates a {@link SourceViewChannel}.
	 *
	 * @param name
	 *        The channel name (for debugging).
	 * @param context
	 *        The context of the view instance the channel belongs to, {@code null} for a view that
	 *        is not displayed in a window.
	 * @param source
	 *        The source to subscribe to while the view is displayed.
	 * @param updateInterval
	 *        The shortest time in milliseconds between two values delivered to the channel; zero to
	 *        deliver every value.
	 */
	public SourceViewChannel(String name, ViewContext context, ChannelSource source, long updateInterval) {
		super(name);
		_name = name;
		_context = context;
		_source = source;
		_updateInterval = updateInterval;
	}

	/**
	 * Whether this channel is currently subscribed to its source.
	 */
	public boolean isSubscribed() {
		return _registration != null;
	}

	/**
	 * Subscribes to the source, unless already subscribed.
	 *
	 * @param scope
	 *        Ignored: the source is not a model observation; attaching only marks the view as
	 *        displayed.
	 */
	@Override
	public void attach(ModelScope scope) {
		if (_registration != null) {
			return;
		}
		ChannelPublisher publisher = new ChannelPublisher(_context, this, _updateInterval);
		Registration registration;
		try {
			registration = _source.subscribe(_context, publisher);
		} catch (RuntimeException ex) {
			publisher.close();
			Logger.error("Failed to subscribe channel '" + _name + "' to its source " + _source + ".", ex,
				SourceViewChannel.class);
			return;
		}
		if (registration == null) {
			publisher.close();
			Logger.error("Source " + _source + " returned no registration for channel '" + _name + "'.",
				SourceViewChannel.class);
			return;
		}
		_publisher = publisher;
		_registration = registration;
	}

	/**
	 * Ends the subscription to the source, if any.
	 */
	@Override
	public void detach() {
		Registration registration = _registration;
		ChannelPublisher publisher = _publisher;
		_registration = null;
		_publisher = null;
		if (publisher != null) {
			publisher.close();
		}
		if (registration != null) {
			try {
				registration.dispose();
			} catch (RuntimeException ex) {
				Logger.error("Failed to end the subscription of channel '" + _name + "' to its source "
					+ _source + ".", ex, SourceViewChannel.class);
			}
		}
	}

	@Override
	public void release() {
		detach();
	}

}
