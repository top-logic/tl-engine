/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;

/**
 * {@link TLModelVisitor} that provides fall-back methods along the type
 * hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTLModelVisitor<R, A> implements TLModelVisitor<R, A> {

	/**
	 * Sole instance of {@link Void}.
	 */
	protected static final Void none = null;

	@Override
	public R visitModel(TLModel model, A arg) {
		return visitModelPart(model, arg);
	}

	@Override
	public R visitModule(TLModule model, A arg) {
		return visitNamedPart(model, arg);
	}

	@Override
	public R visitAssociation(TLAssociation model, A arg) {
		return visitStructuredType(model, arg);
	}

	@Override
	public R visitClass(TLClass model, A arg) {
		return visitStructuredType(model, arg);
	}

	@Override
	public R visitPrimitive(TLPrimitive model, A arg) {
		return visitType(model, arg);
	}

	@Override
	public R visitReference(TLReference model, A arg) {
		return visitClassPart(model, arg);
	}

	@Override
	public R visitAssociationEnd(TLAssociationEnd model, A arg) {
		return visitAssociationPart(model, arg);
	}

	@Override
	public R visitProperty(TLProperty model, A arg) {
		return visitStructuredTypePart(model, arg);
	}

	@Override
	public R visitEnumeration(TLEnumeration model, A arg) {
		return visitType(model, arg);
	}
	
	@Override
	public R visitClassifier(TLClassifier model, A arg) {
		return visitTypePart(model, arg);
	}
	
	/**
	 * Visit case for all {@link TLClassPart} sub-interfaces.
	 */
	protected R visitClassPart(TLClassPart model, A arg) {
		return visitStructuredTypePart(model, arg);
	}

	/**
	 * Visit case for all {@link TLStructuredTypePart} sub-interfaces.
	 */
	protected R visitStructuredTypePart(TLStructuredTypePart model, A arg) {
		return visitTypePart(model, arg);
	}

	/**
	 * Visit case for all {@link TLStructuredType} sub-interfaces.
	 */
	protected R visitStructuredType(TLStructuredType model, A arg) {
		return visitType(model, arg);
	}

	/**
	 * Visit case for all {@link TLAssociationPart} sub-interfaces.
	 */
	protected R visitAssociationPart(TLAssociationPart model, A arg) {
		return visitTypePart(model, arg);
	}

	/**
	 * Visit case for all {@link TLTypePart} sub-interfaces.
	 */
	protected R visitTypePart(TLTypePart model, A arg) {
		return visitNamedPart(model, arg);
	}

	/**
	 * Visit case for all {@link TLNamedPart} sub-interfaces.
	 */
	protected R visitNamedPart(TLNamedPart model, A arg) {
		return visitModelPart(model, arg);
	}

	/**
	 * Visit case for all {@link TLType} sub-interfaces.
	 */
	protected R visitType(TLType model, A arg) {
		return visitNamedPart(model, arg);
	}

	/**
	 * Visit case for all {@link TLModelPart} sub-interfaces.
	 */
	protected R visitModelPart(TLModelPart model, A arg) {
		// Hook for sub classes.
		return null;
	}
	
}
