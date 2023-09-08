/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.util.ResKey;

/**
 * The SecurityMasterBoundChecker asks the configured security master for allowance.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface SecurityMasterBoundChecker extends BoundChecker {

    /**
     * Return <code>true</code> if there is a security master which is currently allowed
     * to be viewed.
     */
    public ResKey hideReasonMaster();

    /**
     * Return <code>true</code> if there is a security master component which allows the
     * given object to be viewed.
     */
    public boolean allowBySecurityMaster(BoundObject anObject, boolean useDefaultChecker);

}
