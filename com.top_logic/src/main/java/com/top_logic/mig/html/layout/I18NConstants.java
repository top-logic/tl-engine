/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.nio.file.WatchService;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * I18N constants for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error message when instantiation of the component in the argument layout fails for
	 * configuration reasons.
	 */
	public static ResKey1 CREATING_COMPONENT_ERROR__LAYOUT;

	/**
	 * Error message if no component to edit could be found.
	 */
	public static ResKey NO_EDITABLE_COMPONENT_ERROR;

	/**
	 * Error message if no tab in the given tabbar is selected.
	 */
	public static ResKey NO_TAB_SELECTED_ERROR;

	/**
	 * Error message if no parent component could be found which is instantiated from a typed
	 * template.
	 */
	public static ResKey NO_PARENT_TEMPLATE_COMPONENT_ERROR;

	/**
	 * Error message if the parent component after deletion of all children could not be replaced
	 * with the updated content.
	 */
	public static ResKey COMPONENT_REPLACING_AFTER_DELETION_ERROR;

	/**
	 * Error message if a component deletion could not be executed because of outer component
	 * references.
	 */
	public static ResKey OUTER_REFERENCES_DELETION_ERROR;

	/**
	 * Title key for the dialog to edit an existent component.
	 */
	public static ResKey EDIT_COMPONENT_TITLE;

	/**
	 * Error message if a component is not instantiated from a typed template.
	 */
	public static ResKey NO_TEMPLATE_COMPONENT_ERROR;

	/**
	 * Error message if a component definition configuration could not be parsed.
	 */
	public static ResKey COMPONENT_DEFINITION_PARSE_ERROR;

	/**
	 * Error message if content could not be read.
	 */
	public static ResKey1 READ_CONTENT_ERROR;

	/**
	 * @en Would you like to set the form as the standard form of this type?
	 * 
	 *     <p>
	 *     Note: The standard form for a type is stored as annotation at the type, not within the
	 *     displaying component. Updating the standard form of a type updates all forms displaying
	 *     that type that does not define a specialized form.
	 *     </p>
	 */
	public static ResKey STORE_FOR_MODEL;

	/**
	 * Message to inform that a bookmark object could not be found. The arguments to resolve the
	 * bookmark object are reported.
	 * 
	 * @see I18NConstants#BOOKMARK_NOT_FOUND
	 */
	public static ResKey1 BOOKMARK_NOT_FOUND__BOOKMARKARGS;

	/**
	 * Message to inform that a bookmark object could not be found. The arguments to resolve the
	 * bookmark object are not reported.
	 * 
	 * @see I18NConstants#BOOKMARK_NOT_FOUND__BOOKMARKARGS
	 */
	public static ResKey BOOKMARK_NOT_FOUND;
	
	public static ResKey ERROR_NOT_IN_STATE = legacyKey("tl.executable.not.inState");

	public static ResKey1 ERROR_DUPLICATE_SEPARATOR__SEPARATOR;

	public static ResKey1 ERROR_UNQUALIFIED_COMPONENT_NAME__COMPONENT_NAME;

	public static ResPrefix ADD_COMPONENT_SELECT_DIALOG;

	public static ResKey NEW_TAB_LABEL;

	public static ResKey1 ERROR_CREATING_COMPONENT_CONFIGURATION__DEFINITION;

	/**
	 * Entry point for a files walk could not be accessed.
	 */
	public static ResKey WALK_STARTING_FILE_ACCESS_ERROR;

	/**
	 * Registration of the {@link WatchService} for a given directory failed.
	 */
	public static ResKey REGISTER_WATCH_SERVICE_FAILED;

	/**
	 * Title of a placeholder component to create other components.
	 */
	public static ResKey PLACEHOLDER_COMPONENT_TITLE;

	/**
	 * Message of the confirmation dialog to delete a component.
	 */
	public static ResKey1 DELETE_COMPONENT_CONFIRMATION__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
