/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.layout.DisplayUnit;

/**
 * A control that can collapse to a small size of its own, e.g. a panel minimized to its header.
 *
 * <p>
 * A container that lays out its children with sizes, such as a {@link ReactSplitPanelControl},
 * hands each collapsible child a {@link CollapseContainer}. Through it the child reports that it
 * collapsed or expanded, so that the container gives its area the collapsed size or the size it had
 * before. While it is open, the child may also ask for a size of its own for a while, e.g. for
 * content that needs more room.
 * </p>
 */
public interface CollapsibleChild {

	/**
	 * Sets the container to which this child reports collapsing and expanding.
	 *
	 * <p>
	 * The container may lay out the child as collapsed from the start, e.g. because it remembered
	 * that the user collapsed it, see {@link CollapseContainer#isCollapsed()}. A child that keeps no
	 * collapse state of its own takes this state over. A child that keeps a collapse state of its
	 * own, e.g. one remembered for the user by the child itself, reports this state right away
	 * instead; the container then follows the child.
	 * </p>
	 *
	 * @param container
	 *        The container of this child, or {@code null} when the child is no longer laid out by a
	 *        container.
	 */
	void setCollapseContainer(CollapseContainer container);

	/**
	 * The container of a {@link CollapsibleChild}, as seen by this child.
	 */
	interface CollapseContainer {

		/**
		 * Whether the container lays out the child as collapsed.
		 */
		boolean isCollapsed();

		/**
		 * Whether the child may collapse.
		 *
		 * <p>
		 * A child must not collapse when nothing would remain open, i.e. when all other children are
		 * collapsed and the container cannot collapse in its own container. A child that stays usable
		 * while collapsed, e.g. a {@link ReactSideRailControl} keeping its strip, may collapse without
		 * asking.
		 * </p>
		 */
		boolean canCollapse();

		/**
		 * Reports that the child collapsed.
		 *
		 * <p>
		 * The container gives the child the given size and remembers the size it had before. A child
		 * that is collapsed already gets the given size, the remembered size stays.
		 * </p>
		 *
		 * @param collapsedSize
		 *        The size of the collapsed child in pixels along the axis of the container.
		 */
		void collapsed(float collapsedSize);

		/**
		 * Reports that the child expanded.
		 *
		 * <p>
		 * The container gives the child the size it had before it collapsed. For a child that is not
		 * collapsed, nothing happens.
		 * </p>
		 */
		void expanded();

		/**
		 * Asks for the given size of the child while it is open, until {@link #releaseSize()}.
		 *
		 * <p>
		 * The request is temporary: the size the user gave the child stays remembered and is what the
		 * child gets back when the request is released. A user who resizes the child while the
		 * request holds changes the requested size, for as long as the request holds.
		 * </p>
		 *
		 * @param size
		 *        The size along the axis of the container.
		 * @param unit
		 *        The unit of the size.
		 */
		void requestSize(float size, DisplayUnit unit);

		/**
		 * Ends a {@link #requestSize(float, DisplayUnit) request}; the child gets the size the user
		 * gave it back. Without a request, nothing happens.
		 */
		void releaseSize();

	}

}
