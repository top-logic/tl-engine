/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import static com.top_logic.layout.form.model.FormFactory.*;

import java.util.List;

import com.lowagie.text.html.HtmlEncoder;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.layout.table.results.TaskResultAccessor;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * Utilities for creating {@link FormField}s for certain {@link Task} or {@link TaskResult}
 * information.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskFormUtil {

	public static FormField createExceptionField(TaskResult taskResult) {
		Object value = TaskResultAccessor.INSTANCE.getValue(taskResult, TaskResultAccessor.EXCEPTION);
		StringField field = FormFactory.newStringField(TaskResultAccessor.EXCEPTION, value, IMMUTABLE);
		field.setControlProvider(ExceptionRendererControlProvider.INSTANCE);
		return field;
	}

	public static FormMember createWarningsField(TaskResult taskResult) {
		List<String> warnings = taskResult.getWarnings();
		boolean value = !CollectionUtil.isEmptyOrNull(warnings);
		BooleanField field = newBooleanField(TaskResultAccessor.WARNINGS, value, IMMUTABLE);
		field.setTooltip(createWarningsTooltipText(warnings));
		field.setTooltipCaption(createWarningsTooltipCaption(warnings));
		field.setControlProvider(WarningsRendererControlProvider.INSTANCE);
		return field;
	}

	public static String createWarningsTooltipText(List<String> warnings) {
		StringBuilder tooltipTextBuilder = new StringBuilder();
		tooltipTextBuilder.append("<ol>");
		for (String warning : warnings) {
			tooltipTextBuilder.append("<li>");
			tooltipTextBuilder.append(HtmlEncoder.encode(warning));
			tooltipTextBuilder.append("</li>");
		}
		tooltipTextBuilder.append("</ol>");
		return tooltipTextBuilder.toString();
	}

	public static String createWarningsTooltipCaption(List<String> warnings) {
		if (warnings.size() == 1) {
			return Resources.getInstance().getMessage(I18NConstants.TOOLTIP_CAPTION_WARNINGS_SINGULAR, warnings.size());
		} else {
			return Resources.getInstance().getMessage(I18NConstants.TOOLTIP_CAPTION_WARNINGS_PLURAL, warnings.size());
		}
	}

	static final class WarningsRendererControlProvider implements ControlProvider {
		/**
		 * Singleton {@link TaskFormUtil.WarningsRendererControlProvider} instance.
		 */
		public static final WarningsRendererControlProvider INSTANCE = new WarningsRendererControlProvider();
	
		private WarningsRendererControlProvider() {
			// Singleton constructor.
		}
	
		@Override
		public Control createControl(Object model, String style) {
			return new SimpleConstantControl<>((BooleanField) model, WarningsFieldRenderer.INSTANCE);
		}
	}

	static final class ExceptionRendererControlProvider implements ControlProvider {
	
		/**
		 * Singleton {@link TaskFormUtil.ExceptionRendererControlProvider} instance.
		 */
		public static final ExceptionRendererControlProvider INSTANCE = new ExceptionRendererControlProvider();
	
		private ExceptionRendererControlProvider() {
			// Singleton constructor.
		}
	
		@Override
		public Control createControl(Object model, String style) {
			StringField field = (StringField) model;
			String value = (String) field.getValue();
			return new SimpleConstantControl<>(value, ExceptionRenderer.INSTANCE);
		}
	}

}
