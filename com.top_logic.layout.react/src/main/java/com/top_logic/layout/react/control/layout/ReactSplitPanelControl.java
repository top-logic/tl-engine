/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;

/**
 * A {@link ReactControl} that arranges child controls along a single axis with optional drag
 * resizing.
 *
 * <p>
 * Tracks which children are collapsed. When all children are collapsed, the split panel itself
 * collapses in its parent (cascading collapse).
 * </p>
 *
 * <p>
 * The React component {@code TLSplitPanel} receives the following state:
 * </p>
 * <ul>
 * <li>{@code orientation} - "horizontal" or "vertical"</li>
 * <li>{@code resizable} - whether splitters are shown between children</li>
 * <li>{@code children} - list of child descriptors, each merging
 * {@code {controlId, module, state}} with layout constraint fields
 * {@code {size, unit, minSize, scrolling, collapsed}}</li>
 * </ul>
 */
public class ReactSplitPanelControl extends ReactControl {

	private static final String REACT_MODULE = "TLSplitPanel";

	/** @see #ReactSplitPanelControl(ReactContext, Orientation, boolean) */
	private static final String ORIENTATION = "orientation";

	/** @see #ReactSplitPanelControl(ReactContext, Orientation, boolean) */
	private static final String RESIZABLE = "resizable";

	/** @see #addChild(ReactControl, ChildConstraint) */
	private static final String CHILDREN = "children";

	/** Child descriptor: the child {@link ReactControl}. */
	private static final String CONTROL = "control";

	/** Child descriptor: size in the given unit. */
	private static final String SIZE = "size";

	/** Child descriptor: size unit ({@code "px"} or {@code "%"}). */
	private static final String UNIT = "unit";

	/** Child descriptor: minimum size in pixels. */
	private static final String MIN_SIZE = "minSize";

	/** Child descriptor: CSS overflow strategy. */
	private static final String SCROLLING = "scrolling";

	/** Child descriptor: whether the child is collapsed. */
	private static final String COLLAPSED = "collapsed";

	/** Command argument: map of control IDs to new pixel sizes. */
	private static final String SIZES_ARG = "sizes";

	private final List<ChildEntry> _children = new ArrayList<>();

	private final List<Map<String, Object>> _childDescriptors = new ArrayList<>();

	private final boolean _resizable;

	private ReactSplitPanelControl _parentSplitPanel;

	private int _indexInParent = -1;

	private final Consumer<Map<String, Float>> _onSizesChanged;

	private final BiConsumer<Integer, Boolean> _onChildCollapsed;

	/**
	 * Creates a new {@link ReactSplitPanelControl} with persistence callbacks.
	 *
	 * @param orientation
	 *        The layout orientation (horizontal or vertical).
	 * @param resizable
	 *        Whether draggable splitters are shown between children.
	 * @param onSizesChanged
	 *        Called after a drag-resize with a map of control ID to new pixel size. May be
	 *        {@code null}.
	 * @param onChildCollapsed
	 *        Called when a child collapses or expands, with ({@code childIndex}, collapsed). May be
	 *        {@code null}.
	 */
	public ReactSplitPanelControl(ReactContext context, Orientation orientation, boolean resizable,
			Consumer<Map<String, Float>> onSizesChanged,
			BiConsumer<Integer, Boolean> onChildCollapsed) {
		super(context, null, REACT_MODULE);
		_resizable = resizable;
		_onSizesChanged = onSizesChanged;
		_onChildCollapsed = onChildCollapsed;

		putState(ORIENTATION, orientation == Orientation.HORIZONTAL ? "horizontal" : "vertical");
		putState(RESIZABLE, Boolean.valueOf(resizable));
		putState(CHILDREN, _childDescriptors);
	}

	/**
	 * Creates a new {@link ReactSplitPanelControl} without persistence callbacks.
	 *
	 * @param orientation
	 *        The layout orientation (horizontal or vertical).
	 * @param resizable
	 *        Whether draggable splitters are shown between children.
	 */
	public ReactSplitPanelControl(ReactContext context, Orientation orientation, boolean resizable) {
		this(context, orientation, resizable, null, null);
	}

	/**
	 * Adds a child control with the given layout constraint.
	 *
	 * @param child
	 *        The child control.
	 * @param constraint
	 *        The layout constraint for the child.
	 */
	public void addChild(ReactControl child, ChildConstraint constraint) {
		addChild(child, constraint, false, 0, null);
	}

	/**
	 * Adds a child control with the given constraint and initial collapse state.
	 *
	 * <p>
	 * Use this overload to restore a previously persisted collapse state. The {@code savedSize} and
	 * {@code savedUnit} define the size the child will expand to when uncollapsed.
	 * </p>
	 *
	 * @param child
	 *        The child control.
	 * @param constraint
	 *        The layout constraint for the child (containing the current/collapsed size).
	 * @param initialCollapsed
	 *        Whether the child starts collapsed.
	 * @param savedSize
	 *        The size to restore when expanding (typically the config default).
	 * @param savedUnit
	 *        The unit of the saved size.
	 */
	public void addChild(ReactControl child, ChildConstraint constraint,
			boolean initialCollapsed, float savedSize, DisplayUnit savedUnit) {
		ChildEntry entry = new ChildEntry(child, constraint);
		if (initialCollapsed) {
			entry._collapsed = true;
			entry._savedSize = savedSize;
			entry._savedUnit = savedUnit;
		}
		_children.add(entry);

		_childDescriptors.add(entry.toDescriptor());

		// If child is a panel, set up parent tracking.
		if (child instanceof ReactPanelControl) {
			((ReactPanelControl) child).setParentSplitPanel(this, _children.size() - 1);
		}
		// If child is a nested split panel, set up parent tracking.
		if (child instanceof ReactSplitPanelControl) {
			((ReactSplitPanelControl) child).setParentSplitPanel(this, _children.size() - 1);
		}
	}

	/**
	 * Removes the child at the given index.
	 *
	 * @param index
	 *        The index of the child to remove.
	 */
	public void removeChild(int index) {
		ChildEntry removed = _children.remove(index);
		_childDescriptors.remove(index);

		// Unregister removed child and its subtree from SSE.
		removed._control.cleanupTree();

		// Update parent indices for subsequent children.
		for (int i = index; i < _children.size(); i++) {
			ReactControl c = _children.get(i)._control;
			if (c instanceof ReactPanelControl) {
				((ReactPanelControl) c).setParentSplitPanel(this, i);
			}
			if (c instanceof ReactSplitPanelControl) {
				((ReactSplitPanelControl) c).setParentSplitPanel(this, i);
			}
		}
	}

	/**
	 * Called by a child {@link ReactPanelControl} or nested {@link ReactSplitPanelControl} when it
	 * collapses or expands.
	 *
	 * <p>
	 * Updates the child's collapsed flag and size. When ALL children are collapsed, cascades to the
	 * parent split panel.
	 * </p>
	 *
	 * @param childIndex
	 *        The index of the child in this split panel.
	 * @param collapsed
	 *        {@code true} if the child collapsed, {@code false} if it expanded.
	 * @param collapsedSize
	 *        The pixel size of the collapsed child (e.g. toolbar header height).
	 */
	void childCollapsed(int childIndex, boolean collapsed, float collapsedSize) {
		ChildEntry entry = _children.get(childIndex);

		if (collapsed) {
			// Store original size for later restoration.
			entry._savedSize = entry._constraint._size;
			entry._savedUnit = entry._constraint._unit;

			entry._constraint._size = collapsedSize;
			entry._constraint._unit = DisplayUnit.PIXEL;
			entry._collapsed = true;
		} else {
			// Restore original size.
			entry._constraint._size = entry._savedSize;
			entry._constraint._unit = entry._savedUnit;
			entry._collapsed = false;
		}

		// Update the serialized child list.
		_childDescriptors.set(childIndex, entry.toDescriptor());

		// Patch the client.
		patchChildren();

		// Cascading collapse: if all children are now collapsed, notify parent.
		if (_parentSplitPanel != null) {
			boolean allCollapsed = _children.stream().allMatch(e -> e._collapsed);
			if (allCollapsed && collapsed) {
				float totalCollapsedSize = 0;
				for (ChildEntry e : _children) {
					totalCollapsedSize += e._constraint._size;
				}
				_parentSplitPanel.childCollapsed(_indexInParent, true, totalCollapsedSize);
			} else if (!collapsed && wasAllCollapsed()) {
				_parentSplitPanel.childCollapsed(_indexInParent, false, 0);
			}
		}
		if (_onChildCollapsed != null) {
			_onChildCollapsed.accept(Integer.valueOf(childIndex), Boolean.valueOf(collapsed));
		}
	}

	/**
	 * Checks whether the child at the given index is allowed to collapse.
	 *
	 * <p>
	 * Returns {@code false} only when collapsing the child would make ALL children collapsed AND the
	 * collapse cannot escalate to a parent split panel (i.e. this is the root). This prevents the
	 * display from becoming broken when no visible panel remains.
	 * </p>
	 *
	 * @param childIndex
	 *        The index of the child that wants to collapse.
	 * @return {@code true} if the collapse is allowed, {@code false} if it must be prevented.
	 */
	boolean canChildCollapse(int childIndex) {
		int nonCollapsedOthers = 0;
		for (int i = 0; i < _children.size(); i++) {
			if (i != childIndex && !_children.get(i)._collapsed) {
				nonCollapsedOthers++;
			}
		}
		if (nonCollapsedOthers > 0) {
			return true;
		}
		// All siblings already collapsed --- this would make ALL children collapsed.
		if (_parentSplitPanel == null) {
			return false; // Root: cannot escalate.
		}
		return _parentSplitPanel.canChildCollapse(_indexInParent);
	}

	/**
	 * Sets the parent split panel for cascading collapse.
	 *
	 * @param parent
	 *        The parent split panel.
	 * @param indexInParent
	 *        The index of this split panel in the parent.
	 */
	void setParentSplitPanel(ReactSplitPanelControl parent, int indexInParent) {
		_parentSplitPanel = parent;
		_indexInParent = indexInParent;
	}

	@Override
	protected void cleanupChildren() {
		for (ChildEntry entry : _children) {
			entry._control.cleanupTree();
		}
	}

private void patchChildren() {
		Map<String, Object> patch = new HashMap<>();
		patch.put(CHILDREN, _childDescriptors);
		patchReactState(patch);
	}

	private boolean wasAllCollapsed() {
		// Before this expand, all other children must still be collapsed.
		int collapsed = 0;
		for (ChildEntry entry : _children) {
			if (entry._collapsed) {
				collapsed++;
			}
		}
		// Exactly one was just expanded, so if all-1 are still collapsed, it was "all collapsed" before.
		return collapsed == _children.size() - 1;
	}

	/**
	 * Layout constraint for a child in a split panel.
	 */
	public static class ChildConstraint {

		float _size;

		DisplayUnit _unit;

		int _minSize;

		Scrolling _scrolling;

		/**
		 * Creates a new {@link ChildConstraint}.
		 *
		 * @param size
		 *        The size in the given unit.
		 * @param unit
		 *        The unit ({@link DisplayUnit#PIXEL} or {@link DisplayUnit#PERCENT}).
		 * @param minSize
		 *        The minimum size in pixels (0 for no minimum).
		 * @param scrolling
		 *        The scrolling strategy.
		 */
		public ChildConstraint(float size, DisplayUnit unit, int minSize, Scrolling scrolling) {
			_size = size;
			_unit = unit;
			_minSize = minSize;
			_scrolling = scrolling;
		}

		/**
		 * Creates a percentage-based constraint with default minimum size and auto scrolling.
		 *
		 * @param percent
		 *        The percentage size.
		 */
		public static ChildConstraint percent(float percent) {
			return new ChildConstraint(percent, DisplayUnit.PERCENT, 0, Scrolling.AUTO);
		}

		/**
		 * Creates a pixel-based constraint with auto scrolling.
		 *
		 * @param pixels
		 *        The pixel size.
		 */
		public static ChildConstraint pixels(float pixels) {
			return new ChildConstraint(pixels, DisplayUnit.PIXEL, 0, Scrolling.AUTO);
		}
	}

	/**
	 * Internal entry tracking a child control with its constraint and collapse state.
	 */
	private static class ChildEntry {

		final ReactControl _control;

		final ChildConstraint _constraint;

		boolean _collapsed;

		float _savedSize;

		DisplayUnit _savedUnit;

		ChildEntry(ReactControl control, ChildConstraint constraint) {
			_control = control;
			_constraint = constraint;
		}

		/**
		 * Creates a merged descriptor map containing both the child control descriptor and
		 * constraint fields.
		 */
		Map<String, Object> toDescriptor() {
			Map<String, Object> map = new HashMap<>();
			// The ReactControl itself will be serialized as {controlId, module, state} by
			// writeJsonValue.
			map.put(CONTROL, _control);
			map.put(SIZE, Float.valueOf(_constraint._size));
			map.put(UNIT, _constraint._unit.getExternalName());
			map.put(MIN_SIZE, Integer.valueOf(_constraint._minSize));
			map.put(SCROLLING, _constraint._scrolling.toOverflowAttribute());
			map.put(COLLAPSED, Boolean.valueOf(_collapsed));
			return map;
		}
	}

	/**
	 * Handles the updateSizes command sent by the React client after a splitter drag operation.
	 */
	@SuppressWarnings("unchecked")
	@ReactCommand("updateSizes")
	void handleUpdateSizes(Map<String, Object> arguments) {
		Map<String, Object> sizes = (Map<String, Object>) arguments.get(SIZES_ARG);
		if (sizes != null) {
			for (int i = 0; i < _children.size(); i++) {
				ChildEntry entry = _children.get(i);
				String controlId = entry._control.getID();
				Object newSize = sizes.get(controlId);
				if (newSize instanceof Number) {
					entry._constraint._size = ((Number) newSize).floatValue();
					entry._constraint._unit = DisplayUnit.PIXEL;
				}
			}
			List<Map<String, Object>> list = _childDescriptors;
			list.clear();
			for (ChildEntry entry : _children) {
				list.add(entry.toDescriptor());
			}
			if (_onSizesChanged != null) {
				Map<String, Float> sizeMap = new LinkedHashMap<>();
				for (ChildEntry entry : _children) {
					sizeMap.put(entry._control.getID(), Float.valueOf(entry._constraint._size));
				}
				_onSizesChanged.accept(sizeMap);
			}
		}
	}
}
