/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.Collection;

import com.top_logic.mig.html.layout.LayoutConfigTree;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * Rule for read rights that a security layout implicitly needs because of the rights of its
 * children.
 *
 * <p>
 * A {@link CompoundSecurityLayout} is only reachable, when the enclosing security layout is
 * visible. Therefore, a role that has any right in a security layout also needs the
 * {@link SimpleBoundCommandGroup#READ read right} in all enclosing security layouts. The
 * propagation stops at the {@link #isDialogRoot(LayoutConfigTreeNode) root of a dialog} with its own
 * security, because such a dialog may be reachable without its opener, e.g. by a goto.
 * </p>
 *
 * <p>
 * Security nodes are the nodes of {@link CompoundSecurityLayout} configurations and the root of
 * the {@link LayoutConfigTree}.
 * </p>
 *
 * @see ApplyRolesCommandHandler
 */
public class ImplicitReadRights {

	/**
	 * Whether the given node represents the top-level component of a dialog.
	 *
	 * <p>
	 * The dialog parent of such a node is its parent in the layout tree, i.e. the component that
	 * declares the dialog.
	 * </p>
	 */
	public static boolean isDialogRoot(LayoutConfigTreeNode node) {
		LayoutConfigTreeNode dialogParent = node.getDialogParent();
		return dialogParent != null && dialogParent == node.getParent();
	}

	/**
	 * Updates the read rights of the enclosing security nodes after the rights of the given role
	 * in the given security node have changed.
	 *
	 * <p>
	 * When the role has any right in the given node, the read right is added to all enclosing
	 * security nodes. Otherwise, the read right is removed from the enclosing security nodes, as
	 * long as no other child needs it.
	 * </p>
	 */
	public static void updateImplicitReadRights(LayoutConfigTreeNode node, BoundedRole role) {
		boolean needsRead = hasAnyRight(node, role);
		LayoutConfigTreeNode child = node;
		LayoutConfigTreeNode parent = securityParent(node);
		while (parent != null && !isDialogRoot(child)) {
			if (!updateRead(parent, role, needsRead)) {
				break;
			}
			child = parent;
			parent = securityParent(parent);
		}
	}

	/**
	 * Adds the read rights that are implicitly needed by the rights of the given role in the given
	 * tree.
	 *
	 * <p>
	 * Existing rights are never removed.
	 * </p>
	 *
	 * @param tree
	 *        The tree in which the read rights are added.
	 * @param role
	 *        The role whose read rights are added.
	 * @param added
	 *        Collection to which each {@link PersBoundComp} is added, in which a read right was
	 *        added.
	 */
	public static void addImplicitReadRights(LayoutConfigTree tree, BoundedRole role,
			Collection<? super PersBoundComp> added) {
		addImplicitReadRights(tree.getRoot(), role, added);
	}

	/**
	 * Adds the implicitly needed read rights in the subtree of the given node.
	 *
	 * @return Whether the enclosing security node needs the read right for the given role.
	 */
	private static boolean addImplicitReadRights(LayoutConfigTreeNode node, BoundedRole role,
			Collection<? super PersBoundComp> added) {
		boolean childNeedsRead = false;
		for (LayoutConfigTreeNode child : node.getChildren()) {
			// Descend into all children, also when the result is already known.
			if (addImplicitReadRights(child, role, added)) {
				childNeedsRead = true;
			}
		}
		if (!isSecurityNode(node)) {
			return childNeedsRead;
		}
		PersBoundComp secComp = node.getSecurityComponent();
		if (childNeedsRead && secComp != null && !hasRight(secComp, role, SimpleBoundCommandGroup.READ)) {
			secComp.addAccess(SimpleBoundCommandGroup.READ, role);
			added.add(secComp);
		}
		if (isDialogRoot(node)) {
			return false;
		}
		return childNeedsRead || hasAnyRight(node, role);
	}

	/**
	 * Sets or removes the read right of the given role in the given security node.
	 *
	 * @return Whether the read right of the node has changed, i.e. whether the enclosing security
	 *         node must be updated, too.
	 */
	private static boolean updateRead(LayoutConfigTreeNode node, BoundedRole role, boolean needsRead) {
		PersBoundComp secComp = node.getSecurityComponent();
		boolean hasRead = secComp != null && hasRight(secComp, role, SimpleBoundCommandGroup.READ);
		if (hasRead == needsRead) {
			return false;
		}
		if (!needsRead && anyChildNeedsRead(node, role)) {
			return false;
		}
		if (secComp != null) {
			if (needsRead) {
				secComp.addAccess(SimpleBoundCommandGroup.READ, role);
			} else {
				secComp.removeAccess(SimpleBoundCommandGroup.READ, role);
			}
		}
		return true;
	}

	/**
	 * Whether any security node below the given node needs its read right for the given role.
	 */
	private static boolean anyChildNeedsRead(LayoutConfigTreeNode node, BoundedRole role) {
		for (LayoutConfigTreeNode child : node.getChildren()) {
			if (isSecurityNode(child)) {
				if (isDialogRoot(child)) {
					// Rights in a dialog do not require the read right of the opener.
					continue;
				}
				if (hasAnyRight(child, role)) {
					return true;
				}
			}
			if (anyChildNeedsRead(child, role)) {
				return true;
			}
		}
		return false;
	}

	private static LayoutConfigTreeNode securityParent(LayoutConfigTreeNode node) {
		LayoutConfigTreeNode parent = node.getParent();
		while (parent != null && !isSecurityNode(parent)) {
			parent = parent.getParent();
		}
		return parent;
	}

	private static boolean isSecurityNode(LayoutConfigTreeNode node) {
		return node.getConfig() instanceof CompoundSecurityLayout.Config || node.getParent() == null;
	}

	private static boolean hasAnyRight(LayoutConfigTreeNode node, BoundedRole role) {
		PersBoundComp secComp = node.getSecurityComponent();
		if (secComp == null) {
			return false;
		}
		for (BoundCommandGroup group : CommandGroupCollector.collectNonSystemGroups(node)) {
			if (hasRight(secComp, role, group)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasRight(PersBoundComp secComp, BoundedRole role, BoundCommandGroup group) {
		return secComp.rolesForCommandGroup(group).contains(role);
	}

}
