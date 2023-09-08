/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.thread;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.ValueDisplayControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.provider.DateTimeLabelProvider;
import com.top_logic.layout.renderers.StackTraceRenderer;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Display details for a {@link ThreadData}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ThreadDetailComponent extends FormComponent implements Runnable {
	
	/**
	 * Configuration options of {@link ThreadDetailComponent}
	 */
	public interface Config extends FormComponent.Config {
		/**
		 * Whether automatic update is enabled by default.
		 */
		@BooleanDefault(true)
		boolean isDefaultActive();

		/**
		 * The time between two UI refreshes in milliseconds.
		 */
		@LongDefault(1000)
		long getUpdateInterval();
	}

	/**
	 * Thread name field.
	 */
	public static final String NAME_FIELD = "name";

	/**
	 * Thread id field.
	 */
	public static final String ID_FIELD = "id";

	/**
	 * Thread state field.
	 */
	public static final String STATE_FIELD = "state";

	/**
	 * Thread kind field.
	 */
	public static final String KIND_FIELD = "kind";

	/**
	 * Thread priority field.
	 */
	public static final String PRIORITY_FIELD = "priority";

	/**
	 * Thread group field.
	 */
	public static final String GROUP_FILED = "group";

	/**
	 * Thread stack field.
	 */
	public static final String STACK_FIELD = "stack";

	/**
	 * Current time field.
	 */
	public static final String TIME_FIELD = "time";

	private static final Property<Object> ACCESSOR = TypedAnnotatable.property(Object.class, "accessor");

	private boolean _active;

	private CommandModel _toggleCommand;

	private ScheduledFuture<?> _timer;

	private long _updateInterval;

	/**
	 * Creates a {@link ThreadDetailComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ThreadDetailComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_active = config.isDefaultActive();
		_updateInterval = config.getUpdateInterval();
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof ThreadData;
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext(getName().localName() + "_form", getResPrefix().append("form"));

		formContext.addMember(createField(ThreadData.ThreadName.INSTANCE, NAME_FIELD));
		formContext.addMember(createField(ThreadData.ThreadState.INSTANCE, STATE_FIELD));
		formContext.addMember(createField(ThreadData.ThreadId.INSTANCE, ID_FIELD));
		formContext.addMember(createField(ThreadData.ThreadKind.INSTANCE, KIND_FIELD));
		formContext.addMember(createField(ThreadData.ThreadPriority.INSTANCE, PRIORITY_FIELD));
		formContext.addMember(createField(ThreadData.ThreadGroup.INSTANCE, GROUP_FILED));
		formContext.addMember(FormFactory.newDisplayField(TIME_FIELD, new Date(), ResourceRenderer.newResourceRenderer(DateTimeLabelProvider.INSTANCE)));

		FormMember stack =
			createField(ThreadData.ThreadStack.INSTANCE, STACK_FIELD);
		stack.setControlProvider(new ValueDisplayControl.ValueDisplay(StackTraceRenderer.INSTANCE));
		formContext.addMember(stack);

		updateField(currentThreadInfo(), formContext);
		return formContext;
	}

	private void updateField(ThreadData info, FormContext formContext) {
		for (Iterator<FormField> fields = formContext.getFields(); fields.hasNext();) {
			FormField field = fields.next();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Accessor<Object> accessor = (Accessor) field.get(ACCESSOR);
			if (accessor != null) {
				field.setValue(accessor.getValue(info, field.getName()));
			}
		}
	}

	private FormMember createField(Accessor<ThreadData> accessor, String name) {
		FormField field = FormFactory.newDisplayField(name, null);
		field.set(ACCESSOR, accessor);
		return field;
	}

	void updateThreadState(FormContext formContext) {
		ThreadData info = currentThreadInfo();
		if (info == null) {
			// Currently no model
			return;
		}
		if (info.update()) {
			fireModelModifiedEvent(info, this);
		}

		updateField(info, formContext);
		formContext.getField(TIME_FIELD).setValue(new Date());
	}

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);
		if (newValue != null) {
			_toggleCommand = new AbstractCommandModel() {
				@Override
				protected HandlerResult internalExecuteCommand(DisplayContext context) {
					toggleActive();
					return HandlerResult.DEFAULT_RESULT;
				}
			};
			newValue.defineGroup(CommandHandlerFactory.STATE_GROUP).addButton(_toggleCommand);
		}
		if (oldValue != null && _toggleCommand != null) {
			ToolBarGroup group = oldValue.getGroup(CommandHandlerFactory.STATE_GROUP);
			if (group != null) {
				group.removeButton(_toggleCommand);

				if (group.isEmpty()) {
					oldValue.removeGroup(CommandHandlerFactory.STATE_GROUP);
				}
			}
		}
	}

	@Override
	protected void handleNewModel(Object newModel) {
		// Note: The super implementation must not be called to prevent invalidation.
	}

	@Override
	protected void updateForm() {
		// Note: Prevent resetting the form context after model change. This would break the UI
		// update without redraw.
		//
		// super.updateForm();
		if (hasFormContext()) {
			updateThreadState(getFormContext());
		}
	}

	/**
	 * Return this model casted to a {@link ThreadData}.
	 * 
	 * @return The model as {@link ThreadData}.
	 */
	protected ThreadData currentThreadInfo() {
		return (ThreadData) getModel();
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();

		updateMonitoringState();
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		cancelTimer();
	}

	/**
	 * Toggle the update mode (automatic vs. paused).
	 */
	protected void toggleActive() {
		_active = !_active;

		updateMonitoringState();
		if (_active) {
			updateThreadState(getFormContext());
		}
	}

	/**
	 * Update the display of the pause button.
	 * 
	 * <p>
	 * When toggle the state of the button we need to change the label and image.
	 * </p>
	 */
	protected void updateMonitoringState() {
		if (_active) {
			if (_timer == null) {
				ScheduledExecutorService executor = getMainLayout().getWindowScope().getUIExecutor();
				_timer = executor.scheduleWithFixedDelay(this, _updateInterval, _updateInterval, TimeUnit.MILLISECONDS);
			}
		} else {
			cancelTimer();
		}

		ThemeImage image = _active ? Icons.BUTTON_STOP : Icons.BUTTON_START;
		ResKey labelKey = _active ? I18NConstants.STOP_MONITORING : I18NConstants.START_MONITORING;

		_toggleCommand.setLabel(Resources.getInstance().getString(labelKey));
		_toggleCommand.setImage(image);
	}

	private void cancelTimer() {
		if (_timer != null) {
			_timer.cancel(false);
			_timer = null;
		}
	}

	@Override
	public void run() {
		updateThreadState(getFormContext());
	}

}
