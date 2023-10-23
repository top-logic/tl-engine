/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;

/**
 * For backwards-compatibility only.
 * 
 * @deprecated Use {@link PersonManager}
 */
@Deprecated
public class TLPersonManager extends PersonManager {

	/**
	 * Creates a {@link TLPersonManager}.
	 */
	public TLPersonManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Use {@link Person#all()}
	 */
	@Deprecated
	public List<Person> getAllAliveFullPersons() {
		return Person.all().stream().filter(Person.FULL_USER_FILTER).collect(Collectors.toList());
	}

	/**
	 * Use {@link Person#all()}
	 */
	@Deprecated
	public List<Person> getAllAliveRestrictedPersons() {
		return Person.all().stream().filter(Person.RESTRICTED_USER_FILTER).collect(Collectors.toList());
	}

}
