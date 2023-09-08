/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import java.util.Collection;
import java.util.HashSet;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.constraints.FilenameEndConstraint;
import com.top_logic.layout.messagebox.SimpleFormDialog;

/**
 * A {@link SimpleFormDialog} to upload a zip file.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class UploadZipDialog extends UploadDataDialog {

	/**
	 * Creates a {@link UploadZipDialog} for the given component.
	 */
	public UploadZipDialog(ResPrefix zipUploadDialogPrefix) {
		super(zipUploadDialogPrefix);
	}

	@Override
	protected Collection<Constraint> getFieldConstraints() {
		HashSet<Constraint> fieldConstraints = new HashSet<>();

		fieldConstraints.add(new FilenameEndConstraint(FileUtilities.ZIP_FILE_ENDING, I18NConstants.ZIP_ENDING_ERROR));

		return fieldConstraints;
	}

}
