/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;

/**
 * Maps persistent object IDs to their {@link Wrapper} implementations.
 * 
 * @see Wrapper#IDENTIFIER_MAPPING
 * 
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class WrapperByIDMapping implements Mapping<TLID, Wrapper> {

    private final String koType;

	/**
	 * Creates a {@link WrapperByIDMapping}.
	 * 
	 * @param koType
	 *        The monomorphic type to resolve the identifier against.
	 */
    public WrapperByIDMapping(String koType) {
        this.koType = koType;
    }

    @Override
	public Wrapper map(TLID id) {
		return WrapperFactory.getWrapper(id, koType);
    }

}
