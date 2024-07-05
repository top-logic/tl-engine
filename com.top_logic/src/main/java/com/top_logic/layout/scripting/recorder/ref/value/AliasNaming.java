/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.UnrecordableNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ModelNamingScheme} of values read from {@link AliasManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AliasNaming extends UnrecordableNamingScheme<String, AliasNaming.Name> {

	/**
	 * {@link ModelName} of {@link AliasNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The name of the alias.
		 */
		String getValue();

		/**
		 * Whether the value for the alias may be <code>null</code>, empty, or not set.
		 * 
		 * <p>
		 * Resolving a non-optional {@link Name} with an empty resolved value leads to an error.
		 * </p>
		 */
		boolean isOptional();

	}

	/**
	 * Creates a {@link AliasNaming}.
	 *
	 */
	public AliasNaming() {
		super(String.class, Name.class);
	}

	@Override
	public String locateModel(ActionContext context, Name name) {
		String resolved = AliasManager.getInstance().getAlias(name.getValue());
		if (!name.isOptional() && StringServices.isEmpty(resolved)) {
			throw ApplicationAssertions.fail(name, "No alias available or value empty: " + name.getValue());
		}
		return resolved;
	}

}
