/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.element.meta.form.validation.FormValidationModel;

import com.top_logic.base.locking.handler.DefaultLockHandler;
import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CombinedViewExecutabilityRule;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandConfirmation;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.form.FormCommandModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Declarative {@link UIElement} that wraps a {@link FormControl}.
 *
 * <p>
 * Establishes a form scope for child elements. Resolves an input channel to obtain the object to
 * display, creates a {@link FormControl}, and makes it available to nested {@link FieldElement}s
 * via the {@link ViewContext}.
 * </p>
 */
public class FormElement extends ContainerElement {

	/**
	 * Configuration for {@link FormElement}.
	 */
	@TagName("form")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getEditMode()}. */
		String EDIT_MODE = "editMode";

		/** Configuration name for {@link #getDirty()}. */
		String DIRTY = "dirty";

		/** Configuration name for {@link #getNoModelMessage()}. */
		String NO_MODEL_MESSAGE = "noModelMessage";

		/** Configuration name for {@link #getWithEditMode()}. */
		String WITH_EDIT_MODE = "withEditMode";

		/** Configuration name for {@link #getInitialEditMode()}. */
		String INITIAL_EDIT_MODE = "initial-edit-mode";

		/** Configuration name for {@link #getModeSwitch()}. */
		String MODE_SWITCH = "mode-switch";

		/** Configuration name for {@link #getLockHandler()}. */
		String LOCK_HANDLER = "lockHandler";

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		@Override
		@ClassDefault(FormElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel providing the object to display in the form.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Optional reference to a channel for publishing edit mode state.
		 */
		@Name(EDIT_MODE)
		@Format(ChannelRefFormat.class)
		ChannelRef getEditMode();

		/**
		 * Optional reference to a channel for publishing the dirty state.
		 */
		@Name(DIRTY)
		@Format(ChannelRefFormat.class)
		ChannelRef getDirty();

		/**
		 * Message to display when no object is available.
		 *
		 * <p>
		 * Defaults to "No object selected." if not specified.
		 * </p>
		 */
		@Name(NO_MODEL_MESSAGE)
		ResKey getNoModelMessage();

		/**
		 * Whether to create edit/save/cancel command models for this form.
		 *
		 * <p>
		 * When {@code true}, command models for edit, save, and cancel are created and contributed
		 * to the enclosing {@link CommandScope}. This allows a parent panel to render toolbar
		 * buttons for switching between view and edit mode without the {@link FormControl} knowing
		 * about UI elements.
		 * </p>
		 */
		@Name(WITH_EDIT_MODE)
		@BooleanDefault(true)
		boolean getWithEditMode();

		/**
		 * Whether the form starts in edit mode immediately.
		 *
		 * <p>
		 * When {@code true}, the form enters edit mode as soon as an object is available, without
		 * requiring the user to click an Edit button. Useful for create dialogs where the object is
		 * always editable.
		 * </p>
		 */
		@Name(INITIAL_EDIT_MODE)
		@BooleanDefault(false)
		boolean getInitialEditMode();

		/**
		 * Whether to show edit/save/cancel mode switching buttons.
		 *
		 * <p>
		 * When {@code false}, no command models for edit, save, and cancel are created. The form
		 * either stays in view mode or edit mode depending on {@link #getInitialEditMode()}. Useful
		 * for create dialogs where the dialog's own buttons handle submission.
		 * </p>
		 */
		@Name(MODE_SWITCH)
		@BooleanDefault(true)
		boolean getModeSwitch();

		/**
		 * Algorithm for edit-mode token handling.
		 *
		 * <p>
		 * Defaults to {@link DefaultLockHandler} which uses the application-wide
		 * {@link com.top_logic.base.locking.LockService LockService}. To disable locking, configure
		 * {@link com.top_logic.base.locking.handler.NoTokenHandling NoTokenHandling}.
		 * </p>
		 */
		@Name(LOCK_HANDLER)
		@ImplementationClassDefault(DefaultLockHandler.class)
		@Options(fun = AllInAppImplementations.class)
		@DisplayMinimized
		PolymorphicConfiguration<LockHandler> getLockHandler();

		/**
		 * Commands to contribute to the enclosing command scope.
		 *
		 * <p>
		 * These commands execute in the form's context and have access to the form model via
		 * {@link com.top_logic.layout.view.command.ReadFormObjectAction ReadFormObjectAction}. They
		 * are contributed to the parent's {@link CommandScope} and rendered as toolbar buttons by
		 * the enclosing panel or window.
		 * </p>
		 */
		@Name(COMMANDS)
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
	}

	private final Config _config;

	private final LockHandler _lockHandler;

	private final List<ViewCommand> _formCommands;

	private final List<ViewCommand.Config> _formCommandConfigs;

	/**
	 * Creates a new {@link FormElement} from configuration.
	 */
	@CalledByReflection
	public FormElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
		_lockHandler = createLockHandler(context, config);

		_formCommands = new ArrayList<>();
		_formCommandConfigs = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewCommand> cmdConfig : config.getCommands()) {
			ViewCommand cmd = context.getInstance(cmdConfig);
			if (cmd != null) {
				_formCommands.add(cmd);
				if (cmdConfig instanceof ViewCommand.Config) {
					_formCommandConfigs.add((ViewCommand.Config) cmdConfig);
				}
			}
		}
	}

	private LockHandler createLockHandler(InstantiationContext context, Config config) {
		if (config.getInitialEditMode()) {
			// No locking needed when the form starts in edit mode (e.g. create dialogs).
			return NoTokenHandling.INSTANCE;
		}

		LockHandler result = context.getInstance(config.getLockHandler());
		if (result != null) {
			return result;
		}

		return NoTokenHandling.INSTANCE;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve input channel.
		ViewChannel inputChannel = context.resolveChannel(_config.getInput());

		// 2. Get initial object from input channel.
		TLObject initialObject = (TLObject) inputChannel.get();

		// 3. Resolve no-model message.
		ResKey messageKey = _config.getNoModelMessage();
		if (messageKey == null) {
			messageKey = I18NConstants.FORM_NO_MODEL;
		}
		String noModelMessage = Resources.getInstance().getString(messageKey);

		// 4. Create FormControl with initial object.
		FormControl formControl = new FormControl(context, initialObject, noModelMessage, _lockHandler);

		// 5. Wire channels.
		formControl.setInputChannel(inputChannel);

		ChannelRef editModeRef = _config.getEditMode();
		if (editModeRef != null) {
			formControl.setEditModeChannel(context.resolveChannel(editModeRef));
		}

		ChannelRef dirtyRef = _config.getDirty();
		if (dirtyRef != null) {
			formControl.setDirtyChannel(context.resolveChannel(dirtyRef));
		}

		// 6. Create edit mode command models if configured.
		if (_config.getWithEditMode() && _config.getModeSwitch()) {
			contributeEditCommands(context, formControl);
		}

		// 7. Create a child context with the form control set, so that
		// nested FieldElements can access it without polluting the parent context.
		ViewContext formContext = context.childContext("form");
		formContext.setFormModel(formControl);

		// 8. Create child controls in the form-scoped context.
		List<IReactControl> childControls = createChildControls(formContext);

		// 9. Convert to ReactControl list and put as state.
		List<ReactControl> reactChildren = childControls.stream()
			.map(c -> (ReactControl) c)
			.collect(Collectors.toList());
		formControl.setChildren(reactChildren);

		// 10. Contribute form-level commands to the enclosing command scope.
		if (!_formCommands.isEmpty()) {
			contributeFormCommands(context, formContext, formControl);
		}

		// 11. Auto-enter edit mode if configured (after children are set so listeners receive
		// the formStateChanged event).
		if (_config.getInitialEditMode()) {
			formControl.enterEditMode();
		}

		return formControl;
	}

	/**
	 * Builds {@link ViewCommandModel}s for the configured form commands and contributes them to the
	 * parent's {@link CommandScope}.
	 *
	 * <p>
	 * The commands are built in the form context (so that actions like
	 * {@link com.top_logic.layout.view.command.ReadFormObjectAction ReadFormObjectAction} can access
	 * the {@link com.top_logic.layout.view.form.FormModel FormModel}), but contributed to the
	 * parent context's command scope (so the enclosing window/panel renders them as toolbar
	 * buttons).
	 * </p>
	 *
	 * @param parentContext
	 *        The parent context whose {@link CommandScope} receives the commands.
	 * @param formContext
	 *        The form-scoped context with the {@link com.top_logic.layout.view.form.FormModel
	 *        FormModel} set.
	 * @param formControl
	 *        The form control for cleanup registration.
	 */
	private void contributeFormCommands(ViewContext parentContext, ViewContext formContext,
			FormControl formControl) {
		CommandScope scope = parentContext.getCommandScope();
		if (scope == null) {
			return;
		}

		List<CommandModel> models = new ArrayList<>();
		for (int i = 0; i < _formCommands.size() && i < _formCommandConfigs.size(); i++) {
			ViewCommand cmd = _formCommands.get(i);
			ViewCommand.Config cmdConfig = _formCommandConfigs.get(i);

			ChannelRef inputRef = cmdConfig.getInput();
			ViewChannel inputChannel = inputRef != null ? formContext.resolveChannel(inputRef) : null;

			ViewExecutabilityRule rule = buildExecutabilityRule(cmdConfig);
			ViewCommandConfirmation confirmation = buildConfirmation(cmdConfig);

			ViewCommandModel inner =
				new ViewCommandModel(cmd, cmdConfig, inputChannel, rule, confirmation);
			inner.attach();

			// Wrap the model so that executeCommand uses the form context (which has the
			// FormModel) instead of the window context passed by the toolbar button.
			FormScopedCommandModel wrapped = new FormScopedCommandModel(inner, formContext, formControl);

			models.add(wrapped);
			scope.addCommand(wrapped);
		}

		// Re-evaluate command executability when form validation state changes.
		// The validation model is created in enterEditMode() which runs after this method.
		// We use a one-shot FormModelListener to wire the validation listener once.
		boolean[] wired = { false };
		formControl.addFormModelListener(source -> {
			if (!wired[0]) {
				FormValidationModel vm = formControl.getValidationModel();
				if (vm != null) {
					wired[0] = true;
					vm.addConstraintValidationListener((overlay, attr, result) -> {
						for (CommandModel model : models) {
							if (model instanceof FormScopedCommandModel fsm) {
								fsm.revalidateExecutability();
							}
						}
					});
				}
			}
		});

		formControl.addCleanupAction(() -> {
			for (CommandModel model : models) {
				scope.removeCommand(model);
				if (model instanceof FormScopedCommandModel) {
					((FormScopedCommandModel) model).getInner().detach();
				}
			}
		});
	}

	private ViewExecutabilityRule buildExecutabilityRule(ViewCommand.Config cmdConfig) {
		List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> ruleConfigs =
			cmdConfig.getExecutability();
		if (ruleConfigs.isEmpty()) {
			return input -> ExecutableState.EXECUTABLE;
		}
		DefaultInstantiationContext instantiation =
			new DefaultInstantiationContext(FormElement.class);
		List<ViewExecutabilityRule> rules = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewExecutabilityRule> ruleConfig : ruleConfigs) {
			ViewExecutabilityRule rule = instantiation.getInstance(ruleConfig);
			if (rule != null) {
				rules.add(rule);
			}
		}
		return CombinedViewExecutabilityRule.combine(rules);
	}

	private ViewCommandConfirmation buildConfirmation(ViewCommand.Config cmdConfig) {
		PolymorphicConfiguration<? extends ViewCommandConfirmation> confirmConfig =
			cmdConfig.getConfirmation();
		if (confirmConfig == null) {
			return null;
		}
		DefaultInstantiationContext instantiation =
			new DefaultInstantiationContext(FormElement.class);
		return instantiation.getInstance(confirmConfig);
	}

	/**
	 * Delegating {@link CommandModel} that overrides the {@link ReactContext} passed to
	 * {@link #executeCommand(ReactContext)} with the form's {@link ViewContext}.
	 *
	 * <p>
	 * This ensures that form-scoped commands (contributed to a parent panel's toolbar) execute with
	 * access to the form model, even though the toolbar button's click handler passes the panel's
	 * context.
	 * </p>
	 */
	private static class FormScopedCommandModel implements CommandModel {

		private final ViewCommandModel _inner;

		private final ViewContext _formContext;

		private final FormControl _formControl;

		FormScopedCommandModel(ViewCommandModel inner, ViewContext formContext, FormControl formControl) {
			_inner = inner;
			_formContext = formContext;
			_formControl = formControl;
		}

		ViewCommandModel getInner() {
			return _inner;
		}

		@Override
		public String getName() {
			return _inner.getName();
		}

		@Override
		public String getLabel() {
			return _inner.getLabel();
		}

		@Override
		public boolean isExecutable() {
			if (!_inner.isExecutable()) {
				return false;
			}
			// Block execution when form has validation errors.
			FormValidationModel validationModel = _formControl.getValidationModel();
			if (validationModel != null && !validationModel.isValid()) {
				return false;
			}
			return true;
		}

		@Override
		public HandlerResult executeCommand(ReactContext context) {
			// Substitute the form context so that actions can access the FormModel.
			return _inner.executeCommand(_formContext);
		}

		@Override
		public String getPlacement() {
			return _inner.getPlacement();
		}

		private List<Runnable> _stateListeners = new ArrayList<>();

		@Override
		public void addStateChangeListener(Runnable listener) {
			_stateListeners.add(listener);
			_inner.addStateChangeListener(listener);
		}

		@Override
		public void removeStateChangeListener(Runnable listener) {
			_stateListeners.remove(listener);
			_inner.removeStateChangeListener(listener);
		}

		/**
		 * Re-evaluates executability and notifies listeners if changed.
		 */
		void revalidateExecutability() {
			for (Runnable listener : _stateListeners) {
				listener.run();
			}
		}
	}

	private void contributeEditCommands(ViewContext context, FormControl formControl) {
		CommandScope scope = context.getCommandScope();
		if (scope == null) {
			return;
		}

		List<FormCommandModel> models = new ArrayList<>();
		models.add(FormCommandModel.editCommand(formControl));
		models.add(FormCommandModel.saveCommand(formControl));
		models.add(FormCommandModel.cancelCommand(formControl));

		for (FormCommandModel model : models) {
			model.attach();
			scope.addCommand(model);
		}

		formControl.addCleanupAction(() -> {
			for (FormCommandModel model : models) {
				scope.removeCommand(model);
				model.detach();
			}
		});
	}
}
