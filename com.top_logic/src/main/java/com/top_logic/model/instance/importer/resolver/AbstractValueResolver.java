/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.resolver;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.importer.schema.CustomValueConf;

/**
 * Algorithm for binding primitive model values to configurations for XML export.
 */
public abstract class AbstractValueResolver<C extends CustomValueConf, T> implements ValueResolver {

	@Override
	public CustomValueConf createValueConf(TLStructuredTypePart attribute, Object value) {
		@SuppressWarnings("unchecked")
		T concreteValue = (T) value;
		return internalCreateValueConf(attribute, concreteValue);
	}

	@Override
	public Object resolve(TLStructuredTypePart attribute, CustomValueConf config) {
		@SuppressWarnings("unchecked")
		C concreteConf = (C) config;
		return internalResolve(attribute, concreteConf);
	}

	/**
	 * Resolves a value form a configuration.
	 * @param attribute
	 *        The context attribute.
	 * @param config
	 *        The value configuration.
	 *
	 * @return The resolved value.
	 */
	protected abstract T internalResolve(TLStructuredTypePart attribute, C config);

	/**
	 * Creates a configuration that can represent the given value in an XML export.
	 *
	 * @param attribute
	 *        The context attribute.
	 * @param value
	 *        The value to export.
	 * @return The configuration item to represent the given value.
	 */
	protected abstract C internalCreateValueConf(TLStructuredTypePart attribute, T value);

}
