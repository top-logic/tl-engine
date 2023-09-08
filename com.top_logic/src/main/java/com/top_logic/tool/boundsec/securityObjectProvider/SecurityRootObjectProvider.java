/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * The SecurityRootObjectProvider uses the security root ({@link BoundHelper#getDefaultObject()})
 * as security object.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
@InApp
public class SecurityRootObjectProvider implements SecurityObjectProvider {

	/** Alias for {@link SecurityRootObjectProvider}. */
    public static String ALIAS_NAME = "securityRoot";

    /** Saves an instance of this class. */
    public static final SecurityRootObjectProvider INSTANCE = new SecurityRootObjectProvider();

    @Override
	public BoundObject getSecurityObject(BoundChecker checker, Object model, BoundCommandGroup commandGroup) {
		return getSecurityRoot();
	}

	/**
	 * Returns the security root ({@link BoundHelper#getDefaultObject()}) as security object.
	 */
	public BoundObject getSecurityRoot() {
		return BoundHelper.getInstance().getDefaultObject();
    }

}
