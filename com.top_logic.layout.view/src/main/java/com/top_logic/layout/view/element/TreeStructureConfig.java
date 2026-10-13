/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.Collection;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration of an element displaying a tree of objects: the object the tree is built from and
 * the TL-Script functions computing the children of an object and what holds an object.
 *
 * <p>
 * The functions are called with the values of the element's {@link Inputs#getInputs() inputs} as
 * leading arguments.
 * </p>
 *
 * @see TreeFunctions
 */
public interface TreeStructureConfig extends ConfigurationItem {

	/** Configuration name for {@link #getRoot()}. */
	String ROOT = "root";

	/** Configuration name for {@link #getChildren()}. */
	String CHILDREN = "children";

	/** Configuration name for {@link #getParents()}. */
	String PARENTS = "parents";

	/** Configuration name for {@link #getCanExpandAll()}. */
	String CAN_EXPAND_ALL = "canExpandAll";

	/**
	 * TL-Script function computing the root object of the tree.
	 *
	 * <p>
	 * Takes the input channel values as positional arguments and returns a single object to be
	 * used as the tree root.
	 * </p>
	 */
	@Name(ROOT)
	@Mandatory
	@NonNullable
	Expr getRoot();

	/**
	 * TL-Script function computing the children of a node.
	 *
	 * <p>
	 * Takes the input channel values followed by the parent business object as last argument.
	 * Returns a {@link Collection} of child business objects.
	 * </p>
	 */
	@Name(CHILDREN)
	@Mandatory
	@NonNullable
	Expr getChildren();

	/**
	 * Optional TL-Script function computing what holds an object in the tree.
	 *
	 * <p>
	 * Takes the input channel values followed by an object as last argument, and returns the object
	 * whose child list holds it - nothing for the object the tree is built from, and for an object
	 * belonging to no tree at all.
	 * </p>
	 *
	 * <p>
	 * It is how the node of an object written to the selection channel is found: the tree walks
	 * from the object up to the one it is built from and descends along that chain, computing only
	 * the child lists on the way. Without it the node is searched for, which computes the child list
	 * of every node passed on the way - affordable for a tree of small extent, not for a large or an
	 * unbounded one.
	 * </p>
	 */
	@Name(PARENTS)
	Expr getParents();

	/**
	 * Whether the tree supports expand-all.
	 */
	@Name(CAN_EXPAND_ALL)
	@BooleanDefault(true)
	boolean getCanExpandAll();

}
