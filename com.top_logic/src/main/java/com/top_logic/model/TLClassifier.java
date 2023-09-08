/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.impl.generated.TLClassifierBase;

/**
 * A classifier of an {@link TLEnumeration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Classifier")
public interface TLClassifier extends TLClassifierBase {

	/**
	 * Whether this is the default {@link TLClassifier} of the {@link #getOwner()}.
	 */
	boolean isDefault();
	
	/**
	 * Sets the value of {@link #isDefault()}.
	 * 
	 * @see TLClassifier#isDefault()
	 */
	void setDefault(boolean b);

	@Override
	@Container
	TLEnumeration getOwner();

	@Override
	default TLType getType() {
		return getOwner();
	}

	@Override
	default void setType(TLType value) {
		throw new UnsupportedOperationException("Cannot change the type of a classifier.");
	}

	/**
	 * The index of this {@link TLClassifier} in the {@link TLEnumeration#getClassifiers()} of its
	 * {@link #getOwner()}.
	 */
	default int getIndex() {
		return getOwner().getClassifiers().indexOf(this);
	}

	@Override
	default ModelKind getModelKind() {
		return ModelKind.CLASSIFIER;
	}

	@Override
	default <R, A> R visitTypePart(TLTypePartVisitor<R, A> v, A arg) {
		return v.visitClassifier(this, arg);
	}

}
