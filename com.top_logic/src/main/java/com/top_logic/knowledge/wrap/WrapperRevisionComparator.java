/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Comparator;

import com.top_logic.knowledge.service.Revision;

/**
 * Comparator for revisions of wrappers.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WrapperRevisionComparator implements Comparator<Wrapper> {

    /** Common reference to this class. */
    public static final WrapperRevisionComparator INSTANCE = new WrapperRevisionComparator();

    @Override
	public int compare(Wrapper aWrapper1, Wrapper aWrapper2) {
        Revision theRev1 = (aWrapper1 == null) ? null : WrapperHistoryUtils.getRevision(aWrapper1);
        Revision theRev2 = (aWrapper2 == null) ? null : WrapperHistoryUtils.getRevision(aWrapper2);

        if (theRev1 == null) {
            return (theRev2 == null) ? 0 : -1;
        }
        else if (theRev2 == null) {
            return 1;
        }
        else { 
            return theRev1.compareTo(theRev2);
        }
    }
}
