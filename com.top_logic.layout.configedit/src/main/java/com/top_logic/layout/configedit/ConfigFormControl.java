/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * The control that puts a {@link ConfigFormModel}'s edit mode on screen: a {@link ConfigEditorControl}
 * over {@link ConfigFormModel#edited()}, framed by Edit, Apply, and Cancel.
 *
 * <p>
 * In view mode the editor writes straight through to the original item, and an "Edit" command
 * {@link ConfigFormModel#startEditing() starts} a working copy. In edit mode the editor is
 * rebuilt over that copy instead, offering "Apply" and "Cancel": Cancel
 * {@link ConfigFormModel#cancelEditing() drops} the copy, Apply runs {@link ConfigValidation} and
 * either {@link ConfigFormModel#apply() carries the copy over} or, if a violation was found,
 * leaves edit mode open and puts every violation on the field that caused it - via
 * {@link ConfigValidation#report(ConfigValidation.Findings, ConfigFieldIndex)} against the
 * {@link ConfigFieldIndex}
 * the current editor filled while it was built. Every mode change rebuilds the editor over
 * {@link ConfigFormModel#edited()} - the editor itself never learns which of the two, original or
 * copy, it was handed.
 * </p>
 *
 * <p>
 * While the editor accepts input - in edit mode, or always with {@link Commands#NONE} - every change
 * to a field checks the edited item again and shows what the check found, see {@link #recheck()} -
 * so a value is questioned where it is entered, rather than only when the user asks for the item to
 * be carried over.
 * </p>
 *
 * <p>
 * A refused Apply reports itself as a failed {@link HandlerResult}, so the application's own error
 * notification carries it - a violation naming a property the editor renders as no field of its
 * own has nowhere else to go, and Apply must never look like a button that did nothing.
 * </p>
 *
 * <p>
 * Where those three commands appear is the caller's choice, see {@link Commands}. In a view they
 * belong in the enclosing panel's toolbar, which is where a form generated from a model puts Edit,
 * Save and Cancel; drawing them as buttons among the fields is what a standalone use wants.
 * {@link Commands#NONE} turns the whole cycle off: the control is then a thin wrapper around the
 * editor over the item itself - today's write-through behaviour, kept available for a caller (e.g.
 * a dialog with a save cycle of its own) that must not gain an edit mode of its own just because it
 * now goes through this class. Its fields are still checked as they change, since the caller's save
 * will refuse over the same findings.
 * </p>
 *
 * <p>
 * A caller that persists what is edited hands in an {@link EditAction} to run on save: the command
 * is then called "Save", and the working copy is carried over only once that action reports success.
 * A second {@link EditAction} may run on Cancel before the copy is dropped. While the working copy
 * differs from the original, the form {@link #isDirty() holds unsaved changes} and tells the
 * observers {@link #observeDirty(Runnable) waiting} for that.
 * </p>
 */
public class ConfigFormControl extends ReactFormLayoutControl {

	/** {@link CommandModel#getName() Name} of the command starting edit mode. */
	public static final String EDIT_COMMAND = "configFormEdit";

	/**
	 * {@link CommandModel#getName() Name} of the command carrying the working copy over, offered
	 * without an {@link EditAction} to run on save.
	 */
	public static final String APPLY_COMMAND = "configFormApply";

	/**
	 * {@link CommandModel#getName() Name} of the command saving the working copy, offered with an
	 * {@link EditAction} to run on save.
	 */
	public static final String SAVE_COMMAND = "configFormSave";

	/** {@link CommandModel#getName() Name} of the command dropping the working copy. */
	public static final String CANCEL_COMMAND = "configFormCancel";

	/**
	 * What a {@link ConfigFormControl} runs over its working copy before an edit session ends: on
	 * save, what persists the edited configuration; on cancel, what has to happen before the edits
	 * are dropped.
	 */
	@FunctionalInterface
	public interface EditAction {

		/**
		 * Runs the action.
		 *
		 * <p>
		 * Exceptions propagate to the command that started the action, which reports them; the edit
		 * session then stays open with the edits intact.
		 * </p>
		 *
		 * @param edited
		 *        The working copy, see {@link ConfigFormModel#edited()}.
		 * @param done
		 *        To be run once the action has succeeded, possibly later, e.g. after a dialog. Ends
		 *        the edit session: on save the working copy is carried over into the original, on
		 *        cancel it is dropped. An action that fails or is aborted does not run it.
		 */
		void run(ConfigurationItem edited, Runnable done);
	}

	/**
	 * Where a {@link ConfigFormControl}'s Edit, Apply and Cancel commands are rendered.
	 */
	public enum Commands {

		/**
		 * Not at all - no edit mode, the editor writes straight through to the item it was given.
		 */
		NONE,

		/**
		 * As buttons of the form itself, beside its fields.
		 *
		 * <p>
		 * For a standalone use that has no toolbar to put them in.
		 * </p>
		 */
		INLINE,

		/**
		 * Not by this control: they are handed out for an enclosing toolbar to render.
		 *
		 * <p>
		 * What a form generated from a model does, and therefore what a configuration form inside a
		 * panel should do - the commands sit in the panel's toolbar and the form fills the panel.
		 * </p>
		 *
		 * @see ConfigFormControl#commands()
		 */
		TOOLBAR;
	}

	private final ReactContext _context;

	private final ConfigFormModel _model;

	private final Commands _commands;

	/** What runs on save before the working copy is carried over, <code>null</code> for nothing. */
	private final EditAction _onSave;

	/** What runs on cancel before the working copy is dropped, <code>null</code> for nothing. */
	private final EditAction _onCancel;

	/**
	 * Whether the form held unsaved changes when this was last computed.
	 *
	 * @see #isDirty()
	 */
	private boolean _dirty;

	/** The observers {@link #observeDirty(Runnable) waiting} for {@link #isDirty()} to change. */
	private final List<Runnable> _dirtyObservers = new ArrayList<>();

	/**
	 * The Edit, Apply and Cancel commands offered to an enclosing toolbar, empty unless
	 * {@link Commands#TOOLBAR} was asked for.
	 *
	 * <p>
	 * Created once, in the constructor, rather than per render: whoever renders the toolbar keeps
	 * the instances it was handed and listens on them, so a rebuild must find the same ones.
	 * </p>
	 */
	private final List<ConfigFormCommand> _toolbarCommands;

	private final ConfigFieldIndex _index = new ConfigFieldIndex();

	/**
	 * Whether editing the item is offered.
	 *
	 * @see #setEditPermission(Supplier)
	 */
	private Supplier<ExecutableState> _editPermission = () -> ExecutableState.EXECUTABLE;

	/**
	 * How the properties of the edited item are displayed, by property name.
	 *
	 * @see #setFieldDisplays(Map)
	 */
	private Map<String, FieldDisplay> _displays = Collections.emptyMap();

	/**
	 * Runs {@link #recheck()} whenever a field of the current editor changes.
	 *
	 * <p>
	 * Installed on the {@link ConfigFieldIndex} once, in the constructor: an observer survives
	 * {@link ConfigFieldIndex#clear()}, so it keeps watching the fields of every editor built
	 * afterwards, while a second one per rebuild would leave the editors' worth of listeners behind
	 * that came before.
	 * </p>
	 */
	private final ConfigFieldPush _onFieldChange = new ConfigFieldPush(this::onFieldChange);

	/**
	 * The {@link ConfigFormModel} listener that rebuilds this control on every mode change,
	 * registered in the constructor and removed in {@link #onCleanup()}. Kept in a field, not
	 * created afresh at each of those two call sites, so {@link ConfigFormModel#removeListener(Runnable)}
	 * actually finds the very instance {@link ConfigFormModel#addListener(Runnable)} was given.
	 */
	private final Runnable _onModeChange = this::rebuild;

	/**
	 * The observers {@link #observeValidity(Runnable) waiting} for errors to appear or go away.
	 */
	private final List<Runnable> _validityObservers = new ArrayList<>();

	/**
	 * Tells the {@link #_validityObservers} about a field whose errors changed. Attached to every
	 * field the editor builds.
	 */
	private final FieldModelListener _onValidityChange = new FieldModelListener() {
		@Override
		public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
			// Reported by the validation change it causes, if any.
		}

		@Override
		public void onEditabilityChanged(FieldModel source, boolean editable) {
			// An error is shown only while the field is editable.
			fireValidityChanged();
		}

		@Override
		public void onValidationChanged(FieldModel source) {
			fireValidityChanged();
		}
	};

	/**
	 * Reveals a field whose value the user changed, so that the check following the change shows
	 * what it found at that field, while fields not touched yet stay unmarked.
	 *
	 * @see #recheck()
	 */
	private static final FieldModelListener REVEAL_ON_CHANGE = new FieldModelListener() {
		@Override
		public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
			if (source instanceof AbstractFieldModel field) {
				field.setRevealed(true);
			}
		}

		@Override
		public void onEditabilityChanged(FieldModel source, boolean editable) {
			// Not a change by the user.
		}

		@Override
		public void onValidationChanged(FieldModel source) {
			// Not a change by the user.
		}
	};

	/**
	 * Creates a {@link ConfigFormControl} with a full edit mode.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 */
	public ConfigFormControl(ReactContext context, ConfigurationItem config) {
		this(context, config, true);
	}

	/**
	 * Creates a {@link ConfigFormControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param withEditMode
	 *        Whether to render Edit/Apply/Cancel around the editor. {@code false} renders the
	 *        editor alone, writing straight through to {@code config} - today's behaviour.
	 */
	public ConfigFormControl(ReactContext context, ConfigurationItem config, boolean withEditMode) {
		this(context, config, withEditMode ? Commands.INLINE : Commands.NONE);
	}

	/**
	 * Creates a {@link ConfigFormControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param commands
	 *        Where the Edit, Apply and Cancel commands are rendered. With
	 *        {@link Commands#TOOLBAR}, the caller must take them from {@link #commands()} and put
	 *        them somewhere - nothing else offers them.
	 */
	public ConfigFormControl(ReactContext context, ConfigurationItem config, Commands commands) {
		this(context, config, commands, null, null);
	}

	/**
	 * Creates a {@link ConfigFormControl} running the given actions when an edit session ends.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param commands
	 *        Where the Edit, Save and Cancel commands are rendered, see
	 *        {@link #ConfigFormControl(ReactContext, ConfigurationItem, Commands)}.
	 * @param onSave
	 *        What runs on save over the checked working copy, before it is carried over;
	 *        <code>null</code> to carry it over right away, offering "Apply" instead of "Save".
	 * @param onCancel
	 *        What runs on cancel over the working copy, before it is dropped; <code>null</code> to
	 *        drop it right away.
	 */
	public ConfigFormControl(ReactContext context, ConfigurationItem config, Commands commands,
			EditAction onSave, EditAction onCancel) {
		super(context);
		_context = context;
		_model = new ConfigFormModel(config);
		_commands = commands;
		_onSave = onSave;
		_onCancel = onCancel;
		_toolbarCommands = commands == Commands.TOOLBAR ? createCommands() : Collections.emptyList();
		_model.addListener(_onModeChange);
		_onFieldChange.watch(_index);
		_index.observeFields(field -> field.addListener(_onValidityChange));
		_index.observeFields(field -> field.addListener(REVEAL_ON_CHANGE));
		rebuild();
	}

	/**
	 * Checks the configuration before whoever writes the form straight through saves it.
	 *
	 * <p>
	 * What Apply checks in edit mode, for a form without one: a violation of the configuration is
	 * put on the field that caused it, and an input a field rejected - which never reached the
	 * configuration - refuses as well.
	 * </p>
	 *
	 * <p>
	 * Confirms every entry not yet confirmed first, as Apply does, see
	 * {@link #checkForHandOver()}.
	 * </p>
	 *
	 * @return Why the configuration must not be saved, <code>null</code> if it may.
	 */
	public ConfigValidation.Refusal checkForSave() {
		return checkForHandOver();
	}

	/**
	 * Confirms every entry not yet confirmed, then checks the configuration as it is to be handed
	 * over.
	 *
	 * <p>
	 * The user who saves has finished editing, so an entry filled in but not confirmed is part of
	 * what is saved. An entry that cannot be confirmed stays pending with the reason at its key
	 * field and refuses the hand-over, see {@link ConfigValidation#confirmPending(ConfigFieldIndex)}.
	 * </p>
	 */
	private ConfigValidation.Refusal checkForHandOver() {
		ConfigValidation.Refusal notConfirmed = ConfigValidation.confirmPending(_index);
		if (notConfirmed != null) {
			return notConfirmed;
		}
		ConfigurationItem edited = _model.edited();
		return ConfigValidation.refusalFor(edited, _index, mandatory(edited));
	}

	/**
	 * Whether some field of the form shows an error, so that saving would be refused.
	 */
	public boolean hasVisibleErrors() {
		return _index.hasVisibleError();
	}

	/**
	 * Calls the given observer whenever an error appears at a field of the form or goes away.
	 *
	 * @param observer
	 *        The observer to call.
	 * @return What ends the observation.
	 */
	public Runnable observeValidity(Runnable observer) {
		_validityObservers.add(observer);
		return () -> _validityObservers.remove(observer);
	}

	private void fireValidityChanged() {
		for (Runnable observer : List.copyOf(_validityObservers)) {
			observer.run();
		}
		// Save is disabled while an error is on display.
		for (ConfigFormCommand command : _toolbarCommands) {
			command.notifyStateChanged();
		}
	}

	/**
	 * The Edit, Apply and Cancel commands, for an enclosing toolbar to render.
	 *
	 * <p>
	 * Empty unless the control was created with {@link Commands#TOOLBAR}. Each command decides for
	 * itself whether it applies to the form's current mode, and is told to re-read that whenever
	 * the mode changes, so the caller has nothing to keep in step.
	 * </p>
	 */
	public List<CommandModel> commands() {
		return Collections.unmodifiableList(_toolbarCommands);
	}

	private List<ConfigFormCommand> createCommands() {
		return List.of(
			new ConfigFormCommand(EDIT_COMMAND, I18NConstants.EDIT, Icons.CONFIG_FORM_EDIT,
				() -> !_model.isEditMode() && !_editPermission.get().isHidden(),
				() -> _editPermission.get().isExecutable(),
				ctx -> edit()),
			new ConfigFormCommand(_onSave == null ? APPLY_COMMAND : SAVE_COMMAND, saveLabel(),
				Icons.CONFIG_FORM_APPLY,
				_model::isEditMode,
				// Errors still hidden do not block it: the attempt is what reveals them.
				() -> !hasVisibleErrors(),
				ctx -> apply()),
			new ConfigFormCommand(CANCEL_COMMAND, I18NConstants.CANCEL, Icons.CONFIG_FORM_CANCEL,
				_model::isEditMode,
				ctx -> cancel()));
	}

	/**
	 * Sets what decides whether this form offers editing its item.
	 *
	 * <p>
	 * Guards the transition into edit mode: the Edit command is hidden or disabled as the
	 * permission says, and starting edit mode is refused while it is not executable. The permission
	 * is read whenever the command is rendered; a caller whose permission changes calls this method
	 * again.
	 * </p>
	 *
	 * @param permission
	 *        The permission, read anew on every use.
	 */
	public void setEditPermission(Supplier<ExecutableState> permission) {
		_editPermission = permission;
		for (ConfigFormCommand command : _toolbarCommands) {
			command.notifyStateChanged();
		}
		if (_commands == Commands.INLINE) {
			rebuild();
		}
	}

	/**
	 * Starts edit mode, unless the {@link #setEditPermission(Supplier) permission} refuses it.
	 */
	private HandlerResult edit() {
		ExecutableState permission = _editPermission.get();
		if (!permission.isExecutable()) {
			return HandlerResult.notExecutable(permission);
		}
		_model.startEditing();
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The label of the command ending an edit session with the edits: "Save" when an
	 * {@link EditAction} persists them, "Apply" when they are only carried over.
	 */
	private ResKey saveLabel() {
		return _onSave == null ? I18NConstants.APPLY : I18NConstants.SAVE;
	}

	/**
	 * The item this form was created for, see {@link ConfigFormModel#original()}.
	 */
	public ConfigurationItem getItem() {
		return _model.original();
	}

	/**
	 * Whether the form is in edit mode, working on a copy of the item it was created for.
	 */
	public boolean isEditMode() {
		return _model.isEditMode();
	}

	/**
	 * Whether the form holds unsaved changes: it is in edit mode, and its working copy differs from
	 * the item it was created for.
	 *
	 * <p>
	 * Compared by value, so an edit that is taken back makes the form clean again. Without an edit
	 * mode, the form writes straight through and never holds anything unsaved.
	 * </p>
	 */
	public boolean isDirty() {
		return _model.isEditMode()
			&& !ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(_model.edited(), _model.original());
	}

	/**
	 * Calls the given observer whenever {@link #isDirty()} changes.
	 *
	 * @param observer
	 *        The observer to call.
	 * @return What ends the observation.
	 */
	public Runnable observeDirty(Runnable observer) {
		_dirtyObservers.add(observer);
		return () -> _dirtyObservers.remove(observer);
	}

	/**
	 * Computes {@link #isDirty()} again and tells the observers if it changed.
	 */
	private void updateDirty() {
		boolean dirty = isDirty();
		if (dirty == _dirty) {
			return;
		}
		_dirty = dirty;
		for (Runnable observer : List.copyOf(_dirtyObservers)) {
			observer.run();
		}
	}

	/**
	 * Saves the working copy as the Save (or Apply) command does: checks it, runs the
	 * {@link EditAction} given for saving, and carries it over once that succeeded.
	 *
	 * @return A failed result naming why the configuration was refused, the default result if it
	 *         was handed on.
	 */
	public HandlerResult save() {
		return apply();
	}

	/**
	 * Drops the working copy and leaves edit mode, without running the {@link EditAction} given for
	 * cancelling.
	 */
	public void discard() {
		if (_model.isEditMode()) {
			_model.cancelEditing();
		}
	}

	/**
	 * Runs on every change to a field of the current editor.
	 */
	private void onFieldChange() {
		recheck();
		updateDirty();
	}

	/**
	 * Rebuilds the editor and its surrounding buttons over {@link ConfigFormModel#edited()}.
	 *
	 * <p>
	 * Registered as the {@link ConfigFormModel}'s listener, so every mode change - Edit, Apply,
	 * Cancel - runs this without the button handlers having to call it themselves.
	 * {@link #apply()} deliberately does not run this on a refusal: a rebuild would discard the
	 * very field models the violations were just put on, leaving the user no way to see what was
	 * wrong.
	 * </p>
	 *
	 * <p>
	 * Disposes the old children directly with {@link ReactControl#cleanupTree()} rather than going
	 * through {@link com.top_logic.layout.react.control.ReactCompositeControl#replaceChildren(List)}:
	 * that method deliberately leaves disposal to the caller, for a container that must defer it -
	 * e.g. past an in-progress observer notification. Nothing here is deferred - the old editor and
	 * buttons are discarded on the spot, not cached for later reuse - so immediate
	 * {@code cleanupTree()} is the right call, the same way {@link ConfigListEditorControl}'s own
	 * rebuild already does it in this package.
	 * </p>
	 *
	 * <p>
	 * The rebuilt {@link ConfigEditorControl} is editable exactly while either {@link #_commands}
	 * is {@link Commands#NONE} (the thin-wrapper, write-through case - it was always editable and
	 * stays so) or the model {@link ConfigFormModel#isEditMode() is in edit mode}. In every other
	 * case - an edit mode offered, model in view mode - it is built read-only: every field
	 * non-editable and no collection action rendered, so a form with a mode never accepts a change
	 * outside of one.
	 * </p>
	 */
	private void rebuild() {
		_onFieldChange.disarmed();

		for (ReactControl child : getChildren()) {
			child.cleanupTree();
		}
		getChildren().clear();
		_index.clear();

		boolean editable = _commands == Commands.NONE || _model.isEditMode();
		ConfigurationItem edited = _model.edited();
		addChild(new ConfigEditorControl(_context, edited, Collections.emptySet(), displays(edited),
			false, _index, editable, edited));

		if (_commands == Commands.INLINE) {
			if (_model.isEditMode()) {
				addChild(applyButton());
				addChild(cancelButton());
			} else if (!_editPermission.get().isHidden()) {
				addChild(editButton());
			}
		}

		// The toolbar holds the command instances, not this control's children, so a rebuild leaves
		// it showing the previous mode's buttons unless it is told to look again.
		for (ConfigFormCommand command : _toolbarCommands) {
			command.notifyStateChanged();
		}

		_onFieldChange.armed();
		updateDirty();
	}

	/**
	 * Displays properties of the edited item as the user interface wants them, in this form only.
	 *
	 * <p>
	 * For instance a value the context of the form fixes, e.g. the name of the model element whose
	 * access rights a dialog edits: it is shown, so that the user sees what is edited, but cannot
	 * be changed there. A property required here is also checked before the form is saved.
	 * </p>
	 *
	 * @param displays
	 *        How to display the properties, by property name. A name the edited item does not have
	 *        is ignored.
	 */
	public void setFieldDisplays(Map<String, FieldDisplay> displays) {
		_displays = Map.copyOf(displays);
		rebuild();
	}

	/**
	 * The {@link #setFieldDisplays(Map) displays} of the properties of the given item.
	 */
	private Map<PropertyDescriptor, FieldDisplay> displays(ConfigurationItem item) {
		Map<PropertyDescriptor, FieldDisplay> result = new HashMap<>();
		for (Map.Entry<String, FieldDisplay> entry : _displays.entrySet()) {
			PropertyDescriptor property = item.descriptor().getProperty(entry.getKey());
			if (property != null) {
				result.put(property, entry.getValue());
			}
		}
		return result;
	}

	/**
	 * The properties of the given item the {@link #setFieldDisplays(Map) displays} require.
	 */
	private Set<PropertyDescriptor> mandatory(ConfigurationItem item) {
		Set<PropertyDescriptor> result = new HashSet<>();
		for (Map.Entry<PropertyDescriptor, FieldDisplay> entry : displays(item).entrySet()) {
			if (entry.getValue().mandatory()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	/**
	 * Checks {@link ConfigFormModel#edited()} again and shows what the check found, run whenever a
	 * field of the editor changes.
	 *
	 * <p>
	 * A value is questioned where it is entered, not only once Apply is pressed. Without this, a
	 * warning would never be read at all in a form of this kind: Apply refuses over a violation and
	 * nothing else, so a successful Apply leaves edit mode and rebuilds the editor over the applied
	 * value, and a warning would be on screen only for as long as some <em>other</em> finding keeps
	 * refusing.
	 * </p>
	 *
	 * <p>
	 * Only while the editor accepts input: in edit mode, or always with {@link Commands#NONE}, where
	 * there is no mode at all and the editor writes straight through to the item - whoever saves that
	 * item refuses over the same findings, so they belong next to the fields as well. In view mode the
	 * editor accepts nothing, so there is nothing to check anything about, and marking what is merely
	 * being looked at would be noise.
	 * </p>
	 *
	 * <p>
	 * {@link ConfigValidation#recheckWhileEditing(ConfigurationItem, ConfigFieldIndex)}, not
	 * {@link ConfigValidation#refusalFor(ConfigurationItem, ConfigFieldIndex)}: a finding is shown at
	 * a field the user has changed, see {@link #REVEAL_ON_CHANGE}, not at one not touched yet; an entry the user
	 * has just started is not yet something to be told to confirm or discard, and an input a field
	 * rejected is nothing this is about to discard - both are Apply's to refuse over, when the user
	 * asks for the configuration to be handed over.
	 * </p>
	 *
	 * <p>
	 * Nothing runs this when edit mode is entered: a finding is a verdict on what the user did, and
	 * a form opened over a configuration whose mandatory properties are empty must not turn red
	 * before it has been touched.
	 * </p>
	 */
	private void recheck() {
		if (_commands != Commands.NONE && !_model.isEditMode()) {
			return;
		}
		ConfigurationItem edited = _model.edited();
		ConfigValidation.recheckWhileEditing(edited, _index, mandatory(edited));
	}

	/**
	 * A refused Apply, carrying the given reason.
	 *
	 * <p>
	 * The reason is set as the result's <em>title</em> - the snackbar's headline - and the details
	 * name what has to change, one line each. Without a title The React
	 * servlet builds the snackbar's headline from {@link HandlerResult#getErrorTitle()} and falls
	 * back to a generic "the command failed" when there is none - which is all the user would read,
	 * since the details it lists come from {@link HandlerResult#getErrorMessage()} and the
	 * exception's causes, not from the error list that {@link HandlerResult#error(ResKey)} fills.
	 * The reason stays in that list as well, so the result still counts as failed.
	 * </p>
	 */
	private static HandlerResult refusal(ResKey reason, List<ResKey> details) {
		HandlerResult result = new HandlerResult();
		result.setErrorTitle(reason);
		result.addErrorMessage(reason);
		for (ResKey detail : details) {
			result.addErrorMessage(detail);
		}
		return result;
	}

	/** A refused Apply whose reason is all there is to say. */
	private static HandlerResult refusal(ResKey reason) {
		return refusal(reason, List.of());
	}

	/**
	 * Checks {@link ConfigFormModel#edited()} against {@link ConfigValidation} and either
	 * {@link ConfigFormModel#apply() carries it over} - which leaves edit mode via the model's
	 * listener - or puts every violation found on the field that caused it, leaving edit mode
	 * untouched and this control unrebuilt.
	 *
	 * <p>
	 * Confirms every entry not yet confirmed first, as if the user had confirmed each, see
	 * {@link #checkForHandOver()}. An entry that cannot be confirmed refuses the Apply.
	 * </p>
	 *
	 * <p>
	 * Takes back every finding the previous refusal placed before checking anything, via
	 * {@link ConfigFieldIndex#clearFindings()}: a finding still holding is placed again a few
	 * lines below, one the user has meanwhile fixed is gone. Without that, a violation could
	 * outlive its own cause - a cross-item constraint is fixed by editing <em>one</em> of the two
	 * fields it flagged, leaving the other one showing an error nothing will ever clear.
	 * </p>
	 *
	 * <p>
	 * Refuses before checking while a field still rejects what was typed into it:
	 * {@link ConfigValidation#check(ConfigurationItem)} inspects the configuration, and a field
	 * that rejected its raw input never wrote to the configuration in the first place - it keeps
	 * the rejected text on screen while the item still holds the last accepted value. Applying
	 * would therefore look clean, leave edit mode, and throw the typed text away without ever
	 * saying so. The classic declarative form refuses the same way, through
	 * {@code FormContext#checkAll()}. That refusal says so at form level too - a refused Apply must
	 * never look like a button that does nothing, whatever the reason for the refusal.
	 * </p>
	 *
	 * <p>
	 * A violation that {@link ConfigValidation#report(ConfigValidation.Findings, ConfigFieldIndex)}
	 * could not place -
	 * a property the editor renders as no field of its own, e.g. a
	 * {@link com.top_logic.basic.config.annotation.Hidden @Hidden} one or one named by a
	 * constraint that reached into another item - is shown at form level instead. Without that,
	 * Apply would refuse with nothing whatsoever on screen to explain it, and the only way out of
	 * the form would be Cancel, discarding the user's work.
	 * </p>
	 */
	private HandlerResult apply() {
		if (!_model.isEditMode()) {
			return HandlerResult.DEFAULT_RESULT;
		}
		ConfigValidation.Refusal refusal = checkForHandOver();
		if (refusal != null) {
			return refusal(refusal.message(), refusal.details());
		}
		ConfigurationItem edited = _model.edited();
		if (_onSave == null) {
			_model.apply();
		} else {
			_onSave.run(edited, () -> endSession(edited, _model::apply));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Drops the working copy, after the {@link EditAction} given for cancelling succeeded.
	 */
	private HandlerResult cancel() {
		if (!_model.isEditMode()) {
			return HandlerResult.DEFAULT_RESULT;
		}
		if (_onCancel == null) {
			_model.cancelEditing();
		} else {
			ConfigurationItem edited = _model.edited();
			_onCancel.run(edited, () -> endSession(edited, _model::cancelEditing));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Ends the edit session over the given working copy by the given transition - unless that
	 * session has ended already, e.g. while an {@link EditAction} waited for a dialog.
	 */
	private void endSession(ConfigurationItem edited, Runnable transition) {
		if (_model.isEditMode() && _model.edited() == edited) {
			transition.run();
		}
	}

	/**
	 * The button offered in view mode, starting edit mode.
	 */
	private ReactButtonControl editButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.EDIT),
			ctx -> edit());
	}

	/**
	 * The button offered in edit mode that {@link #apply()}s the working copy.
	 */
	private ReactButtonControl applyButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(saveLabel()), ctx -> apply());
	}

	/**
	 * The button offered in edit mode that {@link ConfigFormModel#cancelEditing() drops} the
	 * working copy.
	 */
	private ReactButtonControl cancelButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.CANCEL),
			ctx -> cancel());
	}

	@Override
	protected void onCleanup() {
		_model.removeListener(_onModeChange);
	}

}
