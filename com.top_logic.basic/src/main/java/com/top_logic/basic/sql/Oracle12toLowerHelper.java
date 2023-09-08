/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * This class helps using Oracle driver 12 and higher with database version lower than 12.
 * 
 * @author <a href=mailto:msi@top-logic.com>Marc Siebenhaar</a>
 */
public class Oracle12toLowerHelper extends Oracle12Helper {
    
	/**
	 * Configuration options for {@link Oracle12toLowerHelper}.
	 */
	public interface Config extends Oracle12Helper.Config {
		// No additional properties, just to be able to configure different application-wide
		// defaults.
	}

	/**
	 * Creates a {@link Oracle12toLowerHelper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Oracle12toLowerHelper(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean supportsBatchInfo() {
		return false;
	}
}
