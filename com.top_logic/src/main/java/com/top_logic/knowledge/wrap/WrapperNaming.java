/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.util.Utils;

/**
 * {@link ValueNamingScheme} that can be used to identifiy persistent objects by their
 * {@link Wrapper#getName()} property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrapperNaming extends ValueNamingScheme<Wrapper> {

	private static final String NAME_PROPERTY = "name";

	@Override
	public Class<Wrapper> getModelClass() {
		return Wrapper.class;
	}

	@Override
	public Map<String, Object> getName(Wrapper model) {
		return Collections.<String, Object> singletonMap(NAME_PROPERTY, model.getName());
	}

	@Override
	public boolean matches(Map<String, Object> name, Wrapper model) {
		return Utils.equals(name.get(NAME_PROPERTY), model.getName());
	}

}
