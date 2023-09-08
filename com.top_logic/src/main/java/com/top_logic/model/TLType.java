/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.impl.AbstractTLType;
import com.top_logic.model.impl.generated.TLTypeBase;

/**
 * A type in a <i>TopLogic</i> model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Type")
public interface TLType extends TLTypeBase {

	/**
	 * The defining TLModule.
	 */
	@Reference(other = "")
	TLModule getModule();

	/** @see #getModule() */
	void setModule(TLModule value);

	/**
	 * The {@link TLModule} this type belongs to.
	 * 
	 * @return The owner of this type, or <code>null</code> for
	 *         {@link TLPrimitive} types.
	 */
	@Container
	TLScope getScope();

	@Override
	default <T extends TLAnnotation> T getAnnotation(Class<T> annotationInterface) {
		T attributeAnnotation = getAnnotationLocal(annotationInterface);
		if (attributeAnnotation != null) {
			return attributeAnnotation;
		}
		return AbstractTLType.getDefaultsFromType(annotationInterface, this);
	}

	/**
	 * Visits this {@link TLType} with the given {@link TLTypeVisitor}.
	 * 
	 * @param <R>
	 *        The result type.
	 * @param <A>
	 *        The argument type.
	 * @param v
	 *        The visitor.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result of the visit.
	 */
	<R, A> R visitType(TLTypeVisitor<R, A> v, A arg);

	@Override
	default <R, A> R visit(TLModelVisitor<R, A> v, A arg) {
		return visitType(v, arg);
	}

}
