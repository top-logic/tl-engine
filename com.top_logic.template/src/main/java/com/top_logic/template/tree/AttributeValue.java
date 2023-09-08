/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.xml.TagWriter;

/**
 * A {@link TemplateNode} representing the value of an XML attribute.
 * <p>
 * When the template is expanded, the content of this {@link TemplateNode} has to be quoted with
 * {@link TagWriter#writeAttributeText(CharSequence)} or alike.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class AttributeValue extends TemplateNode {

	private final List<TemplateNode> _innerNodes;

	/**
	 * Creates a new {@link AttributeValue} with the given {@link List} of {@link TemplateNode}s as
	 * content.
	 */
	public AttributeValue(List<TemplateNode> innerNodes) {
		_innerNodes = new ArrayList<>(innerNodes);
	}

	/**
	 * Getter for the content of this {@link AttributeValue}.
	 * <p>
	 * Returns a copy of the internally stored {@link List}.
	 * </p>
	 */
	public List<TemplateNode> getContent() {
		return new ArrayList<>(_innerNodes);
	}

	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitAttributeValue(this, arg);
	}

}
