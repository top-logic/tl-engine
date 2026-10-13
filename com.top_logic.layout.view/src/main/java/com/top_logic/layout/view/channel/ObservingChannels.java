/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.listen.ModelScope;

/**
 * Binds the observation of {@link ObservingChannel}s to the display of the controls they serve.
 *
 * <p>
 * A channel that observes the model - a {@link DefaultViewChannel} dropping a deleted object, a
 * {@link DerivedViewChannel} following the objects its inputs hold - observes only while the
 * content reading it is on screen. Whoever creates channels for a control subtree - a view for the
 * channels it declares, a dialog for its parameters, a tile frame, a template instance - hands them
 * to {@link #observeWhileDisplayed(IReactControl, Collection, ReactContext)} together with the root
 * control of that subtree.
 * </p>
 *
 * <p>
 * Only the creator of a channel binds it. A channel of an enclosing view registered into the
 * context of the subtree - a binding of a view reference or a dialog - is observed while the
 * enclosing view is displayed, and is not handed over again.
 * </p>
 */
public final class ObservingChannels {

	private ObservingChannels() {
		// Static utilities only.
	}

	/**
	 * Lets the {@link ObservingChannel}s among the given channels observe while the given control
	 * is displayed.
	 *
	 * <p>
	 * Each time the control is {@link ReactControl#attach() attached}, the channels are
	 * {@link ObservingChannel#attach(ModelScope) attached} to the {@link ReactContext#getModelScope()
	 * model scope} of the given context; each time it is {@link ReactControl#detach() detached},
	 * the channels are {@link ObservingChannel#detach() detached}. The channels are attached before
	 * the children of the control, so the content displays the values the channels validate on
	 * attaching. Nothing is registered when no channel observes, or when the control is no
	 * {@link ReactControl}, which reports no display.
	 * </p>
	 *
	 * @param root
	 *        The root control of the subtree the channels serve.
	 * @param channels
	 *        The channels created for that subtree. Channels an enclosing view owns are not
	 *        passed.
	 * @param context
	 *        The context supplying the model scope to observe on.
	 */
	public static void observeWhileDisplayed(IReactControl root, Collection<? extends ViewChannel> channels,
			ReactContext context) {
		if (!(root instanceof ReactControl control)) {
			return;
		}
		List<ObservingChannel> observing = new ArrayList<>();
		for (ViewChannel channel : channels) {
			if (channel instanceof ObservingChannel observingChannel) {
				observing.add(observingChannel);
			}
		}
		if (observing.isEmpty()) {
			return;
		}
		control.addAttachListener(() -> {
			ModelScope scope = context.getModelScope();
			for (ObservingChannel channel : observing) {
				channel.attach(scope);
			}
		});
		control.addDetachListener(() -> {
			for (ObservingChannel channel : observing) {
				channel.detach();
			}
		});
	}

}
