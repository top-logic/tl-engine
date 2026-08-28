/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.util.Resources;

/**
 * The columns of the Tool-Katalog.
 *
 * <p>
 * Every cell is a string, deliberately: the table's own filters then work on what the reader sees, an
 * Excel export says the same thing as the screen, and sorting by group groups the catalog the way the
 * mental model does. The one thing a cell must never be is a technical name the reader cannot look
 * up.
 * </p>
 *
 * @see ToolCatalogModelBuilder
 */
public class ToolCatalogAccessor extends ReadOnlyAccessor<ToolCatalogEntry> {

	/** The tool's qualified name - what the policy stores and what a denial message prints. */
	public static final String TOOL = "tool";

	/** What the tool does, in one sentence written for an administrator. */
	public static final String DESCRIPTION = "description";

	/** The capability group, as display name and technical name. */
	public static final String GROUP = "group";

	/** What makes the group worth looking at twice, if anything. */
	public static final String RISK = "risk";

	/** Whether the tool declared its group or the policy assigned it. */
	public static final String GROUP_SOURCE = "groupSource";

	/** Which roles reach the tool. */
	public static final String REACHABLE_FOR = "reachableFor";

	@Override
	public Object getValue(ToolCatalogEntry row, String property) {
		switch (property) {
			case TOOL:
				return row.qualifiedName();
			case DESCRIPTION:
				return description(row);
			case GROUP:
				return row.isUnclassified()
					? Resources.getInstance().getString(I18NConstants.GROUP_NONE)
					: ToolAccessLabels.groupLabel(row.group());
			case RISK:
				return ToolAccessLabels.risk(row.group());
			case GROUP_SOURCE:
				return ToolAccessLabels.groupSource(row.groupSource());
			case REACHABLE_FOR:
				return reachableFor(row);
			default:
				return null;
		}
	}

	/**
	 * Who reaches the tool, in the three states that are worth telling apart.
	 *
	 * <p>
	 * "Everyone" is not the same as a list of every configured role: the baseline reaches accounts that
	 * hold no role at all, and a policy whose baseline grants {@code data.read} has said something
	 * about every future role too. "Nobody" is the state that needs an administrator.
	 * </p>
	 */
	private static String reachableFor(ToolCatalogEntry row) {
		return ToolAccessLabels.reachability(row.reachability(), row.reachingRoles());
	}

	/**
	 * What the tool does, for an administrator deciding about it.
	 *
	 * <p>
	 * The tool's own description is the fallback and is marked as one: it is written in English for
	 * the language model, several sentences of call mechanics, and a reader has to know that this row
	 * is not a sentence somebody wrote for them. Marked per row rather than in the column header,
	 * because it is the exception - every tool the framework ships has its own text.
	 * </p>
	 */
	private static String description(ToolCatalogEntry row) {
		String written = AdminToolDescriptions.description(row.qualifiedName());
		if (written != null) {
			return written;
		}
		String modelText = row.descriptionSentence();
		if (modelText.isEmpty()) {
			return "";
		}
		return Resources.getInstance()
			.getString(I18NConstants.DESCRIPTION_FROM_MODEL__TEXT.fill(modelText));
	}

}
