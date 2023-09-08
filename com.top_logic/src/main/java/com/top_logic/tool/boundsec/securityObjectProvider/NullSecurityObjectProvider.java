/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;

/**
 * The SecurityRootObjectProvider uses <code>null</code> as security object.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class NullSecurityObjectProvider implements SecurityObjectProvider {

	/**
	 * Name that is used to configure the {@link NullSecurityObjectProvider} in
	 * {@link SecurityObjectProviderManager}.
	 * 
	 * @see SecurityObjectProviderManager
	 */
	public static final String ALIAS_NAME = "null";

    @Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
        return null;
    }

}
