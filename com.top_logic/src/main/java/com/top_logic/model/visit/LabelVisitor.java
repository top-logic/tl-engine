/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.FastListElementResourceProvider;
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
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.model.resources.TLPartResourceProvider;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.model.resources.TLTypeResourceProvider;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.Resources;

/**
 * {@link TLModelVisitor} resolving the {@link ResourceProvider#getLabel(Object) label} of a model
 * element.
 * 
 * @see TLPartResourceProvider#getLabel(Object)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LabelVisitor implements TLModelVisitor<String, Void> {

	/** Singleton {@link LabelVisitor} instance. */
	public static final LabelVisitor INSTANCE = new LabelVisitor();

	/**
	 * Creates a new {@link LabelVisitor}.
	 */
	protected LabelVisitor() {
		// singleton instance
	}

	@Override
	public String visitModel(TLModel model, Void arg) {
		return Resources.getInstance().getString(I18NConstants.MODEL_NAME);
	}

	@Override
	public String visitPrimitive(TLPrimitive model, Void arg) {
		return TLTypeResourceProvider.getMetaElementLabel(model);
	}

	@Override
	public String visitClass(TLClass model, Void arg) {
		return TLTypeResourceProvider.getMetaElementLabel(model);
	}

	@Override
	public String visitAssociation(TLAssociation model, Void arg) {
		return TLTypeResourceProvider.getMetaElementLabel(model);
	}

	@Override
	public String visitProperty(TLProperty model, Void arg) {
		return TLTypePartResourceProvider.getTLTypePartLabel(model);
	}

	@Override
	public String visitReference(TLReference model, Void arg) {
		return TLTypePartResourceProvider.getTLTypePartLabel(model);
	}

	@Override
	public String visitAssociationEnd(TLAssociationEnd model, Void arg) {
		return TLTypePartResourceProvider.getTLTypePartLabel(model);
	}

	@Override
	public String visitClassifier(TLClassifier model, Void arg) {
		return FastListElementResourceProvider.INSTANCE.getLabel(model);
	}

	@Override
	public String visitEnumeration(TLEnumeration model, Void arg) {
		return FastListElementResourceProvider.INSTANCE.getLabel(model);
	}

	@Override
	public String visitModule(TLModule model, Void arg) {
		return Resources.getInstance().getString(getModuleResourceKey(model), model.getName());
	}

	private ResKey getModuleResourceKey(TLModule module) {
		TLI18NKey annotation = module.getAnnotation(TLI18NKey.class);
		if (annotation != null) {
			return annotation.getValue();
		}

		return TLModelNamingConvention.getModuleLabelKey(module);
	}

}
