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
	 * Creates the input control for a property annotated with the given annotation.
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

}
