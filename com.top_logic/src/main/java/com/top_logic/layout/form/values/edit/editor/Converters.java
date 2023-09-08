/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.convert.NumberToByte;
import com.top_logic.layout.form.values.convert.NumberToDouble;
import com.top_logic.layout.form.values.convert.NumberToFloat;
import com.top_logic.layout.form.values.convert.NumberToInteger;
import com.top_logic.layout.form.values.convert.NumberToLong;
import com.top_logic.layout.form.values.convert.NumberToShort;

/**
 * Default storage conversions for built-in property types.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Converters {

	/**
	 * Default storage conversion for the given property.
	 */
	public static Mapping<Object, Object> valueConverter(PropertyDescriptor property) {
		Mapping<?, ?> converter = CONVERTERS.get(property.getType());
		if (converter == null) {
			return Mappings.identity();
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Mapping<Object, Object> result = (Mapping) converter;
		return result;
	}

	private static final Map<Class<?>, Mapping<?, ?>> CONVERTERS;

	static {
		CONVERTERS = buildConverters();
	}

	private static Map<Class<?>, Mapping<?, ?>> buildConverters() {
		Map<Class<?>, Mapping<?, ?>> converters = new HashMap<>();
		registerConverters(converters);
		return converters;
	}

	/**
	 * Registers {@link Mapping}s for certain types.
	 * 
	 * @param converters
	 *        The mapping type to {@link Mapping} that is being built.
	 */
	private static void registerConverters(Map<Class<?>, Mapping<?, ?>> converters) {
		converters.put(byte.class, NumberToByte.INSTANCE);
		converters.put(Byte.class, NumberToByte.INSTANCE);
		converters.put(short.class, NumberToShort.INSTANCE);
		converters.put(Short.class, NumberToShort.INSTANCE);
		converters.put(int.class, NumberToInteger.INSTANCE);
		converters.put(Integer.class, NumberToInteger.INSTANCE);
		converters.put(long.class, NumberToLong.INSTANCE);
		converters.put(Long.class, NumberToLong.INSTANCE);
		converters.put(float.class, NumberToFloat.INSTANCE);
		converters.put(Float.class, NumberToFloat.INSTANCE);
		converters.put(double.class, NumberToDouble.INSTANCE);
		converters.put(Double.class, NumberToDouble.INSTANCE);
	}

}
