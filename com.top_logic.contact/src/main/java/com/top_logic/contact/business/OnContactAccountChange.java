/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import com.top_logic.element.meta.AttributeChangeListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Listener for the contact reference on accounts.
 */
public class OnContactAccountChange implements AttributeChangeListener {

	@Override
	public void notifyChange(TLObject obj, TLStructuredTypePart attr, Object newValue) {
		Person account = (Person) obj;
		PersonContact contact = (PersonContact) newValue;
		if (contact != null) {
			contact.tUpdateByName("loginName", account.getName());
		}
	}

}
