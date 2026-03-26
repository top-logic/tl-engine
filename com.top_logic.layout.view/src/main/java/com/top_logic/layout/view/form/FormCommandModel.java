/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandModel} that delegates to a {@link FormControl} for form lifecycle operations (edit,
 * save, cancel).
 *
 * <p>
 * Created by the {@link com.top_logic.layout.view.element.FormElement FormElement} when
 * {@code withEditMode="true"}. Listens to form state changes to re-evaluate executability. For
 * example, the edit command is only executable when there is a current object and the form is not
 * already in edit mode.
 * </p>
 */
public class FormCommandModel implements CommandModel {

	private final String _name;

	private final ResKey _labelKey;

	private final String _placement;

	private final ThemeImage _image;

	private final Consumer<ReactContext> _action;

	private final Predicate<FormControl> _executableWhen;

	private final Predicate<FormControl> _visibleWhen;

	private final FormControl _form;

	private boolean _executable;

	private boolean _visible;

	private final List<Runnable> _stateChangeListeners = new ArrayList<>();

	private final FormModelListener _formModelListener = this::handleFormStateChanged;

	private FormCommandModel(String name, ResKey labelKey, ThemeImage image, String placement,
			FormControl form, Consumer<ReactContext> action, Predicate<FormControl> executableWhen,
			Predicate<FormControl> visibleWhen) {
		_name = name;
		_labelKey = labelKey;
		_image = image;
		_placement = placement;
		_form = form;
		_action = action;
		_executableWhen = executableWhen;
		_visibleWhen = visibleWhen;
		_executable = executableWhen.test(form);
		_visible = visibleWhen.test(form);
	}

	/**
	 * Creates the "Edit" command model.
	 *
	 * <p>
	 * Executable when a current object exists and the form is not in edit mode.
	 * </p>
	 *
	 * @param form
	 *        The form control to delegate to.
	 * @return The edit command model.
	 */
	public static FormCommandModel editCommand(FormControl form) {
		return new FormCommandModel("formEdit", I18NConstants.FORM_EDIT, Icons.FORM_EDIT,
			PLACEMENT_TOOLBAR, form,
			ctx -> form.enterEditMode(),
			f -> f.getCurrentObject() != null && !f.isEditMode(),
			f -> f.getCurrentObject() != null && !f.isEditMode());
	}

	/**
	 * Creates the "Save" command model with default behavior.
	 *
	 * <p>
	 * Executable when the form is in edit mode. Calls {@link FormControl#executeSave()}.
	 * </p>
	 *
	 * @param form
	 *        The form control to delegate to.
	 * @return The save command model.
	 */
	public static FormCommandModel saveCommand(FormControl form) {
		return new FormCommandModel("formSave", I18NConstants.FORM_SAVE, Icons.FORM_SAVE,
			PLACEMENT_TOOLBAR, form,
			ctx -> form.executeSave(),
			FormControl::isEditMode,
			FormControl::isEditMode);
	}

	/**
	 * Creates the "Save" command model with a custom action chain.
	 *
	 * <p>
	 * Executable when the form is in edit mode. Executes the given action instead of calling
	 * {@link FormControl#executeSave()}.
	 * </p>
	 *
	 * @param form
	 *        The form control for executability tracking.
	 * @param action
	 *        The custom action to execute on save.
	 * @return The save command model.
	 */
	public static FormCommandModel saveCommand(FormControl form, Consumer<ReactContext> action) {
		return new FormCommandModel("formSave", I18NConstants.FORM_SAVE, Icons.FORM_SAVE,
			PLACEMENT_TOOLBAR, form,
			action,
			FormControl::isEditMode,
			FormControl::isEditMode);
	}

	/**
	 * Creates the "Cancel" command model with default behavior.
	 *
	 * <p>
	 * Executable when the form is in edit mode. Calls {@link FormControl#executeCancel()}.
	 * </p>
	 *
	 * @param form
	 *        The form control to delegate to.
	 * @return The cancel command model.
	 */
	public static FormCommandModel cancelCommand(FormControl form) {
		return new FormCommandModel("formCancel", I18NConstants.FORM_CANCEL, Icons.FORM_CANCEL,
			PLACEMENT_TOOLBAR, form,
			ctx -> form.executeCancel(),
			FormControl::isEditMode,
			FormControl::isEditMode);
	}

	/**
	 * Creates the "Cancel" command model with a custom action chain.
	 *
	 * <p>
	 * Executable when the form is in edit mode. Executes the given action instead of calling
	 * {@link FormControl#executeCancel()}.
	 * </p>
	 *
	 * @param form
	 *        The form control for executability tracking.
	 * @param action
	 *        The custom action to execute on cancel.
	 * @return The cancel command model.
	 */
	public static FormCommandModel cancelCommand(FormControl form, Consumer<ReactContext> action) {
		return new FormCommandModel("formCancel", I18NConstants.FORM_CANCEL, Icons.FORM_CANCEL,
			PLACEMENT_TOOLBAR, form,
			action,
			FormControl::isEditMode,
			FormControl::isEditMode);
	}

	/**
	 * Subscribes to form state changes to track executability.
	 */
	public void attach() {
		_form.addFormModelListener(_formModelListener);
	}

	/**
	 * Unsubscribes from form state changes.
	 */
	public void detach() {
		_form.removeFormModelListener(_formModelListener);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getLabel() {
		return ResourcesModule.getInstance()
			.getBundle(Locale.getDefault())
			.getString(_labelKey);
	}

	@Override
	public String getImage() {
		return _image != null ? _image.toEncodedForm() : null;
	}

	@Override
	public boolean isExecutable() {
		return _executable;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}

	@Override
	public String getPlacement() {
		return _placement;
	}

	@Override
	public HandlerResult executeCommand(ReactContext context) {
		if (!_executable) {
			return HandlerResult.DEFAULT_RESULT;
		}
		_action.accept(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public void addStateChangeListener(Runnable listener) {
		_stateChangeListeners.add(listener);
	}

	@Override
	public void removeStateChangeListener(Runnable listener) {
		_stateChangeListeners.remove(listener);
	}

	private void handleFormStateChanged(FormModel source) {
		boolean newExecutable = _executableWhen.test(_form);
		boolean newVisible = _visibleWhen.test(_form);
		if (newExecutable != _executable || newVisible != _visible) {
			_executable = newExecutable;
			_visible = newVisible;
			for (Runnable listener : _stateChangeListeners) {
				listener.run();
			}
		}
	}
}
