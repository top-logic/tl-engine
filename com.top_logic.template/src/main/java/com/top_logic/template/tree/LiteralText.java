/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import com.top_logic.basic.tools.NameBuilder;

/**
 * A {@link TemplateNode} representing literal text outside of {@link Expression}s.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class LiteralText extends TemplateNode {
	private final String value;

	/**
	 * Creates a new {@link LiteralText}.
	 */
	public LiteralText(String value) {
		this.value = value;
	}

	/**
	 * Getter for the {@link String} value represented by this {@link LiteralText}.
	 */
	public String getValue() {
		return this.value;
	}
	
	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitLiteralText(this, arg);
	}

	@Override
	public String toString() {
		return new NameBuilder(this).add("value", value).buildName();
	}

}