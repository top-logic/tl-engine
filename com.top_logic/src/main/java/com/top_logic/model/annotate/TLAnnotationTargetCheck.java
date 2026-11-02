/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.model.TLTypePart;

/**
 * {@link TLAnnotation} deciding by its own settings whether it fits the attribute it is placed on.
 *
 * <p>
 * The meta-annotation {@link TargetType} restricts an annotation to kinds of target types by its
 * class alone. Some annotations fit an attribute depending on what they are configured with: a
 * setting may supply what the type of the attribute lacks, e.g. a script making a value that the
 * attribute cannot make itself. Such an annotation implements this interface. When a model is
 * loaded, an annotation of an attribute that {@link #checkTarget(TLTypePart) objects} to it is
 * reported as an error and dropped, as one not matching its {@link TargetType} is.
 * </p>
 *
 * @see TargetType
 */
public interface TLAnnotationTargetCheck extends TLAnnotation {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * Checks whether this annotation fits the given attribute.
	 *
	 * <p>
	 * An implementation overrides this default method; the annotation is a configuration
	 * interface, whose methods are properties unless they are default methods.
	 * </p>
	 *
	 * @param target
	 *        The attribute the annotation is placed on, with its type resolved.
	 * @return A description of why the annotation does not fit the attribute, {@code null} if it
	 *         fits.
	 */
	default String checkTarget(TLTypePart target) {
		return null;
	}

}
