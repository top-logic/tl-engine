/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.ViewContext;

/**
 * A server-side origin of channel values - a listener of a message bus, a queue consumer, a timer -
 * feeding a {@link SourceChannelConfig source channel} of a view.
 *
 * <p>
 * One source instance serves every view instance that declares the channel: it is created once
 * from the configuration, and each displayed view instance subscribes to it with a
 * {@link ChannelSink} of its own. A source that shares one external connection among its
 * subscribers manages that sharing itself.
 * </p>
 */
public interface ChannelSource {

	/**
	 * Subscribes the channel of a displayed view to this source.
	 *
	 * <p>
	 * Called on a thread serving a request of the window, when the view declaring the channel is
	 * displayed. The channel knows nothing of what happened before, so the source publishes its
	 * current state into the given sink right away, and every later state after that. Publishing
	 * may happen from any thread.
	 * </p>
	 *
	 * <p>
	 * The returned registration is disposed when the view is hidden or destroyed. After that, the
	 * source publishes nothing more into the sink; publishing anyway is harmless, the sink ignores
	 * it. When the view is displayed again, it subscribes anew.
	 * </p>
	 *
	 * @param context
	 *        The context of the view instance that subscribes.
	 * @param sink
	 *        The sink to publish the values of the channel into.
	 * @return The handle that ends the subscription when {@link Registration#dispose() disposed}.
	 */
	Registration subscribe(ViewContext context, ChannelSink sink);

}
