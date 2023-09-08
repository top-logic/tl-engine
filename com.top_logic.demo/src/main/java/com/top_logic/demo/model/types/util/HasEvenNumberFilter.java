/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import java.util.regex.Pattern;

import com.top_logic.basic.util.RegExpUtil;
import com.top_logic.demo.model.types.DemoTypesAll;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.AbstractAttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;

/**
 * {@link AttributedValueFilter} that accepts {@link DemoTypesAll} that have an even number in their
 * {@link DemoTypesAll#getName() name}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class HasEvenNumberFilter extends AbstractAttributedValueFilter {

	private static final Pattern EVEN_NUMBER_PATTERN = Pattern.compile("[02468]([^0-9]|$)");

	@Override
	public boolean accept(Object candidate, EditContext anAttrubuteupdateContainer) {

		if (!(candidate instanceof DemoTypesAll)) {
			return false;
		}
		String name = ((DemoTypesAll) candidate).getName();
		return RegExpUtil.contains(name, EVEN_NUMBER_PATTERN);
	}

}
