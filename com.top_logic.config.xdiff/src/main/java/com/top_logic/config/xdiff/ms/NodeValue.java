/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import com.top_logic.basic.StringServices;
import com.top_logic.config.xdiff.util.Utils;

/**
 * Implementation of {@link MSXDiffSchema#NODE_VALUE_ELEMENT}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeValue extends MSXDiff {

	private final String _elementXpath;

	private final String _newText;

	/**
	 * Creates a {@link NodeValue}.
	 * 
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param elementXpath
	 *        See {@link #getElementXpath()}.
	 * @param newText
	 *        See {@link #getNewText()}.
	 */
	public NodeValue(String component, String elementXpath, String newText) {
		super(component);

		Utils.checkRequiredAttribute(MSXDiffSchema.NODE_VALUE_ELEMENT, MSXDiffSchema.NODE_VALUE__XPATH_ATTRIBUTE,
			elementXpath);

		_elementXpath = elementXpath;
		_newText = StringServices.nonNull(newText);
	}

	@Override
	public ArtifactType getType() {
		return ArtifactType.NODE_VALUE;
	}

	/**
	 * Reference to the element to modify.
	 */
	public String getElementXpath() {
		return _elementXpath;
	}

	/**
	 * New text contents of the referenced element.
	 */
	public String getNewText() {
		return _newText;
	}

	@Override
	public <R, A> R visit(MSXDiffVisitor<R, A> v, A arg) {
		return v.visitNodeValue(this, arg);
	}

}
