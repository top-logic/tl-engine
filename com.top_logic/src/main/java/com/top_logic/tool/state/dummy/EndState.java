/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state.dummy;

import java.util.List;
import java.util.Set;

import com.top_logic.tool.state.AbstractState;

/**
 * End state.
 *
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class EndState extends AbstractState {

    public EndState(String aKey, List aSuccessorList, Integer theStep, Set theOwners) {
        super(aKey, aSuccessorList, theStep, theOwners);
    }

}
