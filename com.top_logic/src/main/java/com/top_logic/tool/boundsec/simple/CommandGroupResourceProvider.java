/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link BoundCommandGroup}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandGroupResourceProvider extends DefaultResourceProvider {

	/**
	 * Singleton {@link CommandGroupResourceProvider} instance.
	 */
	public static final CommandGroupResourceProvider INSTANCE = new CommandGroupResourceProvider();

	private CommandGroupResourceProvider() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		BoundCommandGroup group = (BoundCommandGroup) object;
		return Resources.getInstance().getString(I18NConstants.COMMAND_GROUP_NAMES.key(group.getID()));
	}

	@Override
	public String getTooltip(Object object) {
		BoundCommandGroup group = (BoundCommandGroup) object;
		String groupId = group.getID();
		Resources resources = Resources.getInstance();
		return resources.getString(I18NConstants.COMMAND_GROUP_NAMES.key(groupId).suffix(".toolTip"),
			resources.getString(I18NConstants.COMMAND_GROUP_NAMES.key(groupId), groupId));
	}

}
