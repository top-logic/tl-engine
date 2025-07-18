/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

/**
 * A {@link SelectionModel} for nodes of a tree structure that provides additional functionality and
 * information.
 */
public interface TreeSelectionModel<T> extends SelectionModel<T> {

	@Override
	default boolean isSelected(T obj) {
		return getNodeSelectionState(obj).isSelected();
	}

	/**
	 * Retrieves the selection state for the given object.
	 * 
	 * <p>
	 * The selection state is only stored specifically for an object, if it cannot be derived from
	 * the selection state of its parent, see {@link DescendantState#ALL} and
	 * {@link DescendantState#NONE}.
	 * </p>
	 */
	public NodeSelectionState getNodeSelectionState(T obj);

	/**
	 * Updates the selection state of the whole sub-tree rooted at the given node.
	 */
	public void setSelectedSubtree(T obj, boolean select);

	/**
	 * Description of the selection state of a tree node.
	 */
	public static enum NodeSelectionState {
		/**
		 * Neither the node nor any of its descendants is selected.
		 */
		NONE(false, DescendantState.NONE),
	
		/**
		 * The node is not selected, but there are selected descendants.
		 */
		SOME_DESCENDANTS(false, DescendantState.SOME),
	
		/**
		 * The node is not selected, but all of its descendants.
		 */
		ALL_DESCENDANTS(false, DescendantState.ALL),
	
		/**
		 * The node is selected, but none of its descendants.
		 */
		SELECTED_NO_DESCENDANTS(true, DescendantState.NONE),
	
		/**
		 * The node and some of its descendants are selected.
		 */
		SELECTED_SOME_DESCENDANTS(true, DescendantState.SOME),
	
		/**
		 * The node and all of its its descendants are selected.
		 */
		FULL(true, DescendantState.ALL);
	
		private DescendantState _childrenState;
	
		private boolean _selected;
	
		/**
		 * Creates a {@link NodeSelectionState}.
		 */
		private NodeSelectionState(boolean selected, DescendantState childState) {
			_selected = selected;
			_childrenState = childState;
		}
	
		/**
		 * Whether the current node is selected itself.
		 */
		public boolean isSelected() {
			return _selected;
		}
	
		/**
		 * Whether all children are selected.
		 */
		public boolean allDescendants() {
			return descendants() == DescendantState.ALL;
		}
	
		/**
		 * Information about the descendants of the current node.
		 */
		public DescendantState descendants() {
			return _childrenState;
		}
	
		/**
		 * The {@link NodeSelectionState} with the given information.
		 */
		public static NodeSelectionState valueOf(boolean selected, DescendantState childrenState) {
			if (selected) {
				return switch (childrenState) {
					case NONE -> SELECTED_NO_DESCENDANTS;
					case SOME -> SELECTED_SOME_DESCENDANTS;
					case ALL -> FULL;
				};
			} else {
				return switch (childrenState) {
					case NONE -> NONE;
					case SOME -> SOME_DESCENDANTS;
					case ALL -> ALL_DESCENDANTS;
				};
			}
		}
	
		/**
		 * Whether the complete subtree has the same selection state.
		 */
		public boolean isHomogeneous() {
			return _selected ? _childrenState == DescendantState.ALL : _childrenState == DescendantState.NONE;
		}
	}

	/**
	 * Description of the selection state of the descendants of a tree node.
	 */
	public static enum DescendantState {
		/**
		 * None of the descendants of the node are selected.
		 */
		NONE,

		/**
		 * There are descendants of the node that are selected.
		 */
		SOME,

		/**
		 * All descendants of the node are selected.
		 */
		ALL;

		/**
		 * Whether all descendants share the same selection state.
		 */
		public boolean isHomogeneous() {
			return switch (this) {
				case NONE, ALL -> true;
				case SOME -> false;
			};
		}

		/**
		 * The state that is assumed for a child that has no explicit state stored.
		 */
		public NodeSelectionState implicitState() {
			return switch (this) {
				case NONE -> NodeSelectionState.NONE;
				case ALL -> NodeSelectionState.FULL;
				case SOME -> NodeSelectionState.NONE;
			};
		}
	}

}
