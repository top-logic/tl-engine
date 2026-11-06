/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;

/**
 * This interface contains all the necessary constant definitions for the
 * {@link StructureEditComponent} component.
 *
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public interface StructureEditComponentConstants {

    /** Name of the column displaying the selection. */
    public static final String COLUMN_SELECTION = "_selection";

    /** Name of the field displaying the tree. */
    public static final String FIELD_NAME_TREE = "formTree";
    /** Name of the hidden field marking a changed tree. */
    public static final String FIELD_NAME_HIDDEN = "treeChanged";
    /** Name of the group containing the tree buttons. */
    public static final String TREE_BUTTONS_GROUP = "treeButtons";
    /** Prefix of the field names of columns. */
    public static final String PREFIX_COLUMN = "column.";
    /** Value of {@link #FIELD_NAME_HIDDEN} marking a changed tree. */
    public static final String CHANGED_VALUE = "changed";

	/** Prefix of the names of spacers between the tree buttons. */
	public static final String COMMAND_SPACER_PREFIX = "spacer_";
    /** Name of the command expanding all nodes. */
    public static final String COMMAND_EXPAND_ALL = "expandAll";
    /** Name of the command collapsing all nodes. */
    public static final String COMMAND_COLLAPSE_ALL = "collapseAll";
    /** Name of the command selecting all nodes. */
    public static final String COMMAND_SELECT_ALL = "selectAll";
    /** Name of the command deselecting all nodes. */
    public static final String COMMAND_DESELECT_ALL = "deselectAll";
    /** Name of the command selecting the siblings of the selected nodes. */
    public static final String COMMAND_SELECT_SIBLINGS = "selectSiblings";
    /** Name of the command inverting the selection. */
    public static final String COMMAND_INVERT_SELECTION = "invertSelection";
    /** Name of the command moving the selected nodes up. */
    public static final String COMMAND_MOVE_UP = "moveUp";
    /** Name of the command moving the selected nodes down. */
    public static final String COMMAND_MOVE_DOWN = "moveDown";
    /** Name of the command moving the selected nodes one level up. */
    public static final String COMMAND_MOVE_LEFT = "moveLeft";
    /** Name of the command moving the selected nodes one level down. */
    public static final String COMMAND_MOVE_RIGHT = "moveRight";
    /** Name of the command removing the selected nodes. */
    public static final String COMMAND_REMOVE = "remove";
    /** Name of the command creating a new node. */
    public static final String COMMAND_CREATE = "create";

    /** Property annotating the tree node to its form group. */
    public static final Property<Object> NODE_PROPERTY = TypedAnnotatable.property(Object.class, "treeNode");

}
