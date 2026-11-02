/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
import java.util.List;

/**
 * How the designer tree displays a {@link DesignTreeNode} structure.
 *
 * <p>
 * The display follows the structure of the {@link DesignTreeNode}s with one exception: a
 * {@link VirtualDesignTreeNode property group} holding exactly one entry is not displayed as a row
 * of its own. The group and its entry share one row, which stands for the entry and is labelled
 * with both, see {@link #displayLabel(DesignTreeNode)}. A group with no entry or with several is
 * displayed as a row of its own.
 * </p>
 *
 * <p>
 * The compaction is a matter of display only: the {@link DesignTreeNode}s keep their structure, so
 * that the structural commands work on the real group and entry. Since the display is computed from
 * the current structure, a structural edit that changes the number of entries of a group changes its
 * display with the next build of the displayed tree.
 * </p>
 */
public class DesignTreeDisplay {

	/**
	 * Separator between the label of a group and the label of its single entry in a shared row.
	 */
	public static final String GROUP_SEPARATOR = " › ";

	/**
	 * The nodes displayed as the children of the row standing for the given node.
	 *
	 * <p>
	 * These are the {@link DesignTreeNode#getChildren() children} of the node, a group with a single
	 * entry replaced by that entry.
	 * </p>
	 */
	public static List<DesignTreeNode> displayedChildren(DesignTreeNode node) {
		List<DesignTreeNode> children = node.getChildren();
		List<DesignTreeNode> result = new ArrayList<>(children.size());
		for (DesignTreeNode child : children) {
			result.add(displayedNode(child));
		}
		return result;
	}

	/**
	 * The node whose row displays the given node.
	 *
	 * @return The single entry of a group displayed together with it, the given node itself
	 *         otherwise.
	 */
	public static DesignTreeNode displayedNode(DesignTreeNode node) {
		if (isCompacted(node)) {
			return node.getChildren().get(0);
		}
		return node;
	}

	/**
	 * The group displayed in the same row as the given entry, or {@code null} if the given node is
	 * displayed in a row of its own.
	 */
	public static VirtualDesignTreeNode sharedGroup(DesignTreeNode entry) {
		if (entry == null) {
			return null;
		}
		DesignTreeNode parent = entry.getParent();
		if (isCompacted(parent)) {
			return (VirtualDesignTreeNode) parent;
		}
		return null;
	}

	/**
	 * The nodes the commands of the row standing for the given node act on.
	 *
	 * @return The given node, followed by the group sharing its row, if any.
	 */
	public static List<DesignTreeNode> commandTargets(DesignTreeNode node) {
		VirtualDesignTreeNode group = sharedGroup(node);
		return group == null ? List.of(node) : List.of(node, group);
	}

	/**
	 * The label of the row standing for the given node.
	 *
	 * @return The {@link DesignTreeNode#getDisplayLabel() display label} of the node, preceded by
	 *         the label of the group sharing its row.
	 */
	public static String displayLabel(DesignTreeNode node) {
		VirtualDesignTreeNode group = sharedGroup(node);
		if (group == null) {
			return node.getDisplayLabel();
		}
		return group.getDisplayLabel() + GROUP_SEPARATOR + node.getDisplayLabel();
	}

	/**
	 * The tooltip of the row standing for the given node, or {@code null} for none.
	 *
	 * @return The {@link DesignTreeNode#getTooltipHtml() tooltip} of the node, or the one of the
	 *         group sharing its row where the node has none.
	 */
	public static String tooltipHtml(DesignTreeNode node) {
		String tooltip = node.getTooltipHtml();
		if (tooltip != null && !tooltip.isEmpty()) {
			return tooltip;
		}
		VirtualDesignTreeNode group = sharedGroup(node);
		return group == null ? tooltip : group.getTooltipHtml();
	}

	/**
	 * Whether the given node is a group displayed in the row of its single entry.
	 */
	private static boolean isCompacted(DesignTreeNode node) {
		return node instanceof VirtualDesignTreeNode && node.getChildren().size() == 1;
	}

}
