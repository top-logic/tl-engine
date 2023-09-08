/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme.upload;

import java.util.Collection;
import java.util.Objects;

import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.dataImport.UploadDataCommand;
import com.top_logic.tool.dataImport.UploadZipDialog;

/**
 * A dialog to upload a ZIP file containing a theme.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeUploadDialog extends UploadZipDialog {

	/**
	 * Context for import post actions.
	 */
	private final LayoutComponent _contextComponent;

	/**
	 * Creates a {@link ThemeUploadDialog} for the given component.
	 */
	public ThemeUploadDialog(LayoutComponent contextComponent) {
		super(I18NConstants.UPLOAD_THEME_DIALOG_PREFIX);

		_contextComponent = Objects.requireNonNull(contextComponent);
	}

	@Override
	protected Collection<Constraint> getFieldConstraints() {
		Collection<Constraint> fieldConstraints = super.getFieldConstraints();

		fieldConstraints.add(ThemeFileStructureContraint.INSTANCE);

		return fieldConstraints;
	}

	@Override
	protected UploadDataCommand getUploadDataCommand() {
		return new ImportUploadedThemeCommand(this, (TableComponent) _contextComponent);
	}

}
