/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.List;

/**
 * A template is a {@link TemplateNode} that exists for grouping other {@link TemplateNode}s and
 * introducing a new scope.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class Template extends TemplateNode {

	private final List<TemplateNode> nodes;

	/**
	 * Creates a new {@link Template}.
	 */
	public Template(List<TemplateNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitTemplate(this, arg);
	}

	/**
	 * Getter for the {@link List} of {@link TemplateNode}s grouped by this {@link Template}.
	 * <p>
	 * <b>Returns the internal {@link List}. Don't change it!</b>
	 * </p>
	 */
	public final List<TemplateNode> getNodes() {
		return nodes;
	}

}