/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import static com.top_logic.basic.util.Utils.*;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.PropertyAccessor;

/**
 * This {@link Accessor} maps the <em>object</em>, not the <em>value</em>, before using the inner
 * {@link Accessor}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public final class MappingPropertyAccessor<S, D> implements PropertyAccessor<S> {

	private final Mapping<? super S, ? extends D> _mapping;

	private final PropertyAccessor<? super D> _innerAccessor;

	/**
	 * Creates an {@link MappingPropertyAccessor}.
	 * 
	 * @param mapping
	 *        Is not allowed to be null.
	 * @param innerAccessor
	 *        Is not allowed to be null.
	 */
	public MappingPropertyAccessor(Mapping<? super S, ? extends D> mapping,
			PropertyAccessor<? super D> innerAccessor) {
		_mapping = requireNonNull(mapping);
		_innerAccessor = requireNonNull(innerAccessor);
	}

	@Override
	public Object getValue(S object) {
		D mappedObject = getMapping().map(object);
		return getInnerAccessor().getValue(mappedObject);
	}

	@Override
	public void setValue(S object, Object value) {
		D mappedValue = getMapping().map(object);
		getInnerAccessor().setValue(mappedValue, value);
	}

	private Mapping<? super S, ? extends D> getMapping() {
		return _mapping;
	}

	private PropertyAccessor<? super D> getInnerAccessor() {
		return _innerAccessor;
	}

}
