/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;

/**
 * A {@link ModelNamingScheme} for {@link DemoPerson}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoPersonNaming extends ValueNamingScheme<DemoPerson> {

	private static final String SURNAME = "surname";

	private static final String GIVEN_NAME = "givenName";

	@Override
	public Class<DemoPerson> getModelClass() {
		return DemoPerson.class;
	}

	@Override
	public Map<String, Object> getName(DemoPerson model) {
		Map<String, Object> result = new HashMap<>();
		result.put(SURNAME, model.getSurname());
		result.put(GIVEN_NAME, model.getGivenName());
		return result;
	}

	@Override
	public boolean matches(Map<String, Object> name, DemoPerson model) {
		return matchesDefault(name, model);
	}

}

