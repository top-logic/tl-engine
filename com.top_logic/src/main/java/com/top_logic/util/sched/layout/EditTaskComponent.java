/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import static com.top_logic.layout.form.model.FormFactory.*;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.layout.block.BlockState;
import com.top_logic.util.sched.layout.block.BlockStateRenderer;
import com.top_logic.util.sched.layout.table.TaskAccessor;
import com.top_logic.util.sched.layout.table.results.TaskResultAccessor;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.schedule.AbstractSchedulingAlgorithm;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * Edit tasks in <i>TopLogic</i>. 
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&aumlnsler</a>
 */
public class EditTaskComponent extends EditComponent {

	/** {@link TypedConfiguration} interface for the {@link EditTaskComponent}. */
	public interface Config extends EditComponent.Config {

		@Override
		@NullDefault
		String getApplyCommand();
	}

	/** The parameter for the task type. */
    public static final String TASK_TYPE = "taskType";

	/** Name of the {@link FormGroup} for basic task information. */
	public static final String NAME_GROUP_BASIC = "basic";

	/** Name of the {@link FormGroup} for the {@link SchedulingAlgorithm} visual border. */
	public static final String NAME_GROUP_SCHEDULE_BORDER = "schedule";

	/** Name of the {@link FormGroup} for the {@link SchedulingAlgorithm} visual content. */
	public static final String NAME_GROUP_SCHEDULE_CONTENT = "schedulingAlgorithm";
	
	/** Name of the {@link FormGroup} for the state information. Example: What was the last result? */
	public static final String NAME_GROUP_STATE = "state";

    /**
     * Constructor with attributes out of the configuration.
     * 
     * @param someAtts       the attributes out of the configuration
     */
    public EditTaskComponent(InstantiationContext context, Config someAtts) throws ConfigurationException {
        super(context, someAtts);
    }
    
    /**
     * Returns true if <code>anObject</code> is an instance of <code>Task</code>.
     * 
     * @param    anObject    The object to test
     * @return   <b>true</b> if <code>anObject</code> is an instance of {@link Task}
     */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject == null || (anObject instanceof Task);
    }

    @Override
	public FormContext createFormContext() {
		ResPrefix theResPrefix = this.getResPrefix();
        FormContext theContext   = new FormContext("fields", theResPrefix);

		Task theModel = (Task) this.getModel();
        
        if (theModel != null) {
        	DateFormat        theFormat = HTMLFormatter.getInstance().getShortDateTimeFormat();
        	FormGroup         theGroup   = new FormGroup(NAME_GROUP_BASIC, theResPrefix);
        	TaskResult currentResult = theModel.getLog().getCurrentResult();
			FormGroup stateGroup = new FormGroup(NAME_GROUP_STATE, getResPrefix());
        	
        	theGroup.addMember(newStringField(TaskAccessor.NAME, TaskAccessor.INSTANCE.getValue(theModel, TaskAccessor.NAME), IMMUTABLE));
        	stateGroup.addMember(newComplexField(TaskAccessor.LAST_SCHEDULE, theFormat, TaskAccessor.INSTANCE.getValue(theModel, TaskAccessor.LAST_SCHEDULE), IMMUTABLE));
        	stateGroup.addMember(newComplexField(TaskAccessor.NEXT_SCHEDULE, theFormat, TaskAccessor.INSTANCE.getValue(theModel, TaskAccessor.NEXT_SCHEDULE), IMMUTABLE));
        	stateGroup.addMember(newComplexField(TaskResultAccessor.START_DATE, theFormat, TaskResultAccessor.INSTANCE.getValue(currentResult, TaskResultAccessor.START_DATE), IMMUTABLE));
        	stateGroup.addMember(newComplexField(TaskResultAccessor.END_DATE, theFormat, TaskResultAccessor.INSTANCE.getValue(currentResult, TaskResultAccessor.END_DATE), IMMUTABLE));
        	stateGroup.addMember(newStringField(TaskResultAccessor.DURATION, renderInterval((Long)TaskResultAccessor.INSTANCE.getValue(currentResult, TaskResultAccessor.DURATION)), IMMUTABLE));
        	stateGroup.addMember(createCombinedStateField(theModel));
			String clusterLock = (String) TaskAccessor.INSTANCE.getValue(theModel, TaskAccessor.CLUSTER_LOCK);
			stateGroup.addMember(newStringField(TaskAccessor.CLUSTER_LOCK, clusterLock, IMMUTABLE));
			theGroup.addMember(createBlockingStateField(theModel));
			theGroup.addMember(createBooleanField(TaskAccessor.IS_BLOCKING_ALLOWED, theModel));
			stateGroup.addMember(TaskFormUtil.createExceptionField(currentResult));
        	theGroup.addMember(newStringField(TaskAccessor.CLASS_NAME, TaskAccessor.INSTANCE.getValue(theModel, TaskAccessor.CLASS_NAME), IMMUTABLE));
			ResKey theMessage = (ResKey) TaskResultAccessor.INSTANCE.getValue(currentResult, TaskResultAccessor.MESSAGE);
			stateGroup.addMember(newStringField(TaskResultAccessor.MESSAGE, Resources.getInstance().getString(theMessage), IMMUTABLE));
			stateGroup.addMember(TaskLogFileRenderUtil.createLogFileField(currentResult));
			theGroup.addMember(createBooleanField(TaskAccessor.IS_ENABLED, theModel));
			theGroup.addMember(createBooleanField(TaskAccessor.PERSISTENT, theModel));
			theGroup.addMember(createBooleanField(TaskAccessor.PER_NODE, theModel));
			theGroup.addMember(createBooleanField(TaskAccessor.RUN_ON_STARTUP, theModel));
			theGroup.addMember(createBooleanField(TaskAccessor.NEEDS_MAINTENANCE_MODE, theModel));
        	
        	theContext.addMember(theGroup);
			theContext.addMember(createScheduleGroup(theModel.getSchedulingAlgorithm()));
			theContext.addMember(stateGroup);
        }

        return (theContext);   
    }

	/**
	 * Creates the {@link FormGroup} containing the information about the
	 * {@link SchedulingAlgorithm} of the {@link Task}.
	 */
	protected FormGroup createScheduleGroup(SchedulingAlgorithm schedule) {
		FormGroup content = AbstractSchedulingAlgorithm.createUI(schedule, NAME_GROUP_SCHEDULE_CONTENT);
		return createSingletonFormGroup(content, NAME_GROUP_SCHEDULE_BORDER, getResPrefix());
	}

	/** Util for creating a {@link FormGroup} containing only the given {@link FormMember}. */
	protected static FormGroup createSingletonFormGroup(FormMember content, String name, ResPrefix i18nPrefix) {
		FormGroup scheduleGroup = new FormGroup(name, i18nPrefix);
		scheduleGroup.addMember(content);
		return scheduleGroup;
	}

	private FormField createBooleanField(String name, Task task) {
		Object value = TaskAccessor.INSTANCE.getValue(task, name);
		return newBooleanField(name, value, IMMUTABLE);
	}

	private FormField createCombinedStateField(Task task) {
		Object value = TaskAccessor.INSTANCE.getValue(task, TaskAccessor.COMBINED_STATE);
		// Options are necessary when doing scripted tests and making assertions on this field.
		List<Object> options = new ArrayList<>(Arrays.asList(ResultType.values()));
		options.addAll(Arrays.asList(TaskState.values()));
		SelectField field = newSelectField(TaskAccessor.COMBINED_STATE, options, !MULTIPLE, IMMUTABLE);
		field.initSingleSelection(value);
		field.setControlProvider(createCombinedStateRendererControlProvider());
		return field;
	}

	private static ControlProvider createCombinedStateRendererControlProvider() {
		return new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				SelectField selectField = (SelectField) model;
				Object singleSelection = selectField.getSingleSelection();
				return new SimpleConstantControl<>(singleSelection, new CombinedStateRenderer());
			}
		};
	}

	private FormField createBlockingStateField(Task task) {
		Object value = TaskAccessor.INSTANCE.getValue(task, TaskAccessor.IS_BLOCKED);
		// Options are necessary when doing scripted tests and making assertions on this field.
		List<BlockState> options = Arrays.asList(BlockState.values());
		SelectField field = newSelectField(TaskAccessor.IS_BLOCKED, options, !MULTIPLE, IMMUTABLE);
		field.initSingleSelection(value);
		field.setControlProvider(BlockingStateRendererControlProvider.INSTANCE);
		return field;
	}

	private CharSequence renderInterval(Long interval) {
		return (interval != null) ? DurationRenderer.getLabel(interval) : "";
	}    

    private static final class BlockingStateRendererControlProvider implements ControlProvider {
		/**
		 * Singleton {@link EditTaskComponent.BlockingStateRendererControlProvider} instance.
		 */
		public static final BlockingStateRendererControlProvider INSTANCE = new BlockingStateRendererControlProvider();

		private BlockingStateRendererControlProvider() {
			// Singleton constructor.
		}

		@Override
		public Control createControl(Object model, String style) {
			SelectField selectField = (SelectField) model;
			BlockState singleSelection = (BlockState) selectField.getSingleSelection();
			return new SimpleConstantControl<>(singleSelection, BlockStateRenderer.INSTANCE);
		}
	}

}
