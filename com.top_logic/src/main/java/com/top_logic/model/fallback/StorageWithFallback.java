/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.fallback;

import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link StorageDetail} that differntiates between explicitly set values and those computed from a
 * fallback strategy.
 */
public interface StorageWithFallback extends StorageDetail {

	/**
	 * The value to use for the given attribute, if none has been
	 * {@link #getExplicitValue(TLObject, TLStructuredTypePart) explicitly set}.
	 */
	Object getFallbackValue(TLObject object, TLStructuredTypePart attribute);

	/**
	 * The value that has been explicitly set for the given attribute on the given object.
	 * 
	 * <p>
	 * A value explicitly set overrides a {@link #getFallbackValue(TLObject, TLStructuredTypePart)
	 * fallback value}.
	 * </p>
	 */
	Object getExplicitValue(TLObject object, TLStructuredTypePart attribute);

}
