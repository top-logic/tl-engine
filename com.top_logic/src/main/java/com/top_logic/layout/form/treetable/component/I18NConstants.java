/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * I18N constants for this package
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix STRUCTURE_EDIT_BUTTONS = legacyPrefix("tl.structureEdit.button.");

	public static ResKey CAN_NOT_REMOVE_ELEMENTS_1;

	public static ResKey CAN_NOT_REMOVE_ELEMENTS_2;

	public static ResKey NO_EXEC_ROOT_SELECTED;

	public static ResKey NO_EXEC_NODES_WITH_DIFFERENT_PARENTS_SELECTED;

	public static ResKey NO_EXEC_EMPTY_SELECTION;

	public static ResKey NO_EXEC_ALREADY_ALL_NODES_SELECTED;

	public static ResKey NOT_EXEC_ALL_SIBLINGS_ALREADY_SELECTED;

	public static ResKey NO_EXEC_NO_SELECTED_NODE_REMOVABLE;

	public static ResKey NO_EXEC_NO_MOVES_POSSIBLE;

	public static ResKey NO_EXEC_MORE_THAN_ONE_NODE_SELECTED;

	public static ResKey EXPORT_NAME;
	
	public static ResPrefix COMPARE_DIALOG;

	public static ResKey SCROLL_UPDATE;

	static {
		initConstants(I18NConstants.class);
	}
}
