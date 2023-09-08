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

    public static final String COLUMN_SELECTION = "_selection";

    public static final String FIELD_NAME_TREE = "formTree";
    public static final String FIELD_NAME_HIDDEN = "treeChanged";
    public static final String TREE_BUTTONS_GROUP = "treeButtons";
    public static final String PREFIX_COLUMN = "column.";
    public static final String CHANGED_VALUE = "changed";

	public static final String COMMAND_SPACER_PREFIX = "spacer_";
    public static final String COMMAND_EXPAND_ALL = "expandAll";
    public static final String COMMAND_COLLAPSE_ALL = "collapseAll";
    public static final String COMMAND_SELECT_ALL = "selectAll";
    public static final String COMMAND_DESELECT_ALL = "deselectAll";
    public static final String COMMAND_SELECT_SIBLINGS = "selectSiblings";
    public static final String COMMAND_INVERT_SELECTION = "invertSelection";
    public static final String COMMAND_MOVE_UP = "moveUp";
    public static final String COMMAND_MOVE_DOWN = "moveDown";
    public static final String COMMAND_MOVE_LEFT = "moveLeft";
    public static final String COMMAND_MOVE_RIGHT = "moveRight";
    public static final String COMMAND_REMOVE = "remove";
    public static final String COMMAND_CREATE = "create";

    public static final Property<Object> NODE_PROPERTY = TypedAnnotatable.property(Object.class, "treeNode");

}
