/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.configedit.ConfigValidation.Violation;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * The control that puts a {@link ConfigFormModel}'s edit mode on screen: a {@link ConfigEditorControl}
 * over {@link ConfigFormModel#edited()}, framed by Edit, Apply, and Cancel.
 *
 * <p>
 * In view mode the editor writes straight through to the original item, and a single "Edit"
 * button {@link ConfigFormModel#startEditing() starts} a working copy. In edit mode the editor is
 * rebuilt over that copy instead, framed by "Apply" and "Cancel": Cancel
 * {@link ConfigFormModel#cancelEditing() drops} the copy, Apply runs {@link ConfigValidation} and
 * either {@link ConfigFormModel#apply() carries the copy over} or, if a violation was found,
 * leaves edit mode open and puts every violation on the field that caused it - via
 * {@link ConfigValidation#report(List, ConfigFieldIndex)} against the {@link ConfigFieldIndex}
 * the current editor filled while it was built. Every mode change rebuilds the editor over
 * {@link ConfigFormModel#edited()} - the editor itself never learns which of the two, original or
 * copy, it was handed.
 * </p>
 *
 * <p>
 * Passing {@code withEditMode = false} turns all of that off: the control is then a thin wrapper
 * around the editor over the item itself, with no buttons at all - today's write-through
 * behaviour, kept available for a caller (e.g. the view designer) that must not gain an edit mode
 * of its own just because it now goes through this class.
 * </p>
 */
public class ConfigFormControl extends ReactFormLayoutControl {

	private final ReactContext _context;

	private final ConfigFormModel _model;

	private final boolean _withEditMode;

	private final ConfigFieldIndex _index = new ConfigFieldIndex();

	/**
	 * The {@link ConfigFormModel} listener that rebuilds this control on every mode change,
	 * registered in the constructor and removed in {@link #onCleanup()}. Kept in a field, not
	 * created afresh at each of those two call sites, so {@link ConfigFormModel#removeListener(Runnable)}
	 * actually finds the very instance {@link ConfigFormModel#addListener(Runnable)} was given.
	 */
	private final Runnable _onModeChange = this::rebuild;

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
		super(context);
		_context = context;
		_model = new ConfigFormModel(config);
		_withEditMode = withEditMode;
		_model.addListener(_onModeChange);
		rebuild();
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
	 */
	private void rebuild() {
		for (ReactControl child : getChildren()) {
			child.cleanupTree();
		}
		getChildren().clear();
		_index.clear();

		addChild(new ConfigEditorControl(_context, _model.edited(), Collections.emptySet(), false, _index));

		if (_withEditMode) {
			if (_model.isEditMode()) {
				addChild(applyButton());
				addChild(cancelButton());
			} else {
				addChild(editButton());
			}
		}
	}

	/**
	 * Checks {@link ConfigFormModel#edited()} against {@link ConfigValidation} and either
	 * {@link ConfigFormModel#apply() carries it over} - which leaves edit mode via the model's
	 * listener - or puts every violation found on the field that caused it, leaving edit mode
	 * untouched and this control unrebuilt.
	 */
	private void apply() {
		List<Violation> violations = ConfigValidation.check(_model.edited());
		if (!violations.isEmpty()) {
			ConfigValidation.report(violations, _index);
			return;
		}
		_model.apply();
	}

	/**
	 * The button offered in view mode, starting edit mode.
	 */
	private ReactButtonControl editButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.EDIT), ctx -> {
			_model.startEditing();
			return HandlerResult.DEFAULT_RESULT;
		});
	}

	/**
	 * The button offered in edit mode that {@link #apply()}s the working copy.
	 */
	private ReactButtonControl applyButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.APPLY), ctx -> {
			apply();
			return HandlerResult.DEFAULT_RESULT;
		});
	}

	/**
	 * The button offered in edit mode that {@link ConfigFormModel#cancelEditing() drops} the
	 * working copy.
	 */
	private ReactButtonControl cancelButton() {
		return new ReactButtonControl(_context, Resources.getInstance().getString(I18NConstants.CANCEL), ctx -> {
			_model.cancelEditing();
			return HandlerResult.DEFAULT_RESULT;
		});
	}

	@Override
	protected void onCleanup() {
		_model.removeListener(_onModeChange);
	}

}
