/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.InstancePresentation;
import com.top_logic.model.resources.TLPartResourceProvider;

/**
 * {@link TLModelVisitor} resolving the {@link ResourceProvider#getImage(Object, Flavor) image} of a
 * model element.
 * 
 * @see TLPartResourceProvider#getImage(Object, Flavor)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ImageVisitor extends DefaultTLModelVisitor<ThemeImage, Flavor> {

	/** The {@link ResourceProvider} this {@link ImageVisitor} creates an image for. */
	protected final ResourceProvider _resourceProvider;

	/**
	 * Creates a new {@link ImageVisitor}.
	 * 
	 * @param resourceProvider
	 *        see {@link #_resourceProvider}.
	 */
	public ImageVisitor(ResourceProvider resourceProvider) {
		_resourceProvider = resourceProvider;
	}

	@Override
	protected ThemeImage visitModelPart(TLModelPart model, Flavor arg) {
		return DefaultResourceProvider.getTypeImage(imageKey(model), arg);
	}

	private String imageKey(TLModelPart model) {
		return _resourceProvider.getType(model);
	}

	@Override
	public ThemeImage visitClassifier(TLClassifier model, Flavor arg) {
		InstancePresentation presentation = model.getAnnotation(InstancePresentation.class);
		if (presentation == null) {
			return ThemeImage.none();
		}
		return presentation.getIcon();
	}

	@Override
	protected ThemeImage visitStructuredTypePart(TLStructuredTypePart model, Flavor arg) {
		String suffix = structuredTypePartSuffix(model);
		return DefaultResourceProvider.getTypeImage(imageKey(model) + suffix, arg);
	}

	private String structuredTypePartSuffix(TLStructuredTypePart model) {
		return suffix(model.isMandatory(), "@mandatory");
	}
	
	@Override
	public ThemeImage visitAssociationEnd(TLAssociationEnd model, Flavor arg) {
		String suffix = endSuffix(model);
		return DefaultResourceProvider.getTypeImage(imageKey(model) + suffix, arg);
	}

	@Override
	public ThemeImage visitReference(TLReference model, Flavor arg) {
		String suffix = endSuffix(model.getEnd());
		return DefaultResourceProvider.getTypeImage(imageKey(model) + suffix, arg);
	}

	private String endSuffix(TLAssociationEnd model) {
		boolean containment = model.isComposite();
		boolean multiple = model.isMultiple();
		return suffix(containment, "@containment") + structuredTypePartSuffix(model)
			+ suffix(multiple, "@multiple");
	}

	/**
	 * Constructs a suffix based on a boolean property.
	 */
	protected static String suffix(boolean option, String suffix) {
		return option ? suffix : "";
	}

}
