/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.lang.annotation.Annotation;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Chooses the {@link ControlProvider} for a configuration property that carries a certain
 * annotation in a declarative form.
 *
 * <p>
 * An {@link AnnotationControlProvider} translates the meaning an annotation states about a
 * property (e.g. that its text spans multiple lines) into the control that displays the property.
 * It is registered for its annotation type in the configuration of the {@link EditorFactory}.
 * </p>
 *
 * @param <A>
 *        The annotation type this provider is registered for.
 *
 * @see EditorFactory.Config#getAnnotationControls()
 */
public interface AnnotationControlProvider<A extends Annotation> {

	/**
	 * The {@link ControlProvider} for a property annotated with the given annotation.
	 *
	 * @param property
	 *        The property for which a form field is displayed.
	 * @param annotation
	 *        The annotation found for the given property (on the property itself or on its value
	 *        type, possibly customized for the current form).
	 * @return The {@link ControlProvider} to display the property's field with, or
	 *         <code>null</code> if the annotation does not ask for a special control in this case.
	 */
	ControlProvider getControlProvider(PropertyDescriptor property, A annotation);

}
