/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collection;

import com.top_logic.basic.config.annotation.External;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Root;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.impl.generated.TLModelPartBase;

/**
 * Common interface for all parts of a <i>TopLogic</i> {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLModelPart extends TLModelPartBase, AnnotationLookup {

	/**
	 * The owner model.
	 */
	@Root
	TLModel getModel();

	/**
	 * Retrieve the annotation instance of the given concrete annotation class.
	 * 
	 * <p>
	 * Annotations of sub-types of the given type are not considered.
	 * </p>
	 * 
	 * @param <T>
	 *        The annotation type.
	 * @param annotationInterface
	 *        The requested annotation class.
	 * @return The annotation instance, or <code>null</code>, if this part has no annotation of the
	 *         given type.
	 */
	@Override
	@Indexed(collection = ANNOTATIONS_ATTR)
	default <T extends TLAnnotation> T getAnnotation(Class<T> annotationInterface) {
		return getAnnotationLocal(annotationInterface);
	}

	/**
	 * The annotation of the requested interface locally defined at this part excluding inherited
	 * annotations and other defaults.
	 * 
	 * @see #getAnnotation(Class)
	 */
	<T extends TLAnnotation> T getAnnotationLocal(Class<T> annotationInterface);

	/**
	 * Sets the {@link #getAnnotation(Class)} property.
	 */
	@External
	void setAnnotation(TLAnnotation annotation);

	/**
	 * Remove the {@link TLAnnotation} of the given type.
	 * <p>
	 * Annotations of a subtype of the given type are not affected.
	 * </p>
	 * <p>
	 * If there is no such annotation, nothing happens.
	 * </p>
	 */
	@External
	void removeAnnotation(Class<? extends TLAnnotation> annotationInterface);

	/**
	 * All annotations added to this part.
	 * 
	 * <p>
	 * This collection cannot be modified.
	 * </p>
	 */
	@Name(ANNOTATIONS_ATTR)
	@Key(TLAnnotation.CONFIGURATION_INTERFACE_NAME)
	Collection<? extends TLAnnotation> getAnnotations();

	/**
	 * The kind of this {@link TLModelPart}.
	 * 
	 * <p>
	 * Replacement for <code>instanceof</code> checks against {@link TLModelPart} instances. Using
	 * the <code>instanceof</code> operator on {@link TLModelPart} instances is not save, since an
	 * implementation objects may implement more interfaces than actually supported.
	 * </p>
	 * 
	 * <p>
	 * {@link #visit(TLModelVisitor, Object) Visiting} a model part using a {@link TLModelVisitor}
	 * is equivalent to switching over the returned {@link ModelKind}.
	 * </p>
	 */
	ModelKind getModelKind();

	/**
	 * Visits this model part with the given visitor.
	 */
	<R,A> R visit(TLModelVisitor<R,A> v, A arg);
	
}
