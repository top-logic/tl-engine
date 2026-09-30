/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.layout;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * {@link OptionMapping} that selects the user name of an {@link Person account}.
 */
public class UserNameMapping implements OptionMapping {

	@Override
	public Object toSelection(Object option) {
		if (option instanceof Person account) {
			return account.getName();
		}
		return null;
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		if (selection == null) {
			return null;
		}
		return Person.byName((String) selection);
	}

}
