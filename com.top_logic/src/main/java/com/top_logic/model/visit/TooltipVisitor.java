/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.format.CommonMark;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.resources.TLPartResourceProvider;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
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

	private LabelProvider _labelProvider;

	/**
	 * Creates a new {@link TooltipVisitor}.
	 */
	public TooltipVisitor(LabelProvider labelProvider) {
		_labelProvider = labelProvider;
	}

	@Override
	protected ResKey visitType(TLType type, Void arg) {
		return I18NConstants.TYPE_TOOLTIP.fill(
			TagUtil.encodeXML(label(type.tType())),
			TagUtil.encodeXML(label(type)),
			TagUtil.encodeXML(label(type.getModule())),

			TagUtil.encodeXML(TLModelUtil.qualifiedName(type)),

			description(TLModelNamingConvention.getTypeLabelKey(type))
		);
	}

	private String label(TLModelPart part) {
		return _labelProvider.getLabel(part);
	}

	/**
	 * Resolves the description (tool-tip resource) of the given resource key and renders it for
	 * insertion into the HTML tool-tip.
	 *
	 * <p>
	 * The description is authored as <i>CommonMark</i> text in the model editor. It is rendered to
	 * HTML for insertion into the tool-tip: This escapes contained special characters (so that a
	 * description containing e.g. {@code <} or {@code >} does not break the tool-tip) and turns
	 * blank-line-separated text into paragraphs.
	 * </p>
	 */
	private static String description(ResKey resourceKey) {
		return CommonMark.toHTML(
			Resources.getInstance().getString(resourceKey.tooltip().fallback(ResKey.text(""))));
	}

	@Override
	public ResKey visitClassifier(TLClassifier classifier, Void arg) {
		return FastListElementLabelProvider.labelKey(classifier).tooltip().fallback(ResKey.text(""));
	}

	@Override
	protected ResKey visitStructuredTypePart(TLStructuredTypePart part, Void arg) {
		return I18NConstants.TYPE_PART_TOOLTIP.fill(
			TagUtil.encodeXML(label(part.tType())),
			TagUtil.encodeXML(label(part)),
			TagUtil.encodeXML(label(part.getType())),
			TagUtil.encodeXML(TLModelUtil.qualifiedName(part)),
			description(TLModelI18N.getI18NKey(part)));
	}

	@Override
	public ResKey visitModule(TLModule module, Void arg) {
		return I18NConstants.MODULE_TOOLTIP.fill(
			TagUtil.encodeXML(label(module.tType())),
			TagUtil.encodeXML(label(module)),
			TagUtil.encodeXML(module.getName()),
			description(LabelVisitor.getModuleResourceKey(module)));
	}
}
