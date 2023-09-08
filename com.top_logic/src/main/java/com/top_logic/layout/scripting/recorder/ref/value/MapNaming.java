/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.value.MapNaming.Name.EntryName;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} of {@link List} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MapNaming extends ModelNamingScheme<Object, Map<?, ?>, MapNaming.Name> {

	/**
	 * {@link ModelName} of {@link MapNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The literal value.
		 */
		List<EntryName> getValues();

		/**
		 * Description of a single {@link Map} enry.
		 */
		interface EntryName extends ConfigurationItem {
			/**
			 * Description of {@link java.util.Map.Entry#getKey() the map entry's key}.
			 */
			ModelName getKey();

			/**
			 * @see #getKey()
			 */
			void setKey(ModelName value);

			/**
			 * Description of {@link java.util.Map.Entry#getValue() the map entry's value}.
			 */
			ModelName getValue();

			/**
			 * @see #getValue()
			 */
			void setValue(ModelName value);
		}

	}

	@Override
	public Class<Object> getContextClass() {
		return Object.class;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<Map<?, ?>> getModelClass() {
		return (Class) Map.class;
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Map<?, ?> locateModel(ActionContext context, Object valueContext, Name name) {
		Map<Object, Object> result = new HashMap<>();
		for (EntryName entryName : name.getValues()) {
			result.put(
				ModelResolver.locateModel(context, valueContext, entryName.getKey()),
				ModelResolver.locateModel(context, valueContext, entryName.getValue()));
		}
		return result;
	}

	@Override
	public Maybe<Name> buildName(Object valueContext, Map<?, ?> model) {
		Name name = TypedConfiguration.newConfigItem(getNameClass());
		List<EntryName> values = name.getValues();
		for (Entry<?, ?> entry : model.entrySet()) {
			EntryName entryName = TypedConfiguration.newConfigItem(Name.EntryName.class);
			Maybe<? extends ModelName> keyName = ModelResolver.buildModelNameIfAvailable(valueContext, entry.getKey());
			if (!keyName.hasValue()) {
				return Maybe.none();
			}
			entryName.setKey(keyName.get());
			Maybe<? extends ModelName> valueName =
				ModelResolver.buildModelNameIfAvailable(valueContext, entry.getValue());
			if (!valueName.hasValue()) {
				return Maybe.none();
			}
			entryName.setValue(valueName.get());
			values.add(entryName);
		}
		return Maybe.some(name);
	}

}
