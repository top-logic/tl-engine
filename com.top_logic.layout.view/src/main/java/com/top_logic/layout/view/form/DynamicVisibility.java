/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.annotate.TLDynamicVisibility;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;

/**
 * Resolves the (possibly dynamic) display mode of a model attribute for the React view form layer.
 *
 * <p>
 * An attribute may declare a {@link TLDynamicVisibility} annotation carrying a {@link ModeSelector}
 * that computes its {@link FormVisibility} per object and reports the attributes the computation
 * depends on. This is resolved here so that {@link AttributeFieldControl} can apply the mode and
 * recompute it when a dependency changes - the visibility/editability counterpart to dynamic
 * options.
 * </p>
 */
public class DynamicVisibility {

	/**
	 * {@link ModeSelector} sentinel used to cache "no dynamic visibility" for an attribute.
	 */
	private static final ModeSelector NONE = new ModeSelector() {
		@Override
		public FormVisibility getMode(TLObject object, TLStructuredTypePart attribute, boolean editMode) {
			return FormVisibility.DEFAULT;
		}

		@Override
		public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
				OverlayLookup overlays) {
			// No dependencies.
		}
	};

	private static final ConcurrentHashMap<TLStructuredTypePart, ModeSelector> SELECTORS =
		new ConcurrentHashMap<>();

	private DynamicVisibility() {
		// Utility class.
	}

	/**
	 * The {@link ModeSelector} declared by the attribute's {@link TLDynamicVisibility} annotation, or
	 * {@code null} if the attribute has no dynamic visibility.
	 */
	public static ModeSelector modeSelector(TLStructuredTypePart part) {
		ModeSelector cached = SELECTORS.computeIfAbsent(part, DynamicVisibility::resolve);
		return cached == NONE ? null : cached;
	}

	private static ModeSelector resolve(TLStructuredTypePart part) {
		TLDynamicVisibility annotation = part.getAnnotation(TLDynamicVisibility.class);
		if (annotation == null) {
			return NONE;
		}
		return TypedConfigUtil.createInstance(annotation.getModeSelector());
	}

}
