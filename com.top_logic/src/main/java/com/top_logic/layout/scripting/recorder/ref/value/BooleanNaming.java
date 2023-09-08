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
 * {@link ModelNamingScheme} of {@link Boolean} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanNaming extends AbstractModelNamingScheme<Boolean, BooleanNaming.Name> {

	/**
	 * {@link ModelName} of {@link BooleanNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The literal value.
		 */
		Boolean getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(Boolean value);

	}

	@Override
	public Class<Boolean> getModelClass() {
		return Boolean.class;
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Boolean locateModel(ActionContext context, Name name) {
		return name.getValue();
	}

	@Override
	protected void initName(Name name, Boolean model) {
		name.setValue(model);
	}

}
