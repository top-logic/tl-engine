/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} of {@link String} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringNaming extends AbstractModelNamingScheme<String, StringNaming.Name> {

	/**
	 * {@link ModelName} of {@link StringNaming}.
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

	@Override
	public Class<String> getModelClass() {
		return String.class;
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public String locateModel(ActionContext context, Name name) {
		return name.getValue();
	}

	@Override
	protected void initName(Name name, String model) {
		name.setValue(model);
	}

}
