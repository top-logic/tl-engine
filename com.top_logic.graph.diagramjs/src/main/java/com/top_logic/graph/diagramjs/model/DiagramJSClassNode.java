/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.model;

import java.util.Collection;
import java.util.List;

import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSLabel;

/**
 * A diagramjs class node in a {@link GraphModel}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface DiagramJSClassNode extends Node {

	/**
	 * Name of {@link #getClassName()} property.
	 */
	String NAME = "name";

	/**
	 * Name of {@link #getClassModifiers()} property.
	 */
	String MODIFIERS = "modifiers";

	/**
	 * Name of {@link #getClassProperties()} property.
	 */
	String PROPERTIES = "properties";

	/**
	 * Name of {@link #getStereotypes()} property.
	 */
	String STEREOTYPES = "stereotypes";

	/**
	 * Name of {@link #isImported()} property.
	 */
	String IMPORTED = "imported";

	/**
	 * Name of this class object.
	 */
	String getClassName();

	/**
	 * @see #getClassName()
	 */
	void setClassName(String name);

	/**
	 * Annotated class modifiers. Only abstract is supported.
	 */
	List<String> getClassModifiers();

	/**
	 * @see #getClassModifiers()
	 */
	void setClassModifiers(List<String> modifiers);

	/**
	 * Properties of this class object.
	 */
	Collection<? extends DefaultDiagramJSLabel> getClassProperties();

	/**
	 * Stereotypes of this class object.
	 */
	List<String> getStereotypes();

	/**
	 * @see #getStereotypes()
	 */
	void setStereotypes(List<String> stereotypes);

	/**
	 * True if the class given by this node is imported i.e. defined outside this model.
	 */
	boolean isImported();

	/**
	 * @see #isImported()
	 */
	void setImported(boolean isImported);

}
