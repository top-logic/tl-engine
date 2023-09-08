/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import com.top_logic.basic.Logger;
import com.top_logic.basic.i18n.I18NChecker;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * {@link I18NChecker} checking the I18N of the command groups.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandGroupI18NCheck implements I18NChecker {

	@Override
	public void checkI18N() {
		Logger.info("Checking command group resources.", CommandGroupI18NCheck.class);
		checkCommandGroupI18N();
		Logger.info("Checking command group resources done.", CommandGroupI18NCheck.class);
	}

	private void checkCommandGroupI18N() {
		CommandGroupRegistry groupRegistry = CommandGroupRegistry.Module.INSTANCE.getImplementationInstance();
		groupRegistry.groupsById().values().forEach(MetaLabelProvider.INSTANCE::getLabel);
	}
}

