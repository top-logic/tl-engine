/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.Factory;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;

/**
 * Container of {@link TLAnnotation}s.
 * 
 * @implNote The type must be annotated with {@link Factory} to exclude it from the configuration
 *           hierarchy. This allows to implement {@link #getAnnotation(Class)} with an indexed
 *           getter of a configuration property, see {@link AnnotationConfigs#getAnnotations()}.
 * 
 * @see AnnotationContainer#EMPTY An empty immutable {@link AnnotationLookup} instance.
 */
@Factory
public interface AnnotationLookup {

	/**
	 * The annotation of the requested type, or <code>null</code>, if no such annotation exists.
	 * 
	 * @param annotationType
	 *        The type of association to request.
	 */
	<T extends TLAnnotation> T getAnnotation(Class<T> annotationType);

}
