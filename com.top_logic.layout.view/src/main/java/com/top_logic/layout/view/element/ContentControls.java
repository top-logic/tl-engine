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
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Utilities for the content a view element displays: building it from the element's children, and
 * retiring it when the element shows something else.
 */
public class ContentControls {

	/**
	 * Retires content a container no longer displays: it is detached and disposed right away.
	 *
	 * <p>
	 * A container exchanges its content from inside a channel notification, and the retired content
	 * may itself listen to the channel that caused the exchange, still pending in the
	 * notification's listener snapshot. Disposing it ends its channel registrations, and a
	 * notification skips a listener whose registration is disposed before it is reached (see
	 * {@link ViewChannel#set(Object)}). The retired content therefore does not hear a value it was not
	 * built for: a discarded form or table does not evaluate its expressions over the new value.
	 * </p>
	 *
	 * <p>
	 * A control of the retired content may still be on the call stack - e.g. the selecting control
	 * whose own channel write replaced the presentation containing it - and continue after its
	 * disposal; a disposed control tolerates such trailing state updates (see
	 * {@link ReactControl#cleanupTree()}).
	 * </p>
	 *
	 * @param content
	 *        The content control to retire.
	 */
	public static void retire(ReactControl content) {
		content.cleanupTree();
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
