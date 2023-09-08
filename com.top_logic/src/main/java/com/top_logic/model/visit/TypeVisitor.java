/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.resources.TLPartResourceProvider;

/**
 * {@link TLModelVisitor} resolving the {@link ResourceProvider#getType(Object) type} of a model
 * element.
 * 
 * @see TLPartResourceProvider#getType(Object)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeVisitor implements TLModelVisitor<String, Void> {

	/**
	 * Creates a new {@link TypeVisitor}.
	 * 
	 * @param resourceProvider
	 *        The {@link ResourceProvider} this {@link TypeVisitor} creates the type for.
	 */
	public TypeVisitor(ResourceProvider resourceProvider) {
		// nothing special here
	}

	private String simpleName(Class<?> clazz) {
		return clazz.getSimpleName();
	}

	@Override
	public String visitAssociation(TLAssociation model, Void arg) {
		return simpleName(TLAssociation.class);
	}

	@Override
	public String visitClass(TLClass model, Void arg) {
		return simpleName(TLClass.class);
	}

	@Override
	public String visitPrimitive(TLPrimitive model, Void arg) {
		return simpleName(TLPrimitive.class);
	}

	@Override
	public String visitEnumeration(TLEnumeration model, Void arg) {
		return simpleName(TLEnumeration.class);
	}

	@Override
	public String visitClassifier(TLClassifier model, Void arg) {
		return simpleName(TLClassifier.class);
	}

	@Override
	public String visitProperty(TLProperty model, Void arg) {
		return simpleName(TLProperty.class);
	}

	@Override
	public String visitReference(TLReference model, Void arg) {
		return simpleName(TLReference.class);
	}

	@Override
	public String visitAssociationEnd(TLAssociationEnd model, Void arg) {
		return simpleName(TLAssociationEnd.class);
	}

	@Override
	public String visitModel(TLModel model, Void arg) {
		return simpleName(TLModel.class);
	}

	@Override
	public String visitModule(TLModule model, Void arg) {
		return simpleName(TLModule.class);
	}

}
