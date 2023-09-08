/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;

/**
 * Utilities for toplogic services.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLServiceUtils {

	/**
	 * Reload the {@link ApplicationConfig}.
	 */
	public static void reloadConfigurations() throws IOError {
		try {
			XMLProperties.Module.INSTANCE.config().resolve();

			ApplicationConfig.getInstance().reload();
		} catch (IOException exception) {
			Logger.error("XMLProperties could not be resolved.", exception, TLServiceUtils.class);

			throw new IOError(exception);
		}
	}

}
