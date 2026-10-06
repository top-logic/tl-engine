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
 * Like {@link NoStorage}, all value accesses fail. In contrast to {@link NoStorage}, also
 * {@link #isReadOnly()} fails: Whether the values of an abstract attribute can be modified is
 * decided by the storage implementations of its concrete overrides, not by the abstract attribute
 * itself.
 * </p>
 *
 * <p>
 * Note: This class must be non-public to prevent the UI from offering it as option for the storage
 * implementation of an attribute.
 * </p>
 */
final class AbstractAttributeStorage extends NoStorage {

	private final TLStructuredTypePart _attribute;

	/**
	 * Creates a {@link AbstractAttributeStorage}.
	 *
	 * @param attribute
	 *        The abstract attribute this storage belongs to.
	 */
	AbstractAttributeStorage(TLStructuredTypePart attribute) {
		_attribute = attribute;
	}

	@Override
	public boolean isReadOnly() {
		throw new UnsupportedOperationException("Abstract attribute '" + TLModelUtil.qualifiedName(_attribute)
			+ "' has no storage. Whether it is read-only is decided by its concrete overrides.");
	}

	@Override
	RuntimeException unsupported(TLStructuredTypePart attribute) {
		return new TopLogicException(
			I18NConstants.ERROR_ACCESS_TO_ABSTRACT_ATTRIBUTE__ATTR.fill(TLModelUtil.qualifiedName(attribute)));
	}

}
