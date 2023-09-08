/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.progress.AJAXProgressComponent;
import com.top_logic.layout.progress.AbstractProgressComponent;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.structure.Expandable.ExpansionState;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ProgressComponent;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.TLContextManager;

/**
 * Recorder for user events in an application.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ScriptingRecorder extends ConfiguredManagedClass<ScriptingRecorder.Config> {

	/**
	 * Defines whether an {@link ApplicationAction} should be recorded, if it is a follow-up action,
	 * i.e. not the first in the {@link InteractionContext interaction}.
	 */
	protected enum FollowupActionRecording {

		/**
		 * Record the {@link ApplicationAction} only if it is the first in this
		 * {@link InteractionContext interaction}.
		 */
		RECORD_ONLY_IF_FIRST_ACTION,

		/**
		 * Always record the {@link ApplicationAction}, even if it is not the first in this
		 * {@link InteractionContext interaction}.
		 */
		RECORD_ALWAYS;

	}

	private static final Property<Boolean> RECORDING_ACTIVE =
		TypedAnnotatable.property(Boolean.class, "recording active", Boolean.FALSE);

	/**
	 * Actions on any child of any {@link TypedAnnotatable} annotated with this property explicitly
	 * set to <code>false</code> are excluded from being recorded.
	 * 
	 * <p>
	 * This is used for example in the GUIs that are used to record assertions.
	 * </p>
	 */
	private static final Property<Boolean> SHOULD_RECORD = TypedAnnotatable.propertyDynamic(Boolean.class,
		"shouldRecord",
		x -> (x instanceof DynamicRecordable) ? Boolean.valueOf(((DynamicRecordable) x).shouldRecord()) : Boolean.TRUE);

	/**
	 * Session key remembering the setting whether to directly record control commands.
	 */
	private static final Property<Boolean> RECORD_TECHNICAL_COMMANDS =
		TypedAnnotatable.property(Boolean.class, "recordTechnicalCommands", false);

	/**
	 * Session key remembering the setting whether recored even statically excluded commands.
	 */
	private static final Property<Boolean> RECORD_ALL_COMMANDS =
		TypedAnnotatable.property(Boolean.class, "recordAllCommands", false);

	@SuppressWarnings({ "unchecked" })
	private static final Property<ValueNamingScheme<?>> NAME_PROVIDER =
		TypedAnnotatable.propertyRaw(ValueNamingScheme.class, "nameProvider");

	/**
	 * If a selection of "nothing selected" is being recorded, but something has to be given to the
	 * API as the object to select, this is used.
	 */
	public static final Object NO_SELECTION = null;

	/**
	 * Configuration for {@link ScriptingRecorder}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ScriptingRecorder> {

		/**
		 * @see #getRecordImplementationDetails()
		 */
		String RECORD_IMPLEMENTATION_DETAILS = "recordImplementationDetails";

		/** No longer used, remove configuration setting. */
		@Name("recordInternationalization")
		@BooleanDefault(true)
		@Deprecated
		boolean getRecordInternationalization();

		/**
		 * Whether to record technical details not required for replay.
		 */
		@Name(RECORD_IMPLEMENTATION_DETAILS)
		@BooleanDefault(true)
		boolean getRecordImplementationDetails();
	}

	/**
	 * Module for instantiation of the {@link ScriptingRecorder}.
	 */
	public static class Module extends TypedRuntimeModule<ScriptingRecorder> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ScriptingRecorder> getImplementation() {
			return ScriptingRecorder.class;
		}

	}

	private final Config _config;
	
	@Override
	protected void startUp() {
		// nothing to do
	}

	@Override
	protected void shutDown() {
		// nothing to do
	}

	/**
	 * Creates a {@link ScriptingRecorder}.
	 */
	public ScriptingRecorder(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
	}

	/**
	 * the configuration
	 */
	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * The currently active {@link ScriptingRecorder}.
	 */
	public static ScriptingRecorder getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * An event that occurs in the application.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Event {

		private final Object _sessionId;

		private ApplicationAction action;
		
		/**
		 * Creates a {@link Event}.
		 *
		 * @param session
		 *        See {@link #getSessionId()}.
		 * @param action
		 *        See {@link #getAction()}.
		 */
		public Event(TLSessionContext session, ApplicationAction action) {
			_sessionId = session.getId();
			this.action = action;
		}
	
		/**
		 * The {@link TLSessionContext#getId()} in which this event occurred.
		 */
		public Object getSessionId() {
			return _sessionId;
		}

		/**
		 * The {@link ApplicationAction} that was performed in the {@link #getSessionId()}.
		 */
		public ApplicationAction getAction() {
			return action;
		}

		/**
		 * Setter for {@link #getAction()}.
		 */
		public void setAction(ApplicationAction action) {
			this.action = action;
		}
	}

	/**
	 * Informs the {@link ScriptingRecorder} that recording of actions is currently enabled (or
	 * disabled).
	 * 
	 * @param enabled
	 *        Whether recording is currently enabled.
	 */
	public void setRecordingActive(boolean enabled) {
		TLContextManager.getSession().set(RECORDING_ACTIVE, enabled);
	}

	/**
	 * Whether the script recorder is currently recording actions.
	 */
	public static boolean isRecordingActive() {
		return isEnabled() && TLContextManager.getSession().get(RECORDING_ACTIVE);
	}

	/**
	 * If the {@link ScriptingRecorder} is enabled.
	 * 
	 * <p>
	 * If <code>false</code>, it will not record any {@link ApplicationAction}s.
	 * </p>
	 * 
	 * @see #isRecordingActive()
	 */
	public static boolean isEnabled() {
		if (!Module.INSTANCE.isActive()) {
			return false;
		}
		return getInstance().getEnabled();
	}

	/**
	 * Whether the instance of {@link ScriptingRecorder} is enabled.
	 */
	public abstract boolean getEnabled();

	/**
	 * Whether recording technical commands is enabled.
	 * 
	 * <p>
	 * When recording technical commands is enabled, all control actions for controls with
	 * identifiable models are directly recorded (instead of higher level actions in the model
	 * level).
	 * </p>
	 */
	public static boolean recordTechnicalCommands(TLSubSessionContext subSession) {
		return subSession.get(RECORD_TECHNICAL_COMMANDS).booleanValue();
	}

	/**
	 * @see #recordTechnicalCommands(TLSubSessionContext)
	 */
	public static void setRecordTechnicalCommands(TLSubSessionContext subSession, boolean newValue) {
		subSession.set(RECORD_TECHNICAL_COMMANDS, Boolean.valueOf(newValue));
	}

	/**
	 * Whether the static exclude from recording of special parts of the UI is temporarily disabled.
	 * 
	 * <p>
	 * If recording all commands is enabled, even those models annotated
	 * {@link #annotateAsDontRecord(TypedAnnotatable)} are also recorded.
	 * </p>
	 */
	public static boolean recordAllCommands(TLSubSessionContext subSession) {
		return subSession.get(RECORD_ALL_COMMANDS).booleanValue();
	}

	/**
	 * @see ScriptingRecorder#recordAllCommands(TLSubSessionContext)
	 */
	public static void setRecordAllCommands(TLSubSessionContext subSession, boolean newValue) {
		subSession.set(RECORD_ALL_COMMANDS, Boolean.valueOf(newValue));
	}

	/**
	 * Provides the configured {@link Config#RECORD_IMPLEMENTATION_DETAILS}.
	 */
	public static boolean recordImplementationDetails() {
		return getInstance().getConfig().getRecordImplementationDetails();
	}

	private static boolean hasVeto() {
		return hasVeto(FollowupActionRecording.RECORD_ONLY_IF_FIRST_ACTION);
	}

	private static boolean hasVeto(FollowupActionRecording followupActionRecording) {
		return getInstance().hasVetoImpl(followupActionRecording);
	}

	/**
	 * Is there a reason against recording the current action?
	 */
	protected abstract boolean hasVetoImpl(FollowupActionRecording followupActionRecording);

	/**
	 * Pause the ScriptingRecorder temporarily.
	 * 
	 * <p>
	 * To continue the recording use {@link #resume()}
	 * </p>
	 */
	public static void pause() {
		getInstance().pauseImpl();
	}

	/**
	 * @see #pause()
	 */
	protected abstract void pauseImpl();

	/**
	 * Resume the ScriptingRecorder if it is paused.
	 * 
	 * <p>
	 * To pause the recording use {@link #pause()}
	 * </p>
	 */
	public static void resume() {
		getInstance().resumeImpl();
	}

	/**
	 * @see #resume()
	 */
	protected abstract void resumeImpl();

	/**
	 * Whether resource lookup should not log missing keys or auto-generate labels from keys, but
	 * return <code>null</code> for undefined resources.
	 */
	public static boolean isResourceInspecting() {
		return getInstance().isResourceInspectingImpl();
	}

	/**
	 * Implementation of {@link #isResourceInspecting()}.
	 */
	protected abstract boolean isResourceInspectingImpl();

	/**
	 * Executes the given {@link ComputationEx2} with {@link #isResourceInspecting()} mode turned
	 * on.
	 * 
	 * @param <T>
	 *        The result type of the {@link ComputationEx2}.
	 * @param computation
	 *        The {@link ComputationEx2} to perform.
	 * @return The result of the given {@link ComputationEx2}.
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T withResourceInspection(
			ComputationEx2<T, E1, E2> computation) throws E1, E2 {
		return getInstance().withResourceInspectionImpl(computation);
	}

	protected abstract <T, E1 extends Throwable, E2 extends Throwable> T withResourceInspectionImpl(
			ComputationEx2<T, E1, E2> computation) throws E1, E2;

	public static void recordFieldInput(FormField field, Object newValue) {
		if (hasVeto()) {
			return;
		}
		try {
			if (mustNotRecord(field)) {
				logIgnoredFieldInput(field, newValue);
				return;
			}
			getInstance().recordFieldInputImpl(field, newValue);
		} catch (Throwable throwable) {
			actionRecordingFailed("Input recording failed.", throwable);
		}
	}

	public static void recordFieldRawInput(AbstractFormField field, Object newValue) {
		if (hasVeto()) {
			return;
		}
		try {
			if (mustNotRecord(field)) {
				logIgnoredFieldInput(field, newValue);
				return;
			}
			getInstance().recordFieldRawInputImpl(field, newValue);
		} catch (Throwable throwable) {
			actionRecordingFailed("Input recording failed.", throwable);
		}
	}

	protected abstract void recordFieldRawInputImpl(AbstractFormField field, Object newValue);

	/**
	 * Implementations of the ScriptingRecorder should not override
	 * {@link #recordFieldInput(FormField, Object)} directly but this hook. <br/>
	 * {@link Throwable}s thrown from this method are caught in
	 * {@link #recordFieldInput(FormField, Object)} and logged as errors. They are not propagated to
	 * ensure the {@link ScriptingRecorder} does not crash the application.
	 */
	protected abstract void recordFieldInputImpl(FormField field, Object newValue);

	private static void logIgnoredFieldInput(FormField field, Object newValue) {
		if (Logger.isDebugEnabled(ScriptingRecorder.class)) {
			String logMessage =
				"Ignoring to record field input. Reason: The field or one of its ancestors is annotated as \"Don't record me and my children!\". Field: '"
					+ field + "'; Input: '" + newValue + "'";
			Logger.debug(logMessage, ScriptingRecorder.class);
		}
	}

	public static Event recordCommand(DisplayContext context, CommandHandler command, LayoutComponent component,
			Map<String, Object> arguments) {
		if (hasVeto()) {
			return null;
		}
		try {
			if (mustNotRecord(component)) {
				logIgnoredCommand(command);
				return null;
			}
			return getInstance().recordCommandImpl(context, command, component, arguments);
		} catch (Throwable throwable) {
			return actionRecordingFailed("Command recording failed.", throwable);
		}
	}

	/**
	 * Implementations of the ScriptingRecorder should not override
	 * {@link #recordCommand(DisplayContext, CommandHandler, LayoutComponent, Map)}
	 * directly but this hook. <br/>
	 * {@link Throwable}s thrown from this method are caught in
	 * {@link #recordCommand(DisplayContext, CommandHandler, LayoutComponent, Map)}
	 * and logged as errors. They are not propagated to ensure the {@link ScriptingRecorder} does
	 * not crash the application.
	 * 
	 * @return The event of recorded for the given command. May be <code>null</code>, in case no
	 *         event is produced.
	 */
	protected abstract Event recordCommandImpl(DisplayContext context, CommandHandler command, LayoutComponent component,
			Map<String, Object> arguments);

	private static void logIgnoredCommand(BoundCommand command) {
		if (Logger.isDebugEnabled(ScriptingRecorder.class)) {
			String logMessage =
				"Ignoring to record command. Reason: The commands component or one of the ancestors of this component is annotated as \"Don't record me and my children!\". Command: '"
					+ command + "'; Command ID: '" + command.getID() + "'";
			Logger.debug(logMessage, ScriptingRecorder.class);
		}
	}

	/**
	 * Has to be called where ever the application waits for progress by displaying the
	 * {@link ProgressComponent}, {@link AbstractProgressComponent} or the
	 * {@link AJAXProgressComponent}.
	 */
	public static void recordAwaitProgress(LayoutComponent component) {
		if (hasVeto(FollowupActionRecording.RECORD_ALWAYS)) {
			return;
		}
		try {
			getInstance().recordAwaitProgressImpl(component);
		} catch (Throwable throwable) {
			actionRecordingFailed("Recording \"await progress\" failed.", throwable);
		}
	}

	/**
	 * Implementations of the ScriptingRecorder should not override
	 * {@link #recordAwaitProgress(LayoutComponent)} directly but this hook. <br/>
	 * {@link Throwable}s thrown from this method are caught in
	 * {@link #recordAwaitProgress(LayoutComponent)} and logged as errors. They are not propagated
	 * to ensure the {@link ScriptingRecorder} does not crash the application.
	 */
	protected abstract void recordAwaitProgressImpl(LayoutComponent component);

	/**
	 * Has to be called to record collapsing or expanding of a toolbar.
	 * 
	 * @param state
	 *        The new {@link ExpansionState}.
	 */
	public static void recordCollapseToolbar(ModelName toolbarOwner, ExpansionState state) {
		if (hasVeto(FollowupActionRecording.RECORD_ALWAYS)) {
			return;
		}
		try {
			getInstance().recordCollapseToolbarImpl(toolbarOwner, state);
		} catch (Throwable throwable) {
			actionRecordingFailed("Recording \"await progress\" failed.", throwable);
		}
	}

	/**
	 * Implementation of {@link #recordCollapseToolbar(ModelName, ExpansionState)}.
	 */
	protected abstract void recordCollapseToolbarImpl(ModelName toolbarOwner, ExpansionState state);

	public static void recordSelection(NamedModel namedModel, Object target, boolean selectState,
			SelectionChangeKind changeKind) {
		if (hasVeto()) {
			return;
		}
		try {
			if (mustNotRecord(namedModel)) {
				logIgnoredSelection(namedModel, target);
				return;
			}
			getInstance().recordSelectionImpl(namedModel, target, selectState, changeKind);
		} catch (Throwable throwable) {
			actionRecordingFailed("Selection recording failed.", throwable);
		}
	}

	/**
	 * Implementations of the ScriptingRecorder should not override
	 * {@link #recordSelection(NamedModel, Object, boolean, SelectionChangeKind)} directly but this
	 * hook.
	 * <p>
	 * {@link Throwable}s thrown from this method are caught in
	 * {@link #recordSelection(NamedModel, Object, boolean, SelectionChangeKind)} and logged as
	 * errors. They are not propagated to ensure the {@link ScriptingRecorder} does not crash the
	 * application.
	 * </p>
	 */
	protected abstract void recordSelectionImpl(NamedModel namedModel, Object target, boolean selectState,
			SelectionChangeKind changeKind);

	private static void logIgnoredSelection(NamedModel namedModel, Object target) {
		if (Logger.isDebugEnabled(ScriptingRecorder.class)) {
			String logMessage =
				"Ignoring to record selection. Reason: The container of the selection or one of the ancestors of it is annotated as \"Don't record me and my children!\". Container: '"
					+ namedModel + "'; Selectee: '" + target + "'";
			Logger.debug(logMessage, ScriptingRecorder.class);
		}
	}

	public static void recordTabSwitch(LayoutComponent tabComponent, int selectedIndex) {
		if (hasVeto()) {
			return;
		}
		try {
			if (mustNotRecord(tabComponent)) {
				logIgnoredTabSwitch(tabComponent);
				return;
			}
			getInstance().recordTabSwitchImpl(tabComponent, selectedIndex);
		} catch (Throwable throwable) {
			actionRecordingFailed("Tab switch recording failed.", throwable);
		}
	}

	/**
	 * Implementations of the ScriptingRecorder should not override
	 * {@link #recordTabSwitch(LayoutComponent, int)} directly but this hook. <br/>
	 * {@link Throwable}s thrown from this method are caught in
	 * {@link #recordTabSwitch(LayoutComponent, int)} and logged as errors. They are not propagated
	 * to ensure the {@link ScriptingRecorder} does not crash the application.
	 */
	protected abstract void recordTabSwitchImpl(LayoutComponent tabComponent, int selectedIndex);

	private static void logIgnoredTabSwitch(LayoutComponent tabComponent) {
		if (Logger.isDebugEnabled(ScriptingRecorder.class)) {
			String logMessage =
				"Ignoring to record tab switch. Reason: The tab component or one of its ancestors is annotated as \"Don't record me and my children!\". Tab Component: '"
					+ tabComponent + "'";
			Logger.debug(logMessage, ScriptingRecorder.class);
		}
	}

	public static void recordButtonCommand(LayoutComponent component, ButtonControl buttonControl) {
		if (hasVeto()) {
			return;
		}
		try {
			if (mustNotRecord(buttonControl)) {
				logIgnoredButtonCommand(component, buttonControl);
				return;
			}
			getInstance().recordButtonCommandImpl(component, buttonControl);
		} catch (Throwable throwable) {
			actionRecordingFailed("Button recording failed.", throwable);
		}
	}

	/**
	 * Implementations of the ScriptingRecorder should not override
	 * {@link #recordButtonCommand(LayoutComponent, ButtonControl)} directly but this hook. <br/>
	 * {@link Throwable}s thrown from this method are caught in
	 * {@link #recordButtonCommand(LayoutComponent, ButtonControl)} and logged as errors. They are
	 * not propagated to ensure the {@link ScriptingRecorder} does not crash the application.
	 */
	protected abstract void recordButtonCommandImpl(LayoutComponent component, ButtonControl buttonControl);

	private static void logIgnoredButtonCommand(LayoutComponent component, ButtonControl buttonControl) {
		if (Logger.isDebugEnabled(ScriptingRecorder.class)) {
			String logMessage =
				"Ignoring to record button command. Reason: The commands component or one of the ancestors of this component is annotated as \"Don't record me and my children!\". Component: '"
					+ component + "' ButtonControl: '" + buttonControl + "'";
			Logger.debug(logMessage, ScriptingRecorder.class);
		}
	}

	/**
	 * Old variant of {@link #recordAction(Supplier)}. Use that instead of this method to ensure the
	 * action creation is done by the {@link ScriptingRecorder}.
	 */
	public static void recordAction(ApplicationAction action) {
		if (hasVeto()) {
			return;
		}
		internalRecordAction(action);
	}

	/**
	 * Records the given action.
	 * <p>
	 * Any {@link Throwable} thrown from the {@link Supplier} is logged and suppressed. This makes
	 * sure the scripting never damages an application function.
	 * </p>
	 * 
	 * @param actionSupplier
	 *        Is not allowed to be or return null.
	 */
	public static void recordAction(Supplier<ApplicationAction> actionSupplier) {
		if (hasVeto()) {
			return;
		}
		internalRecordAction(actionSupplier);
	}

	/**
	 * Record the given assertion.
	 * <p>
	 * Recording an assertion doesn't check whether the current interaction has already been
	 * recorded, as it is a valid use case to record multiple assertions within a single
	 * interaction.
	 * </p>
	 */
	public static void recordAssertion(ApplicationAction assertion) {
		if (hasVeto(FollowupActionRecording.RECORD_ALWAYS)) {
			return;
		}
		internalRecordAction(assertion);
	}

	private static void internalRecordAction(ApplicationAction action) {
		try {
			getInstance().recordActionImpl(action);
		} catch (Throwable throwable) {
			actionRecordingFailed("Action recording failed.", throwable);
		}
	}

	private static void internalRecordAction(Supplier<ApplicationAction> actionSupplier) {
		try {
			getInstance().recordActionImpl(actionSupplier);
		} catch (Throwable throwable) {
			actionRecordingFailed("Action recording failed.", throwable);
		}
	}

	/**
	 * Old variant of {@link #recordActionImpl(Supplier)}. Use that instead of this method to ensure
	 * the action creation is done by the {@link ScriptingRecorder}.
	 */
	protected abstract Event recordActionImpl(ApplicationAction action);

	/**
	 * Implementations of the ScriptingRecorder should not override
	 * {@link #recordAction(ApplicationAction)} directly but this hook. <br/>
	 * {@link Throwable}s thrown from this method are caught in
	 * {@link #recordAction(ApplicationAction)} and logged as errors. They are not propagated to
	 * ensure the {@link ScriptingRecorder} does not crash the application.
	 * 
	 * @return The event of recorded for the given {@link ApplicationAction}. May be
	 *         <code>null</code>, in case no event is produced.
	 */
	protected abstract Event recordActionImpl(Supplier<ApplicationAction> action);

	public static void recordDownload(BinaryDataSource data, boolean showInline) {
		if (hasVeto(FollowupActionRecording.RECORD_ALWAYS)) {
			return;
		}
		try {
			getInstance().recordDownloadImpl(data, showInline);
		} catch (Throwable throwable) {
			actionRecordingFailed("Action recording failed.", throwable);
		}
	}

	/**
	 * Implementations of the ScriptingRecorder should not override
	 * {@link #recordDownload(BinaryDataSource, boolean)} directly but this hook. <br/>
	 * {@link Throwable}s thrown from this method are caught in
	 * {@link #recordDownload(BinaryDataSource, boolean)} and logged as errors. They are not
	 * propagated to ensure the {@link ScriptingRecorder} does not crash the application.
	 */
	protected abstract void recordDownloadImpl(BinaryDataSource data, boolean showInline);

	/**
	 * Records opening the table filter dialog of the given column.
	 * @param tableData
	 *        The table for which the filter dialog should be opened.
	 * @param filterColumnName
	 *        The name of the column to be filtered.
	 */
	public static void recordTableFilter(TableData tableData, String filterColumnName) {
		if (hasVeto()) {
			return;
		}
		try {
			getInstance().recordTableFilterImpl(tableData, filterColumnName);
		} catch (Throwable throwable) {
			actionRecordingFailed("Action recording failed.", throwable);
		}
	}

	/**
	 * Implementation of {@link #recordTableFilter(TableData, String)}.
	 */
	protected abstract void recordTableFilterImpl(TableData tableData, String filterColumnName);

	/**
	 * Records opening the tree filter options popup.
	 * @param tableData
	 *        The table for which the tree filter options popup should be opened.
	 */
	public static void recordOpenTreeFilter(TableData tableData) {
		if (hasVeto()) {
			return;
		}
		try {
			getInstance().recordOpenTreeFilterImpl(tableData);
		} catch (Throwable throwable) {
			actionRecordingFailed("Action recording failed.", throwable);
		}
	}

	/**
	 * Implementation of {@link #recordOpenTreeFilter(TableData)}.
	 */
	protected abstract void recordOpenTreeFilterImpl(TableData tableData);

	/**
	 * Records sorting the table with the given {@link SortConfig}s.
	 * @param table
	 *        The table to sort.
	 * @param sortOrder
	 *        Definition of the requested sort order.
	 */
	public static void recordSortTableColumn(TableData table, List<SortConfig> sortOrder) {
		if (hasVeto()) {
			return;
		}
		try {
			getInstance().recordSortTableColumnImpl(table, sortOrder);
		} catch (Throwable throwable) {
			actionRecordingFailed("Action recording failed.", throwable);
		}
	}

	/**
	 * Implementation of {@link #recordSortTableColumn(TableData, List)}.
	 */
	protected abstract void recordSortTableColumnImpl(TableData table, List<SortConfig> sortOrder);

	/**
	 * Records setting the given columns to the table.
	 * @param table
	 *        The table for columns should be set.
	 * @param columnNames
	 *        Names of the columns to set.
	 */
	public static void recordSetTableColumns(TableData table, List<String> columnNames) {
		if (hasVeto()) {
			return;
		}
		try {
			getInstance().recordSetTableColumnsImpl(table, columnNames);
		} catch (Throwable throwable) {
			actionRecordingFailed("Action recording failed.", throwable);
		}
	}

	/**
	 * Implementation of {@link #recordSetTableColumns(TableData, List)}.
	 */
	protected abstract void recordSetTableColumnsImpl(TableData table, List<String> columnNames);

	/**
	 * Record that a tree node is being expanded/collapsed.
	 * 
	 * @param expand
	 *        true: Expand, false: Collapse
	 */
	public static void recordExpand(TreeData treeData, Object node, boolean expand) {
		if (hasVeto()) {
			return;
		}
		try {
			if (mustNotRecord(treeData)) {
				logIgnoredExpand(treeData, node);
				return;
			}
			getInstance().recordExpandImpl(treeData, node, expand);
		} catch (Throwable throwable) {
			actionRecordingFailed("Tree node expand/collapse recording failed.", throwable);
		}
	}

	/**
	 * Implementation of {@link #recordExpand(TreeData, Object, boolean)}.
	 */
	protected abstract void recordExpandImpl(TreeData treeData, Object node, boolean expand);

	private static void logIgnoredExpand(TreeData treeData, Object node) {
		if (Logger.isDebugEnabled(ScriptingRecorder.class)) {
			String logMessage = "Ignoring to record tree node expansion."
				+ " Reason: The tree or one its containers is annotated as \"Don't record me and my children!\"."
				+ " Tree: '" + treeData + "'; Expanded/Collapsed node: '" + node + "'";
			Logger.debug(logMessage, ScriptingRecorder.class);
		}
	}

	private static Event actionRecordingFailed(String message, Throwable throwable) {
		String messageWithCause = message + " Cause: " + throwable.getMessage();
		try {
			String printedThrowable = ExceptionUtil.printThrowableToString(throwable);
			if (Logger.isDebugEnabled(ScriptingRecorder.class)) {
				Logger.debug(messageWithCause, throwable, ScriptingRecorder.class);
			}
			return getInstance().recordActionImpl(ActionFactory.recordingFailedAction(messageWithCause, printedThrowable));
		} catch (Throwable followUpError) {
			Logger.error(messageWithCause, throwable, ScriptingRecorder.class);
			String followUpMessage = messageWithCause + " Failed to record a 'recording failed' marker action.";
			Logger.error(followUpMessage, followUpError, ScriptingRecorder.class);
			return null;
		}
	}

	/**
	 * Whether the given model is annotated as {@link #annotateAsDontRecord(TypedAnnotatable) don't
	 * record}.
	 */
	public static boolean mustNotRecord(Object model) {
		if (model instanceof TreeControl) {
			return mustNotRecord((TreeControl) model);
		}
		if (model instanceof FormMember) {
			return mustNotRecord((FormMember) model);
		}
		if (model instanceof FormHandler) {
			return mustNotRecord((FormHandler) model);
		}
		if (model instanceof LayoutComponent) {
			return mustNotRecord((LayoutComponent) model);
		}
		if (model instanceof TreeData) {
			return mustNotRecord((TreeData) model);
		}
		if (model instanceof TableData) {
			return mustNotRecord((TableData) model);
		}
		if (model instanceof TypedAnnotatable) {
			return isAnnotatedAsDontRecord((TypedAnnotatable) model);
		}
		if (model instanceof DynamicRecordable) {
			return mustNotRecord((DynamicRecordable) model);
		}
		return false;
	}

	private static boolean mustNotRecord(DynamicRecordable model) {
		return !model.shouldRecord();
	}

	/**
	 * Whether the given {@link TreeControl} is annotated as
	 * {@link #annotateAsDontRecord(TypedAnnotatable) don't record}.
	 */
	public static boolean mustNotRecord(TreeControl treeControl) {
		return isAnnotatedAsDontRecord(treeControl.getData());
	}

	/**
	 * Whether the given {@link ButtonControl} is annotated as
	 * {@link #annotateAsDontRecord(TypedAnnotatable) don't record}.
	 */
	public static boolean mustNotRecord(ButtonControl buttonControl) {
		if (buttonControl.getModel() instanceof FormMember) {
			return mustNotRecord((FormMember) buttonControl.getModel());
		}
		return isAnnotatedAsDontRecord(buttonControl.getModel());
	}

	/**
	 * Whether the given {@link FormMember} or one of its parents is annotated as
	 * {@link #annotateAsDontRecord(TypedAnnotatable) don't record}.
	 */
	public static boolean mustNotRecord(FormMember formMember) {
		if (isAnnotatedAsDontRecord(formMember)) {
			return true;
		}
		FormContainer parent = formMember.getParent();
		if (parent == null) {
			// Did not find a FormContext.
			return false;
		}
		if (formMember instanceof FormContext) {
			return mustNotRecord((FormContext) formMember);
		} else {
			return mustNotRecord(parent);
		}
	}

	/**
	 * Whether the given {@link FormContext} or its {@link FormHandler} is annotated as
	 * {@link #annotateAsDontRecord(TypedAnnotatable) don't record}.
	 */
	public static boolean mustNotRecord(FormContext formContext) {
		if (isAnnotatedAsDontRecord(formContext)) {
			return true;
		}
		if (formContext.getOwningModel() != null) {
			return mustNotRecord(formContext.getOwningModel());
		}
		return false;
	}

	/**
	 * Whether the given {@link FormHandler} is annotated as
	 * {@link #annotateAsDontRecord(TypedAnnotatable) don't record}.
	 */
	public static boolean mustNotRecord(FormHandler formHandler) {
		if (formHandler instanceof LayoutComponent) {
			return mustNotRecord((LayoutComponent) formHandler);
		}
		if (formHandler instanceof TypedAnnotatable) {
			return isAnnotatedAsDontRecord((TypedAnnotatable) formHandler);
		}
		return false;
	}

	/**
	 * Whether the given {@link LayoutComponent} is annotated as
	 * {@link #annotateAsDontRecord(TypedAnnotatable) don't record}.
	 */
	public static boolean mustNotRecord(LayoutComponent layoutComponent) {
		if (isAnnotatedAsDontRecord(layoutComponent)) {
			return true;
		}
		if (layoutComponent.openedAsDialog() && layoutComponent == layoutComponent.getDialogTopLayout()) {
			return mustNotRecord(layoutComponent.getDialogParent());
		}
		if (layoutComponent.getParent() != null) {
			return mustNotRecord(layoutComponent.getParent());
		}
		return false;
	}

	/**
	 * Whether the given {@link TreeData} is annotated as
	 * {@link #annotateAsDontRecord(TypedAnnotatable) don't record}.
	 */
	public static boolean mustNotRecord(TreeData treeData) {
		return isAnnotatedAsDontRecord(treeData);
	}

	/**
	 * Whether the given {@link TableData} is annotated as
	 * {@link #annotateAsDontRecord(TypedAnnotatable) don't record}.
	 */
	public static boolean mustNotRecord(TableData tableData) {
		return isAnnotatedAsDontRecord(tableData);
	}

	/**
	 * Whether the given model was {@link #annotateAsDontRecord(TypedAnnotatable) annotated to
	 * disable recording}.
	 * 
	 * <p>
	 * When checking whether {@link #annotateAsDontRecord(TypedAnnotatable)} is set on some model,
	 * the context has to be checked also, see {@link #mustNotRecord(Object)}.
	 * </p>
	 * 
	 * @see #mustNotRecord(Object)
	 */
	private static boolean isAnnotatedAsDontRecord(TypedAnnotatable container) {
		if (recordAllCommands(DefaultDisplayContext.getDisplayContext().getSubSessionContext())) {
			return false;
		}

		return !container.get(SHOULD_RECORD).booleanValue();
	}

	/**
	 * Marks the {@link TypedAnnotatable} for the {@link ScriptingRecorder} as "don't record this
	 * element or its children".
	 * 
	 * <p>
	 * Note: For storage optimization, it is better to implement {@link DynamicRecordable}, if the
	 * exclusion can be statically determined.
	 * </p>
	 * 
	 * @param annotatable
	 *        The model to exclude from script recording.
	 * @return The given model for call chaining.
	 * 
	 * @see DynamicRecordable
	 * @see #mustNotRecord(Object)
	 */
	public static <T extends TypedAnnotatable> T annotateAsDontRecord(T annotatable) {
		try {
			annotatable.set(SHOULD_RECORD, false);
		} catch (Throwable throwable) {
			Logger.error("Annotating an object as \"Don't record me or my children!\" failed! Object to annotate: '"
				+ annotatable + "'", throwable, ScriptingRecorder.class);
		}
		return annotatable;
	}

	/**
	 * Retrieves a {@link ValueNamingScheme} annotation.
	 */
	public static ValueNamingScheme<?> getNameProvider(TypedAnnotatable model) {
		return model.get(NAME_PROVIDER);
	}

	/**
	 * Adds a {@link ValueNamingScheme} annotation to a {@link TypedAnnotatable}.
	 * 
	 * @see #getNameProvider(TypedAnnotatable)
	 */
	public static void annotateNameProvider(TypedAnnotatable model, ValueNamingScheme<?> identification) {
		if (isEnabled()) {
			model.set(NAME_PROVIDER, identification);
		}
	}

	/**
	 * Enables recording of another {@link ApplicationAction}.
	 * 
	 * <p>
	 * After the call to this method the next action is recorded, also when an application action is
	 * already recorded.
	 * </p>
	 */
	public static void enableFollowUpAction() {
		getInstance().internalAllowFollowupAction();
	}

	/**
	 * Actual implementation of {@link #enableFollowUpAction()}.
	 */
	protected abstract void internalAllowFollowupAction();

	/**
	 * Pointer to the queue of {@link ScriptingRecorder.Event}s.
	 * 
	 * <p>
	 * The resulting {@link Iterator} is concurrently refilled when more {@link Event}s become
	 * available. It can be re-iterated after it is once exhausted.
	 * </p>
	 */
	public abstract Iterator<Event> getEventPointer();

}
