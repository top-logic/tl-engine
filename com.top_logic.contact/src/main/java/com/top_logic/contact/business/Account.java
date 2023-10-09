/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import com.top_logic.base.user.UserInterface;
import com.top_logic.element.meta.kbbased.AttributedPerson;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * An account in a TopLogic application.
 */
public class Account extends AttributedPerson {

	/**
	 * Creates a {@link Account}.
	 */
	public Account(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public UserInterface getUser() {
		return (UserInterface) tValueByName("contact");
	}

	@Override
	public void setUser(UserInterface user) {
		tUpdateByName("contact", user);
	}

}
