/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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
				return ToolAccessLabels.verdict(accessRow.explanation().allowed());
			case RISK:
				return accessRow instanceof EffectiveAccessRow.Capability capability
					? ToolAccessLabels.risk(capability.group()) : null;
			case TOOL_COUNT:
				return accessRow instanceof EffectiveAccessRow.Capability capability
					? Integer.valueOf(capability.toolCount()) : null;
			case PROVENANCE:
				return provenance(accessRow);
			default:
				return null;
		}
	}

	private static String subject(EffectiveAccessRow row) {
		if (row instanceof EffectiveAccessRow.Capability capability) {
			return ToolAccessLabels.groupLabel(capability.group());
		}
		return ((EffectiveAccessRow.ToolRow) row).qualifiedName();
	}

	/**
	 * The provenance sentence, plus the note the {@code script} group carries wherever it appears.
	 */
	private static String provenance(EffectiveAccessRow row) {
		String sentence = ToolAccessLabels.sentence(row.explanation());
		if (row instanceof EffectiveAccessRow.Capability capability
			&& capability.scriptCommitNote() != null) {
			return sentence + " · " + capability.scriptCommitNote();
		}
		return sentence;
	}

}
