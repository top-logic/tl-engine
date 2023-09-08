/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.IGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The LiberalAccessChecker grands access to anything.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class LiberalAccessChecker implements AccessChecker {
    
    public static final LiberalAccessChecker INSTANCE = new LiberalAccessChecker();

    /** the set of all access rights usually expected to be available */
    public static final Set<BoundCommandGroup> ALL_RIGHTS = 
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            new BoundCommandGroup[] {
                SimpleBoundCommandGroup.READ, 
                SimpleBoundCommandGroup.WRITE, 
                SimpleBoundCommandGroup.EXPORT})));

    /** use singleton instance */
    private LiberalAccessChecker() { 
        super();
    }
    
    /**
     * @see com.top_logic.layout.security.AccessChecker#getAccessRights(java.lang.Object, com.top_logic.tool.boundsec.IGroup)
     */
    @Override
	public Set<BoundCommandGroup> getAccessRights(Object aObject, IGroup aGroup) {
        return ALL_RIGHTS;
    }

    /**
     * @see com.top_logic.layout.security.AccessChecker#hasAccessRight(java.lang.Object, com.top_logic.tool.boundsec.IGroup, com.top_logic.tool.boundsec.BoundCommandGroup)
     */
    @Override
	public boolean hasAccessRight(Object aObject, IGroup aGroup,
            BoundCommandGroup aAccessRight) {
        return true;
    }

}

