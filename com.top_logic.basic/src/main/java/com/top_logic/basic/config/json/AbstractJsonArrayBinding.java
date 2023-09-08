/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} for arrays of the given type.
 * 
 * @param <T>
 *        Component type of the array.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractJsonArrayBinding<T> implements JsonValueBinding<T[]> {

	/**
	 * Component type of the result array.
	 */
	protected abstract Class<T> getComponentType();

	@Override
	public boolean isLegalValue(Object value) {
		return true;
	}

	@Override
	public T[] defaultValue() {
		return newArray(0);
	}

	@SuppressWarnings("unchecked")
	private T[] newArray(int size) {
		return (T[]) Array.newInstance(getComponentType(), size);
	}

	@Override
	public Object normalize(Object value) {
		return value == null ? defaultValue() : value;
	}

	@Override
	public T[] loadConfigItem(PropertyDescriptor property, JsonReader in, T[] baseValue) throws IOException, ConfigurationException {
		ArrayList<Object> elements = new ArrayList<>();
		if (baseValue != null) {
			Collections.addAll(elements, baseValue);
		}
		in.beginArray();
		while (in.hasNext()) {
			elements.add(JsonUtilities.readValue(in));
		}
		in.endArray();
		return elements.toArray(this::newArray);
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, T[] item) throws IOException {
		out.beginArray();
		for (Object element : item) {
			JsonUtilities.writeValue(out, element);
		}
		out.endArray();
	}

}

