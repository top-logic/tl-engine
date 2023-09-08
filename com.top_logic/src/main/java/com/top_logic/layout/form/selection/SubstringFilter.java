/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} of {@link String} that tests whether the value contains a fixed substring.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class SubstringFilter implements Filter {
	
	private final String substring;

	public SubstringFilter(String substring) {
		this.substring = substring;
	}

	@Override
	public boolean accept(Object anObject) {
		if (!(anObject instanceof String)) {
			return false;
		}
		return ((String)anObject).indexOf(substring) != -1;
	}
	
}
