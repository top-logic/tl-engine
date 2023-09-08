/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import com.top_logic.contact.business.ContactFactory;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.AbstractAttributedValueFilter;
import com.top_logic.element.structured.wrap.Mandator;

/**
 * {@link AbstractAttributedValueFilter} accepting {@link Mandator}s that can have contacts.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
@Deprecated
public class SupportsContactsFilter extends AbstractAttributedValueFilter {

	/** 
	 * Default CTor for factory 
	 */
	public SupportsContactsFilter() {
		super();
	}

	/** 
	 * Accept root and top-level Mandators
	 * 
	 * @see com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter#accept(java.lang.Object, EditContext)
	 */
	@Override
	public boolean accept(Object anObject, EditContext anAttrubuteupdateContainer) {
		if (!(anObject instanceof Mandator)) {
			return false;
		}
		
		Mandator theMand = (Mandator) anObject;
		return theMand.allowType(ContactFactory.STRUCTURE_NAME);
	}

}
