/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.base.administration.LoggerAdminBean;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.layout.form.component.AbstractFormCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractFormCommandHandler} applying new logging properties.
 * 
 * @see ChangeLogFileBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ApplyLoggingConfigChanged extends AbstractFormCommandHandler {

	/**
	 * Creates a new {@link ApplyLoggingConfigChanged}.
	 */
	public ApplyLoggingConfigChanged(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult applyChanges(LayoutComponent component, FormContext formContext, Object model, Map<String, Object> arguments) {
		SelectField loggingFileField = (SelectField) formContext.getField(ChangeLogFileBuilder.NEW_LOGGING_FILE);
		String newLoggingFile = (String) loggingFileField.getSingleSelection();
		try {
			LoggerAdminBean.configureWithProperties(newLoggingFile);
		} catch (IllegalArgumentException | ModuleException ex) {
			throw reportProblem(ex);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}

