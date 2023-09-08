/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.dataImport.UploadDataCommand;
import com.top_logic.tool.dataImport.UploadDataDialog;

/**
 * A {@link SimpleFormDialog} to upload a scripting template.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ImportScriptRecorderTemplateDialog extends UploadDataDialog {

	private final LayoutComponent _contextComponent;

	/**
	 * Creates a {@link ImportScriptRecorderTemplateDialog} for the given component.
	 */
	public ImportScriptRecorderTemplateDialog(LayoutComponent contextComponent) {
		super(I18NConstants.UPLOAD_SCRIPT_RECORDER_TEMPLATE_DIALOG_PREFIX);

		_contextComponent = Objects.requireNonNull(contextComponent);
	}

	@Override
	protected Collection<Constraint> getFieldConstraints() {
		return Arrays.asList(new ScriptRecorderTemplateConstraint());
	}

	@Override
	protected UploadDataCommand getUploadDataCommand() {
		return new ImportScriptRecorderTemplateCommand(this, (TemplateTreeComponent) _contextComponent);
	}
}
