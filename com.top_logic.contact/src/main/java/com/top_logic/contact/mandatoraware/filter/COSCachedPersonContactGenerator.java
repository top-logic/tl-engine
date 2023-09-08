/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * Generate {@link PersonContact}s that are bound to a part of {@link Mandator}-Structure
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class COSCachedPersonContactGenerator extends MandatorAwareContactGenerator {

	private static final Filter<? super Object> PERSON_CONTACT_FILTER = FilterFactory
		.createClassFilter(PersonContact.class);

	@Override
	protected Filter<? super TLObject> getFilter() {
		return PERSON_CONTACT_FILTER;
	}

	@Override
	protected TLReference getReference() {
		TLClass type = ContactFactory.getInstance().getMetaElement(PersonContact.META_ELEMENT);
		return (TLReference) type.getPartOrFail(COSContactConstants.ATTRIBUTE_MANDATOR);
	}

}
