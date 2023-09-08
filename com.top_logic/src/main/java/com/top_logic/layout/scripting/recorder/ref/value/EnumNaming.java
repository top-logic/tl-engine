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
 * {@link ModelNamingScheme} of {@link Enum} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EnumNaming extends AbstractModelNamingScheme<Enum<?>, EnumNaming.Name> {

	/**
	 * {@link ModelName} of {@link EnumNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The literal value.
		 */
		Enum<?> getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(Enum<?> value);

	}

	@Override
	public Class<Enum<?>> getModelClass() {
		return cast(Enum.class);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Class<Enum<?>> cast(Class<Enum> clazz) {
		return (Class) clazz;
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Enum<?> locateModel(ActionContext context, Name name) {
		return name.getValue();
	}

	@Override
	protected void initName(Name name, Enum<?> model) {
		name.setValue(model);
	}

}
