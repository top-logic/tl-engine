/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.layout;

import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Option provider that gives access to all system accounts.
 */
public class AllUsers extends Function0<List<Person>> {

	@Override
	public List<Person> apply() {
		return Person.all();
	}

}
