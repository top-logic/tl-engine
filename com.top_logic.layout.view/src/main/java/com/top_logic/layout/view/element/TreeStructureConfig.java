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
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * Configuration of an element displaying a tree of objects: the object the tree is built from and
 * the TL-Script functions computing the children of an object and what holds an object.
 *
 * <p>
 * The functions see the values of the element's {@link Inputs#getInputs() inputs} as variables
 * named after their channels, or by their <code>as</code> names; the functions evaluated for a node
 * see the node as the variable {@value NodeAsConfig#DEFAULT_NODE_AS}, or by the name given with
 * {@value NodeAsConfig#NODE_AS}.
 * </p>
 *
 * @see TreeFunctions
 */
public interface TreeStructureConfig extends ConfigurationItem, NodeAsConfig {

	/** Configuration name for {@link #getRoot()}. */
	String ROOT = "root";

	/** Configuration name for {@link #getChildren()}. */
	String CHILDREN = "children";

	/** Configuration name for {@link #getParents()}. */
	String PARENTS = "parents";

	/** Configuration name for {@link #getCanExpandAll()}. */
	String CAN_EXPAND_ALL = "canExpandAll";

	/**
	 * TL-Script expression computing the object the tree is built from.
	 *
	 * <p>
	 * The expression sees the values of the inputs as variables named after their channels, or by
	 * their <code>as</code> names, and returns a single object. The object itself is not displayed,
	 * its children are the top-level nodes.
	 * </p>
	 */
	@Name(ROOT)
	@Mandatory
	@NonNullable
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getRoot();

	/**
	 * TL-Script expression computing the children of a node.
	 *
	 * <p>
	 * The expression sees the values of the inputs as variables named after their channels, or by
	 * their <code>as</code> names, and the business object of the node as the variable
	 * {@value NodeAsConfig#DEFAULT_NODE_AS}, or by the name given with
	 * {@value NodeAsConfig#NODE_AS}. It returns a {@link Collection} of child business objects:
	 * </p>
	 *
	 * <pre>
	 * $node.get(`my:Folder#children`)
	 * </pre>
	 */
	@Name(CHILDREN)
	@Mandatory
	@NonNullable
	@Supplies(value = NodeAsConfig.DEFAULT_NODE_AS, renamedBy = NodeAsConfig.NODE_AS)
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getChildren();

	/**
	 * Optional TL-Script expression computing what holds an object in the tree.
	 *
	 * <p>
	 * The expression sees the values of the inputs as variables named after their channels, or by
	 * their <code>as</code> names, and the object as the variable
	 * {@value NodeAsConfig#DEFAULT_NODE_AS}, or by the name given with
	 * {@value NodeAsConfig#NODE_AS}. It returns the object whose child list holds it - nothing for
	 * the object the tree is built from, and for an object belonging to no tree at all.
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
	@Supplies(value = NodeAsConfig.DEFAULT_NODE_AS, renamedBy = NodeAsConfig.NODE_AS)
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getParents();

	/**
	 * Whether the tree supports expand-all.
	 */
	@Name(CAN_EXPAND_ALL)
	@BooleanDefault(true)
	boolean getCanExpandAll();

}
