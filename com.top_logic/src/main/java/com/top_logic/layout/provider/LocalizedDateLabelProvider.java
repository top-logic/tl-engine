/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import java.util.Date;

import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * Default label provider for dates. Because most of the application issues look at dates 
 * and not at times, this will use the date pattern instead of the dateTime pattern.
 *  
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocalizedDateLabelProvider implements LabelProvider {

	/**
	 * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
	    return HTMLFormatter.getInstance().formatDate((Date) object);
	}
}
