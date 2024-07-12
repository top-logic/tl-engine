/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.basic.AliasManager;
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
		 * The literal value.
		 */
		String getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(String value);

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
		if (resolved == null) {
			throw ApplicationAssertions.fail(name, "No alias available: " + name.getValue());
		}
		return resolved;
	}

}
