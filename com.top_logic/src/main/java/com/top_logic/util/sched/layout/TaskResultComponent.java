/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import static com.top_logic.layout.form.model.FormFactory.*;
import static com.top_logic.util.sched.layout.table.results.TaskResultAccessor.*;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.layout.table.results.TaskResultAccessor;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * An {@link EditComponent} to display the information in an {@link TaskResult}.
 * <p>
 * This is an {@link EditComponent} although nothing can be edited, as it makes the implementation
 * easier. (Ask BHU for details.)
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskResultComponent extends EditComponent {

	/** Name of the {@link FormContext}. */
	public static final String NAME_FORM_CONTEXT = "formContext";

	/**
	 * Configuration of the {@link TaskResultComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditComponent.Config {

		@Override
		@NullDefault
		String getApplyCommand();

	}

	/**
	 * Creates a {@link TaskResultComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TaskResultComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		DateFormat theFormat = HTMLFormatter.getInstance().getShortDateTimeFormat();
		FormContext formContext = new FormContext(NAME_FORM_CONTEXT, getResPrefix());

		TaskResult taskResult = (TaskResult) getModel();
		if (taskResult != null) {

			Object startDate = INSTANCE.getValue(taskResult, START_DATE);
			formContext.addMember(newComplexField(START_DATE, theFormat, startDate, IMMUTABLE));

			Object endDate = INSTANCE.getValue(taskResult, END_DATE);
			formContext.addMember(newComplexField(END_DATE, theFormat, endDate, IMMUTABLE));

			Long duration = (Long) INSTANCE.getValue(taskResult, DURATION);
			formContext.addMember(newStringField(DURATION, renderInterval(duration), IMMUTABLE));

			formContext.addMember(createResultField(taskResult));

			formContext.addMember(TaskFormUtil.createExceptionField(taskResult));

			formContext.addMember(TaskFormUtil.createWarningsField(taskResult));

			formContext.addMember(TaskLogFileRenderUtil.createLogFileField(taskResult));

			ResKey messageI18N = (ResKey) INSTANCE.getValue(taskResult, MESSAGE);
			String translatedMessage = Resources.getInstance().getString(messageI18N);
			formContext.addMember(newStringField(MESSAGE, translatedMessage, IMMUTABLE));

			Object clusterNode = INSTANCE.getValue(taskResult, CLUSTER_NODE);
			formContext.addMember(newStringField(CLUSTER_NODE, clusterNode, IMMUTABLE));

		}
		return formContext;
	}

	private FormField createResultField(TaskResult taskResult) {
		Object value = TaskResultAccessor.INSTANCE.getValue(taskResult, TaskResultAccessor.RESULT);
		// Options are useful when doing scripted tests and making assertions on this field.
		List<ResultType> options = Arrays.asList(ResultType.values());
		SelectField field = newSelectField(TaskResultAccessor.RESULT, options, !MULTIPLE, IMMUTABLE);
		field.initSingleSelection(value);
		field.setControlProvider(ResultTypeControlProvider.INSTANCE);
		return field;
	}

	private CharSequence renderInterval(Long interval) {
		return (interval != null) ? DurationRenderer.getLabel(interval) : "";
	}

	static final class ResultTypeControlProvider implements ControlProvider {
	
		/**
		 * Singleton {@link TaskResultComponent.ResultTypeControlProvider} instance.
		 */
		public static final ResultTypeControlProvider INSTANCE = new ResultTypeControlProvider();
	
		private ResultTypeControlProvider() {
			// Singleton constructor.
		}
	
		@Override
		public Control createControl(Object model, String style) {
			SelectField selectField = (SelectField) model;
			ResultType singleSelection = (ResultType) selectField.getSingleSelection();
			return new SimpleConstantControl<>(singleSelection, ResultTypeRenderer.INSTANCE);
		}
	}

}
