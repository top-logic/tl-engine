/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.base.context.SubSessionListener;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.TLContextManager;

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
 *
 * <h2>Work that outlives the subscription</h2>
 *
 * <p>
 * A subscription lasts as long as the view is displayed, not as long as the work behind it. Most
 * sources do nothing while nobody looks and release everything when the subscription ends. A
 * monitor that records what happens on a bus or in the database, however, must keep recording
 * while its tab is hidden, so that the whole log is there when the tab is displayed again. Such a
 * source binds the recording to the sub-session of the window instead of the subscription:
 * </p>
 *
 * <ul>
 * <li>{@link #subscribe(ViewContext, ChannelSink)} runs on a request thread of the window, so
 * {@link TLContextManager#getSubSession()} is the sub-session of the window. Each window has a
 * sub-session of its own.</li>
 * <li>The source keys the recording by that sub-session: it starts the recording on the first
 * subscription (or when a command of the view starts it), and every later subscription in the same
 * window - the tab displayed again, the view rebuilt in the window - finds the running recording
 * and publishes its log right away.</li>
 * <li>Disposing a subscription only stops publishing into its sink; the recording goes on.</li>
 * <li>A {@link SubSessionListener} registered with
 * {@link TLSubSessionContext#addUnboundListener(SubSessionListener)} ends the recording at the
 * latest: it is notified when the sub-session is removed, which happens when the window is torn
 * down - closed, or abandoned by its client - and when the session ends. The view is being torn
 * down at that time, so the listener only releases resources; it does not publish anymore. A
 * recording stopped earlier removes its listener with
 * {@link TLSubSessionContext#removeUnboundListener(SubSessionListener)}.</li>
 * </ul>
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
	 * it. When the view is displayed again, it subscribes anew. Whatever the source keeps running
	 * beyond the registration is bound to the sub-session of the window, see the class
	 * documentation.
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
