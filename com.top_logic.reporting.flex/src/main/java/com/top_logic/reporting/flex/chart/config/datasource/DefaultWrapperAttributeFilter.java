/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;

/**
 * Default filter for attribute-values of a wrapper. In case of wrapper-typed values the unversioned
 * identities are compared.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class DefaultWrapperAttributeFilter implements Filter<Object> {

	private static final Mapping<Object, Object> UNVERSIONED_KEY_MAPPING = new Mapping<>() {

		@Override
		public Object map(Object input) {
			if (input instanceof Wrapper) {
				return WrapperHistoryUtils.getUnversionedIdentity((Wrapper) input);
			}
			return input;
		}
	};

	private final Filter<Object> _inner;
	private String _name;

	/**
	 * Applies the given filter to the value of the given meta-attribute.
	 */
	@SuppressWarnings("unchecked")
	public DefaultWrapperAttributeFilter(TLStructuredTypePart ma, Filter<?> inner) {
		_inner = (Filter<Object>) inner;
		_name = ma.getName();
	}

	@Override
	public boolean accept(Object anObject) {
		return _inner.accept(toUnversionedKeys(((Wrapper) anObject).getValue(_name)));
	}

	/**
	 * Translates the given input to unversioned output: Returns {@link ObjectReference}s for
	 * {@link Wrapper}
	 */
	public static Object toUnversionedKeys(Object value) {
		if (value instanceof Collection) {
			return Mappings.map(UNVERSIONED_KEY_MAPPING, (Collection<?>) value);
		}
		return UNVERSIONED_KEY_MAPPING.map(value);
	}

}