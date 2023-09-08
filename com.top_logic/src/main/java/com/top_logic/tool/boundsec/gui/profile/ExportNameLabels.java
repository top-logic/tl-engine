/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;

/**
 * Full path label of the {@link SecurityNode}: The path in the layout tree followed by the command
 * group.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExportNameLabels implements LabelProvider {

	/** Singleton {@link ExportNameLabels} instance. */
	public static final ExportNameLabels INSTANCE = new ExportNameLabels();

	private ExportNameLabels() {
		// singleton instance
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof SecurityNode) {
			return buildFullName(Resources.getInstance(), new StringBuilder(), (SecurityNode) object).toString();
		} else {
			return StringServices.EMPTY_STRING;
		}
	}

	private StringBuilder buildFullName(Resources resources, StringBuilder builder, SecurityNode node) {
		if (node instanceof CommandNode) {
			builder = buildConfigName(resources, builder, (ConfigNode) node.getParent());
			builder.append(" (").append(node.getLabel(resources)).append(")");
		} else {
			builder = buildConfigName(resources, builder, (ConfigNode) node);
		}
		return builder;
	}

	private StringBuilder buildConfigName(Resources resources, StringBuilder builder, ConfigNode node) {
		SecurityNode parent = node.getParent();
		if (parent != null) {
			int formerLength = builder.length();
			buildConfigName(resources, builder, (ConfigNode) parent);
			if (formerLength < builder.length()) {
				builder.append(" > ");
			}
		}
		builder.append(node.getLabel(resources));
		return builder;
	}

}