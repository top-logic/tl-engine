/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.resolver;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.importer.XMLInstanceImporter;

/**
 * {@link InstanceResolver} that can resolve {@link Person} instances by login name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccountResolver implements InstanceResolver {

	/**
	 * Resolver kind to register {@link AccountResolver}s for.
	 * 
	 * @see XMLInstanceImporter#addResolver(String, InstanceResolver)
	 */
	public static final String KIND = "account";

	private PersonManager _pm;

	/**
	 * Creates a {@link AccountResolver}.
	 *
	 * @param pm
	 *        The {@link PersonManager} providing access to {@link Person} instances.
	 */
	public AccountResolver(PersonManager pm) {
		_pm = pm;
	}

	@Override
	public TLObject resolve(String kind, String id) {
		return _pm.getPersonByName(id);
	}

	@Override
	public String buildId(TLObject obj) {
		return ((Person) obj).getName();
	}

}
