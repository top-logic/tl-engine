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

import com.top_logic.react.flow.data.Box;
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
		visit(root, visitor, false);
	}

	/**
	 * Calls the given visitor for the given widget and for every widget it contains, each one
	 * once.
	 *
	 * <p>
	 * In contrast to {@link #visitAll(Widget, Consumer)}, the traversal does not leave the given
	 * widget: it does not follow the {@link Box#getParent() parent} of a box, and it does not
	 * follow a box whose parent is another widget than the one referring to it.
	 * </p>
	 *
	 * @param root
	 *        The widget to start at, or <code>null</code> to visit nothing.
	 * @param visitor
	 *        What to do with each visited widget.
	 */
	public static void visitContents(Widget root, Consumer<? super Widget> visitor) {
		visit(root, visitor, true);
	}

	private static void visit(Widget root, Consumer<? super Widget> visitor, boolean containedOnly) {
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
				Widget next = referenced.get(n);
				if (containedOnly && !contains(widget, next)) {
					continue;
				}
				pending.add(next);
			}
		}
	}

	/**
	 * Whether the given widget referred to by the given owner is part of the owner, not its
	 * container or a box contained elsewhere.
	 */
	private static boolean contains(Widget owner, Widget widget) {
		if (owner instanceof Box box && box.getParent() == widget) {
			return false;
		}
		if (widget instanceof Box box) {
			return box.getParent() == owner;
		}
		return true;
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
