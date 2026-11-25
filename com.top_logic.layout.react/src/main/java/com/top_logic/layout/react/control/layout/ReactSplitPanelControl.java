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
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;

/**
 * A {@link ReactControl} that arranges child controls along a single axis with optional drag
 * resizing.
 *
 * <p>
 * Tracks which children are collapsed. A child that is a {@link CollapsibleChild} reports when it
 * collapses, and its area shrinks to the size the child reports. When all children are collapsed,
 * the split panel itself collapses in its own container, being a {@link CollapsibleChild} itself
 * (cascading collapse).
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
public class ReactSplitPanelControl extends ReactControl implements CollapsibleChild {

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

	/**
	 * Command sent by the client after a splitter drag.
	 *
	 * @see #handleUpdateSizes(Map)
	 */
	public static final String UPDATE_SIZES_COMMAND = "updateSizes";

	/**
	 * Command sent by the client to give the panes their default sizes back.
	 *
	 * @see #handleResetSizes()
	 */
	public static final String RESET_SIZES_COMMAND = "resetSizes";

	/**
	 * Argument of {@link #UPDATE_SIZES_COMMAND}: map of the control IDs of the open panes to their
	 * sizes, each in the unit of its pane.
	 */
	public static final String SIZES_ARG = "sizes";

	private final List<ChildEntry> _children = new ArrayList<>();

	private final List<Map<String, Object>> _childDescriptors = new ArrayList<>();

	private final boolean _resizable;

	private CollapseContainer _container;

	private final Consumer<Map<String, Float>> _onSizesChanged;

	private final BiConsumer<Integer, Boolean> _onChildCollapsed;

	private final Runnable _onSizesReset;

	/**
	 * Creates a new {@link ReactSplitPanelControl} with persistence callbacks.
	 *
	 * @param orientation
	 *        The layout orientation (horizontal or vertical).
	 * @param resizable
	 *        Whether draggable splitters are shown between children.
	 * @param onSizesChanged
	 *        Called after a drag-resize with a map of control ID to size of every pane in pane
	 *        order, each in the unit of its pane; for a collapsed pane, the size it expands to. May
	 *        be {@code null}.
	 * @param onChildCollapsed
	 *        Called when a child collapses or expands, with ({@code childIndex}, collapsed). May be
	 *        {@code null}.
	 */
	public ReactSplitPanelControl(ReactContext context, Orientation orientation, boolean resizable,
			Consumer<Map<String, Float>> onSizesChanged,
			BiConsumer<Integer, Boolean> onChildCollapsed) {
		this(context, orientation, resizable, onSizesChanged, onChildCollapsed, null);
	}

	/**
	 * Creates a new {@link ReactSplitPanelControl} with persistence callbacks.
	 *
	 * @param orientation
	 *        The layout orientation (horizontal or vertical).
	 * @param resizable
	 *        Whether draggable splitters are shown between children.
	 * @param onSizesChanged
	 *        Called after a drag-resize with a map of control ID to size of every pane in pane
	 *        order, each in the unit of its pane; for a collapsed pane, the size it expands to. May
	 *        be {@code null}.
	 * @param onChildCollapsed
	 *        Called when a child collapses or expands, with ({@code childIndex}, collapsed). May be
	 *        {@code null}.
	 * @param onSizesReset
	 *        Called after the user reset the panes to their default sizes, so that remembered sizes
	 *        are forgotten. May be {@code null}.
	 */
	public ReactSplitPanelControl(ReactContext context, Orientation orientation, boolean resizable,
			Consumer<Map<String, Float>> onSizesChanged,
			BiConsumer<Integer, Boolean> onChildCollapsed, Runnable onSizesReset) {
		super(context, null, REACT_MODULE);
		_resizable = resizable;
		_onSizesChanged = onSizesChanged;
		_onChildCollapsed = onChildCollapsed;
		_onSizesReset = onSizesReset;

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
	 *        The size to restore when expanding.
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

		if (child instanceof CollapsibleChild collapsible) {
			collapsible.setCollapseContainer(new EntryContainer(entry));
		}

		if (isAttached()) {
			// Added into an already displayed split panel.
			child.attach();
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

		if (removed._control instanceof CollapsibleChild collapsible) {
			collapsible.setCollapseContainer(null);
		}

		// No longer displayed, so its contributions to the surroundings are withdrawn before disposal.
		removed._control.detach();

		// Unregister removed child and its subtree from SSE.
		removed._control.cleanupTree();
	}

	/**
	 * Sets the size the given child returns to when the user resets the panes.
	 *
	 * <p>
	 * By default it is the size the child was added with. A child added with a size the user dragged it to earlier is
	 * given its configured size here.
	 * </p>
	 */
	public void setDefaultSize(ReactControl child, float size, DisplayUnit unit) {
		for (ChildEntry entry : _children) {
			if (entry._control == child) {
				entry._defaultSize = size;
				entry._defaultUnit = unit;
				return;
			}
		}
		throw new IllegalArgumentException("Not a child of this split panel: " + child.getID());
	}

	/**
	 * Updates the collapse state of the given child, reported through its {@link CollapseContainer}.
	 *
	 * <p>
	 * When all children are collapsed afterwards, the split panel collapses in its own container to
	 * the sum of the collapsed sizes; when a child expands out of that state, it expands again.
	 * </p>
	 *
	 * @param entry
	 *        The child that collapsed or expanded.
	 * @param collapsed
	 *        {@code true} if the child collapsed, {@code false} if it expanded.
	 * @param collapsedSize
	 *        The pixel size of the collapsed child (e.g. toolbar header height).
	 */
	private void childCollapsed(ChildEntry entry, boolean collapsed, float collapsedSize) {
		int childIndex = _children.indexOf(entry);
		if (childIndex < 0) {
			// No longer a child.
			return;
		}
		boolean changed = entry._collapsed != collapsed;
		if (!changed && !collapsed) {
			return;
		}
		boolean allCollapsedBefore = allCollapsed();

		if (collapsed) {
			if (changed) {
				// Store original size for later restoration.
				entry._savedSize = entry._constraint._size;
				entry._savedUnit = entry._constraint._unit;
			}

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

		// Cascading collapse.
		if (_container != null) {
			if (allCollapsed()) {
				_container.collapsed(collapsedSize());
			} else if (allCollapsedBefore) {
				_container.expanded();
			}
		}
		if (changed && _onChildCollapsed != null) {
			_onChildCollapsed.accept(Integer.valueOf(childIndex), Boolean.valueOf(collapsed));
		}
	}

	/**
	 * Checks whether the given child is allowed to collapse.
	 *
	 * <p>
	 * Returns {@code false} only when collapsing the child would make ALL children collapsed AND the
	 * collapse cannot escalate to the container of this split panel. This prevents the display from
	 * becoming broken when no visible panel remains.
	 * </p>
	 *
	 * @param entry
	 *        The child that wants to collapse.
	 * @return {@code true} if the collapse is allowed, {@code false} if it must be prevented.
	 */
	private boolean canChildCollapse(ChildEntry entry) {
		for (ChildEntry other : _children) {
			if (other != entry && !other._collapsed) {
				return true;
			}
		}
		// All siblings already collapsed --- this would make ALL children collapsed.
		if (_container == null) {
			return false; // Root: cannot escalate.
		}
		return _container.canCollapse();
	}

	/**
	 * Sets the container of this split panel and reports to it whether all children are collapsed,
	 * since this split panel is collapsed exactly then.
	 */
	@Override
	public void setCollapseContainer(CollapseContainer container) {
		_container = container;
		if (container != null) {
			if (allCollapsed()) {
				container.collapsed(collapsedSize());
			} else {
				container.expanded();
			}
		}
	}

	private float collapsedSize() {
		float result = 0;
		for (ChildEntry e : _children) {
			result += e._constraint._size;
		}
		return result;
	}

	private void patchChildren() {
		putState(CHILDREN, _childDescriptors);
	}

	private boolean allCollapsed() {
		return !_children.isEmpty() && _children.stream().allMatch(e -> e._collapsed);
	}

	/**
	 * The {@link CollapseContainer} of a child of this split panel.
	 */
	private final class EntryContainer implements CollapseContainer {

		private final ChildEntry _entry;

		EntryContainer(ChildEntry entry) {
			_entry = entry;
		}

		@Override
		public boolean isCollapsed() {
			return _entry._collapsed;
		}

		@Override
		public boolean canCollapse() {
			return canChildCollapse(_entry);
		}

		@Override
		public void collapsed(float collapsedSize) {
			childCollapsed(_entry, true, collapsedSize);
		}

		@Override
		public void expanded() {
			childCollapsed(_entry, false, 0);
		}

		@Override
		public void requestSize(float size, DisplayUnit unit) {
			if (_entry._requestedUnit == unit && _entry._requestedSize == size) {
				return;
			}
			_entry._requestedSize = size;
			_entry._requestedUnit = unit;
			updateDescriptor(_entry);
		}

		@Override
		public void releaseSize() {
			if (_entry._requestedUnit == null) {
				return;
			}
			_entry._requestedUnit = null;
			updateDescriptor(_entry);
		}
	}

	private void updateDescriptor(ChildEntry entry) {
		int index = _children.indexOf(entry);
		if (index < 0) {
			// No longer a child.
			return;
		}
		_childDescriptors.set(index, entry.toDescriptor());
		patchChildren();
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
		 * The size in {@link #getUnit()}.
		 */
		public float getSize() {
			return _size;
		}

		/**
		 * The unit of {@link #getSize()}.
		 */
		public DisplayUnit getUnit() {
			return _unit;
		}

		/**
		 * The minimum size in pixels (0 for no minimum).
		 */
		public int getMinSize() {
			return _minSize;
		}

		/**
		 * The scrolling strategy.
		 */
		public Scrolling getScrolling() {
			return _scrolling;
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

		float _defaultSize;

		DisplayUnit _defaultUnit;

		/**
		 * The size the child asks for while it is open, see {@link CollapseContainer#requestSize(float,
		 * DisplayUnit)}.
		 */
		float _requestedSize;

		/** The unit of {@link #_requestedSize}, {@code null} without a request. */
		DisplayUnit _requestedUnit;

		ChildEntry(ReactControl control, ChildConstraint constraint) {
			_control = control;
			_constraint = constraint;
			_defaultSize = constraint._size;
			_defaultUnit = constraint._unit;
		}

		/**
		 * Whether the child is laid out with the size it asked for instead of the size of its
		 * constraint.
		 */
		boolean hasRequest() {
			return _requestedUnit != null && !_collapsed;
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
			boolean requested = hasRequest();
			map.put(SIZE, Float.valueOf(requested ? _requestedSize : _constraint._size));
			map.put(UNIT, (requested ? _requestedUnit : _constraint._unit).getExternalName());
			map.put(MIN_SIZE, Integer.valueOf(_constraint._minSize));
			map.put(SCROLLING, _constraint._scrolling.toOverflowAttribute());
			map.put(COLLAPSED, Boolean.valueOf(_collapsed));
			return map;
		}
	}

	/**
	 * Handles the {@link #UPDATE_SIZES_COMMAND} sent by the React client after a splitter drag.
	 *
	 * <p>
	 * The client sends the sizes of the open panes, each in the unit of its pane. Every pane keeps its
	 * unit. A collapsed pane is left untouched: its constraint holds the size of its collapsed header,
	 * and the size it expands to does not change by a drag. A pane laid out with the size its child
	 * {@link CollapseContainer#requestSize(float, DisplayUnit) asked for} takes the dragged size for as
	 * long as the request holds, in the unit of the request; the size the user gave the pane stays
	 * remembered and is what the pane gets back when the request ends.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@ReactCommandHandler(value = UPDATE_SIZES_COMMAND, params = @ReactParam(name = SIZES_ARG, type = "object",
		required = true, description = "Map of child control id to its new size in the pane's unit; only open panes."))
	void handleUpdateSizes(Map<String, Object> arguments) {
		Map<String, Object> sizes = (Map<String, Object>) arguments.get(SIZES_ARG);
		if (sizes != null) {
			for (ChildEntry entry : _children) {
				if (entry._collapsed) {
					continue;
				}
				Object newSize = sizes.get(entry._control.getID());
				if (newSize instanceof Number) {
					float size = ((Number) newSize).floatValue();
					if (entry.hasRequest()) {
						// The size is in the unit of the request, and lasts as long as the request.
						entry._requestedSize = size;
						continue;
					}
					entry._constraint._size = size;
				}
			}
			_childDescriptors.clear();
			for (ChildEntry entry : _children) {
				_childDescriptors.add(entry.toDescriptor());
			}
			patchChildren();
			if (_onSizesChanged != null) {
				Map<String, Float> sizeMap = new LinkedHashMap<>();
				for (ChildEntry entry : _children) {
					float size = entry._collapsed ? entry._savedSize : entry._constraint._size;
					sizeMap.put(entry._control.getID(), Float.valueOf(size));
				}
				_onSizesChanged.accept(sizeMap);
			}
		}
	}

	/**
	 * Handles the {@link #RESET_SIZES_COMMAND} sent by the React client on a double click on a splitter: every pane
	 * gets its default size back, a collapsed one the size it expands to.
	 */
	@ReactCommandHandler(value = RESET_SIZES_COMMAND)
	void handleResetSizes() {
		for (ChildEntry entry : _children) {
			if (entry._collapsed) {
				entry._savedSize = entry._defaultSize;
				entry._savedUnit = entry._defaultUnit;
			} else {
				entry._constraint._size = entry._defaultSize;
				entry._constraint._unit = entry._defaultUnit;
			}
		}
		_childDescriptors.clear();
		for (ChildEntry entry : _children) {
			_childDescriptors.add(entry.toDescriptor());
		}
		patchChildren();
		if (_onSizesReset != null) {
			_onSizesReset.run();
		}
	}

	/**
	 * Structural: a split-pane layout container, elided from the headless projection (its panels
	 * remain as addressable nodes).
	 */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}
}
