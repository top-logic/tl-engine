/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import com.top_logic.tool.boundsec.BoundCheckerComponent;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * Static utilities for coloring search fields.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class QueryUtils {
	
	/** Command group names */
	public static final String PUBLISH_NAME = "publish";

	public static final String OWNER_WRITE_NAME = "ownerWrite";

	/** Field and Group names */
	public static final String FORM_GROUP	        = "publishGroup";
	public static final String PUBLISH_QUERY_FIELD  = "publishQuery";
	public static final String VISIBLE_GROUPS_FIELD = "visibleGroups";
	public static final String IS_EXTENDED          = "isExtended";
	
    public static final ExecutabilityRule SAVE_DEL_RULE = CombinedExecutabilityRule.combine(InViewModeExecutable.INSTANCE, NullModelDisabled.INSTANCE);
    
	/**
	 * Whether {@link #PUBLISH_NAME} and {@link #OWNER_WRITE_NAME} are allowed.
	 */
	public static boolean allowWriteAndPublish(BoundCheckerComponent checker) {
		return checker.allow(CommandGroupRegistry.resolve(OWNER_WRITE_NAME))
			&& checker.allow(CommandGroupRegistry.resolve(PUBLISH_NAME));
	}

}
