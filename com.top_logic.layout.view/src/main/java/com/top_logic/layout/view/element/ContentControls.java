/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelNotificationScope;

/**
 * Utilities for the content a view element displays: building it from the element's children, and
 * retiring it when the element shows something else.
 */
public class ContentControls {

	/**
	 * Retires content a container no longer displays: it stops being displayed right away, and is
	 * disposed once the channel notification in progress has unwound.
	 *
	 * <p>
	 * The two halves happen at different times because they answer to different constraints.
	 * Detaching cannot wait: a container exchanges its content from inside a channel notification,
	 * and another listener of the same channel - still pending in the channel's listener snapshot -
	 * may update a control of the content just replaced. Sending that update would address a control
	 * the client has already unmounted, and the browser would go looking for data the server no
	 * longer serves. Disposal, on the other hand, cannot happen yet: the retired controls may
	 * themselves be listeners pending in that same snapshot, and tearing them down synchronously
	 * would let those listeners run on a disposed control.
	 * </p>
	 *
	 * <p>
	 * Outside a notification there is nothing to wait for and the disposal runs immediately.
	 * </p>
	 *
	 * @param content
	 *        The content control to retire.
	 *
	 * @see ChannelNotificationScope#afterNotification(Runnable)
	 */
	public static void retire(ReactControl content) {
		content.detach();
		ChannelNotificationScope.current().afterNotification(content::cleanupTree);
	}

	/**
	 * Instantiates the given {@link UIElement}s in the given context and {@link #combine(ViewContext,
	 * List) combines} their controls into one.
	 *
	 * @param elements
	 *        The child elements to instantiate; may be empty (yields an empty
	 *        {@link ReactStackControl}).
	 * @param context
	 *        The context each child is created in.
	 * @return A single control: the sole child's control, or a {@link ReactStackControl} wrapping
	 *         all of them.
	 */
	public static ReactControl toControl(List<UIElement> elements, ViewContext context) {
		List<IReactControl> controls = elements.stream()
			.map(element -> element.createControl(context))
			.collect(Collectors.toList());
		return combine(context, controls);
	}

	/**
	 * Combines already-created controls into one: the sole control when there is exactly one,
	 * otherwise a {@link ReactStackControl} wrapping all of them.
	 *
	 * @param context
	 *        The context for a wrapping {@link ReactStackControl}, if one is needed.
	 * @param controls
	 *        The child controls; may be empty (yields an empty {@link ReactStackControl}).
	 * @return The single control, or a {@link ReactStackControl} of all controls.
	 */
	public static ReactControl combine(ViewContext context, List<? extends IReactControl> controls) {
		if (controls.size() == 1) {
			return (ReactControl) controls.get(0);
		}
		List<ReactControl> reactChildren = controls.stream()
			.map(control -> (ReactControl) control)
			.collect(Collectors.toList());
		return new ReactStackControl(context, reactChildren);
	}
}
