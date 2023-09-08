/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder;

import static com.top_logic.basic.shared.string.StringServicesShared.*;
import static com.top_logic.layout.scripting.action.ActionFactory.*;
import static com.top_logic.layout.scripting.recorder.ScriptingRecorder.FollowupActionRecording.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.GCQueue;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.thread.StackTrace;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommand;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.basic.DebuggingConfig;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.WrappedCommandModel;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.AwaitProgressAction;
import com.top_logic.layout.scripting.action.CommandAction;
import com.top_logic.layout.scripting.action.FormInput;
import com.top_logic.layout.scripting.action.FormRawInput;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.ui.button.LabeledButtonNaming;
import com.top_logic.layout.scripting.recorder.specialcases.ValueScopeFactory;
import com.top_logic.layout.scripting.runtime.action.FuzzyGotoActionOp.FuzzyGoto;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.Expandable.ExpansionState;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.Utils;

/**
 * {@link ScriptingRecorder} that dumps events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies(InitialGroupManager.Module.class)
public class DefaultScriptingRecorder extends ScriptingRecorder {

	private static final Property<Boolean> SCRIPTING_ENABLED =
		TypedAnnotatable.property(Boolean.class, "scripting enabled");

	/**
	 * Configuration for {@link ScriptingRecorder}.
	 */
	public interface Config extends ScriptingRecorder.Config {
		/**
		 * Enables the scripting recorder. See {@link Config#getEnabled}. As the GUI inspection
		 * features are used to record assertions, they are enabled by this setting, too.
		 * 
		 * @see DebuggingConfig#getInspectEnabled()
		 */
		String ENABLED = "enabled";

		/** Getter for {@link Config#ENABLED}. */
		@Name(ENABLED)
		@BooleanDefault(false)
		boolean getEnabled();

		/**
		 * The name of the {@link Group} in which a user must be see to use the
		 * {@link ScriptingRecorder}.
		 * 
		 * <p>
		 * May be <code>null</code> in which case the {@link InitialGroupManager#getDefaultGroup()
		 * default group} is used.
		 * </p>
		 */
		@Nullable
		String getScriptingGroup();

	}

	/**
	 * Creates a new {@link DefaultScriptingRecorder}.
	 */
	public DefaultScriptingRecorder(InstantiationContext context, Config config) {
		super(context, config);
		if (config.getScriptingGroup() != null) {
			_scriptingGroup = Group.getGroupByName(config.getScriptingGroup());
			if (_scriptingGroup == null) {
				context.error("Unknown user group '" + config.getScriptingGroup() + "'.");
			}
		} else {
			_scriptingGroup = InitialGroupManager.getInstance().getDefaultGroup();
		}
	}

	/**
	 * Name of the file containing the script recorder tree. This name is used to identify the window
	 * from which no action must be recorded.
	 */
	private static final String SCRIPT_RECORDER_XML = "ScriptRecorder.xml";

	/**
	 * How long will the ScriptingFramework wait for a progress in an {@link AwaitProgressAction}?
	 */
	private static final int MAX_WAIT_PROGRESS = 5 * 60 * 1000;

	private final GCQueue<Event> events = new GCQueue<>();

	public interface FieldReference extends ConfigurationItem {
		String getQualifiedName();
		void setQualifiedName(String value);
	}

	public interface AttributeReference extends ConfigurationItem {
		boolean hasInstance();
		void setInstance(boolean value);
		
		String getAttributeName();
		void setAttributeName(String value);
	}

	private static final Property<Boolean> PROPERTY_INTERACTION_RECORDED =
		TypedAnnotatable.property(Boolean.class, DefaultScriptingRecorder.class.getName() + ".InteractionRecorded", false);

	private ThreadLocal<Boolean> paused = new ThreadLocal<>();

	private ThreadLocal<Boolean> resourceInspecting = new ThreadLocal<>();

	private final Group _scriptingGroup;

	@Override
	protected boolean hasVetoImpl(FollowupActionRecording followupActionRecording) {
		if (!isRecordingActive()) {
			Logger.error("ScriptingRecorder has been called, but it's disabled!", new StackTrace(),
				DefaultScriptingRecorder.class);
			return true;
		}
		if (isPaused()) {
			return true;
		}
		if (!hasInteractionContext()) {
			return true;
		}
		if ((followupActionRecording == RECORD_ONLY_IF_FIRST_ACTION) && isInteractionRecorded()) {
			return true;
		}
		WindowScope windowScope = getWindowScope();
		if (windowScope == null) {
			/* This may happen e.g. when an URL with a bookmark was entered that triggers a tab
			 * switch. */
			return true;
		}
		if (windowScope.getName().qualifiedName().contains(SCRIPT_RECORDER_XML)) {
			return true;
		}
		boolean dontRecordDialogOpened = windowScope.getDialogs()
			.stream()
			.map(DialogWindowControl::getDialogModel)
			.filter(model -> !model.isClosed())
			.anyMatch(ScriptingRecorder::mustNotRecord);
		if (dontRecordDialogOpened) {
			return true;
		}
		return false;
	}

	@Override
	protected void pauseImpl() {
		paused.set(true);
	}

	@Override
	protected void resumeImpl() {
		paused.set(false);
	}

	/**
	 * Is the ScriptingRecorder temporarily paused?
	 */
	private boolean isPaused() {
		return Boolean.TRUE.equals(paused.get());
	}

	private boolean hasInteractionContext() {
		return getInteractionContext() != null;
	}

	/**
	 * Has this user interaction already been recorded?
	 */
	private boolean isInteractionRecorded() {
		return getInteractionContext().get(PROPERTY_INTERACTION_RECORDED);
	}

	/**
	 * Set that this interaction has already been recorded.
	 */
	private void setInteractionRecorded() {
		getInteractionContext().set(PROPERTY_INTERACTION_RECORDED, true);
	}

	@Override
	protected void internalAllowFollowupAction() {
		getInteractionContext().reset(PROPERTY_INTERACTION_RECORDED);
	}

	private InteractionContext getInteractionContext() {
		return ThreadContextManager.getInteraction();
	}

	@Override
	protected boolean isResourceInspectingImpl() {
		return Boolean.TRUE.equals(resourceInspecting.get());
	}

	@Override
	protected <T, E1 extends Throwable, E2 extends Throwable> T withResourceInspectionImpl(
			ComputationEx2<T, E1, E2> computation) throws E1, E2 {
		Boolean before = resourceInspecting.get();
		resourceInspecting.set(Boolean.TRUE);
		try {
			return computation.run();
		} finally {
			resourceInspecting.set(before);
		}
	}

	@Override
	protected Event recordCommandImpl(DisplayContext context, CommandHandler command, LayoutComponent component,
			Map<String, Object> arguments) {
		if (isIgnoredCommand(context, command, component, arguments)) {
			if (Logger.isDebugEnabled(DefaultScriptingRecorder.class)) {
				String logMessage =
					"Ignoring to record command. Reason: The command's id ('" + command.getID()
						+ "') is on the list of ignored commands. Command: '" + command + "'";
				Logger.debug(logMessage, DefaultScriptingRecorder.class);
			}
			return null;
		}
		checkCommandFromComponentConsistency(command, component);
		ApplicationAction commandAction = commandAction(component, command, arguments);

		return recordActionImpl(commandAction);
	}

	private boolean isIgnoredCommand(DisplayContext context, CommandHandler command, LayoutComponent component,
			Map<String, Object> arguments) {
		return command.mustNotRecordCommand(context, component, arguments);
	}

	private void checkCommandFromComponentConsistency(CommandHandler command, LayoutComponent component) {
		CommandHandler retrievedCommand = component.getCommandById(command.getID());
		if (!Utils.equals(retrievedCommand, command)) {
			String retrievedId = (retrievedCommand == null) ? null : retrievedCommand.getID();
			throw new RuntimeException("Command cannot be retrieved from component, as"
				+ "'component.getCommandById(command.getId()) != command'. Original command is '" + command
				+ "' (id: " + command.getID() + ") but component.getCommandById(" + command.getID() + ") returns '"
				+ retrievedCommand + "' (id: " + retrievedId + "). Component: " + component);
		}
	}

	/**
	 * Create a documented {@link CommandAction}.
	 */
	protected CommandAction commandAction(final LayoutComponent component, final CommandHandler command,
			Map<String, Object> arguments) {
		CommandAction commandAction;
		if (command instanceof GotoHandler) {
			commandAction = ActionFactory.gotoAction(component.getName(), command.getID(), arguments);
		} else {
			commandAction = ActionFactory.commandAction(component.getName(), command.getID(), arguments);
			commandAction.setCommandLabel(Resources.getInstance().getString(command.getResourceKey(component), null));
		}

		if (recordImplementationDetails()) {
			commandAction.setComponentImplementationComment(component.getClass().getName());
			commandAction.setCommandImplementationComment(command.getClass().getName());
		}

		return commandAction;
	}

	@Override
	protected void recordAwaitProgressImpl(LayoutComponent component) {
		recordActionImpl(ActionFactory.awaitProgressAction(MAX_WAIT_PROGRESS, component.getName()));
	}

	@Override
	protected void recordFieldInputImpl(FormField field, Object newValue) {
		FormInput action = TypedConfiguration.newConfigItem(FormInput.class);
		action.setField(ModelResolver.buildModelName(field));
		action.setValue(ModelResolver.buildModelName(field, newValue));
		recordActionImpl(action);
	}

	@Override
	protected void recordFieldRawInputImpl(AbstractFormField field, Object newValue) {
		FormRawInput action = TypedConfiguration.newConfigItem(FormRawInput.class);
		action.setField(ModelResolver.buildModelName(field));
		action.setValue(ModelResolver.buildModelName(field, newValue));
		recordActionImpl(action);
	}

	@Override
	protected void recordCollapseToolbarImpl(ModelName toolbarOwner, ExpansionState state) {
		recordActionImpl(ActionFactory.collapseToolbar(toolbarOwner, state));
	}

	@Override
	protected void recordSelectionImpl(NamedModel namedModel, Object target, boolean selectionState,
			SelectionChangeKind changeKind) {
		if (target instanceof Collection && ((Collection<?>) target).size() == 1) {
			target = CollectionUtils.extractSingleton((Collection<?>) target);
		}
		ApplicationAction selectAction = ActionFactory.selectObject(namedModel, target, selectionState, changeKind);
		recordActionImpl(selectAction);
	}

	@Override
	protected void recordTabSwitchImpl(LayoutComponent tabComponent, int selectedIndex) {
		TabComponent tabBar = (TabComponent) tabComponent;

		ModelName componentName = ModelResolver.buildModelName(tabBar.getChild(selectedIndex));
		FuzzyGoto action = ActionFactory.fuzzyGoto(componentName);
		recordActionImpl(action);
	}

	@Override
	protected void recordButtonCommandImpl(LayoutComponent component, ButtonControl buttonControl) {
		CommandModel model = buttonControl.getModel();
		Maybe<? extends ModelName> modelName = ModelResolver.buildModelNameIfAvailable(model);
		if (modelName.hasValue()) {
			recordActionImpl(commandExecution(modelName.get()));
			return;
		}
		if (isRecordedAsCommand(model)) {
			if (Logger.isDebugEnabled(DefaultScriptingRecorder.class)) {
				String logMessage =
					"Ignoring to record ButtonControl, because it will be recorded as command. ButtonControl: "
						+ StringServices.getObjectDescription(buttonControl);
				Logger.debug(logMessage, DefaultScriptingRecorder.class);
			}
			return;
		}

		String label = buttonControl.getLabel();
		if (isEmpty(label)) {
			return;
		}
		ComponentName componentName = component.getName();
		recordActionImpl(buttonAction(componentName, model.get(LabeledButtonNaming.BUSINESS_OBJECT), label));
	}

	@Override
	protected void recordDownloadImpl(BinaryDataSource data, boolean showInline) {
		recordActionImpl(ActionFactory.downloadCheck(data));
	}

	@Override
	protected void recordTableFilterImpl(TableData tableData, String filterColumnName) {
		String columnLabel = ScriptTableUtil.getColumnLabel(filterColumnName, tableData);
		recordActionImpl(ActionFactory.openTableFilter(tableData.getModelName(), columnLabel));
	}

	@Override
	protected void recordOpenTreeFilterImpl(TableData tableData) {
		recordActionImpl(ActionFactory.openTreeFilter(tableData.getModelName()));
	}

	@Override
	protected void recordSortTableColumnImpl(TableData table, List<SortConfig> sortOrder) {
		recordActionImpl(ActionFactory.sortTableColumn(table, sortOrder));
	}

	@Override
	protected void recordSetTableColumnsImpl(TableData table, List<String> columnNames) {
		recordActionImpl(ActionFactory.setTableColumns(table, columnNames));
	}

	@Override
	protected void recordExpandImpl(TreeData treeData, Object node, boolean expand) {
		ModelName treeDataName = treeData.getModelName();
		Maybe<? extends ModelName> pathRef = ValueScopeFactory.referenceContextLocalValue(treeData, node);
		if (!pathRef.hasValue()) {
			throw new AssertionError("Recording expansion of tree node '" + node
				+ "' failed! Path to node could not be referenced.");
		}
		recordActionImpl(ActionFactory.expandAction(treeDataName, pathRef.get(), expand));
	}

	/**
	 * Checks, whether the given button is recorded when its {@link CommandHandler} is invoked
	 * through:
	 * {@link CommandDispatcher#dispatchCommand(CommandHandler, DisplayContext, LayoutComponent, Map)}
	 */
	private boolean isRecordedAsCommand(CommandModel model) {
		return isComponentCommand(model);
	}

	private boolean isComponentCommand(Command model) {
		return 
			(model instanceof ComponentCommandModel) ||
			(model instanceof ComponentCommand) ||
			(model instanceof WrappedCommandModel && isComponentCommand(((WrappedCommandModel) model).unwrap()));
	}

	@Override
	protected Event recordActionImpl(ApplicationAction action) {
		recordContextInformation(action);
		return output(action);
	}

	@Override
	protected Event recordActionImpl(Supplier<ApplicationAction> actionSupplier) {
		ApplicationAction action = actionSupplier.get();
		recordContextInformation(action);
		return output(action);
	}

	private void recordContextInformation(ApplicationAction action) {
		TLContext context = TLContext.getContext();
		if (context == null) {
			return;
		}
		recordUser(action, context);
		recordWindow(action, context);
	}

	private void recordUser(ApplicationAction action, TLContext context) {
		action.setUserID(context.getCurrentUserName());
	}

	private void recordWindow(ApplicationAction action, TLContext context) {
		/* The window name is stored only when it is not the main window. */
		if (getWindowScope() != mainWindowScope(context)) {
			action.setWindowName(getWindowScope().getName());
		}
	}

	private WindowScope getWindowScope() {
		return DefaultDisplayContext.getDisplayContext().getWindowScope();
	}

	private WindowScope mainWindowScope(TLContext context) {
		return context.getLayoutContext().getMainLayout().getWindowScope();
	}

	private Event output(ApplicationAction action) {
		if (Logger.isDebugEnabled(DefaultScriptingRecorder.class)) {
			Logger.debug("Tracing action: " + action, DefaultScriptingRecorder.class);
		}
		Event event = new Event(TLContextManager.getSession(), action);
		events.add(event);
		setInteractionRecorded();
		return event;
	}

	@Override
	public Iterator<Event> getEventPointer() {
		return events.iterator();
	}

	/**
	 * the configuration
	 */
	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * If the {@link ScriptingRecorder} is disabled, it will not record any {@link ApplicationAction}s.
	 */
	@Override
	public boolean getEnabled() {
		if (!getConfig().getEnabled()) {
			return false;
		}
		if (_scriptingGroup == null) {
			// No restriction on group.
			return true;
		}
		TLContext context = TLContext.getContext();
		if (context == null) {
			return true;
		}
		if (context.isCurrentSuperUser()) {
			// Super user sees everything
			return true;
		}
		Boolean enabled = context.get(SCRIPTING_ENABLED);
		if (enabled == null) {
			if (_scriptingGroup.containsPerson(context.getCurrentPersonWrapper())) {
				enabled = true;
			} else {
				enabled = false;
			}
			context.set(SCRIPTING_ENABLED, enabled);
		}
		return enabled.booleanValue();
	}
}
