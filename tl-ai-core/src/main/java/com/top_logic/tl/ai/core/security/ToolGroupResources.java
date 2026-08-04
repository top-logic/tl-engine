/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.util.Resources;

/**
 * How a {@link ToolGroups tool group} presents itself to a human.
 *
 * <p>
 * Used as the option renderer of the group properties of a {@link ToolAccessPolicy}: the label is
 * what an administrator picks from the list, and {@link #getTooltip(Object)} is the sentence that
 * says what granting it lets an agent do. A group that no resource describes - one an application,
 * an MCP server or a {@code <tool-groups>} entry contributed - keeps its technical name, which is
 * the most honest thing to show for a name nobody documented.
 * </p>
 *
 * <p>
 * The stored value is unaffected: what is written to the configuration file is always the technical
 * group name.
 * </p>
 *
 * @see ToolGroups#labelKey(String)
 */
public class ToolGroupResources extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		String group = groupName(object);
		if (group == null) {
			return null;
		}
		ResKey label = ToolGroups.labelKey(group);
		return label == null ? group : Resources.getInstance().getString(label) + " (" + group + ")";
	}

	@Override
	public String getTooltip(Object object) {
		return description(groupName(object));
	}

	/**
	 * The one sentence saying what the named group covers.
	 *
	 * @param group
	 *        The technical group name, may be <code>null</code>.
	 * @return The explanation, or <code>null</code> for a group no resource describes.
	 */
	public static String description(String group) {
		ResKey label = ToolGroups.labelKey(group);
		return label == null ? null : Resources.getInstance().getString(label.tooltipOptional());
	}

	private static String groupName(Object object) {
		return object instanceof String name ? name : null;
	}

}
