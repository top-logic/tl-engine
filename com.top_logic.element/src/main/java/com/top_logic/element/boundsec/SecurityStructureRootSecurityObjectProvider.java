/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec;

import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * The SecurityStructureRootSecurityObjectProvider uses the security structure root
 * as security object. Falls back to ({@link BoundHelper#getDefaultObject()}) if there
 * is no SecurityStructure set up.
 *
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class SecurityStructureRootSecurityObjectProvider implements SecurityObjectProvider {

    public static String ALIAS_NAME = "securityStrRoot";

    /** Saves an instance of this class. */
    public static final SecurityStructureRootSecurityObjectProvider INSTANCE = new SecurityStructureRootSecurityObjectProvider();

    @Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		return ElementBoundHelper.getSecurityRoot();
    }

}
