/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.fallback;

import com.top_logic.basic.col.Sink;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.util.Pointer;

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
	 * Computes the {@link #getFallbackValue(TLObject, TLStructuredTypePart) fallback value} over
	 * the edits of a form and reports the attributes the computation reads.
	 *
	 * <p>
	 * The computation runs in a {@link TracingScope}: each read of an attribute value is reported to
	 * the given trace, and an object with an overlay in the given lookup is read through its
	 * overlay.
	 * </p>
	 *
	 * @param object
	 *        The object whose fallback value is computed, typically an overlay of a form.
	 * @param attribute
	 *        The attribute with this storage.
	 * @param trace
	 *        Receives the attributes read by the computation.
	 * @param overlays
	 *        The overlays of the form, <code>null</code> outside of a form.
	 * @return The fallback value.
	 */
	default Object traceFallbackValue(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		try (TracingScope scope = TracingScope.open(trace, overlays)) {
			return getFallbackValue(object, attribute);
		}
	}

	/**
	 * The value that has been explicitly set for the given attribute on the given object.
	 *
	 * <p>
	 * A value explicitly set overrides a {@link #getFallbackValue(TLObject, TLStructuredTypePart)
	 * fallback value}.
	 * </p>
	 */
	Object getExplicitValue(TLObject object, TLStructuredTypePart attribute);

	/**
	 * Explicitly sets the value of the given attribute on the given object.
	 *
	 * <p>
	 * The value is stored in a way that it is later found again through
	 * {@link #getExplicitValue(TLObject, TLStructuredTypePart)}. An explicitly set value overrides
	 * the {@link #getFallbackValue(TLObject, TLStructuredTypePart) fallback value}.
	 * </p>
	 *
	 * @see #getExplicitValue(TLObject, TLStructuredTypePart)
	 */
	void setExplicitValue(TLObject object, TLStructuredTypePart attribute, Object value);

	/**
	 * The attribute holding the explicit value.
	 *
	 * <p>
	 * {@link #setExplicitValue(TLObject, TLStructuredTypePart, Object)} writes the explicit value
	 * to this attribute of the object, {@link #getExplicitValue(TLObject, TLStructuredTypePart)}
	 * reads it from there. A change of this attribute is therefore a change of the attribute with
	 * fallback.
	 * </p>
	 */
	TLStructuredTypePart getStorageAttr();

	/**
	 * CSS class to set on values that have an explicit value assigned, or <code>null</code> if
	 * explicit values should not be visually highlighted.
	 */
	String getCssExplicit();

	/**
	 * CSS class to set on locations where the fallback value is used, or <code>null</code> if
	 * explicit values should not be visually highlighted.
	 */
	String getCssFallback();

}
