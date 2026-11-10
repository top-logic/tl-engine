/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;

/**
 * Resolves the (possibly dynamic) mode of a model attribute of an edited object.
 *
 * <p>
 * An attribute may declare a {@link TLDynamicVisibility} annotation carrying a {@link ModeSelector}
 * that computes its {@link FormVisibility} per object and reports the attributes the computation
 * depends on. This is resolved here, so that the display of the attribute and the validation of
 * its value follow the same mode and compute it again when a dependency changes.
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
		public void traceDependencies(TLObject object, TLStructuredTypePart attribute, boolean editMode,
				Sink<Pointer> trace, OverlayLookup overlays) {
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

	/**
	 * The mode of the given attribute on the given object: the mode its {@link ModeSelector}
	 * computes, {@link FormVisibility#DEFAULT} for an attribute without one.
	 *
	 * @param object
	 *        The displayed object.
	 * @param part
	 *        The attribute.
	 * @param editMode
	 *        Whether the object is edited.
	 */
	public static FormVisibility mode(TLObject object, TLStructuredTypePart part, boolean editMode) {
		ModeSelector selector = modeSelector(part);
		return selector == null ? FormVisibility.DEFAULT : selector.getMode(object, part, editMode);
	}

	/**
	 * Whether the value of the given attribute is shown on the given object in the given mode.
	 *
	 * <p>
	 * The mode {@link FormVisibility#DEFAULT} keeps what the static visibility of the attribute
	 * says, see {@link #isStaticallyHidden(TLObject, TLStructuredTypePart)}. See
	 * {@link FormVisibility#isVisible(boolean)} for the other modes.
	 * </p>
	 *
	 * @param mode
	 *        The mode of the attribute on the object, {@link FormVisibility#DEFAULT} for an
	 *        attribute without dynamic visibility.
	 * @param object
	 *        The displayed object.
	 * @param part
	 *        The attribute.
	 */
	public static boolean isVisible(FormVisibility mode, TLObject object, TLStructuredTypePart part) {
		return mode.isVisible(!isStaticallyHidden(object, part));
	}

	/**
	 * Whether the attribute is hidden by its static visibility on the given object: its create
	 * visibility ({@link DisplayAnnotations#getCreateVisibility(TLModelPart)}) on a
	 * {@link #isNewObject(TLObject) new object}, its visibility on any other.
	 */
	public static boolean isStaticallyHidden(TLObject object, TLStructuredTypePart part) {
		return isNewObject(object) ? DisplayAnnotations.isHiddenInCreate(part) : DisplayAnnotations.isHidden(part);
	}

	/**
	 * Whether the given attribute requires a value on the given object in the given mode.
	 *
	 * <p>
	 * The mode {@link FormVisibility#DEFAULT} keeps what the static visibility of the attribute
	 * requires: its create visibility
	 * ({@link DisplayAnnotations#getCreateVisibility(TLModelPart)}) on a
	 * {@link #isNewObject(TLObject) new object}, its visibility on any other. See
	 * {@link FormVisibility#isMandatory(boolean)} for the other modes.
	 * </p>
	 *
	 * @param mode
	 *        The mode of the attribute on the object, {@link FormVisibility#DEFAULT} for an
	 *        attribute without dynamic visibility.
	 * @param object
	 *        The edited object.
	 * @param part
	 *        The attribute.
	 */
	public static boolean isMandatory(FormVisibility mode, TLObject object, TLStructuredTypePart part) {
		return mode.isMandatory(isNewObject(object)
			? DisplayAnnotations.isMandatoryInCreate(part)
			: DisplayAnnotations.isMandatory(part));
	}

	/**
	 * Whether the given object is a new object that is not stored yet: a transient object, or the
	 * editing buffer (of editing buffers) of one. A buffer of a stored object edits that object.
	 *
	 * @see TLFormObjectBase#getEditedObject()
	 */
	public static boolean isNewObject(TLObject object) {
		TLObject base = object;
		while (base instanceof TLFormObjectBase buffer) {
			if (buffer.isCreate()) {
				return true;
			}
			base = buffer.getEditedObject();
		}
		return base != null && base.tTransient();
	}

}
