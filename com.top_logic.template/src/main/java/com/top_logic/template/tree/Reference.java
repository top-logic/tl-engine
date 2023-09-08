/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.List;

import com.top_logic.template.model.ExpansionModel;

/**
 * Represents a reference to a variable or parameter from the {@link ExpansionModel}.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class Reference extends Expression {
	
	private final boolean      isModelRef;
	private final String       nameSpace;
	private final List<String> path;

	private final String _fullQualifiedName;

	/**
	 * Creates a new {@link Reference}.
	 */
	public Reference(boolean isModelRef, String nameSpace, List<String> path) {
		this.isModelRef = isModelRef;
		this.nameSpace = nameSpace;
		this.path = path;
		_fullQualifiedName = calcFullQualifiedName();
	}
	
	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitReference(this, arg);
	}

	/**
	 * Is this a reference to a parameter in the {@link ExpansionModel}?
	 * <p>
	 * If not, it's a reference to a variable.
	 * </p>
	 */
	public final boolean isModelRef() {
		return isModelRef;
	}

	/**
	 * Getter for the namespace of this {@link Reference}.
	 * <p>
	 * The namespace is the part of the {@link Reference} before the ':'. <br/>
	 * (Not including a possible trailing '$', which indicates whether this {@link #isModelRef() is
	 * a model reference}.)
	 * </p>
	 */
	public final String getNameSpace() {
		return nameSpace;
	}

	/**
	 * Getter for the path of this {@link Reference}.
	 * <p>
	 * The elements in the path {@link List} are sorted left to right and are separated by '.'
	 * (dots) when this {@link Reference} is written as {@link String} in a template.
	 * </p>
	 */
	public final List<String> getPath() {
		return path;
	}

	/**
	 * Returns the full qualified name of this {@link Reference}. That means: How it would be
	 * written out in a template.
	 */
	public String getFullQualifiedName() {
		return _fullQualifiedName;
	}

	private String calcFullQualifiedName() {
		StringBuilder builder = new StringBuilder();
		if (isModelRef) {
			builder.append("$");
		}
		if (nameSpace != null) {
			builder.append(nameSpace).append(":");
		}
		for (String pathElement : path) {
			builder.append(pathElement).append(".");
		}
		// -1 to cut the trailing '.'
		return builder.substring(0, builder.length() - 1);
	}

	@Override
	public String toString() {
		return _fullQualifiedName;
	}

}