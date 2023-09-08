/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import com.top_logic.basic.col.Mapping;
import com.top_logic.knowledge.wrap.list.FastListElement;

/**
 * {@link Mapping} of {@link String} to {@link FastListElement} through
 * {@link FastListElement#getElementByName(String)}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ParseFastlistElementMapping implements Mapping {

    public static final Mapping INSTANCE = new ParseFastlistElementMapping();
    
    private ParseFastlistElementMapping() {
    }
    
    @Override
	public Object map(Object aInput) {
		return FastListElement.getElementByName((String) aInput);
    }

}

