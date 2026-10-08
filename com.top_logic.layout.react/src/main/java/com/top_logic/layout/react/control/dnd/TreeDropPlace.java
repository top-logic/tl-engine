/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link DropPlace} in a control displaying a tree: a node and a {@link DropZone zone} of it, or
 * the place beside the nodes.
 *
 * <p>
 * A drop {@link DropMode#ONTO onto} a node in any of its zones is made onto that node, and is
 * marked as a highlight of it. An {@link DropMode#ORDERED insertion} in the upper part of a node X
 * inserts before X among its siblings, marked before X; in its middle part as the first children of
 * X, X highlighted; in its lower part as the first children of X where X is expanded and has
 * children, and after X among its siblings otherwise, marked after X; beside the nodes as the last
 * top-level nodes, the control highlighted. A drop on the {@link DropMode#CONTROL control as a
 * whole} is one wherever it is made.
 * </p>
 *
 * <p>
 * The reference objects of a location are the {@link DropTreeNavigation#businessObject(Object)
 * business objects} of the nodes, and the parent of a top-level node is the
 * {@link DropTreeNavigation#topLevelParent() object the top-level nodes are the children of}.
 * </p>
 *
 * @param <N>
 *        The type of the nodes.
 */
public final class TreeDropPlace<N> implements DropPlace {

	private final DropTreeNavigation<N> _tree;

	/** The node the drop was made on, {@code null} for a drop beside the nodes. */
	private final N _node;

	/** The client-side key of {@link #_node}, {@code null} without a node. */
	private final String _nodeKey;

	/** The zone of the node the drop was made in, {@link DropZone#NONE} without a node. */
	private final DropZone _zone;

	/**
	 * Creates a {@link TreeDropPlace}.
	 *
	 * @param tree
	 *        The structure of the nodes of the control.
	 * @param node
	 *        The node the drop was made on, {@code null} for a drop beside the nodes.
	 * @param nodeKey
	 *        The client-side key of the node, the item a marker of the node is drawn at.
	 * @param zone
	 *        The zone of the node the drop was made in; ignored without a node.
	 */
	public TreeDropPlace(DropTreeNavigation<N> tree, N node, String nodeKey, DropZone zone) {
		_tree = tree;
		_node = node;
		_nodeKey = node == null ? null : nodeKey;
		_zone = node == null ? DropZone.NONE : zone;
	}

	/**
	 * The place beside the nodes of the given tree.
	 */
	public static <N> TreeDropPlace<N> beside(DropTreeNavigation<N> tree) {
		return new TreeDropPlace<>(tree, null, null, DropZone.NONE);
	}

	@Override
	public DropLocation location(DropMode mode) {
		switch (mode) {
			case CONTROL:
				return new DropLocation.Control();
			case ONTO:
				if (_node == null) {
					return null;
				}
				return new DropLocation.Onto(_tree.businessObject(_node));
			case ORDERED:
				return insertion();
		}
		throw new IllegalArgumentException("Unknown drop mode: " + mode);
	}

	private DropLocation.Insert insertion() {
		switch (_zone) {
			case UPPER:
				return new DropLocation.Insert(parentObject(_node), _tree.businessObject(_node));
			case MIDDLE:
				return new DropLocation.Insert(_tree.businessObject(_node), businessObject(firstChild(_node)));
			case LOWER: {
				N firstChild = _tree.isExpanded(_node) ? firstChild(_node) : null;
				if (firstChild != null) {
					return new DropLocation.Insert(_tree.businessObject(_node), _tree.businessObject(firstChild));
				}
				return new DropLocation.Insert(parentObject(_node), businessObject(nextSibling(_node)));
			}
			case NONE:
				break;
		}
		return new DropLocation.Insert(_tree.topLevelParent(), null);
	}

	@Override
	public DropMarker marker(DropLocation location) {
		if (location instanceof DropLocation.Onto) {
			return DropMarker.INTO;
		}
		if (location instanceof DropLocation.Insert) {
			switch (_zone) {
				case UPPER:
					return DropMarker.BEFORE;
				case MIDDLE:
					return DropMarker.INTO;
				case LOWER:
					return DropMarker.AFTER;
				case NONE:
					break;
			}
		}
		return DropMarker.CONTROL;
	}

	@Override
	public String markerKey(DropMarker marker) {
		return marker == DropMarker.CONTROL ? null : _nodeKey;
	}

	/** The business object of the given node, {@code null} for no node. */
	private Object businessObject(N node) {
		return node == null ? null : _tree.businessObject(node);
	}

	/** The object whose children the given node is among. */
	private Object parentObject(N node) {
		N parent = _tree.parent(node);
		return parent == null ? _tree.topLevelParent() : _tree.businessObject(parent);
	}

	/** The first child of the given node, {@code null} for a leaf. */
	private N firstChild(N node) {
		List<? extends N> children = _tree.children(node);
		return children.isEmpty() ? null : children.get(0);
	}

	/**
	 * The node following the given one among its siblings, {@code null} for the last one.
	 *
	 * <p>
	 * A node is found among its siblings by its business object, so a navigation may hand out a
	 * fresh value for the same node every time it is asked.
	 * </p>
	 */
	private N nextSibling(N node) {
		Object object = _tree.businessObject(node);
		List<? extends N> siblings = siblings(_tree, node);
		for (int n = 0, cnt = siblings.size(); n < cnt; n++) {
			if (Objects.equals(object, _tree.businessObject(siblings.get(n)))) {
				return n + 1 < cnt ? siblings.get(n + 1) : null;
			}
		}
		return null;
	}

	private static <N> List<? extends N> siblings(DropTreeNavigation<N> tree, N node) {
		N parent = tree.parent(node);
		return parent == null ? tree.topLevel() : tree.children(parent);
	}

	/**
	 * Whether the reference objects of the given location are places of the given tree: the target
	 * of a drop onto a node is the object of a node, the parent of an insertion is the object of a
	 * node - or the {@link DropTreeNavigation#topLevelParent() parent of the top-level nodes} -, and
	 * the object an insertion is made before is a child of that parent.
	 *
	 * <p>
	 * This is what a replayed drop is checked by, which names its location by business objects:
	 * a location the tree does not display is a drift.
	 * </p>
	 *
	 * @param tree
	 *        The structure of the nodes.
	 * @param nodeOf
	 *        The node of a business object, {@code null} if the tree displays none.
	 * @param location
	 *        The location to check.
	 */
	public static <N> boolean displays(DropTreeNavigation<N> tree, Function<Object, N> nodeOf,
			DropLocation location) {
		if (location instanceof DropLocation.Onto onto) {
			return onto.target() == null || nodeOf.apply(onto.target()) != null;
		}
		if (location instanceof DropLocation.Insert insert) {
			List<? extends N> siblings;
			if (Objects.equals(insert.parent(), tree.topLevelParent())) {
				siblings = tree.topLevel();
			} else {
				N parent = insert.parent() == null ? null : nodeOf.apply(insert.parent());
				if (parent == null) {
					return false;
				}
				siblings = tree.children(parent);
			}
			if (insert.before() == null) {
				return true;
			}
			for (N sibling : siblings) {
				if (insert.before().equals(tree.businessObject(sibling))) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}
