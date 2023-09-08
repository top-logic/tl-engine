/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import java.util.Set;

import com.top_logic.basic.col.SetBuilder;


/**
 * Some constants to define the Attributes used for the Lifecylce.
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface LifecycleAttributes {

    // Compare with declaration in KBMEta.xml

	/** Time when object was created. Type is long (Milliseconds) */
	public static final String CREATED     = "created";

	/** Id found int TLContext on creation of the object. Type is String */
	public static final String CREATOR     = "creatorid";
    
    /** Time when object was last modified. Type is long (Milliseconds) */
    public static final String MODIFIED     = "modified";

    /** Id found int TLContext on modifying the Object. Type is String */
    public static final String MODIFIER     = "modifier";

	public static final Set<String> LSANAMES = new SetBuilder<String>(4).add(CREATED).add(CREATOR).add(MODIFIED)
		.add(MODIFIER).toUnmodifiableSet();
}
