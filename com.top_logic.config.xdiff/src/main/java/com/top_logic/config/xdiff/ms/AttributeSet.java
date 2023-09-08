/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import com.top_logic.config.xdiff.util.Utils;


/**
 * Implementation of the {@link MSXDiffSchema#ATTRIBUTE_SET_ELEMENT}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeSet extends MSXDiff {

	private final String _elementXpath;

	private final String _attributeName;

	private final String _attributeValue;

	/**
	 * Creates a {@link AttributeSet}.
	 * 
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param elementXpath
	 *        See {@link #getElementXpath()}
	 * @param attributeName
	 *        See {@link #getAttributeName()}.
	 * @param attributeValue
	 *        See {@link #getAttributeValue()}.
	 */
	public AttributeSet(String component, String elementXpath, String attributeName, String attributeValue) {
		super(component);

		Utils.checkRequiredAttribute(MSXDiffSchema.ATTRIBUTE_SET_ELEMENT, MSXDiffSchema.ATTRIBUTE_SET__XPATH_ATTRIBUTE,
			elementXpath);

		Utils.checkRequiredAttribute(MSXDiffSchema.ATTRIBUTE_SET_ELEMENT, MSXDiffSchema.ATTRIBUTE_SET__NAME_ATTRIBUTE,
			attributeName);

		Utils.checkRequiredAttribute(MSXDiffSchema.ATTRIBUTE_SET_ELEMENT, MSXDiffSchema.ATTRIBUTE_SET__VALUE_ATTRIBUTE,
			attributeValue);

		_elementXpath = elementXpath;
		_attributeName = attributeName;
		_attributeValue = attributeValue;
	}

	@Override
	public ArtifactType getType() {
		return ArtifactType.ATTRIBUTE_SET;
	}

	/**
	 * Reference to the element witch is modified.
	 */
	public String getElementXpath() {
		return _elementXpath;
	}

	/**
	 * The name of the attribute to set.
	 */
	public String getAttributeName() {
		return _attributeName;
	}

	/**
	 * The value of the attribute to set.
	 */
	public String getAttributeValue() {
		return _attributeValue;
	}

	@Override
	public <R, A> R visit(MSXDiffVisitor<R, A> v, A arg) {
		return v.visitAttributeSet(this, arg);
	}

}
