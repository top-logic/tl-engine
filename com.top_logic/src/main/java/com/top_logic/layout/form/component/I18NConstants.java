/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.base.services.simpleajax.JavaScriptResKey;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants used in JavaScript control handlers.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey BUTTON_ACTIVATION_UPDATE;

	@JavaScriptResKey
	public static ResKey1 FIELD_CHARACTERS_LEFT__SPACE;

	@JavaScriptResKey
	public static ResKey FIELD_FULL;

	@JavaScriptResKey
	public static ResKey1 FIELD_OVERFULL__DELETE;

	public static ResKey WARNING_CONFIRM_TITLE;

	public static ResKey WARNING_CONFIRM_HEADING;

	public static ResKey WARNING_CONFIRM_MESSAGE;

	public static ResKey WARNING_CONFIRM_ACTION;

	@JavaScriptResKey
	public static ResKey NO_BOOKMARK_SUPPORT;
	
	public static ResKey APPLY = legacyKey("tl.command.apply");

	public static ResKey CANCEL = legacyKey("tl.command.cancel");

	public static ResKey DELETE = legacyKey("tl.command.delete");

	public static ResKey DISCARD = legacyKey("tl.command.discard");

	/**
	 * @en Reload
	 * @tooltip Resets the view and reloads it from underlying data.
	 */
	@CalledByReflection
	public static ResKey INVALIDATE;

	public static ResKey NEW = legacyKey("tl.command.new");

	public static ResKey SAVE = legacyKey("tl.command.save");

	public static ResKey SWITCH_TO_EDIT = legacyKey("tl.command.switchToEdit");

	public static ResKey ERROR_DELETE_NOT_POSSIBLE;

	public static ResKey CANNOT_KEEP_EDIT_MODE;

	public static ResKey ERROR_CANNOT_LOCK_OBJECT;

	public static ResKey ERROR_CONCURRENT_EDIT;

	public static ResKey1 ERROR_CONCURRENT_EDIT__USER;

	public static ResKey ERROR_VIEW_CREATION;

	public static ResKey1 ERROR_APPLY_FAILED__MSG;

	public static ResKey ERROR_MODEL_NOT_SUPPORTED;

	public static ResKey ERROR_NO_MODEL;

	public static ResKey ERROR_INPUT_VALIDATION_FAILED;

	public static ResKey ERROR_COMMIT_FAILED;

	public static ResKey ERROR_CANNOT_DELETE;

	public static ResKey ERROR_CANNOT_SAVE_TITLE;

	public static ResKey ERROR_CANNOT_SAVE_MESSAGE;

	public static ResKey ERROR_CANNOT_EDIT;

	public static ResKey ERROR_COMPONENT_CHANNEL_EXPECTED;

	static {
		initConstants(I18NConstants.class);
	}
}
