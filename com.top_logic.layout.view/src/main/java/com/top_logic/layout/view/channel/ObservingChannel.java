/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.model.listen.ModelScope;

/**
 * A {@link ViewChannel} that observes the model while the view declaring it is displayed.
 *
 * <p>
 * A channel whose value is computed from the objects other channels hold must follow those objects
 * being edited, not only the channel values being replaced. That observation costs a model listener
 * per object, so it runs only while the view is on screen: the view
 * {@link #attach(ModelScope) attaches} its channels when its root control is displayed and
 * {@link #detach() detaches} them when the control disappears - a tab nobody looks at, a closed
 * dialog, a tile frame covered by a drill-down.
 * </p>
 *
 * <p>
 * Attaching catches up with what happened in between: a change made while the channel was detached
 * reached no listener, so the value is recomputed when the observation resumes.
 * </p>
 */
public interface ObservingChannel extends ViewChannel {

	/**
	 * Begins observing on the given {@link ModelScope}, and catches up with the changes made while
	 * detached.
	 *
	 * @param scope
	 *        The scope the model listeners register on, {@code null} for a view built outside a
	 *        browser window, which observes no object.
	 */
	void attach(ModelScope scope);

	/**
	 * Stops observing, until the view is displayed again.
	 */
	void detach();

}
