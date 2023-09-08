/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.filter;

import com.top_logic.contact.business.CompanyContact;
import com.top_logic.element.meta.kbbased.filtergen.ContextFreeAttributeValueFilter;

/**
 * Filter CompanyContacts
 * 
 * TODO KHA/HBU use some generic class filter.
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
@Deprecated
public class CompanyContactFilter extends ContextFreeAttributeValueFilter {

	
	public CompanyContactFilter() {
		super();
	}

	@Override
	public boolean accept(Object obj) {
		return obj instanceof CompanyContact;
	}

}
