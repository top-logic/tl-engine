/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * The columns of the Effektive-Rechte table.
 *
 * <p>
 * A tree table hands its accessor the <em>node</em>, not the object the node carries, so every column
 * starts by unwrapping. The invisible root carries a placeholder and is never rendered.
 * </p>
 *
 * @see EffectiveAccessRow
 */
public class EffectiveAccessAccessor extends ReadOnlyAccessor<Object> {

	/** The capability group or the tool - the tree column. */
	public static final String SUBJECT = "subject";

	/** Allowed or denied. */
	public static final String VERDICT = "verdict";

	/** What makes the capability group worth looking at twice, if anything. */
	public static final String RISK = "risk";

	/** How many tools a capability group holds. */
	public static final String TOOL_COUNT = "toolCount";

	/** Which rule decided, and which rule it overrode. */
	public static final String PROVENANCE = "provenance";

	@Override
	public Object getValue(Object node, String property) {
		Object row = node instanceof TLTreeNode<?> treeNode ? treeNode.getBusinessObject() : node;
		if (!(row instanceof EffectiveAccessRow accessRow)) {
			// The invisible tree root, which the table never renders.
			return null;
		}
		switch (property) {
			case SUBJECT:
				return subject(accessRow);
			case VERDICT:
				// A section is a heading over rows that each carry their own verdict; one word for
				// "one of six" would be wrong whichever word it was.
				return accessRow instanceof EffectiveAccessRow.RunSection ? null
					: ToolAccessLabels.verdict(ToolAccessVerdict.of(accessRow.allowed()));
			case RISK:
				return accessRow instanceof EffectiveAccessRow.Capability capability
					? ToolAccessLabels.risk(capability.group()) : null;
			case TOOL_COUNT:
				if (accessRow instanceof EffectiveAccessRow.Capability capability) {
					return Integer.valueOf(capability.toolCount());
				}
				return accessRow instanceof EffectiveAccessRow.RunSection section
					? Integer.valueOf(section.totalCount()) : null;
			case PROVENANCE:
				return accessRow.provenance();
			default:
				return null;
		}
	}

	private static String subject(EffectiveAccessRow row) {
		if (row instanceof EffectiveAccessRow.Capability capability) {
			return ToolAccessLabels.groupLabel(capability.group());
		}
		if (row instanceof EffectiveAccessRow.RunSection section) {
			return ToolAccessLabels.dimensionLabel(section.dimension());
		}
		if (row instanceof EffectiveAccessRow.RunName name) {
			return name.name();
		}
		return ((EffectiveAccessRow.ToolRow) row).qualifiedName();
	}

}
