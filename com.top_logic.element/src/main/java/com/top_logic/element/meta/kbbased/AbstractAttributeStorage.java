/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link StorageImplementation} of an attribute that is declared <code>abstract</code>.
 *
 * <p>
 * An abstract attribute has no values of its own: Its values are stored by its concrete overrides.
 * Like {@link NoStorage}, all value accesses fail and the attribute is {@link #isReadOnly() read
 * only}. In contrast to {@link NoStorage}, a value access reports that the attribute is abstract.
 * </p>
 *
 * <p>
 * Note: This class must be non-public to prevent the UI from offering it as option for the storage
 * implementation of an attribute.
 * </p>
 */
final class AbstractAttributeStorage extends NoStorage {

	/**
	 * Singleton {@link AbstractAttributeStorage} instance.
	 */
	public static final AbstractAttributeStorage INSTANCE = new AbstractAttributeStorage();

	private AbstractAttributeStorage() {
		super();
	}

	@Override
	RuntimeException unsupported(TLStructuredTypePart attribute) {
		return new TopLogicException(
			I18NConstants.ERROR_ACCESS_TO_ABSTRACT_ATTRIBUTE__ATTR.fill(TLModelUtil.qualifiedName(attribute)));
	}

}
