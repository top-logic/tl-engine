/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.util.List;

/**
 * {@link StartTagTemplate} that is later on enhanced to a special rendering template.
 * 
 * @see ForeachTemplate
 * @see ConditionalTemplate
 */
public class SpecialStartTag extends StartTagTemplate {

	private final StartTagTemplate _inner;

	private final TemplateEnhancer _builder;

	/**
	 * Creates a {@link SpecialStartTag}.
	 */
	public SpecialStartTag(StartTagTemplate inner, TemplateEnhancer builder) {
		super(inner.getLine(), inner.getColumn(), inner.getName());
		_inner = inner;
		_builder = builder;
	}

	/**
	 * The {@link TemplateEnhancer} to create a functional tag out of the annotated tag.
	 */
	public TemplateEnhancer getBuilder() {
		return _builder;
	}

	/**
	 * The tag that should be rendered conditionally.
	 */
	public StartTagTemplate getInner() {
		return _inner;
	}

	@Override
	public void addAttribute(TagAttributeTemplate attribute) {
		_inner.addAttribute(attribute);
	}

	@Override
	public List<TagAttributeTemplate> getAttributes() {
		return _inner.getAttributes();
	}

	@Override
	public boolean isEmpty() {
		return _inner.isEmpty();
	}

	@Override
	public void setEmpty(boolean isEmpty) {
		_inner.setEmpty(isEmpty);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}
