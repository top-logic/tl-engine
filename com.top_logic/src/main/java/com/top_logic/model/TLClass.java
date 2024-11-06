/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.impl.generated.TLClassBase;
import com.top_logic.model.util.TLModelUtil;

/**
 * A class type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Business object type")
public interface TLClass extends TLClassBase {

	/**
	 * Whether this class is abstract.
	 * 
	 * <p>
	 * An abstract class may not have instances. An abstract class cannot be
	 * {@link #isFinal() final}.
	 * </p>
	 */
	boolean isAbstract();
	
	/**
	 * Sets the {@link #isAbstract()} property.
	 */
	void setAbstract(boolean value);
	
	/**
	 * Whether this class is final.
	 * 
	 * <p>
	 * An final class may not have subclasses. A final class cannot be
	 * {@link #isAbstract() abstract}.
	 * </p>
	 */
	boolean isFinal();

	/**
	 * Sets the {@link #isFinal()} property.
	 */
	void setFinal(boolean value);
	
	/**
	 * The classes, this class extends directly.
	 * <p>
	 * To get all generalizations, use either
	 * {@link TLModelUtil#getTransitiveGeneralizations(TLClass)} or
	 * {@link TLModelUtil#getReflexiveTransitiveGeneralizations(TLClass)}.
	 * </p>
	 */
	@Reference(other = "specializations")
	List<TLClass> getGeneralizations();

	/**
	 * The {@link TLClass}es directly {@link #getGeneralizations() specializing} this class.
	 * <p>
	 * To get all generalizations, use either
	 * {@link TLModelUtil#getTransitiveSpecializations(TLClass)},
	 * {@link TLModelUtil#getReflexiveTransitiveSpecializations(TLClass)} or
	 * {@link TLModelUtil#getConcreteReflexiveTransitiveSpecializations(TLClass)}.
	 * </p>
	 */
	@Reference(other = "generalizations")
	Collection<TLClass> getSpecializations();

	/**
	 * Type safe access to {@link TLStructuredType#getLocalParts()}.
	 * 
	 * @return Same value as {@link TLStructuredType#getLocalParts()}.
	 * 
	 * @see TLStructuredType#getLocalParts()
	 * @see TLAssociation#getAssociationParts()
	 */
	List<TLClassPart> getLocalClassParts();

	@Override
	default List<? extends TLStructuredTypePart> getAllParts() {
		return getAllClassParts();
	}

	/**
	 * All {@link TLClassPart}s including inherited ones.
	 * 
	 * <p>
	 * In a stable, but unspecified order.
	 * </p>
	 * 
	 * @return A potentially unmodifiable {@link List}.
	 */
	default List<? extends TLClassPart> getAllClassParts() {
		return TLModelUtil.calcAllPartsUncached(this);
	}

	@Override
	default <T extends TLAnnotation> T getAnnotation(Class<T> annotationInterface) {
		T direct = TLClassBase.super.getAnnotation(annotationInterface);
		if (direct != null) {
			return direct;
		}

		if (AnnotationInheritance.Policy.getInheritancePolicy(annotationInterface) == Policy.INHERIT) {
			for (TLClass generalization : getGeneralizations()) {
				T inherited = generalization.getAnnotation(annotationInterface);
				if (inherited != null) {
					return inherited;
				}
			}
		}

		return null;
	}

	@Override
	default ModelKind getModelKind() {
		return ModelKind.CLASS;
	}

	@Override
	default <R, A> R visitType(TLTypeVisitor<R, A> v, A arg) {
		return v.visitClass(this, arg);
	}

}
