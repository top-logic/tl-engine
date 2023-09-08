/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.List;

import com.top_logic.base.administration.LoggerAdminBean;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} creating a {@link FormContext} to change log file.
 * 
 * @see ApplyLoggingConfigChanged
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeLogFileBuilder implements ModelBuilder {

	/** Name of the {@link StringField} holding the name of the current log file. */
	public static final String CURRENT_LOGGING_FILE = "currentLoggingFile";

	/** Name of the {@link SelectField} holding the name of the new log file. */
	public static final String NEW_LOGGING_FILE = "newLoggingFile";

	/**
	 * Singleton {@link ChangeLogFileBuilder} instance.
	 */
	public static final ChangeLogFileBuilder INSTANCE = new ChangeLogFileBuilder();

	private ChangeLogFileBuilder() {
		// Singleton constructor.
	}

	/**
	 * @see com.top_logic.mig.html.ModelBuilder#getModel(java.lang.Object,
	 *      com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		FormContext fc = new FormContext(aComponent);
		LoggerAdminBean logger = LoggerAdminBean.Module.INSTANCE.getImplementationInstance();

		String currentLoggingFile = logger.getConfig().getLoggingConfig();
		fc.addMember(FormFactory.newStringField(CURRENT_LOGGING_FILE, currentLoggingFile, FormFactory.IMMUTABLE));

		List<String> existingConfigFiles = logger.getExistingConfigFiles();
		SelectField newLogFile = FormFactory.newSelectField(NEW_LOGGING_FILE, existingConfigFiles);
		newLogFile.setMandatory(true);
		fc.addMember(newLogFile);
		return fc;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

}

