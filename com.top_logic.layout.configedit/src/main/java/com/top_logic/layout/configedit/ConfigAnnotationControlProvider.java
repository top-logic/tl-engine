/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.lang.annotation.Annotation;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Creates the {@link ReactControl} for a configuration property that carries a certain annotation.
 *
 * <p>
 * A {@link ConfigAnnotationControlProvider} translates the meaning an annotation states about a
 * property (e.g. that its text spans multiple lines) into the control that edits the property. It
 * is registered for its annotation type in the configuration of the {@link ConfigControlService}.
 * </p>
 *
 * @param <A>
 *        The annotation type this provider is registered for.
 *
 * @see ConfigControlService.Config#getAnnotationControls()
 */
public interface ConfigAnnotationControlProvider<A extends Annotation> {

	/**
	 * Whether this provider edits the value of the given model.
	 *
	 * <p>
	 * A provider that cannot edit the value in the form the model holds it in (e.g. a text area for
	 * a value that is not {@link ConfigControlService#holdsText(ConfigFieldModel) held as text})
	 * declines it. The {@link ConfigControlService} then chooses the control as if the property did
	 * not carry the annotation.
	 * </p>
	 *
	 * @param model
	 *        The field model of the property, see
	 *        {@link #createControl(ReactContext, ConfigFieldModel, Annotation)}.
	 * @param annotation
	 *        The annotation found for the property, on the property itself or on its value type.
	 * @return Whether {@link #createControl(ReactContext, ConfigFieldModel, Annotation)} creates
	 *         the control for the model; <code>true</code> by default.
	 */
	default boolean accepts(ConfigFieldModel model, A annotation) {
		return true;
	}

	/**
	 * Creates the input control for a property annotated with the given annotation.
	 *
	 * <p>
	 * Called only for a model this provider {@link #accepts(ConfigFieldModel, Annotation)
	 * accepts}.
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model providing value, editability, and change notifications. Its
	 *        {@link ConfigFieldModel#getProperty() property} carries the metadata. The annotation
	 *        does not decide the model's value domain: the model holds the value in the form the
	 *        {@link ConfigControlService} chooses for the property without regard to the
	 *        annotation.
	 * @param annotation
	 *        The annotation found for the property, on the property itself or on its value type.
	 * @return The control for the field input widget, never <code>null</code>.
	 */
	ReactControl createControl(ReactContext context, ConfigFieldModel model, A annotation);

	/**
	 * Whether the control this provider creates for the given model takes the whole row of a form
	 * with several columns.
	 *
	 * <p>
	 * Called only for a model this provider {@link #accepts(ConfigFieldModel, Annotation)
	 * accepts}. A control editing a text that spans several lines needs the width of the row. An
	 * explicit {@link com.top_logic.layout.form.values.edit.annotation.RenderWholeLine} annotation
	 * of the property or its value type takes precedence over this decision.
	 * </p>
	 *
	 * @param model
	 *        The field model the control is created for.
	 * @param annotation
	 *        The annotation found for the property, on the property itself or on its value type.
	 * @return Whether the field takes the whole row; <code>false</code> by default.
	 */
	default boolean rendersWholeLine(ConfigFieldModel model, A annotation) {
		return false;
	}

}
