/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.compare;

import static com.top_logic.config.xdiff.model.NodeFactory.*;

import com.top_logic.config.xdiff.model.QName;

/**
 * Names for the XML schema representing difference descriptions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface XDiffSchemaConstants {

	/**
	 * The namespace of the language.
	 */
	public static final String XDIFF_NS = "http://top-logic.com/ns/xdiff/1.0";

	/**
	 * Local name of {@link #DIFF_ELEMENT_NAME}.
	 */
	public static final String DIFF_ELEMENT = "diff";

	/**
	 * Local name of {@link #DELETE_ELEMENT_NAME}.
	 */
	public static final String DELETE_ELEMENT = "delete";

	/**
	 * Local name of {@link #INSERT_ELEMENT_NAME}.
	 */
	public static final String INSERT_ELEMENT = "insert";

	/**
	 * Local name of {@link #ATTRIBUTES_ELEMENT_NAME}.
	 */
	public static final String ATTRIBUTES_ELEMENT = "attributes";

	/**
	 * Local name of {@link #REMOVE_ELEMENT_NAME}.
	 */
	public static final String REMOVE_ELEMENT = "remove";

	/**
	 * Local name of {@link #ADD_ELEMENT_NAME}.
	 */
	public static final String ADD_ELEMENT = "set";

	/**
	 * Optional root element that signals an exchange ({@link #DELETE_ELEMENT} and
	 * {@link #ADD_ELEMENT}) of the original root node.
	 */
	public static final QName DIFF_ELEMENT_NAME = qname(XDIFF_NS, DIFF_ELEMENT);

	/**
	 * Element wrapped around a deleted subtree.
	 */
	public static final QName DELETE_ELEMENT_NAME = qname(XDIFF_NS, DELETE_ELEMENT);

	/**
	 * Element wrapped around an inserted subtree.
	 */
	public static final QName INSERT_ELEMENT_NAME = qname(XDIFF_NS, INSERT_ELEMENT);

	/**
	 * Element describing a change of attributes of its parent node.
	 * 
	 * <p>
	 * An {@link #ATTRIBUTES_ELEMENT} consists of (at most) two children elements
	 * {@link #REMOVE_ELEMENT} and {@link #ADD_ELEMENT}. If both, {@link #REMOVE_ELEMENT} and
	 * {@link #ADD_ELEMENT} are given, they must appear in exactly that order. At least one of
	 * {@link #REMOVE_ELEMENT} or {@link #ADD_ELEMENT} must occur as child of
	 * {@link #ATTRIBUTES_ELEMENT}.
	 * </p>
	 */
	public static final QName ATTRIBUTES_ELEMENT_NAME = qname(XDIFF_NS, ATTRIBUTES_ELEMENT);

	/**
	 * Child of {@link #ATTRIBUTES_ELEMENT} carrying all attributes that were removed or updated in
	 * the {@link #ATTRIBUTES_ELEMENT}'s parent node.
	 * 
	 * <p>
	 * The values of the attributes of the {@link #REMOVE_ELEMENT} contain the values from the
	 * source document.
	 * </p>
	 */
	public static final QName REMOVE_ELEMENT_NAME = qname(XDIFF_NS, REMOVE_ELEMENT);

	/**
	 * Child of {@link #ATTRIBUTES_ELEMENT} carrying all attributes that were added or updated in
	 * the {@link #ATTRIBUTES_ELEMENT}'s parent node.
	 * 
	 * <p>
	 * The values of the attributes of the {@link #ADD_ELEMENT} contain the values from the
	 * destination document.
	 * </p>
	 */
	public static final QName ADD_ELEMENT_NAME = qname(XDIFF_NS, ADD_ELEMENT);

}
