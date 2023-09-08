/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.resources.TLPartResourceProvider;
import com.top_logic.util.Resources;

/**
 * {@link TLModelVisitor} resolving the {@link ResourceProvider#getTooltip(Object) tooltip} of a
 * model element.
 * 
 * @see TLPartResourceProvider#getTooltip(Object)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TooltipVisitor extends DefaultTLModelVisitor<ResKey, Void> {

	/** The {@link ResourceProvider} this {@link TooltipVisitor} creates a tooltip for. */
	protected final ResourceProvider _resourceProvider;

	/**
	 * Creates a new {@link TooltipVisitor}.
	 * 
	 * @param resourceProvider
	 *        see {@link #_resourceProvider}
	 */
	public TooltipVisitor(ResourceProvider resourceProvider) {
		_resourceProvider = resourceProvider;
	}

	@Override
	protected ResKey visitType(TLType model, Void arg) {
		return I18NConstants.TYPE_TOOLTIP.fill(
			TagUtil.encodeXML(getLabel(model)),
			TagUtil.encodeXML(getTypeName(model)),
			TagUtil.encodeXML(getLabel(model.getModule())),
			TagUtil.encodeXML(model.getName()));
	}

	private String getLabel(TLModelPart model) {
		return _resourceProvider.getLabel(model);
	}

	private String getTypeName(TLModelPart model) {
		String modelType = _resourceProvider.getType(model);
		return Resources.getInstance().getString(I18NConstants.TYPENAME.key(modelType));
	}

	@Override
	public ResKey visitClassifier(TLClassifier model, Void arg) {
		ResKey labelKey = FastListElementLabelProvider.labelKey(model);
		return labelKey.tooltip();
	}

	@Override
	protected ResKey visitTypePart(TLTypePart model, Void arg) {
		return I18NConstants.TYPE_PART_TOOLTIP.fill(
			TagUtil.encodeXML(MetaLabelProvider.INSTANCE.getLabel(model)),
			TagUtil.encodeXML(getTypeName(model)),
			TagUtil.encodeXML(getLabel(model.getOwner())),
			TagUtil.encodeXML(getLabel(model.getType())),
			TagUtil.encodeXML(model.getName()));
	}

	@Override
	public ResKey visitModule(TLModule model, Void arg) {
		return I18NConstants.MODULE_TOOLTIP.fill(
			TagUtil.encodeXML(getLabel(model)),
			TagUtil.encodeXML(getTypeName(model)),
			TagUtil.encodeXML(model.getName()));
	}
}
