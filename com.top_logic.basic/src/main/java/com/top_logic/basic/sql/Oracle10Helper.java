/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * This class helps using Oracle and greater using any driver.
 * 
 * Tested with oracle 10 - 12
 * 
 * @author <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class Oracle10Helper extends OracleHelper {
    
	/**
	 * Configuration options for {@link Oracle10Helper}.
	 */
	public interface Config extends OracleHelper.Config {
		// No additional properties, just to be able to configure different application-wide
		// defaults.
	}

	/**
	 * Creates a {@link Oracle10Helper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Oracle10Helper(InstantiationContext context, Config config) {
		super(context, config);
	}


}
