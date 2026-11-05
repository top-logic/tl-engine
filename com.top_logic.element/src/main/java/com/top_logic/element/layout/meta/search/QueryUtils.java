/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import com.top_logic.tool.boundsec.BoundChecker;
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

	/** Name of the command group for modifying queries that only the owner may execute. */
	public static final String OWNER_WRITE_NAME = "ownerWrite";

	/** Field and Group names */
	public static final String FORM_GROUP	        = "publishGroup";

	/** Name of the field whether a query is published. */
	public static final String PUBLISH_QUERY_FIELD  = "publishQuery";

	/** Name of the field selecting the groups a query is published to. */
	public static final String VISIBLE_GROUPS_FIELD = "visibleGroups";

	/** Name of the query value storing whether it is an extended search. */
	public static final String IS_EXTENDED          = "isExtended";
	
	/**
	 * Executability rule for saving and deleting queries: only in view mode and with a selected
	 * query.
	 */
    public static final ExecutabilityRule SAVE_DEL_RULE = CombinedExecutabilityRule.combine(InViewModeExecutable.INSTANCE, NullModelDisabled.INSTANCE);
    
	/**
	 * Whether {@link #PUBLISH_NAME} and {@link #OWNER_WRITE_NAME} are allowed.
	 */
	public static boolean allowWriteAndPublish(BoundCheckerComponent checker) {
		return BoundChecker.allowCommand(checker, CommandGroupRegistry.resolve(OWNER_WRITE_NAME), checker.getModel())
			&& BoundChecker.allowCommand(checker, CommandGroupRegistry.resolve(PUBLISH_NAME), checker.getModel());
	}

}
