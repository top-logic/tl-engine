/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

/**
 * {@link AnnotationLookup} that can be enhanced with additional annotations.
 * 
 * <p>
 * This is a memory-optimized variant of an immutable map of {@link TLAnnotation} indexed by their
 * configuration type. This implementation is only useful, if only a small number of annotations is
 * expected, because the implementation has a lookup complexity of <code>O(n)</code>.
 * </p>
 * 
 * @see #with(TLAnnotation)
 */
public interface AnnotationContainer extends AnnotationLookup {

	/**
	 * The empty {@link AnnotationContainer}.
	 */
	AnnotationContainer EMPTY = new AnnotationContainer() {
		@Override
		public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
			return null;
		}

		@Override
		public AnnotationContainer without(Class<? extends TLAnnotation> annotationType) {
			return this;
		}
	};

	/**
	 * Creates an {@link AnnotationContainer} that additionally contains the given annotation.
	 */
	default AnnotationContainer with(TLAnnotation annotation) {
		Class<? extends TLAnnotation> newAnnotationType = annotation.getConfigurationInterface();
		AnnotationContainer fallback = without(newAnnotationType);
		
		return new AnnotationContainer() {
			@Override
			public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
				if (annotationType == newAnnotationType) {
					@SuppressWarnings("unchecked")
					T result = (T) annotation;
					return result;
				}
				return fallback.getAnnotation(annotationType);
			}

			@Override
			public AnnotationContainer without(Class<? extends TLAnnotation> annotationType) {
				if (annotationType == newAnnotationType) {
					return fallback;
				}
				AnnotationContainer newFallback = fallback.without(annotationType);
				if (newFallback == fallback) {
					return this;
				}
				return newFallback.with(annotation);
			}
		};
	}

	/**
	 * The {@link AnnotationContainer} that does contain the same annotations of this one except
	 * annotations of the given type.
	 */
	AnnotationContainer without(Class<? extends TLAnnotation> annotationType);
}
