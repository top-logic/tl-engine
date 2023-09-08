/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound;

import com.top_logic.layout.component.Selectable;
import com.top_logic.tool.boundsec.BoundChecker;

/**
 * Provides a setter so the Delegating component can set the the secondary checker.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public interface CompoundSecurityBoundChecker extends BoundChecker, Selectable {

    /** 
     * Set the checker to which the allow methods are delegated.
     * 
     * @param aChecker  the checker
     */
    public void setDelegationDestination(BoundChecker aChecker);
}
