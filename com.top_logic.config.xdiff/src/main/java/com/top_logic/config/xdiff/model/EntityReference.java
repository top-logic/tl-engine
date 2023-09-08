/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

/**
 * Entity reference {@link Node}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EntityReference extends Node {

	private String _name;

	EntityReference(String name) {
		_name = name;
	}
	
	/**
	 * Entity name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the {@link #getName()} property.
	 */
	public void setName(String name) {
		_name = name;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.ENTITY_REFERENCE;
	}

	@Override
	public <R, A> R visit(NodeVisitor<R, A> v, A arg) {
		return v.visitEntityReference(this, arg);
	}

}
