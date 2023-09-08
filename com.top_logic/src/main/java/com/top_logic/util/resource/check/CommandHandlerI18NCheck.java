/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource.check;

import java.util.Collection;

import com.top_logic.basic.Logger;
import com.top_logic.basic.i18n.I18NChecker;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link I18NChecker} checking I18N for configured commands.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandHandlerI18NCheck implements I18NChecker {

	@Override
	public void checkI18N() {
		Logger.info("Checking command resources.", CommandHandlerI18NCheck.class);
		checkCommandI18N();
		Logger.info("Checking command resources done.", CommandHandlerI18NCheck.class);
	}

	private void checkCommandI18N() {
		/* Report missing I18N. At least for the performance monitor, a label for each command is
		 * required. */
		I18NBundle defaultBundle =
			ResourcesModule.getInstance().getBundle(ResourcesModule.getInstance().getDefaultLocale());
		Collection<CommandHandler> handlerById = CommandHandlerFactory.getInstance().getAllHandlers();
		for (CommandHandler handler : handlerById) {
			ResKey key = handler.getResourceKey(null);
			defaultBundle.getString(key);
		}
	}

}

