/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.Named;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.annotation.Label;

/**
 * Representation of a {@link com.top_logic.knowledge.wrap.person.Person}'s role.
 * 
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
@Label("role")
public interface BoundRole extends Named {
    
    /**
	 * The ID of this role
	 */
    public TLID getID();
    
}
