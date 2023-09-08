/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.util;

import java.util.regex.Pattern;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.form.FormMember;

/**
 * Match {@link FormMember#getLabel()} with a {@link Pattern}.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class RegularExpressionLabelFilter implements Filter {

    private Pattern pattern;
    
    public RegularExpressionLabelFilter(String anExpression) {
        super();
        this.pattern = Pattern.compile(anExpression);
    }

    @Override
	public boolean accept(Object anObject) {
        return this.pattern.matcher(((FormMember) anObject).getLabel()).matches();
    }
    
    public static RegularExpressionLabelFilter getFilter(String anRegEx) {
        return new RegularExpressionLabelFilter(anRegEx);
    }

}
