/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.config.annotation.UnorderedEnum;
import com.top_logic.model.impl.generated.TLEnumerationBase;

/**
 * An enumeration type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Classification")
public interface TLEnumeration extends TLEnumerationBase {

	/**
	 * Whether the {@link #getClassifiers()} list define the order in which to present a selection
	 * to the user.
	 * 
	 * @see UnorderedEnum
	 */
	default boolean isOrdered() {
		return getAnnotation(UnorderedEnum.class) == null;
	}

	/**
	 * The {@link TLClassifier}s of this {@link TLEnumeration}.
	 */
	List<TLClassifier> getClassifiers();
	
	/**
	 * The {@link TLClassifier} from {@link #getClassifiers()} with the given name, or
	 * <code>null</code>, if no such {@link TLClassifier} exists.
	 */
	default TLClassifier getClassifier(String name) {
		for (TLClassifier classifier : getClassifiers()) {
			if (classifier.getName().equals(name)) {
				return classifier;
			}
		}
		return null;
	}

	@Override
	default ModelKind getModelKind() {
		return ModelKind.ENUMERATION;
	}

	@Override
	default List<? extends TLStructuredTypePart> getLocalParts() {
		return Collections.emptyList();
	}

	@Override
	default TLStructuredTypePart getPart(String name) {
		return null;
	}

	@Override
	default <R, A> R visitType(TLTypeVisitor<R, A> v, A arg) {
		return v.visitEnumeration(this, arg);
	}

}
