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

/**
 * Utilities for turning view-element children into a single {@link ReactControl}.
 */
public class ContentControls {

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
