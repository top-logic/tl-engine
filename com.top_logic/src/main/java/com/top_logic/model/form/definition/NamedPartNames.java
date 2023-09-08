/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.model.TLNamedPart;

/**
 * Conversion from select options to {@link PropertyDescriptor} values and vice-versa for
 * {@link TLNamedPart}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NamedPartNames implements OptionMapping {

	@Override
	public Object toSelection(Object option) {
		return map(option, NamedPartNames::toName);
	}

	private static String toName(Object option) {
		return ((TLNamedPart) option).getName();
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		return map(selection, x -> find(allOptions, x));
	}

	private static Object find(Iterable<?> allOptions, Object selection) {
		for (Object entry : allOptions) {
			if (entry instanceof TLNamedPart) {
				if (selection.equals(((TLNamedPart) entry).getName())) {
					return entry;
				}
			}
		}
		return null;
	}

	private static <T> Object map(Object value, Function<Object, ?> fun) {
		if (value == null) {
			return null;
		} else if (value instanceof Collection<?>) {
			return mapCollection((Collection<?>) value, fun);
		} else {
			return fun.apply(value);
		}
	}

	private static <T> List<T> mapCollection(Collection<?> values, Function<Object, T> fun) {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}
		List<T> result = new ArrayList<>();
		for (Object optionElement : values) {
			result.add(fun.apply(optionElement));
		}
		return result;
	}

}