/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} that is composed of two other {@link StorageMapping}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MappingChain<T> implements StorageMapping<T> {

	private final StorageMapping<T> _m1;

	private final StorageMapping<?> _m2;

	/**
	 * Creates a {@link MappingChain}.
	 * 
	 * @param m1
	 *        The {@link StorageMapping} that is executed first when storing (see
	 *        {@link #getStorageObject(Object)}).
	 * @param m2
	 *        The {@link StorageMapping} that is invoked on the result of the first one when
	 *        storing.
	 */
	public MappingChain(StorageMapping<T> m1, StorageMapping<?> m2) {
		_m1 = m1;
		_m2 = m2;
	}

	@Override
	public Class<? extends T> getApplicationType() {
		return _m1.getApplicationType();
	}

	@Override
	public T getBusinessObject(Object aStorageObject) {
		Object m2BusinessObject = _m2.getBusinessObject(aStorageObject);
		return _m1.getBusinessObject(m2BusinessObject);
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		Object m1StorageObject = _m1.getStorageObject(aBusinessObject);
		return _m2.getStorageObject(m1StorageObject);
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return _m1.isCompatible(businessObject);
	}

}
