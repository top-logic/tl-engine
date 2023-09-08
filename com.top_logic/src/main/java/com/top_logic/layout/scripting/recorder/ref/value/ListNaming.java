/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} of {@link List} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListNaming extends ModelNamingScheme<Object, List<?>, ListNaming.Name> {

	/**
	 * {@link ModelName} of {@link ListNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The literal value.
		 */
		List<ModelName> getValues();

	}

	@Override
	public Class<Object> getContextClass() {
		return Object.class;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<List<?>> getModelClass() {
		return (Class) List.class;
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public List<?> locateModel(ActionContext context, Object valueContext, Name name) {
		ArrayList<Object> result = new ArrayList<>();
		for (ModelName modelName : name.getValues()) {
			result.add(ModelResolver.locateModel(context, valueContext, modelName));
		}
		return result;
	}

	@Override
	public Maybe<Name> buildName(Object valueContext, List<?> model) {
		Name name = TypedConfiguration.newConfigItem(getNameClass());
		List<ModelName> values = name.getValues();
		for (Object value : model) {
			Maybe<? extends ModelName> entryName = ModelResolver.buildModelNameIfAvailable(valueContext, value);
			if (!entryName.hasValue()) {
				return Maybe.none();
			}
			values.add(entryName.get());
		}
		return Maybe.some(name);
	}

}
