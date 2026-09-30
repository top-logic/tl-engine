/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.Widget;

/**
 * Traversal of the {@link Widget}s a {@link Diagram} is built from.
 *
 * <p>
 * A widget holds the widgets it is built from in its properties: the content of a decoration, the
 * contents of a layout, the edges of a graph, the root of a diagram. This traversal walks all of
 * them without knowing any of these properties, by reading the {@link Widget}'s own property values
 * - whoever adds a widget-valued property to the model is walked along with the rest.
 * </p>
 */
public class WidgetTraversal {

	/**
	 * Calls the given visitor for the given widget and for every widget reachable from it, each one
	 * once.
	 *
	 * <p>
	 * Reachability follows every property holding a widget, a widget among its elements, or a
	 * collection of either: what a widget contains, and what it merely refers to - the parent of a
	 * box, the selection of a diagram - alike. A traversal started at the {@link Diagram} therefore
	 * visits the whole diagram, and one started at a widget within it does too.
	 * </p>
	 *
	 * @param root
	 *        The widget to start at, or <code>null</code> to visit nothing.
	 * @param visitor
	 *        What to do with each visited widget. It may change the widget it is called with;
	 *        changes to the widgets the traversal has not reached yet decide what it reaches.
	 */
	public static void visitAll(Widget root, Consumer<? super Widget> visitor) {
		if (root == null) {
			return;
		}

		Set<Widget> visited = new HashSet<>();
		List<Widget> pending = new ArrayList<>();
		List<Widget> referenced = new ArrayList<>();
		pending.add(root);
		while (!pending.isEmpty()) {
			Widget widget = pending.remove(pending.size() - 1);
			if (!visited.add(widget)) {
				continue;
			}
			visitor.accept(widget);

			referenced.clear();
			for (Object value : widget.values()) {
				collectWidgets(value, referenced);
			}
			// Taken from the end of the pending list, so the widgets of a property are visited in
			// the order the property holds them.
			for (int n = referenced.size() - 1; n >= 0; n--) {
				pending.add(referenced.get(n));
			}
		}
	}

	/**
	 * Adds the widgets the given property value consists of to the given list.
	 */
	private static void collectWidgets(Object value, List<Widget> result) {
		if (value instanceof Widget widget) {
			result.add(widget);
		} else if (value instanceof Iterable<?> elements) {
			for (Object element : elements) {
				collectWidgets(element, result);
			}
		}
	}

}
