/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.model.listen.ModelScope;

/**
 * A {@link ViewChannel} that is live only while the view declaring it is displayed.
 *
 * <p>
 * Some channels follow something outside the channel graph: a {@link DerivedViewChannel} computed
 * from the objects other channels hold follows those objects being edited, not only the channel
 * values being replaced; a {@link SourceViewChannel} receives the values a server-side
 * {@link ChannelSource} publishes. Following costs resources - a model listener per object, a
 * subscription to the source - so it runs only while the view is on screen: the view
 * {@link #attach(ModelScope) attaches} its channels when its root control is displayed and
 * {@link #detach() detaches} them when the control disappears - a tab nobody looks at, a closed
 * dialog, a tile frame covered by a drill-down.
 * </p>
 *
 * <p>
 * Attaching catches up with what happened in between: a change made while the channel was detached
 * reached no listener, so a derived value is recomputed when the observation resumes, and a source
 * publishes its current state when subscribed again.
 * </p>
 */
public interface ObservingChannel extends ViewChannel {

	/**
	 * Begins following, and catches up with the changes made while detached.
	 *
	 * @param scope
	 *        The scope model listeners register on, {@code null} for a view built outside a browser
	 *        window, which observes no object. A channel that observes no model ignores it.
	 */
	void attach(ModelScope scope);

	/**
	 * Stops following, until the view is displayed again.
	 */
	void detach();

}
