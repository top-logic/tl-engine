/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import com.top_logic.event.logEntry.LogEntryFilter;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * The LogEntrySecurityObjectProvider uses the selection of a LogEntryFilter as security
 * object.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class LogEntrySecurityObjectProvider implements SecurityObjectProvider {

    @Override
	public BoundObject getSecurityObject(BoundChecker aChecker,Object model, BoundCommandGroup aCommandGroup) {
        return (BoundObject)((LogEntryFilter)((LayoutComponent)aChecker).getMaster().getModel()).getSelection();
    }

}
