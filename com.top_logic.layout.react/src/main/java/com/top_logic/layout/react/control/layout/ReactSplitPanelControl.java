/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

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

	private static final String ORIENTATION = "orientation";

	private static final String RESIZABLE = "resizable";

	private static final String CHILDREN = "children";

	// Child descriptor keys.
	private static final String SIZE = "size";

	private static final String UNIT = "unit";

	private static final String MIN_SIZE = "minSize";

	private static final String SCROLLING = "scrolling";

	private static final String COLLAPSED = "collapsed";

	// Command argument keys.
	private static final String SIZES_ARG = "sizes";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new UpdateSizesCommand());

	private final List<ChildEntry> _children = new ArrayList<>();

	private final boolean _resizable;

	private ReactSplitPanelControl _parentSplitPanel;

	private int _indexInParent = -1;

	/**
	 * Creates a new {@link ReactSplitPanelControl}.
	 *
	 * @param orientation
	 *        The layout orientation (horizontal or vertical).
	 * @param resizable
	 *        Whether draggable splitters are shown between children.
	 */
	public ReactSplitPanelControl(Orientation orientation, boolean resizable) {
		super(null, REACT_MODULE, COMMANDS);
		_resizable = resizable;

		getReactState().put(ORIENTATION, orientation == Orientation.HORIZONTAL ? "horizontal" : "vertical");
		getReactState().put(RESIZABLE, Boolean.valueOf(resizable));
		getReactState().put(CHILDREN, new ArrayList<>());
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
		ChildEntry entry = new ChildEntry(child, constraint);
		_children.add(entry);

		childList().add(entry.toDescriptor());

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
		childList().remove(index);

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
		childList().set(childIndex, entry.toDescriptor());

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

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> childList() {
		return (List<Map<String, Object>>) getReactState().get(CHILDREN);
	}

	private void patchChildren() {
		Map<String, Object> patch = new HashMap<>();
		patch.put(CHILDREN, childList());
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
			map.put("control", _control);
			map.put(SIZE, Float.valueOf(_constraint._size));
			map.put(UNIT, _constraint._unit.getExternalName());
			map.put(MIN_SIZE, Integer.valueOf(_constraint._minSize));
			map.put(SCROLLING, _constraint._scrolling.toOverflowAttribute());
			map.put(COLLAPSED, Boolean.valueOf(_collapsed));
			return map;
		}
	}

	/**
	 * Command sent by the React client after a splitter drag operation.
	 */
	public static class UpdateSizesCommand extends ControlCommand {

		static final String COMMAND = "updateSizes";

		/** Creates a new {@link UpdateSizesCommand}. */
		public UpdateSizesCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_SPLIT_PANEL_RESIZE;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactSplitPanelControl splitPanel = (ReactSplitPanelControl) control;
			Map<String, Object> sizes = (Map<String, Object>) arguments.get(SIZES_ARG);
			if (sizes != null) {
				for (int i = 0; i < splitPanel._children.size(); i++) {
					ChildEntry entry = splitPanel._children.get(i);
					String controlId = entry._control.getID();
					Object newSize = sizes.get(controlId);
					if (newSize instanceof Number) {
						entry._constraint._size = ((Number) newSize).floatValue();
						entry._constraint._unit = DisplayUnit.PIXEL;
					}
				}
				// Update state but don't patch back (the client already has the correct sizes).
				List<Map<String, Object>> list = splitPanel.childList();
				list.clear();
				for (ChildEntry entry : splitPanel._children) {
					list.add(entry.toDescriptor());
				}
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}
}
