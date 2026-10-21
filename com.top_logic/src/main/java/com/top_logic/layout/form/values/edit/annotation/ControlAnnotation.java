/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.layout.form.values.edit.ControlAnnotationFormat;
import com.top_logic.layout.form.values.edit.EditorFactory;

/**
 * Meta-annotation marking an annotation that states what the value of a configuration property
 * means, so that a configuration form chooses the control for the property.
 *
 * <p>
 * The marked annotation names no control. Each form stack maps it to a control in its own
 * configuration, e.g. {@link EditorFactory.Config#getAnnotationControls()} for the classic UI. Only
 * marked annotations can be keys of these maps, see {@link ControlAnnotationFormat}.
 * </p>
 *
 * <p>
 * A marked annotation must also be annotated {@link Retention} {@link RetentionPolicy#RUNTIME}.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ControlAnnotation {

	// Pure marker.

}
