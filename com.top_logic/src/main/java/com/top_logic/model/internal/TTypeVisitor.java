/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.internal;

import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.impl.generated.TlModelFactory;

/**
 * {@link TLModelVisitor} determining the {@link TLObject#tType()} of a {@link TLModelPart}.
 * 
 * <p>
 * 
 * Argument is expected to be the {@link TLModule} {@link TlModelFactory#TL_MODEL_STRUCTURE}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TTypeVisitor implements TLModelVisitor<TLStructuredType, TLModule> {

	/** Singleton {@link TTypeVisitor} instance. */
	public static final TTypeVisitor INSTANCE = new TTypeVisitor();

	/**
	 * Creates a new {@link TTypeVisitor}.
	 */
	protected TTypeVisitor() {
		// singleton instance
	}

	@Override
	public TLStructuredType visitPrimitive(TLPrimitive model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLPrimitive.TL_PRIMITIVE_TYPE);
	}

	@Override
	public TLStructuredType visitEnumeration(TLEnumeration model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLEnumeration.TL_ENUMERATION_TYPE);
	}

	@Override
	public TLStructuredType visitClass(TLClass model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLClass.TL_CLASS_TYPE);
	}

	@Override
	public TLStructuredType visitAssociation(TLAssociation model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLAssociation.TL_ASSOCIATION_TYPE);
	}

	@Override
	public TLStructuredType visitClassifier(TLClassifier model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLClassifier.TL_CLASSIFIER_TYPE);
	}

	@Override
	public TLStructuredType visitProperty(TLProperty model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLProperty.TL_PROPERTY_TYPE);
	}

	@Override
	public TLStructuredType visitReference(TLReference model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLReference.TL_REFERENCE_TYPE);
	}

	@Override
	public TLStructuredType visitAssociationEnd(TLAssociationEnd model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLAssociationEnd.TL_ASSOCIATION_END_TYPE);
	}

	@Override
	public TLStructuredType visitModel(TLModel model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLModel.TL_MODEL_TYPE);
	}

	@Override
	public TLStructuredType visitModule(TLModule model, TLModule arg) {
		return (TLStructuredType) arg.getType(TLModule.TL_MODULE_TYPE);
	}

}

