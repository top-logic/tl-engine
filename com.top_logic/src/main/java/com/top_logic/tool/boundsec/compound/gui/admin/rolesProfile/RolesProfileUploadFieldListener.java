/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import static com.top_logic.basic.util.Utils.*;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.CommandModelUtilities;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;

/**
 * Listen to changes in the upload-field and enables the import button when a valid roles-profiles
 * file was uploaded.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class RolesProfileUploadFieldListener implements ValueListener {

	private final ButtonUIModel _updateCommand;

	/**
	 * Creates a {@link RolesProfileUploadFieldListener}.
	 * 
	 * @param updateCommand
	 *        Is not allowed to be null.
	 */
	public RolesProfileUploadFieldListener(ButtonUIModel updateCommand) {
		_updateCommand = requireNonNull(updateCommand);
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (newValue == null) {
			ResKey i18nKey = I18NConstants.IMPORT_ROLES_PROFILE_NOTHING_SELECTED;
			CommandModelUtilities.setNonExecutable(_updateCommand, i18nKey);
		} else {
			CommandModelUtilities.setExecutable(_updateCommand);
		}
	}

}
