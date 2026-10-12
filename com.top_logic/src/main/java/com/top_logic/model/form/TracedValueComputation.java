/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form;

import com.top_logic.basic.col.Sink;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.Pointer;

/**
 * Computation of a derived attribute value that can trace the model values it depends on.
 *
 * <p>
 * Implemented by a {@link StorageDetail} that computes the value of its attribute from other model
 * values. A tracing evaluation that reads such an attribute uses this computation instead of the
 * plain value lookup, so that the accesses inside the computation are reported as dependencies and
 * the value reflects the edited values of a form.
 * </p>
 */
public interface TracedValueComputation {

	/**
	 * Computes the value of the given attribute for the given object.
	 *
	 * <p>
	 * Every model access performed by the computation is reported to the given {@link Sink}. Model
	 * values are read through the given {@link OverlayLookup}, so that the computation is based on
	 * the edited values of objects that have an overlay.
	 * </p>
	 *
	 * @param object
	 *        The object whose attribute value is computed. This may itself be an overlay.
	 * @param attribute
	 *        The (derived) attribute whose value is computed.
	 * @param trace
	 *        A {@link Sink} of {@link Pointer}s to the model values the computation accesses. The
	 *        computation reports all those {@link Pointer}s to the given {@link Sink}.
	 * @param overlays
	 *        The overlay lookup, or <code>null</code> if not available.
	 * @return The computed value of the attribute.
	 */
	Object computeTraced(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace, OverlayLookup overlays);

}
