/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import static com.top_logic.basic.CollectionUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The BoundCheckerMixin provides implementations for the {@link BoundChecker} interface
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class BoundCheckerMixin {

    /**
	 * Get all {@link BoundChecker}s amongst the elements of the given collections.
	 * 
	 * @param children
	 *        Is allowed to be null.
	 * @param dialogs
	 *        Is allowed to be null.
	 */
	public static Collection<BoundChecker> getChildCheckers(Collection<?> children, Collection<?> dialogs) {
		List<BoundChecker> result = new ArrayList<>();
		result.addAll(getBoundCheckers(children));
		result.addAll(getBoundCheckers(dialogs));
        return result;
    }

    /**
	 * Get all {@link BoundChecker}s in the given {@link Collection}.
	 * 
	 * @param children
	 *        Is allowed to be null.
	 */
	public static List<BoundChecker> getBoundCheckers(Collection<?> children) {
		List<BoundChecker> result = new ArrayList<>();

		for (Object child : nonNull(children)) {
			if (child instanceof BoundChecker) {
				result.add((BoundChecker) child);
            }    
        }
		return result;
    }

}

