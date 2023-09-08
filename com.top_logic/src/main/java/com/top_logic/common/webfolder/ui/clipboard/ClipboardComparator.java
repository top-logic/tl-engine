/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.clipboard;

import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.model.TLObject;

/**
 * {@link WrapperNameComparator} that keeps {@link WebFolder} instances
 * together.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClipboardComparator extends WrapperNameComparator {

	// Note: Not a singleton, because the class is locale-dependent.

	@Override
	public int compare(TLObject o1, TLObject o2) {
        boolean isFolder1 = o1 instanceof WebFolder;
		boolean isFolder2 = o2 instanceof WebFolder;

        if (isFolder1 && !isFolder2) {
            return -1;
        }
        else if (isFolder2 && !isFolder1) {
            return 1;
        }

        return super.compare(o1, o2);
	}

}
