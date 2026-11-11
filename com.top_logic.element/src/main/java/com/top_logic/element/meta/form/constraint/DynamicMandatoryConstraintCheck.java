/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.constraint;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DynamicVisibility;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link MandatoryConstraintCheck} of an attribute whose mode a {@link ModeSelector} computes: a
 * value is required where the mode requires one, see
 * {@link DynamicVisibility#isMandatory(com.top_logic.model.form.definition.FormVisibility, TLObject, TLStructuredTypePart)}.
 *
 * <p>
 * The check depends on the values the mode is computed from, so it is evaluated again when one of
 * them changes.
 * </p>
 */
public class DynamicMandatoryConstraintCheck extends MandatoryConstraintCheck {

	private final ModeSelector _selector;

	/**
	 * Creates a {@link DynamicMandatoryConstraintCheck}.
	 *
	 * @param selector
	 *        The {@link ModeSelector} computing the mode of the checked attribute.
	 */
	public DynamicMandatoryConstraintCheck(ModeSelector selector) {
		_selector = selector;
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		if (!DynamicVisibility.isMandatory(_selector.getMode(object, attribute, true), object, attribute)) {
			return null;
		}
		return super.check(object, attribute);
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		_selector.traceDependencies(object, attribute, true, trace, overlays);
	}

}
