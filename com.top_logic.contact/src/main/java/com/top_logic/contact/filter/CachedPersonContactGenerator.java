/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.filter;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;

/**
 * Cache the result (PersonContacts)
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class CachedPersonContactGenerator extends ListGeneratorAdaptor {

	/**
	 * Public constructor for {@link GeneratorFactory}.
	 * 
	 * <p>
	 * Note: This constructor must only be called once by the {@link GeneratorFactory}, since it
	 * registers itself to the persistency layer for update notification and never unregisters.
	 * </p>
	 */
	@CalledByReflection
	public CachedPersonContactGenerator() {
	}

	@Override
	public synchronized List<?> generateList(EditContext editContext) {
		return ContactFactory.getInstance().getAllContacts(ContactFactory.PERSON_TYPE);
	}
	
}
