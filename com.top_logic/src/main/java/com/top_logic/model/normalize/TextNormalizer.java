/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.normalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ValueNormalizer;

/**
 * Base class for {@link ValueNormalizer}s rewriting a text.
 *
 * <p>
 * A value that is not a text (a number, a date, an object, or no value at all) is kept unchanged.
 * The values of a multiple attribute are normalized one by one: A list stays a list in the same
 * order, a set stays a set. An element that is normalized to no value stays in the collection as
 * empty element.
 * </p>
 *
 * @implNote Subclasses implement {@link #normalizeText(String)}.
 */
public abstract class TextNormalizer<C extends PolymorphicConfiguration<?>> extends AbstractConfiguredInstance<C>
		implements ValueNormalizer {

	/**
	 * Creates a {@link TextNormalizer} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TextNormalizer(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object normalize(TLObject object, TLStructuredTypePart attribute, Object value) {
		return normalizeValue(value);
	}

	private Object normalizeValue(Object value) {
		if (value instanceof String text) {
			return normalizeText(text);
		}
		if (value instanceof Collection<?> values) {
			Collection<Object> result = values instanceof Set<?> ? new LinkedHashSet<>() : new ArrayList<>();
			for (Object element : values) {
				result.add(normalizeValue(element));
			}
			return result;
		}
		return value;
	}

	/**
	 * The canonical form of the given text.
	 *
	 * @param text
	 *        The entered text, not <code>null</code>.
	 * @return The canonical form of the given text, <code>null</code> for no value.
	 */
	protected abstract String normalizeText(String text);

}
